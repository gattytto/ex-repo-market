// Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.digitalasset.examples.repoTrading;

import com.digitalasset.examples.repoTrading.model.DomainObject;
import com.digitalasset.examples.repoTrading.util.Configuration;
import com.digitalasset.examples.repoTrading.util.ModelMapper;

import com.daml.ledger.javaapi.DamlLedgerClient;
import com.daml.ledger.javaapi.components.Bot;
import com.daml.ledger.javaapi.components.LedgerViewFlowable;
import com.daml.ledger.javaapi.components.helpers.CommandsAndPendingSet;
import com.daml.ledger.javaapi.components.helpers.CreatedContract;
import com.daml.ledger.javaapi.data.Command;
import com.daml.ledger.javaapi.data.ExerciseCommand;
import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.Record;
import com.daml.ledger.javaapi.data.SubmitCommandsRequest;
import com.daml.ledger.javaapi.data.TransactionFilter;
import com.google.protobuf.Timestamp;
import io.reactivex.Flowable;
import org.pcollections.PMap;
import org.pcollections.PSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public abstract class RepoMarketBot {

    private static final Logger log = LoggerFactory.getLogger(RepoMarketBot.class);

    // Constants
    final static String ONBOARDING_WORKFLOW_ID = "Onboarding";
    final static String TRADE_INJECTION_WORKFLOW_ID = "TradeInjection";
    final static String NETTING_WORKFLOW_ID = "Netting";
    final static String SETTLEMENT_WORKFLOW_ID = "Settlement";

    // Template identities
    final Identifier ccpTemplateId;
    final Identifier tradeTemplateId;
    final Identifier cashRequestTemplateId;
    final Identifier novatedTradeTemplateId;
    final Identifier nettingGroupTemplateId;
    final Identifier netObligationRequestTemplateId;
    final Identifier netObligationTemplateId;
    final Identifier dvpTemplateId;
    final Identifier cashAllocatedDvpTemplateId;
    final Identifier allocatedDvpTemplateId;
    final Identifier settledDvpTemplateId;
    final Identifier cashTemplateId;
    final Identifier securityTemplateId;

    // Instance vars
    private final RepoTradingMain mainClass;
    private String party;

    RepoMarketBot(RepoTradingMain mainClass, String party) {
        this.mainClass = mainClass;
        this.party = party;

        this.ccpTemplateId = templateIdFor("CCP", "CCP");
        this.tradeTemplateId = templateIdFor("Trade", "Trade");
        this.cashRequestTemplateId = templateIdFor("CashRequest", "CashRequest");
        this.novatedTradeTemplateId = templateIdFor("Trade", "NovatedTrade");
        this.nettingGroupTemplateId = templateIdFor("Netting", "NettingGroup");
        this.netObligationRequestTemplateId = templateIdFor("NetObligation", "NetObligationRequest");
        this.netObligationTemplateId = templateIdFor("NetObligation", "NetObligation");
        this.dvpTemplateId = templateIdFor("DvP", "DvP");
        this.cashAllocatedDvpTemplateId = templateIdFor("DvP", "CashAllocatedDvP");
        this.allocatedDvpTemplateId = templateIdFor("DvP", "AllocatedDvP");
        this.settledDvpTemplateId = templateIdFor("DvP", "SettledDvP");
        this.cashTemplateId = templateIdFor("Cash", "Cash");
        this.securityTemplateId = templateIdFor("Security", "Security");
    }

    public int run(String [] args) throws java.io.IOException {
        Bot.wire(RepoTradingMain.APP_ID, mainClass.getClient(), getTransactionFilter(), this::runProcess, RepoMarketBot::asDomainObject);
        logMessage("bot started");
        return 0;
    }

    private static DomainObject asDomainObject(CreatedContract created) {
        return ModelMapper.domainObjectFromRecord(identifierToString(created.getTemplateId()),created.getCreateArguments());
    }

    private Flowable<CommandsAndPendingSet> runProcess(LedgerViewFlowable.LedgerView<DomainObject> ledgerView) {
        Stream<CommandsAndPendingSet> cmdStream = process(ledgerView)
            .filter(cps -> !cps.equals(CommandsAndPendingSet.empty));
        return Flowable.fromIterable(cmdStream::iterator);
    }

    public abstract Stream<CommandsAndPendingSet> process(LedgerViewFlowable.LedgerView<DomainObject> ledgerView);

    public abstract TransactionFilter getTransactionFilter();

    /**
     * Helper method to build a CommandAndPendingSet with default values
     *
     * @param workflowId the workflowId to use
     * @param commandList the commands to execute
     * @param pendingSet the contract ids that shoud be marked as pending
     *
     * @return a CommandsAndPendingSet
     */
    CommandsAndPendingSet newCommandAndPendingSet(String workflowId, List<Command> commandList, PMap<Identifier, PSet<String>> pendingSet) {
        SubmitCommandsRequest commands = new SubmitCommandsRequest(
            workflowId,
            RepoTradingMain.APP_ID,
            UUID.randomUUID().toString(),
            this.party,
            Timestamp.newBuilder().setSeconds(Instant.EPOCH.toEpochMilli() / 1000).build(),
            Timestamp.newBuilder().setSeconds(Instant.EPOCH.plusSeconds(10).toEpochMilli() / 1000).build(),
            commandList);
        return new CommandsAndPendingSet(commands, pendingSet);
    }

    ExerciseCommand newExercise(Identifier templateId, String contractId, String choice, Record.Field ... args) {
        log.debug("new exercise: {}, {}, {}, {}", templateId, contractId, choice, args);

        return new ExerciseCommand(
            templateId,contractId,
            choice,
            new Record(Arrays.asList(args)));
    }

    void submitCommands(String workflowId, List<Command> commands) {

        String commandId = UUID.randomUUID().toString();
        log.debug("{} submits command id={}, commands={}", getParty(), commandId, commands);

        // asynchronously send the commands
        getClient().getCommandSubmissionClient().submit(
            workflowId, RepoTradingMain.APP_ID,
            commandId, getParty(),
            Timestamp.newBuilder().setSeconds(Instant.EPOCH.toEpochMilli() / 1000).build(),
            Timestamp.newBuilder().setSeconds(Instant.EPOCH.plusSeconds(5).toEpochMilli() / 1000).build(),
            commands
        ).blockingGet();
    }

    Identifier templateIdFor(String subModule, String templateName) {
        return new Identifier(getPackageId(),"Main."+subModule,templateName);
    }

    void logMessage(String message) {
        RepoTradingMain.logMessage(getParty(), message);
    }

    void logError(String message) {
        RepoTradingMain.logError(getParty(), message);
    }

    public RepoTradingMain getMainClass() {
        return mainClass;
    }

    String getParty() {
        return party;
    }

    public void setParty(String party) {
        this.party = party;
    }

    private String getPackageId() {
        return mainClass.getPackageId();
    }

    String getOperatorName() {
        return mainClass.getOperatorName();
    }

    String getCcpName() {
        return mainClass.getCcpName();
    }

    String getPaymentProcessorName() {
        return mainClass.getPaymentProcessorName();
    }

    Configuration getConfiguration() {
        return mainClass.getConfiguration();
    }

    private DamlLedgerClient getClient() {
        return mainClass.getClient();
    }

    private static String identifierToString (Identifier identifier) {
        return identifier.getModuleName().concat(":").concat(identifier.getEntityName());
    }
}
