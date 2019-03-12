// Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
// SPDX-License-Identifier: (Apache-2.0 OR BSD-3-Clause)

package com.digitalasset.examples.repoTrading.util;

import com.digitalasset.examples.repoTrading.model.Cash;
import com.digitalasset.examples.repoTrading.model.DomainObject;
import com.digitalasset.examples.repoTrading.model.DvP;
import com.digitalasset.examples.repoTrading.model.LockedCash;
import com.digitalasset.examples.repoTrading.model.MergedSecurity;
import com.digitalasset.examples.repoTrading.model.NetObligation;
import com.digitalasset.examples.repoTrading.model.NovatedTrade;
import com.digitalasset.examples.repoTrading.model.RecordMapper;
import com.digitalasset.examples.repoTrading.model.Security;
import com.digitalasset.examples.repoTrading.model.Trade;
import com.digitalasset.examples.repoTrading.model.TradeRegistrationRequest;

import com.daml.ledger.javaapi.data.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelMapper {

    private static final Logger log = LoggerFactory.getLogger(ModelMapper.class);

    public static DomainObject domainObjectFromRecord(String templateId, Record createArgs) {
        DomainObject modelObject;
        switch(templateId) {
            case "Main.Cash:Cash": modelObject = Cash.fromRecord(createArgs); break;
            case "Main.Cash:LockedCash": modelObject = LockedCash.fromRecord(createArgs); break;
            case "Main.Security:Security": modelObject = Security.fromRecord(createArgs); break;
            case "Main.Security:MergedSecurity": modelObject = MergedSecurity.fromRecord(createArgs); break;
            case "Main.Trade:NovatedTrade": modelObject = NovatedTrade.fromRecord(createArgs); break;
            case "Main.DvP:DvP": modelObject = DvP.fromRecord(createArgs); break;
            case "Main.DvP:CashAllocatedDvP": modelObject = DvP.fromRecord(createArgs); break;
            case "Main.DvP:AllocatedDvP": modelObject = DvP.fromRecord(createArgs); break;
            case "Main.DvP:SettledDvP": modelObject = DvP.fromRecord(createArgs); break;
            case "Main.NetObligation:NetObligation": modelObject = NetObligation.fromRecord(createArgs); break;
            case "Main.Trade:TradeRegistrationRequest": modelObject = TradeRegistrationRequest.fromRecord(createArgs); break;
            case "Main.Trade:Trade": modelObject = Trade.fromRecord(createArgs); break;
            default: modelObject = RecordMapper.fromRecord(createArgs); break;
        }
        log.trace("{} maps to {} ", templateId, modelObject.getClass());
        return modelObject;
    }

}
