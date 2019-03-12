// Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.digitalasset.examples.repoTrading.model;

import com.daml.ledger.javaapi.data.Record;

import java.time.LocalDate;

public class NovatedTrade implements DomainObject {

//    participantId : Party
//    -- true when participant is lender and CCP is borrower, false otherwise
//    isBuy: Bool
//    tradeInfo : TradeRecord
//    ccp: Party

    private String participantId;
    private Boolean isBuy;
    private TradeInfo tradeInfo;
    private String ccp;

    public static NovatedTrade fromRecord(Record createArguments) {
        return new NovatedTrade(new RecordMapper(createArguments));
    }

    private NovatedTrade(RecordMapper mapper) {
        this.participantId = mapper.getPartyField(0);
        this.isBuy = mapper.getBooleanField(1);
        this.tradeInfo = TradeInfo.fromRecord(mapper.getRecordField(2));
    }

    public String getDomainKey() {
        return String.format("%s:%s:%s:%s",
            getTradeInfo().getSettlementDate().toString(),
            getParticipantId(),
            getTradeInfo().getCusip(),
            getTradeInfo().getCurrency());
    }

    public String getParticipantId() {
        return participantId;
    }

    public Boolean isBuy() {
        return isBuy;
    }

    public TradeInfo getTradeInfo() {
        return tradeInfo;
    }

    public String getCcp() {
        return ccp;
    }

    public LocalDate getSettlementDate(){
        return getTradeInfo().getSettlementDate();
    }
}
