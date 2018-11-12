package com.github.timeking.transferrer.model;

import com.github.timeking.transferrer.model.Account;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertTrue;

@Slf4j
public class AccountTest {

    @Test
    public void givenAccount_whenDecrement_thenShouldReturnProperValue() {
        // given
        Account a = new Account(10);

        // when
        a.adjustBy(-5);

        // then
        assertThat(a.getBalance()).isEqualTo(5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenAccount_whenDecrementTooMuch_thenShouldThrow() {
        // given
        Account a = new Account(10);

        // when
        a.adjustBy(-11);
    }

    @Test
    public void givenTwoThreads_whenBothApplyOperation_thenShouldThrow() throws InterruptedException {
        // given
        ExecutorService ex = Executors.newFixedThreadPool(2);
        Account a = new Account(10);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        AtomicBoolean exceptionThrown = new AtomicBoolean(false);

        // when
        ex.submit(() -> {
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }

            try {
                a.adjustBy(-6);
            } catch (IllegalArgumentException e) {
                exceptionThrown.set(true);
            }
        });
        ex.submit(() -> {
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
            try {
                a.adjustBy(-5);
            } catch (IllegalArgumentException e) {
                exceptionThrown.set(true);
            }
        });

        countDownLatch.countDown();
        ex.awaitTermination(1, TimeUnit.SECONDS);
        ex.shutdown();

        // then
        assertTrue(exceptionThrown.get());
    }

    @Test
    public void givenTwoAccounts_whenFailedWhileTransferring_thenShouldRollbackTransaction() {
        // given
        Account a = new Account(10);
        Account b = new Account(10);

        // when
        a.transferTo(b, 5);

        // then
        assertThat(a.getBalance()).isEqualTo(5);
        assertThat(b.getBalance()).isEqualTo(15);

        // and
        try {
            a.transferTo(b, 20);
        } catch (IllegalArgumentException e) {
            log.info("Failed to transfer money: " + e.getMessage());
        }

        // then
        assertThat(a.getBalance()).isEqualTo(5);
        assertThat(b.getBalance()).isEqualTo(15);
    }

    @Test
    public void givenTwoThreads_whenBothTryToTransfer_thenShouldNotDeadlock() throws InterruptedException {
        // given
        ExecutorService ex = Executors.newFixedThreadPool(2);
        Account a = new Account(10);
        Account b = new Account(10);
        CountDownLatch countDownLatch = new CountDownLatch(1);

        // when
        ex.submit(() -> {
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            a.transferTo(b, 10);
        });
        ex.submit(() -> {
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            b.transferTo(a, 1);

        });

        countDownLatch.countDown();
        ex.awaitTermination(1, TimeUnit.SECONDS);
        ex.shutdown();

        // then
        assertThat(a.getBalance()).isEqualTo(1);
        assertThat(b.getBalance()).isEqualTo(19);
    }

    @Test
    public void givenThreeThreads_whenAllTryToTransfer_thenShouldNotDeadlock() throws InterruptedException {
        // given
        ExecutorService ex = Executors.newFixedThreadPool(3);
        Account a = new Account(10);
        Account b = new Account(10);
        Account c = new Account(10);
        CountDownLatch countDownLatch = new CountDownLatch(1);

        // when
        ex.submit(() -> {
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            a.transferTo(b, 10);
        });
        ex.submit(() -> {
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            b.transferTo(c, 2);

        });
        ex.submit(() -> {
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            c.transferTo(a, 1);

        });

        countDownLatch.countDown();
        ex.awaitTermination(1, TimeUnit.SECONDS);
        ex.shutdown();

        // then
        assertThat(a.getBalance()).isEqualTo(1);
        assertThat(b.getBalance()).isEqualTo(18);
        assertThat(c.getBalance()).isEqualTo(11);
    }

}