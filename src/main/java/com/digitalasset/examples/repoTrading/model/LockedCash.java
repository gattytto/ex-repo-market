// Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.digitalasset.examples.repoTrading.model;

import com.daml.ledger.javaapi.data.Record;

public class LockedCash implements DomainObject {

//    cash : Cash
//    ccp : Party

    private Cash cash;      // : Cash
    private String ccp;     // : Party

    public static LockedCash fromRecord(Record createArgs) {
        return new LockedCash(new RecordMapper(createArgs));
    }

    private LockedCash(RecordMapper mapper) {
        this.ccp = mapper.getPartyField(0);
        this.cash = Cash.fromRecord(mapper.getRecordField(1));

    }

    public Cash getCash() {
        return cash;
    }

    public String getCcp() {
        return ccp;
    }
}
