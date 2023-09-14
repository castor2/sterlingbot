package com.invest.coin.controller.webapi;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.invest.coin.domain.model.ErrorResponse;
import com.invest.coin.domain.service.quant.momentum.volatility_range_breakout.VolatilityRangeBreakoutBuyService;
import com.invest.coin.domain.service.quant.momentum.volatility_range_breakout.VolatilityRangeBreakoutStoplossService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ErrorController {
	
	private final VolatilityRangeBreakoutBuyService volatilityRangeBreakoutBuyService;
	private final VolatilityRangeBreakoutStoplossService volatilityRangeBreakoutStoplossService;
	
	@ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(HttpServletRequest request, Exception e) {
		log.error("Request: " + request.getRequestURL());
        log.error("handleException", e);

        ErrorResponse response
                = ErrorResponse
                        .builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .message(e.toString())
                        .build();

        volatilityRangeBreakoutBuyService.setChecking(false);
        volatilityRangeBreakoutStoplossService.setChecking(false);
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
