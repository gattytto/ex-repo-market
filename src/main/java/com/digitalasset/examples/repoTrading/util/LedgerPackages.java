// Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.digitalasset.examples.repoTrading.util;

import com.digitalasset.daml_lf.DamlLf;
import com.digitalasset.daml_lf.DamlLf1;
import com.digitalasset.ledger.api.v1.PackageServiceOuterClass;

import com.daml.ledger.javaapi.LedgerClient;
import com.daml.ledger.javaapi.PackageClient;
import com.google.protobuf.InvalidProtocolBufferException;
import io.reactivex.Flowable;

import java.util.Optional;

public class LedgerPackages {

    private final LedgerClient client;

    public LedgerPackages(LedgerClient client) {
        this.client = client;
    }

    /**
     * Inspects all DAML packages that are registered on the ledger and returns the id of the package that contains the
     * module
     *
     * This is useful during development when the DAML model changes a lot, so that the package id doesn't need to
     * be updated manually after each change.
     *
     * @param module the name of the module to look for
     * @return the package id of the example DAML module
     */
    public String packageIdFor(String module) {
        PackageClient packageService = client.getPackageClient();

        // fetch a list of all package ids available on the ledger
        Flowable<String> packagesIds = packageService.listPackages();

        // fetch all packages and find the package that contains the PingPong module
        String packageId = packagesIds
            .flatMap(p -> packageService.getPackage(p).toFlowable())
            .filter(r -> containsModule(r,module))
            .map(PackageServiceOuterClass.GetPackageResponse::getHash)
            .firstElement().blockingGet();

        if (packageId == null) {
            // No package on the ledger contained the PingPong module
            throw new RuntimeException(String.format("Module %s is not available on the ledger",module));
        }
        return packageId;
    }

    private boolean containsModule(PackageServiceOuterClass.GetPackageResponse getPackageResponse, String module) {
        try {
            // parse the archive payload
            DamlLf.ArchivePayload payload = DamlLf.ArchivePayload.parseFrom(getPackageResponse.getArchivePayload());
            // get the DAML LF package
            DamlLf1.Package lfPackage = payload.getDamlLf1();
            // check if the named module is in the current package package
            Optional<DamlLf1.Module> repoModule = lfPackage.getModulesList().stream()
                .filter(m -> m.getName().getSegmentsList().contains(module)).findFirst();

            if (repoModule.isPresent())
                return true;

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return false;
    }
}
