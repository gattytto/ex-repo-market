// Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
// SPDX-License-Identifier: (Apache-2.0 OR BSD-3-Clause)

package com.digitalasset.examples.repoTrading.model;

import com.daml.ledger.javaapi.data.Record;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DvP implements DomainObject {

//    ccp: Party
//    paymentProcessor : Party
//    payer: Party
//    receiver: Party
//    settlementDate: Time
//    cusip: Text
//    currency: Text
//    paymentAmount: Decimal
//    quantity: Decimal

//    ccp: Party
//    paymentProcessor : Party
    private String payer;           //: Party
    private String receiver;        //: Party
    private LocalDate settlementDate;    //: Time
    private String cusip;           // Text
    private String currency;        //: Text
    private BigDecimal paymentAmount;  //: Decimal
    private BigDecimal quantity;       //: Decimal

    public static DvP fromRecord(Record createArgs) {
        return new DvP(new RecordMapper(createArgs));
    }


    private DvP(RecordMapper mapper) {
        this.payer = mapper.getPartyField(2);
        this.receiver = mapper.getPartyField(3);
        this.settlementDate = mapper.getDateField(4);
        this.cusip = mapper.getTextField(5);
        this.currency = mapper.getTextField(6);
        this.paymentAmount = mapper.getDecimalField(7);
        this.quantity =mapper.getDecimalField(8);
    }

    public String getCusip() {
        return cusip;
    }

    public LocalDate getSettlementDate() {
        return settlementDate;
    }

    public String getCurrency() {
        return currency;
    }

    public String getPayer() {
        return payer;
    }

    public String getReceiver() {
        return receiver;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

}
