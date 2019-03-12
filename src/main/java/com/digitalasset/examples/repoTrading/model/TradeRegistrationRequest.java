// Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.digitalasset.examples.repoTrading.model;

import com.daml.ledger.javaapi.data.Record;

public class TradeRegistrationRequest implements DomainObject {

//    tradeRequester : Party
//    tradeCounterParty : Party
//    tradeInfo : TradeRecord
//    ccp : Party

    private String tradeRequester;
    private String tradeCounterParty;
    private TradeInfo tradeInfo;
    private String ccp;

    public static TradeRegistrationRequest fromRecord(Record record) {
        return new TradeRegistrationRequest(new RecordMapper(record));
    }

    private TradeRegistrationRequest(RecordMapper mapper) {
        this.tradeRequester = mapper.getPartyField(0);
        this.tradeCounterParty = mapper.getPartyField(1);
        this.tradeInfo = TradeInfo.fromRecord(mapper.getRecordField(2));
        this.ccp = mapper.getPartyField(3);
    }

    public String getTradeRequester() {
        return tradeRequester;
    }

    public String getTradeCounterParty() {
        return tradeCounterParty;
    }

    public TradeInfo getTradeInfo() {
        return tradeInfo;
    }

    public String getCcp() {
        return ccp;
    }
}
