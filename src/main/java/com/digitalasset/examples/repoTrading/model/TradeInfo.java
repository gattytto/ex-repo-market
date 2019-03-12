// Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
// SPDX-License-Identifier: (Apache-2.0 OR BSD-3-Clause)

package com.digitalasset.examples.repoTrading.model;

import com.daml.ledger.javaapi.data.Record;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TradeInfo {

//    tradeId: Integer;
//    cusip: Text;
//    settlementDate: Time;
//    tradeDate: Time;
//    collateralQuantity: Decimal;
//    price: Decimal;
//    repoRate: Decimal;
//    term: Integer;
//    startAmount : Decimal;
//    endAmount : Decimal;
//    currency : Text

    private long tradeId;
    private String cusip;
    private LocalDate settlementDate;
    private LocalDate tradeDate;
    private BigDecimal collateralQuantity;
    private BigDecimal price;
    private BigDecimal repoRate;
    private long term;
    private BigDecimal startAmount;
    private BigDecimal endAmount;
    private String currency;

    public static TradeInfo fromRecord(Record record) {
        return new TradeInfo(new RecordMapper(record));
    }

    private TradeInfo(RecordMapper mapper) {
        this.tradeId = mapper.getInt64Field(0);
        this.cusip = mapper.getTextField(1);
        this.settlementDate = mapper.getDateField(2);
        this.tradeDate = mapper.getDateField(3);
        this.collateralQuantity =  mapper.getDecimalField(4);
        this.price = mapper.getDecimalField(5);
        this.repoRate = mapper.getDecimalField(6);
        this.term = mapper.getInt64Field(7);
        this.startAmount = mapper.getDecimalField(8);
        this.endAmount = mapper.getDecimalField(9);
        this.currency = mapper.getTextField(10);
    }

    public long getTradeId() {
        return tradeId;
    }

    public String getCusip() {
        return cusip;
    }

    public LocalDate getSettlementDate() {
        return settlementDate;
    }

    public LocalDate getTradeDate() {
        return tradeDate;
    }

    public BigDecimal getCollateralQuantity() {
        return collateralQuantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getRepoRate() {
        return repoRate;
    }

    public long getTerm() {
        return term;
    }

    public BigDecimal getStartAmount() {
        return startAmount;
    }

    public BigDecimal getEndAmount() {
        return endAmount;
    }

    public String getCurrency() {
        return currency;
    }

}
