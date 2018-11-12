package com.github.timeking.transferrer.controller;

import com.github.timeking.transferrer.dto.TransferDto;
import com.github.timeking.transferrer.dto.TransferState;
import io.javalin.Context;
import io.javalin.apibuilder.CrudHandler;
import org.eclipse.jetty.http.HttpStatus;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TransferController implements CrudHandler {
    private final AccountManager accountManager;

    private final ConcurrentLinkedQueue<TransferDto> transferQueue = new ConcurrentLinkedQueue<>();

    public TransferController(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    @Override
    public void create(@NotNull Context ctx) {
        TransferDto transferDto = ctx.bodyAsClass(TransferDto.class);
        if (transferDto.getAccountFrom() == null) {
            ctx.status(HttpStatus.BAD_REQUEST_400);
            return;
        }
        if (transferDto.getAccountTo() == null) {
            ctx.status(HttpStatus.BAD_REQUEST_400);
            return;
        }
        transferDto.setDate(Instant.now());
        UUID transferId = UUID.randomUUID();
        transferDto.setTransferId(transferId);
        transferDto.setState(TransferState.SUBMITTED);
        transferQueue.offer(transferDto);
        startTransfer(transferDto);
        ctx.json(transferDto);
        ctx.status(HttpStatus.CREATED_201);
    }

    private CompletableFuture<Void> startTransfer(TransferDto transferDto) {
        return CompletableFuture.runAsync(() ->
            accountManager.transfer(
                    transferDto.getAccountFrom(),
                    transferDto.getAccountTo(),
                    transferDto.getAmount()
            )
        ).whenComplete((v, ex) -> {
            if (ex != null) {
                transferDto.setState(TransferState.FAILED);
            } else {
                transferDto.setState(TransferState.TRANSFERRED);
            }
        });
    }

    @Override
    public void getAll(@NotNull Context ctx) {
        ctx.json(transferQueue);
    }

    @Override
    public void getOne(@NotNull Context ctx, @NotNull String s) {
        UUID transferId;
        try {
            transferId = UUID.fromString(s);
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST_400);
            return;
        }
        Optional<TransferDto> optTransfer = transferQueue.stream()
                .filter(transferDto -> transferDto.getTransferId().equals(transferId))
                .findAny();
        ctx.status(HttpStatus.NOT_FOUND_404);
        optTransfer.ifPresent(transfer -> ctx.json(transfer).status(HttpStatus.OK_200));
    }

    @Override
    public void update(@NotNull Context ctx, @NotNull String s) {
        ctx.status(HttpStatus.NOT_IMPLEMENTED_501);
    }

    @Override
    public void delete(@NotNull Context ctx, @NotNull String s) {
        ctx.status(HttpStatus.NOT_IMPLEMENTED_501);
    }
}
