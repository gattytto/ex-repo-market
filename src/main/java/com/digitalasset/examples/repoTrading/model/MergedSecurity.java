// Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
// SPDX-License-Identifier: (Apache-2.0 OR BSD-3-Clause)

package com.digitalasset.examples.repoTrading.model;

import com.daml.ledger.javaapi.data.Record;

import java.math.BigDecimal;

public class MergedSecurity implements DomainObject {

    private String cusip;
    private String owner;
    private BigDecimal collateralQuantity;
    private String ccp;

    public static MergedSecurity fromRecord(Record createArgs) {
        return new MergedSecurity(new RecordMapper(createArgs));
    }

    private MergedSecurity(RecordMapper mapper) {

//        cusip: Text
//        owner: Party
//        collateralQuantity: Decimal
//        ccp: Party

        this.cusip = mapper.getTextField(0);
        this.owner = mapper.getPartyField(1);
        this.collateralQuantity = mapper.getDecimalField(2);
        this.ccp = mapper.getPartyField(3);
    }

    public String getCusip() {
        return cusip;
    }

    public String getOwner() {
        return owner;
    }

    public BigDecimal getCollateralQuantity() {
        return collateralQuantity;
    }

    public String getCcp() {
        return ccp;
    }
}
