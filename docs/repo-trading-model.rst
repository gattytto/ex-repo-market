Repurchase Agreements Trading Model
-----------------------------------

Previous: `README <../README.rst>`_

This section explains the key workflows involved in tri-party repurchase agreements.

Repurchase Agreements (Repos)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

A Repurchase Agreement (**Repo**) is a form of short-term loan to raise capital. It often involves one party selling a security to another party for cash, and buying it back the next day.

A repo acts as a collateral-backed, interest-bearing loan, with the buyer acting as a lender and the seller as a borrower.

Tri-Party Repos and Clearing Houses
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The most common type of repo is a **tri-party repo**, where a clearing house acts as the middleman between the buyer and seller.

The clearing house accepts securities from the seller and cash from the buyer, then gives cash to the seller and securities to the buyer. Upon maturity, the buyer sells securities back to the clearing house, and the seller takes them back in exchange for the cash.

The Clearing House Process
~~~~~~~~~~~~~~~~~~~~~~~~~~

.. list-table::
  :widths: 1 1
  :header-rows: 0

  * - **1. The scenario**

      Two parties want to transact a repo trade.
    - .. figure:: img/scenario.png
  * - **2. Novation**

      The clearing house becomes a central counterparty for the trading partners: that is, it **novates** the trade, turning a single trade into two trades with a central counterparty.
    - .. figure:: img/novation.png
  * - **3. Transfer**

      The clearing house takes ownership of collateral from both parties.
    - .. figure:: img/transfer.png
  * - **4. Grouping**

      Rather than executing transfers individually, the clearing house groups them based on:

      - counterparty
      - settlement date
      - security CUSIP (an identifier for financial instruments)
      - currency
    -
  * - **5. Netting**

      The clearing house bundles the grouped trades into Net Broker Obligations: this is called **netting**.

      It uses these to create Delivery versus Payment contracts that reflect the net trades of each trading party and their obligations to the clearing house.
    - .. figure:: img/netting.png
  * - **6. Settling**

      Finally, according to these obligations, collateral is transferred to and from the clearing house. This **settles** all trades.
    - .. figure:: img/settling.png

Next: `DAML Implementation <daml-implementation.rst>`_