package com.invest.coin.domain.service.quant.momentum.volatility_range_breakout;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.invest.coin.domain.entity.coin.VolatilityRangeBreakout;
import com.invest.coin.domain.model.CoinType;
import com.invest.coin.domain.model.quant.momentum.volatility_range_breakout.VolatilityRangeBreakoutStatus;
import com.invest.coin.domain.model.upbit.trade.UpbitOrder;
import com.invest.coin.domain.repository.coin.VolatilityRangeBreakoutRepository;
import com.invest.coin.domain.service.upbit.trade.UpbitTradeService;
import com.invest.coin.domain.util.DateUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class VolatilityRangeBreakoutSellService {
	
	private final UpbitTradeService upbitTradeService;
	private final VolatilityRangeBreakoutRepository volatilityRangeBreakoutRepository;
	
	@Async
	public void sell(CoinType coinType) {
		log.debug("coinType : {} , sell", coinType.name());
		ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
		List<VolatilityRangeBreakout> volatilityRangeBreakouts = volatilityRangeBreakoutRepository.findByCoinTypeAndDateStringAndDatetimeId(coinType.name(), DateUtil.getDateString(now.minusDays(1)), String.valueOf(now.getHour()));
		if (null == volatilityRangeBreakouts) {
			return;
		}
		volatilityRangeBreakouts.stream().forEach(
				volatilityRangeBreakout -> {
					if (volatilityRangeBreakout.getStatus().equals(VolatilityRangeBreakoutStatus.NOT_BREAKOUT.getCode())) {
						return;
					}
					
					if (volatilityRangeBreakout.getStatus().equals(VolatilityRangeBreakoutStatus.BREAKOUT_REQUEST.getCode())) {
						volatilityRangeBreakout.setUpdatedAt(ZonedDateTime.now(ZoneId.of("Asia/Seoul")));
						volatilityRangeBreakout.setStatus(VolatilityRangeBreakoutStatus.NOT_BREAKOUT.getCode());
						volatilityRangeBreakoutRepository.save(volatilityRangeBreakout);
						return;
					}
					
					if (volatilityRangeBreakout.getStatus().equals(VolatilityRangeBreakoutStatus.BUY_DONE.getCode())) {
						sellOrder(coinType, volatilityRangeBreakout);
					}
				}
			);
	}
	
	// Check if remaining sell order exist
	public void sellRemainOrder(CoinType coinType) {
		List<VolatilityRangeBreakout> remainVolatilityRangeBreakouts = volatilityRangeBreakoutRepository.findByCoinTypeAndStatus(coinType.name(), VolatilityRangeBreakoutStatus.BUY_DONE.getCode());
		if (null == remainVolatilityRangeBreakouts) {
			return;
		}
		
		ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
		int date = Integer.parseInt(DateUtil.getDateString(now.minusDays(1)));
		int datetime = now.getHour();
		
		remainVolatilityRangeBreakouts.stream().forEach(
				volatilityRangeBreakout -> {
					int dataDate = Integer.parseInt(volatilityRangeBreakout.getDateString());
					int dataDatetime = Integer.parseInt(volatilityRangeBreakout.getDatetimeId());
					
					if (date > dataDate || (date == dataDate && datetime > dataDatetime)) {
						sellOrder(coinType, volatilityRangeBreakout);
					}
				});
	}
	
	public void sellOrder(CoinType coinType, VolatilityRangeBreakout volatilityRangeBreakout) {
		UpbitOrder upbitSellOrder = upbitTradeService.sellMarketPrice(coinType, volatilityRangeBreakout.getBuyCount());
		log.debug("coinType : {} , upbitSellOrder : {}", upbitSellOrder);
		
		volatilityRangeBreakout.setSellUuid(upbitSellOrder.getUuid());
		volatilityRangeBreakout.setUpdatedAt(ZonedDateTime.now(ZoneId.of("Asia/Seoul")));
		volatilityRangeBreakout.setStatus(VolatilityRangeBreakoutStatus.SELL_REQUEST.getCode());
		volatilityRangeBreakoutRepository.save(volatilityRangeBreakout);
	}
	
	@Async
	public void confirmSellOrder(CoinType coinType) {
		log.debug("coinType : {} , confirmSellOrder", coinType.name());
		List<VolatilityRangeBreakout> volatilityRangeBreakouts = volatilityRangeBreakoutRepository.findByCoinTypeAndStatus(coinType.name(), VolatilityRangeBreakoutStatus.SELL_REQUEST.getCode());
		if (null == volatilityRangeBreakouts || volatilityRangeBreakouts.isEmpty()) {
			return;
		}
		
		volatilityRangeBreakouts.stream().forEach(
				volatilityRangeBreakout -> {
					
					UpbitOrder upbitSellOrderDetail = upbitTradeService.getOrder(volatilityRangeBreakout.getSellUuid());
					log.debug("coinType : {} , upbitSellOrderDetail : {}", upbitSellOrderDetail);
					if (upbitSellOrderDetail.getState().equalsIgnoreCase("wait")) {
						return;
					}
					
					BigDecimal sellCount = BigDecimal.ZERO;
					BigDecimal sellAvgPrice = BigDecimal.ZERO;
					if (upbitSellOrderDetail.getTrades().size() == 1) {
						sellCount = upbitSellOrderDetail.getTrades().get(0).getVolume();
						sellAvgPrice = upbitSellOrderDetail.getTrades().get(0).getPrice();
					} else {
						sellCount = upbitSellOrderDetail.getTrades().stream()
				                .map(trade -> trade.getVolume())
				                .reduce(BigDecimal.ZERO, BigDecimal::add);
						
						BigDecimal sellSumPrice = upbitSellOrderDetail.getTrades().stream()
				                .map(trade -> trade.getVolume().multiply(trade.getPrice().setScale(8, RoundingMode.HALF_UP)))
				                .reduce(BigDecimal.ZERO, BigDecimal::add);
						
						sellAvgPrice = sellSumPrice.divide(sellCount, 8, RoundingMode.HALF_UP);
					}
					
					volatilityRangeBreakout.setSellPrice(sellAvgPrice);
					volatilityRangeBreakout.setSellCount(sellCount);
					volatilityRangeBreakout.setFee(upbitSellOrderDetail.getPaidFee().add(volatilityRangeBreakout.getFee()));
					volatilityRangeBreakout.setSellAt(ZonedDateTime.now(ZoneId.of("Asia/Seoul")));
					volatilityRangeBreakout.setUpdatedAt(ZonedDateTime.now(ZoneId.of("Asia/Seoul")));
					volatilityRangeBreakout.setStatus(VolatilityRangeBreakoutStatus.SELL_DONE.getCode());
					volatilityRangeBreakoutRepository.save(volatilityRangeBreakout);
					
				});
	}

}
