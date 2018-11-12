package com.github.timeking.transferrer.dto;

import com.github.timeking.transferrer.model.Account;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class AccountDto {
    private UUID accountId;
    private int balance;
    private Instant lastModified;

    public static AccountDto from(UUID accountId, Account account) {
        return AccountDto.builder()
                .lastModified(Instant.ofEpochMilli(account.getLastModified()))
                .balance(account.getBalance())
                .accountId(accountId)
                .build();
    }

}
