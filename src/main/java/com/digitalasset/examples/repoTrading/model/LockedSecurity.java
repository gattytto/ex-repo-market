// Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
// SPDX-License-Identifier: (Apache-2.0 OR BSD-3-Clause)

package com.digitalasset.examples.repoTrading.model;

import com.daml.ledger.javaapi.data.Record;

public class LockedSecurity implements DomainObject {

//    security : Security
//    ccp : Party

    private Security security;  // : Security
    private String ccp;         // : Party

    public static LockedSecurity fromRecord(Record createArgs) {
        return new LockedSecurity(new RecordMapper(createArgs));
    }

    private LockedSecurity(RecordMapper mapper) {
        this.security = Security.fromRecord(mapper.getRecordField(0));
        this.ccp = mapper.getPartyField(1);
    }

    public Security getSecurity() {
        return security;
    }

    public String getCcp() {
        return ccp;
    }
}
