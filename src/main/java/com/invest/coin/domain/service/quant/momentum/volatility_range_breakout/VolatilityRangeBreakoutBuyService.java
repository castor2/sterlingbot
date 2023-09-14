package com.invest.coin.domain.service.quant.momentum.volatility_range_breakout;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.invest.coin.domain.entity.coin.VolatilityRangeBreakout;
import com.invest.coin.domain.model.CoinType;
import com.invest.coin.domain.model.quant.momentum.volatility_range_breakout.VolatilityRangeBreakoutStatus;
import com.invest.coin.domain.model.upbit.trade.UpbitOrder;
import com.invest.coin.domain.repository.coin.VolatilityRangeBreakoutRepository;
import com.invest.coin.domain.service.upbit.market.UpbitMarketService;
import com.invest.coin.domain.service.upbit.trade.UpbitTradeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class VolatilityRangeBreakoutBuyService {
	
	private Map<String, AtomicBoolean> checking = new HashMap<>();
	
	private final UpbitMarketService upbitMarketService;
	private final UpbitTradeService upbitTradeService;
	private final VolatilityRangeBreakoutRepository volatilityRangeBreakoutRepository;
	
	@PostConstruct
    public void init() {
        for(CoinType coinType : CoinType.values()) {
        	checking.put(coinType.getUpbitTicker(), new AtomicBoolean());
		}
    }
	
	public void checkAndBuy(CoinType coinType) {
		log.debug("coinType : {} , checkAndBuy", coinType.name());
		if (checking.get(coinType.getUpbitTicker()).get()) {
			return;
		}
		checking.get(coinType.getUpbitTicker()).set(true);
		List<VolatilityRangeBreakout> volatilityRangeBreakouts = volatilityRangeBreakoutRepository.findByCoinTypeAndStatus(coinType.name(), VolatilityRangeBreakoutStatus.BREAKOUT_REQUEST.getCode());
		if (null == volatilityRangeBreakouts || volatilityRangeBreakouts.isEmpty()) {
			checking.get(coinType.getUpbitTicker()).set(false);
			return;
		}
		
		BigDecimal currentPrice = upbitMarketService.getCurrentPrice(coinType);
		volatilityRangeBreakouts.stream().forEach(
				volatilityRangeBreakout -> {
					log.debug("cointype : {}, datetime : {}, targetPrice : {} , currentPrice : {}", coinType.name(), volatilityRangeBreakout.getDateString() + volatilityRangeBreakout.getDatetimeId(), volatilityRangeBreakout.getTargetPrice(), currentPrice);
					if (currentPrice.compareTo(volatilityRangeBreakout.getTargetPrice()) >= 0) {
						UpbitOrder upbitOrder = upbitTradeService.buyMarketPrice(coinType, volatilityRangeBreakout.getTargetPrice().multiply(volatilityRangeBreakout.getTargetCount()).setScale(8, RoundingMode.HALF_UP));
						log.debug("coinType : {} , upbitBuyOrder : {}", upbitOrder);
						
						volatilityRangeBreakout.setBuyUuid(upbitOrder.getUuid());
						volatilityRangeBreakout.setStatus(VolatilityRangeBreakoutStatus.BUY_REQURST.getCode());
						
						volatilityRangeBreakoutRepository.save(volatilityRangeBreakout);
					}
				});
		checking.get(coinType.getUpbitTicker()).set(false);
	}
	
	public void setChecking(boolean value) {
		for(CoinType coinType : CoinType.values()) {
			checking.get(coinType.getUpbitTicker()).set(value);
		}
	}
	
	@Async
	public void confirmBuyOrder(CoinType coinType) {
		log.debug("coinType : {} , confirmBuyOrder", coinType.name());
		List<VolatilityRangeBreakout> volatilityRangeBreakouts = volatilityRangeBreakoutRepository.findByCoinTypeAndStatus(coinType.name(), VolatilityRangeBreakoutStatus.BUY_REQURST.getCode());
		if (null == volatilityRangeBreakouts || volatilityRangeBreakouts.isEmpty()) {
			return;
		}
		
		volatilityRangeBreakouts.stream().forEach(
				volatilityRangeBreakout -> {
					UpbitOrder upbitOrderDetail = upbitTradeService.getOrder(volatilityRangeBreakout.getBuyUuid());
					log.debug("coinType : {} , upbitBuyOrderDetail : {}", upbitOrderDetail);
					if (upbitOrderDetail.getState().equalsIgnoreCase("wait")) {
						return;
					}
					
					BigDecimal buyCount = BigDecimal.ZERO;
					BigDecimal buyAvgPrice = BigDecimal.ZERO;
					if (upbitOrderDetail.getTrades().size() == 1) {
						buyCount = upbitOrderDetail.getTrades().get(0).getVolume();
						buyAvgPrice = upbitOrderDetail.getTrades().get(0).getPrice();
					} else {
						buyCount = upbitOrderDetail.getTrades().stream()
				                .map(trade -> trade.getVolume())
				                .reduce(BigDecimal.ZERO, BigDecimal::add);
						
						BigDecimal buySumPrice = upbitOrderDetail.getTrades().stream()
				                .map(trade -> trade.getVolume().multiply(trade.getPrice().setScale(8, RoundingMode.HALF_UP)))
				                .reduce(BigDecimal.ZERO, BigDecimal::add);
						
						buyAvgPrice = buySumPrice.divide(buyCount, 8, RoundingMode.HALF_UP);
					}
					
					volatilityRangeBreakout.setBuyPrice(buyAvgPrice);
					volatilityRangeBreakout.setBuyCount(buyCount);
					volatilityRangeBreakout.setBuyAt(ZonedDateTime.now(ZoneId.of("Asia/Seoul")));
					volatilityRangeBreakout.setStatus(VolatilityRangeBreakoutStatus.BUY_DONE.getCode());
					volatilityRangeBreakout.setFee(upbitOrderDetail.getPaidFee());
					
					volatilityRangeBreakoutRepository.save(volatilityRangeBreakout);
					
				});
		
	}

}
