package com.nedap.university;

import com.nedap.university.clientAndServer.Client;
import com.nedap.university.clientAndServer.Server;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Date;

public class Main {

    private static boolean keepAlive = true;
    private static boolean running = false;
    private static final int FIRST_CLIENT_UDP_PORT = 9001;
    private static final int LAST_CLIENT_UDP_PORT = 9999;

    private Main() {}

    public static void main(String[] args) {
        Client client = null;
        Server server = null;
        running = true;
        System.out.println("Hello, Nedap University!");
        String name = (args.length > 0 ? args[0] : "Pi");

        initShutdownHook();

        if(name.equals("Pi")) {
            (new Server()).start();
        } else {
            client = new Client();
            client.start();
        }

        while (keepAlive) {
            //Check if client/server is still running
            if(server != null) {
                if(!server.isRunning()) {
                    running = false;
                }
            }

            if(client != null) {
                if(!client.isRunning()) {
                    running = false;
                }
            }

            Utils.sleep(1000);
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
                    Utils.sleep(10);
                }
            }
        };
        Runtime.getRuntime().addShutdownHook(shutdownThread);
    }

    private static int getFreePort() {
        for(int i = FIRST_CLIENT_UDP_PORT; i < LAST_CLIENT_UDP_PORT; i += 2) {
            if(isLocalPortFree(i)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isLocalPortFree(int port) {
        try {
            new ServerSocket(port).close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
