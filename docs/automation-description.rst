Automation Description
----------------------

Previous: `DAML Implementation <daml-implementation.rst>`_

Without external inputs, the DAML models described above will do nothing. External processes provide this input, and automate several aspects of the running system. 

This includes:

* System setup, including invitation of role contracts for parties: An Operator, Clearing House (CCP) and several Trading Participants
* Injection of trade requests, and acceptance by a counterparty
* Settlement processing as described above, including trade novation, netting and DvP settlement

Implementation Overview
~~~~~~~~~~~~~~~~~~~~~~~

All the automation processes are implemented using the Java Reactive Components Ledger Bindings library. Full details of this library, as well as other examples, can be found in the documentation for the ledger API `Java Bindings <https://docs.daml.com/packages/bindings-java-tutorial/index.html>`_. You can get more general information with the `Ledger API <https://docs.daml.com/packages/ledger-api-introduction/index.html>`_ documentation.

This library provides a framework for building 'state-based' automation. With this approach, the application provides code that is a mapping from a particular application state to a set of API commands to be sent to the ledger as a response to this state.

The library take care of interfacing to the low-level gRPC-based ledger API, and provides a stable state for the application to query. As part of doing this, it handles the asynchronous nature of the ledger API by implementing a pending set - that is, a set of the contract ID's that have had choices exercised on them, but whose completion notifications have not yet been received. The library takes care of removing them from the state once the commands have been sent, while watching for completions that indicate wether the command succeeded or failed. These completion are then used by the library to either permanently remove the pending IDs from the state, or re-insert them because the command failed, and they have not been archived.

This mapping from state to command set is termed a 'Bot'.

Application Processes
~~~~~~~~~~~~~~~~~~~~~

The automation consists of a Java application implementing a Bot for each of the parties in the system. These are:

* The Operator - `OperatorBot`_
* The Central Clearing House, or CCP - `ClearingHouseBot`_
* The Payment Processor - `PaymentProcessorBot`_
* Each of the Trading Participants - `TradingParticipantBot`_

Each of these is implemented as a Java class, defining the Bot function, and all supporting code. All are derived from a common base class, ``RepoMarketBot`` that defines common methods for all of them.

Bot classes
~~~~~~~~~~~

RepoMarketBot
#############

`RepoMarketBot.java <../src/main/java/com/digitalasset/examples/repoTrading/RepoMarketBot.java>`_  is a common base class for all other automation bots in the application. It defines and provides base implementations of the three primary methods required by the Reactive Components library: 

* the bot function `runProcess() <../src/main/java/com/digitalasset/examples/repoTrading/RepoMarketBot.java#L92-L98>`_ that defines the mapping between the ledger state and a command set.
* the method `asDomainObject() <../src/main/java/com/digitalasset/examples/repoTrading/RepoMarketBot.java#L88-L90>`_, which transform raw creation and archive events into more useful formats, such as instances of application-specific domain classes (described below in `Domain Modelling`_)
* the method `getTransactionFilter() <../src/main/java/com/digitalasset/examples/repoTrading/RepoMarketBot.java#L100>`_ that returns a ``TransactionFilter`` instance defining the events the bot will receive, in terms of both a party or parties involved, and a specific set of template IDs.

`runProcess() <../src/main/java/com/digitalasset/examples/repoTrading/RepoMarketBot.java#L92-L98>`_ is the main bot function, and is shared by all subclasses. It receives a view of the ledger state from the bot framework, and applies this to a sub-method, `process() <../src/main/java/com/digitalasset/examples/repoTrading/RepoMarketBot.java#L98>`_. This is an abstract method on ``RepoMarketBot``, and is implemented in sub-classes to provide bot-specific behavior. 

``process()`` returns a stream of ``CommandsAndPendingSet`` instances which will ultimately be passed to the Bot framework for execution. These define a set of commands, and an associated pending set, and are created by the bot after examining the state provided in the ``ledgerView``. This stream is filtered to remove any empty instances (returned by some part of the bot function which does not require any action), and is then turned into a ``Flowable`` which is returned to the library for processing.

`asDomainObject() <../src/main/java/com/digitalasset/examples/repoTrading/RepoMarketBot.java#L88-L90>`_ is the transform method that is provided to the bot framework to transform events. It delegates to the method `domainObjectFromRecord <../src/main/java/com/digitalasset/examples/repoTrading/util/ModelMapper.java#L26-L51>`_ on the class ``ModelMapper``, passing the raw representation of a contract ``Create`` event, along with the template ID. 

`getTransactionFilter() <../src/main/java/com/digitalasset/examples/repoTrading/RepoMarketBot.java#L100>`_ is the method that will return a ``TransactionFilter`` instance that will be used when connecting the bot to the library. It is again an abstract method that all sub-classes must implement as required.

Since all sub-class share these methods, the ``RepoMarketBot`` class can also be the point at which the application wires itself into the bindings library. It does this in the `run() <../src/main/java/com/digitalasset/examples/repoTrading/RepoMarketBot.java#L82-L86>`_ method, using the ``Bot.wire`` static method provide by the library, passing in an application ID, a ledger client interface class instance, and the three methods described above. This ``run`` method is then called by the `run() <../src/main/java/com/digitalasset/examples/repoTrading/RepoTradingMain.java#L83-L126>`_ method on the main class `RepoTradingMain`_.

OperatorBot
###########

`OperatorBot.java <../src/main/java/com/digitalasset/examples/repoTrading/OperatorBot.java>`_ is responsible for the initial setup of the system. It

* creates an instance of the genesis contract.
* invites the clearing house (CCP) and payment processor parties to join the system
* reads trading parties from a file and invites each to join the system

The process is started by an explicit creation of the genesis contract, called from the class `run() <../src/main/java/com/digitalasset/examples/repoTrading/OperatorBot.java#L53-L63>`_ method. Once this contract is created, all other changes in the system are triggered by exercise commands on existing contract instances. 

The bot then waits for the appearance of this contract in the active contract set provided to it's `process() <../src/main/java/com/digitalasset/examples/repoTrading/OperatorBot.java#L85-L93>`_ method. When this appears, the method `inviteCcpAndPaymentProcessor() <../src/main/java/com/digitalasset/examples/repoTrading/OperatorBot.java#L95-L121>`_ is called, which exercises the choices ``InviteCCP`` and ``InvitePaymentProcessor`` on the contract. This is responded to by the `ClearingHouseBot <../src/main/java/com/digitalasset/examples/repoTrading/ClearingHouseBot.java#L179-L199>`_ which accepts the invite and causes the CCP role contract to be created.

Once the CCP contract appears in the active contract set, trading participants can be invited. This is done by `inviteTradingParties() <../src/main/java/com/digitalasset/examples/repoTrading/OperatorBot.java#L123-L148>`_. Parties are read from a default trading parties file, and are created by exercising an invite  choice ``InviteTradingParticipant`` on this contract. These invites are responded to by each `TradingParticipantBot`_

PaymentProcessorBot
###################

`PaymentProcessorBot.java <../src/main/java/com/digitalasset/examples/repoTrading/PaymentProcessorBot.java>`_ is responsible for responding to requests for cash. Currently, these requests are called entirely from the DAML code, so the only function required by the bot is to respond to invitation requests from the operator.

Since the operator, payment processor and ccp are all parties to most transactions in the system, there is a multi-state propose-accept workflow required to include the payment processor. It therefore does not respond directly to the invite request generated by the `OperatorBot`_. Instead, the CCP accepts that, which in turn creates another invitation contract (``CCPInvite``) that the payment processor bot finally accepts. This ensures that the operator, ccp and paymentProcessor parties are all authorized stakeholders on subsequent contract creation.

TradingParticipantBot
#####################

`TradingParticipantBot.java <../src/main/java/com/digitalasset/examples/repoTrading/TradingParticipantBot.java>`_ is responsible for initiating trades in the system on behalf of a trading party. An instance of a ``TradingPartyBot`` is started for each trading participant in the system, and injects trades on behalf of that party by exercising the ``RequestTrade`` choice on it's role contract, ``TradingParticipant``. This means that this contract must be authorized by the ccp and operator to do this.

This is done by operator invitation, as described in `OperatorBot`. The first task of this bot is therefore to respond to these requests by exercising the ``AcceptTradingInvite`` choice when the invitation contract appears. This is done in the bots implementation of the `process() <../src/main/java/com/digitalasset/examples/repoTrading/TradingParticipantBot.java#L196-L207>`_ method

Once this is done, a ``TradingParticipant`` role contract is created, and trade injection can begin. This is done by the method `injectTrades() <../src/main/java/com/digitalasset/examples/repoTrading/TradingParticipantBot.java#L226-L242>`_. In a real-life system, this would normally come via electronic messaging. In this case, the operation is simulated by providing a list of trades to inject via a csv-formatted trade file.

This is passed to the bot as a `command line argument <../src/main/java/com/digitalasset/examples/repoTrading/TradingParticipantBot.java#L138-L139>`_, and contains a list of trades to be injected. Sample trade files are provided in the ``data`` folder. Each line of the file defines a repo trade between a buyer and a seller. The file is read by each bot, which selects lines from the file for which it's party is defined as the buyer. It creates a ``RequestTrade`` choice from that line with the appropriate arguments, and then exercises this on it's ``TradingParticipant`` role contract.

So that it's response to events from the ledger is not impeded by this injection loop, the injection loop is run in a background thread, implemented as a ``Runnable`` class, `TradeStreamer <../src/main/java/com/digitalasset/examples/repoTrading/TradingParticipantBot.java#L49-L120>`_.

Each injection exercise is spaced with a 2 second delay.

Since a trading participant can also be a counterparty on a trade, the bot must also be able to accept trade requests. This is done by responding to ``TradeRegistrationRequests`` generated by trade injections in the method `acceptTradeRequest() <../src/main/java/com/digitalasset/examples/repoTrading/TradingParticipantBot.java#L244-L263>`_

The final responsibility of the bot is to agree to net obligations created by the ccp as part of trade settlement. This is done by responding to instances of the ``NetObligationRequest`` template, by exercising the ``AcceptNetObligation`` choice

ClearingHouseBot
################

`ClearingHouseBot.java <../src/main/java/com/digitalasset/examples/repoTrading/ClearingHouseBot.java>`_ is responsible for controlling all aspects of the settlement process, and is the main actor in the system. It's `process() <../src/main/java/com/digitalasset/examples/repoTrading/ClearingHouseBot.java#L133-L150>`_ method is the driver of this processing.

After accepting an invite from the `OperatorBot`_ to join the system, starts a simple http server that implements routes ``settlement`` and ``tradeState``. These endpoints allow the bot to be remotely controlled. This is promariliy for initiating settlement. In a real system, this would be initiated by polling the time, but here, it is manually started by performing a GET to the ``settlement`` endpoint with a ``date`` parameter of the required settlement date. When the bot detects this contract instance, it stars settlement by calling `novateTrade() <../src/main/java/com/digitalasset/examples/repoTrading/ClearingHouseBot.java#L217-L241>`_ to novate all trades that have the specified settlement date. This creates two novated trades for each participant trade. Once all trades have been novated, netting can begin, and this is detected by counting the expected number of novated trades.

Netting is performed by `createNettingGroups <../src/main/java/com/digitalasset/examples/repoTrading/ClearingHouseBot.java#L307-L371>`_ which sorts all novated trades into groups of similar trades that can be netted out. This forms a netting group, and is defined by novated trades that have the same settlement date, participant, cusip and currency. This forms a grouping key, and is defined by the method `getDomainKey() <../src/main/java/com/digitalasset/examples/repoTrading/model/NovatedTrade.java#L33-L39>`_ implemented on the domain model class `NovatedTrade <../src/main/java/com/digitalasset/examples/repoTrading/model/NovatedTrade.java>`_. Using this method, the netting groups can be formed as sets of trades with the same key, and formed using the Java Streams method `groupingBy() <../src/main/java/com/digitalasset/examples/repoTrading/ClearingHouseBot.java#L344-L346>`_

Once these groups have been formed, ``NettingGroup`` contracts are created by the method `netTrades() <../src/main/java/com/digitalasset/examples/repoTrading/ClearingHouseBot.java#L373-L396>`_ by exercising the choice ``FormNettingGroups`` on the CCP role contract. These are collected by the CCP as they are created, and the trades netted by exercising the choice ``NetTrades``. This creates a ``NetObligationRequest`` contract, which is picked up by the appropriate ``TradingParticipantBot`` and accepted as agreed. The acceptance creates a ``NetObligation`` contract specifying the netted obligation between the CCP and the trading participant for the given security.

A DvP can now be created for each of the obligations. This is done by the the method `createDvP() <../src/main/java/com/digitalasset/examples/repoTrading/ClearingHouseBot.java#L398-L429>`_ when the ``NetObligation`` is received, by exercising either the ``CreateBuyDvP`` or ``CreateSellDvP``, depending on whether the CCP is the buyer or seller of the security. As part of this exercise, cash for the buy side, and cash and securities for the sell side are also created. These created ``Cash`` and ``Security`` contracts are collected and stored by the CCP, ready for allocation and settlement.

The bot can now look for instances of DvP's that are ready to settle. These are represented by instances of the ``AllocatedDvP``, and are initially created from the sell side DvP creation. These are picked up by the method `settleDvP() <../src/main/java/com/digitalasset/examples/repoTrading/ClearingHouseBot.java#L431-L449>`_ and settled by exercising the ``Settle` choice. This unlocks the assets, transfers them to the new owners, and creates an instance of a ``SettledDvP`` contract as a record.

Since the CCP is the receiver on sell side contracts, settling this first transfers securities from the seller participant to the CCP. These can then be used to settle the buy side DvP. The CCP does this by collecting securities transferred to them, and comparing these with buy side DvP's waiting for security allocation. This is done in the method `allocateSecurities() <../src/main/java/com/digitalasset/examples/repoTrading/ClearingHouseBot.java#L538-L560>`_ which is called each time the ledger view changes. This method collects securities available for allocation, and tries to allocate them to each buy side DvP. Whenever a set of securities is found that can be allocated to a DvP, the method creates an exercise to do the allocation, and returns.

Allocation is done by scanning the set of available securities for a match against a given DvP. A match is found when the securities CUSIP matches that of the DvP being allocated. The security is then added to a list of  allocated securities, along with a running total of the quantity of securities allocated. The allocation succeeds when the list of securities has not been exhausted, and the sum of allocated securities is equal to or greater than the amount required by the given DvP. At this point, there are now enough securities to complete allocation, and an `AllocationResult <../src/main/java/com/digitalasset/examples/repoTrading/ClearingHouseBot.java#L451-L483>`_ is returned. If the allocation exhausts available securities before satisfying the quantity in the DvP, and empty ``AllocationResult`` is returned.

The returned ``AllocationResult`` indicates whether it has been allocated, and if so, is used to build an exercise command to do the allocation. This results in a new ``AllocatedDvP`` contract, which is then settled in the same manner as described above.

Settlement is complete when the expected number of ``SettledDvP`` contracts are received. Since each ``NetObligation`` creates a single DvP, this is equal to the number of ``NettingGroups`` created. This is tested in the method `finishSettlement() <../src/main/java/com/digitalasset/examples/repoTrading/ClearingHouseBot.java#L562-L605>`_ which resets the state ready for settlement initiation on a new date.

RepoTradingMain
~~~~~~~~~~~~~~~

`RepoTradingMain.java <../src/main/java/com/digitalasset/examples/repoTrading/RepoTradingMain.java>`_ forms the main class for the application, and is shared by all bot processes. The core of it's functionality is contained in it's `run() <../src/main/java/com/digitalasset/examples/repoTrading/RepoTradingMain.java#L83-L126>`_ method, which is responsible for creating and running an instance of one of the bot classes described above. This choice is determined by an initial program argument (the 'command verb') passed in when the program is started, and therefore determines the function of the bot.

The ``run`` method splits the program arguments into two parts, the first of which it parses for itself. This determines the command verb and any general option arguments. It then creates a client connection to the ledger, and runs an instance of a bot class by switching on the command verb. It returns 0 if no errors occur, or an error code indicating the error.

This ``run`` method is in turn called from the classes static `main() <../src/main/java/com/digitalasset/examples/repoTrading/RepoTradingMain.java#L68-L81>`_ method. This short method creates an instance of itself and calls the ``run`` method with all provided program arguments. It then waits for a termination signal from any thread, or terminates immediately with any returned error code.

Domain Modelling
~~~~~~~~~~~~~~~~

In order to make coding easier, it is good practice to convert the generic record format returned by the ledger API into explicit class instances representing these domain contracts. The bot framework supports this by allowing a conversion function to be passed to the library when the bot is wired up (``Bot.wire`` function).

This function is implemented by the method ``asDomainObject`` on the ``RepoMarketBot`` base class. This delegates to the `ModelMapper <../src/main/java/com/digitalasset/examples/repoTrading/util/ModelMapper.java>`_ class, which associates specific classes with a ``String`` ID extracted from the API record format. This then creates an instance of the mapped class using a static factory method ``fromRecord``, e.g `DvP.fromRecord() <../src/main/java/com/digitalasset/examples/repoTrading/model/DvP.java#L33-L35>`_.

The method ``fromRecord`` uses a  constructor to build the domain model instance. The instance contains instance variables mapping all or part of the API record, with the mapping between instance variable and record fields being done using a known index of the record field. The API returns contract parameters in the same order they are declared, so field indexes can be derived from the DAML source. 

The task of mapping from an API creation record to a domain model instance also involves correct typing of the returned values. This is done by calling the correct conversion method on teh generic ``Value`` objects returned by the API, and this is centralized in the class `RecordMapper <../src/main/java/com/digitalasset/examples/repoTrading/model/RecordMapper.java>`_. This class wraps an API ``Record``, and provides helper methods to convert a ``Value`` at a particular parameter index into a value of the correct type - e.g `getPartyField() <../src/main/java/com/digitalasset/examples/repoTrading/model/RecordMapper.java#L52-L57>`_. These are called by the domain model constructor as needed for each instance variable.