package com.invest.coin.domain.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CoinType {
	BTC("KRW-BTC", new BigDecimal(1000), "BitCoin"),
	ETH("KRW-ETH", new BigDecimal(500), "Etherium"),
	BCH("KRW-BCH", new BigDecimal(50), "BitCoinCash"),
	SBD("KRW-SBD", new BigDecimal(10), "StreamDollar"),
	DOGE("KRW-DOGE", new BigDecimal(10), "DogeCoin"),
	STMX("KRW-STMX", new BigDecimal(10), "StromX"),
	EOS("KRW-EOS", new BigDecimal(5), "EOS"),
	BEAM("KRW-BEAM", new BigDecimal(10), "BEAM");
	
	private String upbitTicker;
	private BigDecimal priceUnit;
	private String name;
	

}
