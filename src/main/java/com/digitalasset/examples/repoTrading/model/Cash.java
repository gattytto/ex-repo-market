// Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
// SPDX-License-Identifier: (Apache-2.0 OR BSD-3-Clause)

package com.digitalasset.examples.repoTrading.model;

import com.daml.ledger.javaapi.data.Record;

import java.math.BigDecimal;

public class Cash implements DomainObject {

    private String ccp;
    private String owner;
    private String currency;
    private BigDecimal amount;
    private String paymentProcessor;

    public static Cash fromRecord(Record createArgs) {
        return new Cash(new RecordMapper(createArgs));
    }

    private Cash(RecordMapper mapper) {

//        ccp: Party
//        owner: Party
//        currency: Text
//        amount: Decimal
//        paymentProcessor: Party

        this.ccp = mapper.getPartyField(0);
        this.owner = mapper.getPartyField(1);
        this.currency = mapper.getTextField(2);
        this.amount = mapper.getDecimalField(3);
        this.paymentProcessor = mapper.getPartyField(4);
    }

    public BigDecimal getamount() {
        return amount;
    }

    public String getCcp() {
        return ccp;
    }

    public String getOwner() {
        return owner;
    }

    public String getCurrency() {
        return currency;
    }

    public String getPaymentProcessor() {
        return paymentProcessor;
    }
}
