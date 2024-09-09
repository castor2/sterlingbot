package com.invest.coin.domain.repository.coin;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.invest.coin.domain.entity.coin.VolatilityRangeBreakout;

public interface VolatilityRangeBreakoutRepository extends JpaRepository<VolatilityRangeBreakout, Long> {

	List<VolatilityRangeBreakout> findByCoinTypeAndDateStringAndDatetimeId(String coinType, String dateString, String datetimeId);

	List<VolatilityRangeBreakout> findByCoinTypeAndStatus(String coinType, String status);

	@Query("SELECT v FROM VolatilityRangeBreakout v WHERE v.coinType = :coinType AND v.datetimeId = :datetimeId AND v.dateString > :dateString")
	List<VolatilityRangeBreakout> findByCoinTypeAndDatetimeIdAndDateStringRange(
			@Param("coinType") String coinType,
			@Param("datetimeId") String datetimeId, 
			@Param("dateString") String dateString);

	List<VolatilityRangeBreakout> findByCoinTypeAndStatusAndIdLessThanEqual(String coinType, String status, long id);

}
