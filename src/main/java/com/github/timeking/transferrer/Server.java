package com.github.timeking.transferrer;

import com.github.timeking.transferrer.controller.AccountController;
import com.github.timeking.transferrer.controller.AccountManager;
import com.github.timeking.transferrer.controller.TransferController;
import io.javalin.Javalin;
import io.javalin.JavalinEvent;
import io.javalin.apibuilder.ApiBuilder;

import java.util.concurrent.CountDownLatch;

public class Server {
    private final AccountManager accountManager = new AccountManager();
    private final AccountController accountController = new AccountController(accountManager);
    private final TransferController transferController = new TransferController(accountManager);

    private final CountDownLatch shutdownLatch = new CountDownLatch(1);

    public Server(Application application) {
        Javalin app = Javalin.create()
                .port(application.getPort())
                .contextPath(application.getContextPath())
                .defaultContentType("application/json")
                .event(JavalinEvent.SERVER_STOPPED, shutdownLatch::countDown)
                .start();

        app.routes(() -> {
            ApiBuilder.crud("/accounts/:account-id", accountController);
            ApiBuilder.crud("/transfers/:transfer-id", transferController);
        });
    }

    public void awaitForShutdown() throws InterruptedException {
        shutdownLatch.await();
    }
}
