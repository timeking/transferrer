package com.github.timeking.transferrer.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class TransferDto {
    private UUID transferId;
    private Instant date;
    private UUID accountFrom;
    private UUID accountTo;
    private int amount;
    private TransferState state;

}
