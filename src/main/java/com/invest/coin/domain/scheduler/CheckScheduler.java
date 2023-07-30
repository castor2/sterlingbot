package com.invest.coin.domain.scheduler;

import java.math.BigDecimal;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.invest.coin.domain.model.CoinType;
import com.invest.coin.domain.service.quant.momentum.volatility_range_breakout.VloatilityRangeBreakoutBuyService;
import com.invest.coin.domain.service.quant.momentum.volatility_range_breakout.VloatilityRangeBreakoutSellService;
import com.invest.coin.domain.service.quant.momentum.volatility_range_breakout.VloatilityRangeBreakoutService;
import com.invest.coin.domain.service.quant.momentum.volatility_range_breakout.VloatilityRangeBreakoutStoplossService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CheckScheduler {
	
	private final VloatilityRangeBreakoutService vloatilityRangeBreakoutService;
	private final VloatilityRangeBreakoutBuyService vloatilityRangeBreakoutBuyService;
	private final VloatilityRangeBreakoutSellService vloatilityRangeBreakoutSellService;
	private final VloatilityRangeBreakoutStoplossService vloatilityRangeBreakoutStoplossService;
	
	// Check if buy or not (every 1 sec)
	@Scheduled(fixedDelay = 1000) 
	public void checkAndBuy() {
		try {
			for(CoinType coinType : CoinType.values()) {
				vloatilityRangeBreakoutBuyService.checkAndBuy(coinType);
			}
		} catch (Exception e) {
			e.printStackTrace();
			vloatilityRangeBreakoutBuyService.setChecking(false);
		}
		
	}
	
	// Check buy completed (every 1 min)
	@Scheduled(fixedDelay = 1000 * 60) 
	public void confirmBuyOrder() {
		for(CoinType coinType : CoinType.values()) {
			vloatilityRangeBreakoutBuyService.confirmBuyOrder(coinType);
		}
	}
	
	// Check sell completed (every 1 min)
	@Scheduled(fixedDelay = 1000 * 60) 
	public void confirmSellOrder() {
		for(CoinType coinType : CoinType.values()) {
			vloatilityRangeBreakoutSellService.confirmSellOrder(coinType);
		}
	}
	
	// Check loss cut (every 5 sec, loss cut if more than 5% loss)
	@Scheduled(fixedDelay = 1000 * 5) 
	public void checkStopLossAndSellOrder() {
		try {
			for(CoinType coinType : CoinType.values()) {
				vloatilityRangeBreakoutStoplossService.checkStopLossAndSellOrder(coinType);
			}
		} catch (Exception e) {
			e.printStackTrace();
			vloatilityRangeBreakoutStoplossService.setChecking(false);
		}
	}
	
	// Calculate buy price (every 1 hour, sharp)
	@Scheduled(cron = "0 1 * * * *") 
	public void calculate() {
		BigDecimal targetVloatilityRate = BigDecimal.valueOf(0.04);
		for(CoinType coinType : CoinType.values()) {
			vloatilityRangeBreakoutService.calculate(coinType, targetVloatilityRate);
		}
	}
	
	// Sell (every 1 hour, sharp)
	@Scheduled(cron = "0 0 * * * *") 
	public void sell() {
		for(CoinType coinType : CoinType.values()) {
			vloatilityRangeBreakoutSellService.sell(coinType);
		}
	}
	
	// Check if remaining sell orders exist
	@Scheduled(fixedDelay = 1000 * 60) 
		public void sellRemain() {
			for(CoinType coinType : CoinType.values()) {
				vloatilityRangeBreakoutSellService.sellRemainOrder(coinType);
			}
		}

}
