package com.algaworks.algadelivery.delivery.tracking.infrastructure.http.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourierPayoutResultModel {
    private BigDecimal payoutFee;
}
