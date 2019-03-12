// Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
// SPDX-License-Identifier: (Apache-2.0 OR BSD-3-Clause)

package com.digitalasset.examples.repoTrading.model;

import com.daml.ledger.javaapi.data.Record;

import java.math.BigDecimal;
import java.time.LocalDate;

public class NetObligation implements DomainObject {

//    ccp: Party
//    receiver : Party
//    payer : Party
//    paymentProcessor : Party
//    cusip : Text
//    currency : Text
//    paymentAmount : Decimal
//    quantity : Decimal
//    settlementDate : Time

    private String ccp;
    private String receiver;
    private String payer;
    private String paymentProcessor;
    private String cusip;
    private String currency;
    private BigDecimal paymentAmount;
    private BigDecimal quantity;
    private LocalDate settlementDate;

    public static NetObligation fromRecord(Record createArgs) {
        return new NetObligation(new RecordMapper(createArgs));
    }

    private NetObligation(RecordMapper mapper) {
        this.ccp  = mapper.getPartyField(0);
        this.receiver = mapper.getPartyField(1);
        this.payer = mapper.getPartyField(2);
        this.paymentProcessor = mapper.getPartyField(3);
        this.cusip = mapper.getTextField(4);
        this.currency = mapper.getTextField(5);
        this.paymentAmount = mapper.getDecimalField(6);
        this.quantity = mapper.getDecimalField(7);
        this.settlementDate = mapper.getDateField(8);
    }

    public String getCcp() {
        return ccp;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getPayer() {
        return payer;
    }

    public String getPaymentProcessor() {
        return paymentProcessor;
    }

    public String getCusip() {
        return cusip;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public LocalDate getSettlementDate() {
        return settlementDate;
    }
}
