// Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
// SPDX-License-Identifier: (Apache-2.0 OR BSD-3-Clause)

package com.digitalasset.examples.repoTrading.model;

public interface DomainObject {

    default String getDomainKey() {
        throw new RuntimeException(getClass()+": domain key udefined");
    }
}
