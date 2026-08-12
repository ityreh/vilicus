package com.vilicus.finance.model.camt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * CamtTransaction — Normalized transaction extracted from CAMT.052 XML.
 *
 * Represents a single transaction entry (Ntry) from a CAMT.052 bank statement.
 * Fields are normalized from the CAMT XML structure for use in the Transaction entity.
 *
 * This is NOT a JAXB class — it's a simple POJO used after parsing.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CamtTransaction {

    /**
     * Transaction ID from bank (Prtry/Id field in CAMT).
     * Unique per account — used for deduplication.
     */
    private String txId;

    /**
     * Transaction date (BookgDt from CAMT).
     * When the transaction was booked/recorded.
     */
    private LocalDate txDate;

    /**
     * Transaction amount (always positive).
     * Sign is stored in direction field (DEBIT/CREDIT).
     */
    private BigDecimal amount;

    /**
     * Transaction direction: DEBIT or CREDIT.
     * Extracted from CdtDbtInd field in CAMT (CRDT or DBTR).
     */
    private String direction;

    /**
     * Description/purpose of transaction.
     * From RmtInf/Ustrd (unstructured remittance information).
     */
    private String description;

    /**
     * Counterparty name (who sent/received money).
     * From Dbtr/Nm (debtor) or Cdtr/Nm (creditor) depending on direction.
     */
    private String counterparty;

    /**
     * Additional reference/booking text.
     * From RmtInf fields or other bank references.
     */
    private String reference;

    /**
     * Running account balance after this transaction.
     * From CurrAdjustment or ClosingBalance fields (optional in CAMT).
     */
    private BigDecimal balance;

    /**
     * Value date (when amount was cleared).
     * From ValDt field in CAMT.
     */
    private LocalDate valueDate;

    /**
     * Bank transaction code (metadata).
     * From BkTxCd/Domn/Cd (e.g., "PMNT" for payment).
     * Used for categorization rules.
     */
    private String bankTransactionCode;

    /**
     * Whether this is an internal transfer (same-bank, between accounts).
     * Useful for filtering in UI.
     */
    private Boolean isInternalTransfer;

    @Override
    public String toString() {
        return String.format("CamtTransaction{txId=%s, date=%s, amount=%s, direction=%s, counterparty=%s}",
                txId, txDate, amount, direction, counterparty);
    }
}
