package com.erick.irc;

import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void testaSlave() {
        IrcClient slave = new SlaveClient(0);

        try {
            slave.connect();

            slave.startListening("#meu_canal");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void testaMaster() {
        final IrcClient master = new MasterClient();
        try {
            // This thread is necessary since the scanner will lock the program
            // while there is not something to be read in System.in
            Thread userThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Scanner scanner = new Scanner(System.in);

                    while (scanner.hasNext()) {
                        String line = scanner.nextLine();
                        try {
                            if(line.toLowerCase().startsWith("/msg ")){
                                String target = line.substring(5, line.indexOf(' ', 6));
                                line = line.substring(line.indexOf(' ', 6) + 1);
                                master.sayPrivately(line, target);
                            }
                            else
                                master.say(line);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            userThread.start();

            master.connect();
            master.startListening("#meu_canal");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        //testaMaster();
        testaSlave();
    }
}


