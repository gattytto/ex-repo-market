// Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
// SPDX-License-Identifier: (Apache-2.0 OR BSD-3-Clause)

package com.digitalasset.examples.repoTrading.model;

import com.daml.ledger.javaapi.data.Record;

public class Trade  implements DomainObject {

//    borrower : Party
//    lender : Party
//    tradeInfo : TradeRecord
//    ccp : Party

    private String borrower;
    private String lender;
    private TradeInfo tradeInfo;
    private String ccp;

    public static Trade fromRecord(Record record) {
        return new Trade(new RecordMapper(record));
    }

    private Trade(RecordMapper mapper) {
        this.borrower = mapper.getPartyField(0);
        this.lender = mapper.getPartyField(1);
        this.tradeInfo = TradeInfo.fromRecord(mapper.getRecordField(2));
        this.ccp = mapper.getPartyField(3);
    }

    public String getSeller() {
        return borrower;
    }

    public String getBuyer() {
        return lender;
    }

    public TradeInfo getTradeInfo() {
        return tradeInfo;
    }

    public String getCcp() {
        return ccp;
    }
}
