package com.github.timeking.transferrer;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
@Getter
public class Application {

    @Parameter(names = {"--port", "-p"}, description = "Server port")
    private int port = 8080;

    @Parameter(names = {"--context-path", "-c"}, description = "Context path")
    private String contextPath = "/api";

    public static void main(String[] args) {
        Application application = new Application();
        JCommander jCommander = JCommander.newBuilder()
                .args(args)
                .addObject(application)
                .build();

        if (args.length != 0 && Arrays.asList(args).contains("--help")) {
            jCommander.usage();
            System.exit(1);
            return;
        }
        try {
            jCommander.parse(args);
        } catch (ParameterException ex) {
            jCommander.usage();
            System.exit(1);
            return;
        }

        Server server = new Server(application);
        try {
            server.awaitForShutdown();
        } catch (InterruptedException e) {
            log.info("Server shutdown wait is interrupted !");
        }
        log.info("Server stopped!");
    }

}
