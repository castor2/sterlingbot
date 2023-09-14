package com.invest.coin.controller.webapi;

import java.math.BigDecimal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.invest.coin.domain.model.CoinType;
import com.invest.coin.domain.service.quant.momentum.volatility_range_breakout.VolatilityRangeBreakoutBuyService;
import com.invest.coin.domain.service.quant.momentum.volatility_range_breakout.VolatilityRangeBreakoutSellService;
import com.invest.coin.domain.service.quant.momentum.volatility_range_breakout.VolatilityRangeBreakoutService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class VolatilityRangeBreakoutController {
	
	private final VolatilityRangeBreakoutService volatilityRangeBreakoutService;
	private final VolatilityRangeBreakoutBuyService volatilityRangeBreakoutBuyService;
	private final VolatilityRangeBreakoutSellService volatilityRangeBreakoutSellService;
	
	@GetMapping("/coin/calculate")
	public String order(BigDecimal targetVolatilityRate) {
		for(CoinType coinType : CoinType.values()) {
			volatilityRangeBreakoutService.calculate(coinType, targetVolatilityRate);
		}
		return "calculate";
	}
	
	@GetMapping("/coin/check_and_buy")
	public String checkAndBuy() {
		for(CoinType coinType : CoinType.values()) {
			volatilityRangeBreakoutBuyService.checkAndBuy(coinType);
		}
		return "calculate";
	}
	
	@GetMapping("/coin/sell")
	public String sell() {
		for(CoinType coinType : CoinType.values()) {
			volatilityRangeBreakoutSellService.sell(coinType);
		}
		return "sell";
	}

}
