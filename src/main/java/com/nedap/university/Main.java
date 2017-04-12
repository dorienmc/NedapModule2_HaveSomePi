package com.nedap.university;

import com.nedap.university.clientAndServer.Client;
import com.nedap.university.clientAndServer.Server;
import java.util.Date;

public class Main {

    private static boolean keepAlive = true;
    private static boolean running = false;

    private Main() {}

    public static void main(String[] args) {
        running = true;
        System.out.println("Hello, Nedap University!");
        String name = (args.length > 0 ? args[0] : "Pi");

        initShutdownHook();

        if(name.equals("Pi")) {
            (new Server()).start();
        } else {
            (new Client()).start();
        }

        while (keepAlive) {
            try {
                // do useful stuff
                //System.out.println("Current time " + (new Date()));
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("Stopped");
        running = false;
    }

    private static void initShutdownHook() {
        final Thread shutdownThread = new Thread() {
            @Override
            public void run() {
                keepAlive = false;
                while (running) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        };
        Runtime.getRuntime().addShutdownHook(shutdownThread);
    }
}
