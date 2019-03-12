#!/usr/bin/env bash

# Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
# SPDX-License-Identifier: (Apache-2.0 OR BSD-3-Clause)

BASE=$(dirname $0)/..
cd $BASE

mvn test && da run damlc -- test src/main/daml/RepoMarketTests.daml