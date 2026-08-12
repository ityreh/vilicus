package com.vilicus.finance.service;

import com.vilicus.finance.exception.CamtParseException;
import com.vilicus.finance.model.camt.CamtTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * CamtParser — Parses CAMT.052 (ISO 20022) XML bank statements.
 *
 * Extracts:
 * - Statement info: IBAN, dates, balances
 * - Transaction entries: txId, date, amount, direction, description, counterparty
 *
 * CAMT.052 Structure (simplified):
 * ```
 * BkToCstmrStmt (root)
 *   └─ Stmt (statement)
 *       ├─ AcctId (account with IBAN)
 *       ├─ Bal (opening balance)
 *       └─ Ntry (transaction entries)
 *           ├─ Amt (amount)
 *           ├─ CdtDbtInd (CRDT or DBTR)
 *           ├─ BookgDt (transaction date)
 *           ├─ RltdPties (counterparty info)
 *           └─ RmtInf (description/reference)
 * ```
 *
 * Namespaces handled:
 * - http://www.w3.org/2000/xmlns/ (XML namespace)
 * - urn:iso:std:iso:20022:tech:xsd:camt.052.001.02 (CAMT namespace)
 */
@Service
@Slf4j
public class CamtParser {

    private static final String CAMT_NAMESPACE = "urn:iso:std:iso:20022:tech:xsd:camt.052.001.02";
    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Parse CAMT.052 XML file and extract transactions.
     *
     * @param file XML file to parse
     * @return CamtParseResult containing statement info and transactions
     * @throws CamtParseException if parsing fails
     */
    public CamtParseResult parseFile(File file) throws CamtParseException {
        log.info("Parsing CAMT.052 file: {}", file.getName());

        try {
            DocumentBuilder builder = createDocumentBuilder();
            Document doc = builder.parse(file);
            return parseDocument(doc);
        } catch (SAXException | IOException | ParserConfigurationException e) {
            log.error("Failed to parse CAMT file: {}", e.getMessage());
            throw new CamtParseException("Failed to parse CAMT.052 file: " + e.getMessage(), e);
        }
    }

    /**
     * Parse CAMT.052 XML from InputStream.
     *
     * @param inputStream input stream containing XML
     * @return CamtParseResult containing statement info and transactions
     * @throws CamtParseException if parsing fails
     */
    public CamtParseResult parseInputStream(InputStream inputStream) throws CamtParseException {
        log.info("Parsing CAMT.052 from input stream");

        try {
            DocumentBuilder builder = createDocumentBuilder();
            Document doc = builder.parse(inputStream);
            return parseDocument(doc);
        } catch (SAXException | IOException | ParserConfigurationException e) {
            log.error("Failed to parse CAMT stream: {}", e.getMessage());
            throw new CamtParseException("Failed to parse CAMT.052 stream: " + e.getMessage(), e);
        }
    }

    /**
     * Parse DOM document and extract statement + transactions.
     *
     * @param doc parsed XML document
     * @return CamtParseResult
     * @throws CamtParseException if required elements are missing
     */
    private CamtParseResult parseDocument(Document doc) throws CamtParseException {
        log.debug("Extracting data from CAMT.052 document");

        Element root = doc.getDocumentElement();
        if (!root.getLocalName().equals("Document")) {
            throw new CamtParseException("Invalid CAMT.052: root element must be 'Document'");
        }

        // Find BkToCstmrStmt (Bank-to-Customer Statement)
        Element bkToCstmrStmt = getFirstChildByLocalName(root, "BkToCstmrStmt");
        if (bkToCstmrStmt == null) {
            throw new CamtParseException("Invalid CAMT.052: missing BkToCstmrStmt element");
        }

        // Find Stmt (Statement)
        Element stmt = getFirstChildByLocalName(bkToCstmrStmt, "Stmt");
        if (stmt == null) {
            throw new CamtParseException("Invalid CAMT.052: missing Stmt element");
        }

        // Extract account info
        String iban = extractIban(stmt);
        BigDecimal openingBalance = extractOpeningBalance(stmt);
        BigDecimal closingBalance = extractClosingBalance(stmt);
        LocalDate statementDateFrom = extractStatementDateFrom(stmt);
        LocalDate statementDateTo = extractStatementDateTo(stmt);

        // Extract transactions
        List<CamtTransaction> transactions = extractTransactions(stmt);

        if (transactions.isEmpty()) {
            throw new CamtParseException("No transactions found in the CAMT.052 statement");
        }

        log.info("CAMT.052 parsed successfully: {} transactions, IBAN={}, {} to {}",
                transactions.size(), iban, statementDateFrom, statementDateTo);

        return CamtParseResult.builder()
                .iban(iban)
                .openingBalance(openingBalance)
                .closingBalance(closingBalance)
                .statementDateFrom(statementDateFrom)
                .statementDateTo(statementDateTo)
                .transactions(transactions)
                .build();
    }

    /**
     * Extract IBAN from statement.
     */
    private String extractIban(Element stmt) {
        Element acctId = getFirstChildByLocalName(stmt, "AcctId");
        if (acctId == null) return null;

        Element othr = getFirstChildByLocalName(acctId, "Othr");
        if (othr == null) return null;

        Element id = getFirstChildByLocalName(othr, "Id");
        if (id == null) return null;

        return id.getTextContent();
    }

    /**
     * Extract opening balance from statement.
     */
    private BigDecimal extractOpeningBalance(Element stmt) {
        NodeList balances = stmt.getElementsByTagNameNS(CAMT_NAMESPACE, "Bal");
        if (balances.getLength() == 0) return null;

        // First Bal is typically opening balance
        Element bal = (Element) balances.item(0);
        Element amt = getFirstChildByLocalName(bal, "Amt");
        if (amt == null) return null;

        try {
            return new BigDecimal(amt.getTextContent().trim());
        } catch (NumberFormatException e) {
            log.warn("Invalid opening balance amount: {}", amt.getTextContent());
            return null;
        }
    }

    /**
     * Extract closing balance from statement.
     */
    private BigDecimal extractClosingBalance(Element stmt) {
        NodeList balances = stmt.getElementsByTagNameNS(CAMT_NAMESPACE, "Bal");
        if (balances.getLength() < 2) return null;

        // Last Bal is typically closing balance
        Element bal = (Element) balances.item(balances.getLength() - 1);
        Element amt = getFirstChildByLocalName(bal, "Amt");
        if (amt == null) return null;

        try {
            return new BigDecimal(amt.getTextContent().trim());
        } catch (NumberFormatException e) {
            log.warn("Invalid closing balance amount: {}", amt.getTextContent());
            return null;
        }
    }

    /**
     * Extract statement start date.
     */
    private LocalDate extractStatementDateFrom(Element stmt) {
        // Look for date in FrToDt (from-to date)
        Element frToDt = getFirstChildByLocalName(stmt, "FrToDt");
        if (frToDt != null) {
            Element frDt = getFirstChildByLocalName(frToDt, "FrDt");
            if (frDt != null) {
                return parseDate(frDt.getTextContent());
            }
        }

        // Fallback: use first transaction date
        NodeList entries = stmt.getElementsByTagNameNS(CAMT_NAMESPACE, "Ntry");
        if (entries.getLength() > 0) {
            Element firstEntry = (Element) entries.item(0);
            Element bookgDt = getFirstChildByLocalName(firstEntry, "BookgDt");
            if (bookgDt != null) {
                return parseDate(bookgDt.getTextContent());
            }
        }

        return null;
    }

    /**
     * Extract statement end date.
     */
    private LocalDate extractStatementDateTo(Element stmt) {
        Element frToDt = getFirstChildByLocalName(stmt, "FrToDt");
        if (frToDt != null) {
            Element toDt = getFirstChildByLocalName(frToDt, "ToDt");
            if (toDt != null) {
                return parseDate(toDt.getTextContent());
            }
        }

        // Fallback: use last transaction date
        NodeList entries = stmt.getElementsByTagNameNS(CAMT_NAMESPACE, "Ntry");
        if (entries.getLength() > 0) {
            Element lastEntry = (Element) entries.item(entries.getLength() - 1);
            Element bookgDt = getFirstChildByLocalName(lastEntry, "BookgDt");
            if (bookgDt != null) {
                return parseDate(bookgDt.getTextContent());
            }
        }

        return null;
    }

    /**
     * Extract all transaction entries (Ntry elements) from statement.
     */
    private List<CamtTransaction> extractTransactions(Element stmt) throws CamtParseException {
        List<CamtTransaction> transactions = new ArrayList<>();

        NodeList entries = stmt.getElementsByTagNameNS(CAMT_NAMESPACE, "Ntry");
        log.debug("Found {} transaction entries", entries.getLength());

        for (int i = 0; i < entries.getLength(); i++) {
            try {
                Element entry = (Element) entries.item(i);
                CamtTransaction tx = parseTransaction(entry);
                if (tx != null) {
                    transactions.add(tx);
                }
            } catch (Exception e) {
                log.warn("Failed to parse transaction entry {}: {}", i, e.getMessage());
                // Continue parsing other entries
            }
        }

        return transactions;
    }

    /**
     * Parse a single transaction entry (Ntry element).
     */
    private CamtTransaction parseTransaction(Element entry) throws CamtParseException {
        CamtTransaction.CamtTransactionBuilder builder = CamtTransaction.builder();

        // Amount
        Element amt = getFirstChildByLocalName(entry, "Amt");
        if (amt != null) {
            try {
                builder.amount(new BigDecimal(amt.getTextContent().trim()));
            } catch (NumberFormatException e) {
                log.warn("Invalid amount in transaction: {}", amt.getTextContent());
                return null;
            }
        }

        // Direction (CRDT = Credit/incoming, DBTR = Debit/outgoing)
        Element cdtDbtInd = getFirstChildByLocalName(entry, "CdtDbtInd");
        if (cdtDbtInd != null) {
            String code = cdtDbtInd.getTextContent().trim();
            builder.direction(code.equals("CRDT") ? "CREDIT" : "DEBIT");
        }

        // Booking date
        Element bookgDt = getFirstChildByLocalName(entry, "BookgDt");
        if (bookgDt != null) {
            LocalDate txDate = parseDate(bookgDt.getTextContent());
            builder.txDate(txDate);
        }

        // Value date
        Element valDt = getFirstChildByLocalName(entry, "ValDt");
        if (valDt != null) {
            LocalDate valueDate = parseDate(valDt.getTextContent());
            builder.valueDate(valueDate);
        }

        // Transaction ID (from Prtry/Id)
        Element bkTxCd = getFirstChildByLocalName(entry, "BankTxCd");
        if (bkTxCd != null) {
            Element prtry = getFirstChildByLocalName(bkTxCd, "Prtry");
            if (prtry != null) {
                Element id = getFirstChildByLocalName(prtry, "Id");
                if (id != null) {
                    builder.txId(id.getTextContent().trim());
                }
            }
        }

        // Bank transaction code (PMNT, etc.)
        Element domn = getFirstChildByLocalName(bkTxCd, "Domn");
        if (domn != null) {
            Element cd = getFirstChildByLocalName(domn, "Cd");
            if (cd != null) {
                builder.bankTransactionCode(cd.getTextContent().trim());
            }
        }

        // Counterparty (debtor or creditor name)
        String counterparty = extractCounterparty(entry);
        builder.counterparty(counterparty);

        // Description and reference (remittance info)
        String description = extractDescription(entry);
        builder.description(description != null ? description : "No description");

        String reference = extractReference(entry);
        if (reference != null) {
            builder.reference(reference);
        }

        // Running balance
        Element entryDtls = getFirstChildByLocalName(entry, "EntryDetails");
        if (entryDtls != null) {
            Element txDtls = getFirstChildByLocalName(entryDtls, "TxDtls");
            if (txDtls != null) {
                Element rltdAcctBal = getFirstChildByLocalName(txDtls, "RltdAcctBal");
                if (rltdAcctBal != null) {
                    Element bal = getFirstChildByLocalName(rltdAcctBal, "Bal");
                    if (bal != null) {
                        Element balAmt = getFirstChildByLocalName(bal, "Amt");
                        if (balAmt != null) {
                            try {
                                builder.balance(new BigDecimal(balAmt.getTextContent().trim()));
                            } catch (NumberFormatException e) {
                                log.warn("Invalid balance amount: {}", balAmt.getTextContent());
                            }
                        }
                    }
                }
            }
        }

        return builder.build();
    }

    /**
     * Extract counterparty name (debtor or creditor).
     */
    private String extractCounterparty(Element entry) {
        // Try RltdPties (related parties)
        Element rltdPties = getFirstChildByLocalName(entry, "RltdPties");
        if (rltdPties != null) {
            // Try Dbtr (debtor) first
            Element dbtr = getFirstChildByLocalName(rltdPties, "Dbtr");
            if (dbtr != null) {
                Element nm = getFirstChildByLocalName(dbtr, "Nm");
                if (nm != null) {
                    return nm.getTextContent().trim();
                }
            }

            // Try Cdtr (creditor)
            Element cdtr = getFirstChildByLocalName(rltdPties, "Cdtr");
            if (cdtr != null) {
                Element nm = getFirstChildByLocalName(cdtr, "Nm");
                if (nm != null) {
                    return nm.getTextContent().trim();
                }
            }
        }

        return null;
    }

    /**
     * Extract transaction description (purpose).
     */
    private String extractDescription(Element entry) {
        // Try RmtInf (remittance information)
        Element rmtInf = getFirstChildByLocalName(entry, "RmtInf");
        if (rmtInf != null) {
            // Unstructured info
            Element ustrd = getFirstChildByLocalName(rmtInf, "Ustrd");
            if (ustrd != null) {
                return ustrd.getTextContent().trim();
            }

            // Structured info
            Element strd = getFirstChildByLocalName(rmtInf, "Strd");
            if (strd != null) {
                Element cdtrRefInf = getFirstChildByLocalName(strd, "CdtrRefInf");
                if (cdtrRefInf != null) {
                    Element ref = getFirstChildByLocalName(cdtrRefInf, "Ref");
                    if (ref != null) {
                        return ref.getTextContent().trim();
                    }
                }
            }
        }

        return null;
    }

    /**
     * Extract additional reference text.
     */
    private String extractReference(Element entry) {
        Element bkTxCd = getFirstChildByLocalName(entry, "BankTxCd");
        if (bkTxCd != null) {
            Element prtry = getFirstChildByLocalName(bkTxCd, "Prtry");
            if (prtry != null) {
                Element ref = getFirstChildByLocalName(prtry, "Ref");
                if (ref != null) {
                    return ref.getTextContent().trim();
                }
            }
        }

        return null;
    }

    /**
     * Helper: Get first child element by local name (ignores namespaces).
     */
    private Element getFirstChildByLocalName(Element parent, String localName) {
        if (parent == null) return null;

        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                Element child = (Element) children.item(i);
                if (child.getLocalName().equals(localName)) {
                    return child;
                }
            }
        }

        return null;
    }

    /**
     * Parse ISO date string (YYYY-MM-DD).
     */
    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr.trim(), ISO_DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse date: {}", dateStr);
            return null;
        }
    }

    /**
     * Create a secure DocumentBuilder (prevents XXE attacks).
     */
    private DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        // Disable XXE (XML External Entity) attacks
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        // Enable namespace awareness
        factory.setNamespaceAware(true);

        return factory.newDocumentBuilder();
    }

    /**
     * Result of parsing CAMT.052 — contains statement info and transactions.
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class CamtParseResult {
        private String iban;
        private BigDecimal openingBalance;
        private BigDecimal closingBalance;
        private LocalDate statementDateFrom;
        private LocalDate statementDateTo;
        private List<CamtTransaction> transactions;
    }
}
