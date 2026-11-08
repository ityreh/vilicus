package com.vilicus.finance.service;

import com.vilicus.finance.exception.CamtParseException;
import com.vilicus.finance.model.camt.CamtTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CamtParser.
 *
 * Tests:
 * - Valid CAMT.052 parsing (happy path)
 * - Multiple transaction entries
 * - IBAN extraction
 * - Balance extraction
 * - Date parsing
 * - Error handling (malformed XML, missing elements)
 */
public class CamtParserTest {

    private CamtParser parser;

    @BeforeEach
    void setUp() {
        parser = new CamtParser();
    }

    /**
     * Sample valid CAMT.052 XML statement.
     */
    private String getSampleCamtXml() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:camt.052.001.02">
                  <BkToCstmrStmt>
                    <GrpHdr>
                      <MsgId>MSG001</MsgId>
                      <CreDtTm>2026-08-12T10:30:00</CreDtTm>
                    </GrpHdr>
                    <Stmt>
                      <Id>STMT001</Id>
                      <AcctId>
                        <Othr>
                          <Id>DE89370400440532013000</Id>
                        </Othr>
                      </AcctId>
                      <FrToDt>
                        <FrDt>2026-08-01</FrDt>
                        <ToDt>2026-08-31</ToDt>
                      </FrToDt>
                      <Bal>
                        <Amt Ccy="EUR">1000.00</Amt>
                      </Bal>
                      <Bal>
                        <Amt Ccy="EUR">1500.00</Amt>
                      </Bal>
                      <Ntry>
                        <Amt>100.50</Amt>
                        <CdtDbtInd>DBTR</CdtDbtInd>
                        <BookgDt>2026-08-10</BookgDt>
                        <ValDt>2026-08-10</ValDt>
                        <BankTxCd>
                          <Domn>
                            <Cd>PMNT</Cd>
                          </Domn>
                          <Prtry>
                            <Id>TXN-001</Id>
                          </Prtry>
                        </BankTxCd>
                        <RltdPties>
                          <Dbtr>
                            <Nm>John Doe</Nm>
                          </Dbtr>
                        </RltdPties>
                        <RmtInf>
                          <Ustrd>Invoice #12345</Ustrd>
                        </RmtInf>
                      </Ntry>
                      <Ntry>
                        <Amt>250.75</Amt>
                        <CdtDbtInd>CRDT</CdtDbtInd>
                        <BookgDt>2026-08-12</BookgDt>
                        <ValDt>2026-08-12</ValDt>
                        <BankTxCd>
                          <Domn>
                            <Cd>PMNT</Cd>
                          </Domn>
                          <Prtry>
                            <Id>TXN-002</Id>
                          </Prtry>
                        </BankTxCd>
                        <RltdPties>
                          <Cdtr>
                            <Nm>Acme Corp</Nm>
                          </Cdtr>
                        </RltdPties>
                        <RmtInf>
                          <Ustrd>Salary Payment August</Ustrd>
                        </RmtInf>
                      </Ntry>
                    </Stmt>
                  </BkToCstmrStmt>
                </Document>
                """;
    }

    @Test
    void testParseValidCamt_Success() {
        InputStream input = new ByteArrayInputStream(getSampleCamtXml().getBytes());
        CamtParser.CamtParseResult result = parser.parseInputStream(input);

        assertNotNull(result);
        assertEquals("DE89370400440532013000", result.getIban());
        assertEquals(0, BigDecimal.valueOf(1000.00).compareTo(result.getOpeningBalance()));
        assertEquals(0, BigDecimal.valueOf(1500.00).compareTo(result.getClosingBalance()));
        assertEquals(LocalDate.of(2026, 8, 1), result.getStatementDateFrom());
        assertEquals(LocalDate.of(2026, 8, 31), result.getStatementDateTo());
        assertEquals(2, result.getTransactions().size());
    }

    @Test
    void testParseValidCamt_FirstTransaction() {
        InputStream input = new ByteArrayInputStream(getSampleCamtXml().getBytes());
        CamtParser.CamtParseResult result = parser.parseInputStream(input);

        CamtTransaction tx = result.getTransactions().get(0);
        assertEquals("TXN-001", tx.getTxId());
        assertEquals(LocalDate.of(2026, 8, 10), tx.getTxDate());
        assertEquals(0, BigDecimal.valueOf(100.50).compareTo(tx.getAmount()));
        assertEquals("DEBIT", tx.getDirection());
        assertEquals("John Doe", tx.getCounterparty());
        assertEquals("Invoice #12345", tx.getDescription());
    }

    @Test
    void testParseValidCamt_SecondTransaction() {
        InputStream input = new ByteArrayInputStream(getSampleCamtXml().getBytes());
        CamtParser.CamtParseResult result = parser.parseInputStream(input);

        CamtTransaction tx = result.getTransactions().get(1);
        assertEquals("TXN-002", tx.getTxId());
        assertEquals(LocalDate.of(2026, 8, 12), tx.getTxDate());
        assertEquals(BigDecimal.valueOf(250.75), tx.getAmount());
        assertEquals("CREDIT", tx.getDirection());
        assertEquals("Acme Corp", tx.getCounterparty());
        assertEquals("Salary Payment August", tx.getDescription());
    }

    @Test
    void testParseValidCamt_DebitTransactionDirection() {
        InputStream input = new ByteArrayInputStream(getSampleCamtXml().getBytes());
        CamtParser.CamtParseResult result = parser.parseInputStream(input);

        CamtTransaction tx = result.getTransactions().get(0);
        assertEquals("PMNT", tx.getBankTransactionCode()); // From Domn/Cd
        assertEquals("DEBIT", tx.getDirection()); // Converted from DBTR
    }

    @Test
    void testParseValidCamt_CreditTransactionDirection() {
        InputStream input = new ByteArrayInputStream(getSampleCamtXml().getBytes());
        CamtParser.CamtParseResult result = parser.parseInputStream(input);

        CamtTransaction tx = result.getTransactions().get(1);
        assertEquals("CREDIT", tx.getDirection());
    }

    @Test
    void testParseInvalidXml_NotDocument() {
        String invalidXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <NotDocument>
                </NotDocument>
                """;

        InputStream input = new ByteArrayInputStream(invalidXml.getBytes());

        CamtParseException exception = assertThrows(CamtParseException.class, () -> {
            parser.parseInputStream(input);
        });

        assertTrue(exception.getMessage().contains("root element must be 'Document'"));
    }

    @Test
    void testParseInvalidXml_MissingBkToCstmrStmt() {
        String invalidXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:camt.052.001.02">
                </Document>
                """;

        InputStream input = new ByteArrayInputStream(invalidXml.getBytes());

        CamtParseException exception = assertThrows(CamtParseException.class, () -> {
            parser.parseInputStream(input);
        });

        assertTrue(exception.getMessage().contains("missing BkToCstmrStmt element"));
    }

    @Test
    void testParseInvalidXml_MissingStmt() {
        String invalidXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:camt.052.001.02">
                  <BkToCstmrStmt>
                  </BkToCstmrStmt>
                </Document>
                """;

        InputStream input = new ByteArrayInputStream(invalidXml.getBytes());

        CamtParseException exception = assertThrows(CamtParseException.class, () -> {
            parser.parseInputStream(input);
        });

        assertTrue(exception.getMessage().contains("missing Stmt element"));
    }

    @Test
    void testParseEmptyDocument() {
        String emptyXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:camt.052.001.02">
                  <BkToCstmrStmt>
                    <Stmt>
                      <Id>STMT001</Id>
                      <Bal><Amt>0</Amt></Bal>
                    </Stmt>
                  </BkToCstmrStmt>
                </Document>
                """;

        InputStream input = new ByteArrayInputStream(emptyXml.getBytes());

        CamtParseException exception = assertThrows(CamtParseException.class, () -> {
            parser.parseInputStream(input);
        });

        assertTrue(exception.getMessage().contains("No transactions found"));
    }

    @Test
    void testParseMultipleTransactions() {
        String multiTxXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:camt.052.001.02">
                  <BkToCstmrStmt>
                    <Stmt>
                      <AcctId><Othr><Id>DE89370400440532013000</Id></Othr></AcctId>
                      <Bal><Amt>0</Amt></Bal>
                      <Ntry><Amt>10</Amt><CdtDbtInd>DBTR</CdtDbtInd><BookgDt>2026-08-01</BookgDt><RmtInf><Ustrd>TX1</Ustrd></RmtInf></Ntry>
                      <Ntry><Amt>20</Amt><CdtDbtInd>CRDT</CdtDbtInd><BookgDt>2026-08-02</BookgDt><RmtInf><Ustrd>TX2</Ustrd></RmtInf></Ntry>
                      <Ntry><Amt>30</Amt><CdtDbtInd>DBTR</CdtDbtInd><BookgDt>2026-08-03</BookgDt><RmtInf><Ustrd>TX3</Ustrd></RmtInf></Ntry>
                    </Stmt>
                  </BkToCstmrStmt>
                </Document>
                """;

        InputStream input = new ByteArrayInputStream(multiTxXml.getBytes());
        CamtParser.CamtParseResult result = parser.parseInputStream(input);

        assertEquals(3, result.getTransactions().size());
        assertEquals("TX1", result.getTransactions().get(0).getDescription());
        assertEquals("TX2", result.getTransactions().get(1).getDescription());
        assertEquals("TX3", result.getTransactions().get(2).getDescription());
    }

    @Test
    void testParseTransactionWithoutTxId() {
        String noTxIdXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:camt.052.001.02">
                  <BkToCstmrStmt>
                    <Stmt>
                      <AcctId><Othr><Id>DE89370400440532013000</Id></Othr></AcctId>
                      <Bal><Amt>0</Amt></Bal>
                      <Ntry>
                        <Amt>100</Amt>
                        <CdtDbtInd>DBTR</CdtDbtInd>
                        <BookgDt>2026-08-01</BookgDt>
                        <RmtInf><Ustrd>Payment</Ustrd></RmtInf>
                      </Ntry>
                    </Stmt>
                  </BkToCstmrStmt>
                </Document>
                """;

        InputStream input = new ByteArrayInputStream(noTxIdXml.getBytes());
        CamtParser.CamtParseResult result = parser.parseInputStream(input);

        // Should still parse, txId is optional
        assertEquals(1, result.getTransactions().size());
        CamtTransaction tx = result.getTransactions().get(0);
        assertNull(tx.getTxId());
        assertEquals("Payment", tx.getDescription());
    }

    @Test
    void testParseTransactionWithGermanSpecialCharacters() {
        String germanXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:camt.052.001.02">
                  <BkToCstmrStmt>
                    <Stmt>
                      <AcctId><Othr><Id>DE89370400440532013000</Id></Othr></AcctId>
                      <Bal><Amt>0</Amt></Bal>
                      <Ntry>
                        <Amt>100</Amt>
                        <CdtDbtInd>DBTR</CdtDbtInd>
                        <BookgDt>2026-08-01</BookgDt>
                        <RltdPties><Dbtr><Nm>Müller &amp; Co. GmbH</Nm></Dbtr></RltdPties>
                        <RmtInf><Ustrd>Miete Wohnung — Hausmeister 2026</Ustrd></RmtInf>
                      </Ntry>
                    </Stmt>
                  </BkToCstmrStmt>
                </Document>
                """;

        InputStream input = new ByteArrayInputStream(germanXml.getBytes());
        CamtParser.CamtParseResult result = parser.parseInputStream(input);

        assertEquals(1, result.getTransactions().size());
        CamtTransaction tx = result.getTransactions().get(0);
        assertEquals("Müller & Co. GmbH", tx.getCounterparty());
        assertEquals("Miete Wohnung — Hausmeister 2026", tx.getDescription());
    }

    @Test
    void testParseDecimalAmounts() {
        String decimalsXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:camt.052.001.02">
                  <BkToCstmrStmt>
                    <Stmt>
                      <AcctId><Othr><Id>DE89370400440532013000</Id></Othr></AcctId>
                      <Bal><Amt>0.00</Amt></Bal>
                      <Ntry>
                        <Amt>1234567.89</Amt>
                        <CdtDbtInd>DBTR</CdtDbtInd>
                        <BookgDt>2026-08-01</BookgDt>
                        <RmtInf><Ustrd>Large amount</Ustrd></RmtInf>
                      </Ntry>
                      <Ntry>
                        <Amt>0.01</Amt>
                        <CdtDbtInd>CRDT</CdtDbtInd>
                        <BookgDt>2026-08-02</BookgDt>
                        <RmtInf><Ustrd>Cent amount</Ustrd></RmtInf>
                      </Ntry>
                    </Stmt>
                  </BkToCstmrStmt>
                </Document>
                """;

        InputStream input = new ByteArrayInputStream(decimalsXml.getBytes());
        CamtParser.CamtParseResult result = parser.parseInputStream(input);

        assertEquals(2, result.getTransactions().size());
        assertEquals(BigDecimal.valueOf(1234567.89), result.getTransactions().get(0).getAmount());
        assertEquals(BigDecimal.valueOf(0.01), result.getTransactions().get(1).getAmount());
    }

    @Test
    void testParseDateFormats() {
        String datesXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:camt.052.001.02">
                  <BkToCstmrStmt>
                    <Stmt>
                      <AcctId><Othr><Id>DE89370400440532013000</Id></Othr></AcctId>
                      <FrToDt>
                        <FrDt>2020-01-15</FrDt>
                        <ToDt>2030-12-31</ToDt>
                      </FrToDt>
                      <Bal><Amt>0</Amt></Bal>
                      <Ntry>
                        <Amt>100</Amt>
                        <CdtDbtInd>DBTR</CdtDbtInd>
                        <BookgDt>2025-06-30</BookgDt>
                        <RmtInf><Ustrd>Test</Ustrd></RmtInf>
                      </Ntry>
                    </Stmt>
                  </BkToCstmrStmt>
                </Document>
                """;

        InputStream input = new ByteArrayInputStream(datesXml.getBytes());
        CamtParser.CamtParseResult result = parser.parseInputStream(input);

        assertEquals(LocalDate.of(2020, 1, 15), result.getStatementDateFrom());
        assertEquals(LocalDate.of(2030, 12, 31), result.getStatementDateTo());
        assertEquals(LocalDate.of(2025, 6, 30), result.getTransactions().get(0).getTxDate());
    }

    @Test
    void testParseMalformedXml() {
        String malformedXml = "<Document><BkToCstmrStmt><Stmt>broken";

        InputStream input = new ByteArrayInputStream(malformedXml.getBytes());

        assertThrows(CamtParseException.class, () -> {
            parser.parseInputStream(input);
        });
    }
}
