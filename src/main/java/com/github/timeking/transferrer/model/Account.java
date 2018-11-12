package com.github.timeking.transferrer.model;

import org.multiverse.api.StmUtils;
import org.multiverse.api.callables.TxnCallable;
import org.multiverse.api.references.TxnInteger;
import org.multiverse.api.references.TxnLong;

public class Account {
	//private final UUID id;
    private final TxnLong lastModified;
    private final TxnInteger balance;

    public Account(int initialBalance) {
        this.lastModified = StmUtils.newTxnLong(System.currentTimeMillis());
        this.balance = StmUtils.newTxnInteger(initialBalance);
    }

    public Integer getBalance() {
        return balance.atomicGet();
    }

    public long getLastModified() {
        return lastModified.atomicGet();
    }

    void adjustBy(int amount) {
        adjustBy(amount, System.currentTimeMillis());
    }

    private void adjustBy(int amount, long date) {
        StmUtils.atomic(() -> {
            balance.increment(amount);
            lastModified.set(date);

            if (balance.get() < 0) {
                throw new IllegalArgumentException("Not enough money");
            }
        });
    }

    public void transferTo(Account other, int amount) {
        StmUtils.atomic(() -> {
            long date = System.currentTimeMillis();
            adjustBy(-amount, date);
            other.adjustBy(amount, date);
        });
    }

    @Override
    public String toString() {
        return StmUtils.atomic((TxnCallable<String>) txn ->
                String.format("Balance: %d lastModifiedDate: %d",
                        balance.get(txn), lastModified.get(txn)
                )
        );
    }
}