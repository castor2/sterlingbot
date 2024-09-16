package com.invest.coin.domain.scheduler;

import java.math.BigDecimal;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.invest.coin.domain.model.CoinType;
import com.invest.coin.domain.service.quant.momentum.volatility_range_breakout.VolatilityRangeBreakoutBuyService;
import com.invest.coin.domain.service.quant.momentum.volatility_range_breakout.VolatilityRangeBreakoutSellService;
import com.invest.coin.domain.service.quant.momentum.volatility_range_breakout.VolatilityRangeBreakoutService;
import com.invest.coin.domain.service.quant.momentum.volatility_range_breakout.VolatilityRangeBreakoutStoplossService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CheckScheduler {
	
	private final VolatilityRangeBreakoutService volatilityRangeBreakoutService;
	private final VolatilityRangeBreakoutBuyService volatilityRangeBreakoutBuyService;
	private final VolatilityRangeBreakoutSellService volatilityRangeBreakoutSellService;
	private final VolatilityRangeBreakoutStoplossService volatilityRangeBreakoutStoplossService;
	
	// Check if buy or not (every 1 sec)
	@Scheduled(fixedDelay = 1000) 
	public void checkAndBuy() {
		try {
			for(CoinType coinType : CoinType.values()) {
				volatilityRangeBreakoutBuyService.checkAndBuy(coinType);
			}
		} catch (Exception e) {
			e.printStackTrace();
			volatilityRangeBreakoutBuyService.setChecking(false);
		}
		
	}
	
	// Check buy completed (every 1 min)
	@Scheduled(fixedDelay = 1000 * 60) 
	public void confirmBuyOrder() {
		for(CoinType coinType : CoinType.values()) {
			volatilityRangeBreakoutBuyService.confirmBuyOrder(coinType);
		}
	}
	
	// Check sell completed (every 1 min)
	@Scheduled(fixedDelay = 1000 * 60) 
	public void confirmSellOrder() {
		for(CoinType coinType : CoinType.values()) {
			volatilityRangeBreakoutSellService.confirmSellOrder(coinType);
		}
	}
	
	// Check loss cut (every 5 sec, loss cut if more than 5% loss)
	@Scheduled(fixedDelay = 1000 * 5) 
	public void checkStopLossAndSellOrder() {
		try {
			for(CoinType coinType : CoinType.values()) {
				volatilityRangeBreakoutStoplossService.checkStopLossAndSellOrder(coinType);
			}
		} catch (Exception e) {
			e.printStackTrace();
			volatilityRangeBreakoutStoplossService.setChecking(false);
		}
	}
	
	// Calculate buy price (every 1 hour, sharp)
	@Scheduled(cron = "0 1 * * * *") 
	public void calculate() {
		BigDecimal targetVolatilityRate = BigDecimal.valueOf(0.04);
		for(CoinType coinType : CoinType.values()) {
			volatilityRangeBreakoutService.calculate(coinType, targetVolatilityRate);
		}
	}
	
	// Sell (every 1 hour, sharp)
	@Scheduled(cron = "0 0 * * * *") 
	public void sell() {
		try {
			for(CoinType coinType : CoinType.values()) {
				volatilityRangeBreakoutSellService.sell(coinType);
			}
		} catch (Exception e) {
			e.printStackTrace();
			volatilityRangeBreakoutSellService.setChecking(false);
		}
	}
	
	// Check if remaining sell orders exist
	@Scheduled(fixedDelay = 1000 * 60) 
	public void sellRemain() {
		try {
			for(CoinType coinType : CoinType.values()) {
				volatilityRangeBreakoutSellService.sellRemainOrder(coinType);
			}
		} catch (Exception e) {
			e.printStackTrace();
			volatilityRangeBreakoutSellService.setChecking(false);
		}
	}

}
