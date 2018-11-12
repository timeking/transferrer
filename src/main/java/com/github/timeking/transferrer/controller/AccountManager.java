package com.github.timeking.transferrer.controller;

import com.github.timeking.transferrer.model.Account;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AccountManager {
    private final ConcurrentHashMap<UUID, Account> accountMap = new ConcurrentHashMap<>();

    /**
     * @return new account with initial balance
     */
    public UUID register(int initialBalance) {
        UUID accountId = UUID.randomUUID();
        Account account = new Account(initialBalance);
        accountMap.put(accountId, account);
        return accountId;
    }

    /**
     * Returns optional account by its uuid
     * @return account or empty
     */
    public Optional<Account> get(UUID accountId) {
        return Optional.ofNullable(accountMap.get(accountId));
    }

    /**
     * @return all existing accounts
     */
    public Map<UUID, Account> list() {
        return new HashMap<>(accountMap);
    }

    /**
     * Removes account by uuid
     * @return true if removed
     */
    public boolean delete(UUID accountId) {
        return accountMap.remove(accountId) != null;
    }

    public void transfer(UUID accountIdFrom, UUID accountIdTo, int amount) {
        Account accountFrom = get(accountIdFrom).orElseThrow(() -> new IllegalArgumentException("No account " + accountIdFrom));
        Account accountTo = get(accountIdTo).orElseThrow(() -> new IllegalArgumentException("No account " + accountIdTo));
        accountFrom.transferTo(accountTo, amount);
    }
}
