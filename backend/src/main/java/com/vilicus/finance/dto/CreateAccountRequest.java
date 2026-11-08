package com.vilicus.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new account.
 *
 * Request payload for POST /api/accounts
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {

    @NotBlank(message = "Account name is required")
    @Size(min = 1, max = 100, message = "Account name must be between 1 and 100 characters")
    private String name;

    @NotBlank(message = "IBAN is required")
    @Size(min = 15, max = 34, message = "IBAN must be between 15 and 34 characters")
    @Pattern(regexp = "^[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}$",
             message = "IBAN format is invalid (must be: 2 letters + 2 digits + alphanumeric)")
    private String iban;

    @NotBlank(message = "Account type is required")
    @Pattern(regexp = "^(BANK_ACCOUNT|CREDIT_CARD|CASH|SAVINGS|INVESTMENT)$",
             message = "Account type must be one of: BANK_ACCOUNT, CREDIT_CARD, CASH, SAVINGS, INVESTMENT")
    private String type;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters (ISO 4217)")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be 3 uppercase letters (e.g., EUR, USD, GBP)")
    private String currency;
}
