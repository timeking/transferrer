package com.github.timeking.transferrer.controller;

import com.github.timeking.transferrer.dto.AccountDto;
import com.github.timeking.transferrer.model.Account;
import io.javalin.Context;
import io.javalin.apibuilder.CrudHandler;
import org.eclipse.jetty.http.HttpStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class AccountController implements CrudHandler {
    private final AccountManager accountManager;

    public AccountController(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    @Override
    public void create(@NotNull Context ctx) {
        AccountDto accountDto = ctx.bodyAsClass(AccountDto.class);
        UUID newUuid = accountManager.register(accountDto.getBalance());
        Optional<Account> account = accountManager.get(newUuid);
        ctx.json(AccountDto.from(newUuid, account.get()));
        ctx.status(HttpStatus.CREATED_201);
    }

    @Override
    public void delete(@NotNull Context ctx, @NotNull String s) {
        UUID accountId;
        try {
            accountId = UUID.fromString(s);
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST_400);
            return;
        }
        accountManager.delete(accountId);
        ctx.status(HttpStatus.NO_CONTENT_204);
    }

    @Override
    public void getAll(@NotNull Context ctx) {
        List<AccountDto> accountDtos = accountManager.list()
                .entrySet().stream()
                .map(e -> AccountDto.from(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
        ctx.json(accountDtos);
        ctx.status(HttpStatus.OK_200);
    }

    @Override
    public void getOne(@NotNull Context ctx, @NotNull String s) {
        UUID accountId;
        try {
            accountId = UUID.fromString(s);
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST_400);
            return;
        }
        Optional<Account> optAccount = accountManager.get(accountId);
        ctx.status(HttpStatus.NOT_FOUND_404);
        optAccount
                .map(account -> AccountDto.from(accountId, account))
                .ifPresent(account -> {
                    ctx.json(account).status(HttpStatus.OK_200);
                });
    }

    @Override
    public void update(@NotNull Context ctx, @NotNull String s) {
        ctx.status(HttpStatus.NOT_IMPLEMENTED_501);
    }
}
