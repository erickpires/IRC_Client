package com.erick.irc;

import java.io.*;
import java.net.Socket;

/**
 * Created by erick on 9/11/14.
 *
 * To use a IrcClient you need to extend this class and
 * give a implementation to the method handleMessage(String).
 * This method is called every time a new message is received.
 *
 * Some example code is available in Main class, which creates instances of
 * the two children class MasterClient and SlaveClient.
 *
 * The usage is pretty simple, just call the method connect to start a connection with
 * the server passed in the constructor. Then call the method startListening(String)
 * that requires the channel name as parameter.
 * Note that this method locks the execution of the program. If it is not the wanted behaviour
 * you should manually make it run in another thread.
 */
public abstract class IrcClient {

    private static final int defaultPort = 6667;

    private String server;
    private String nick;
    private String loginName;
    private int port;
    private String realName;
    private String channel;

    private BufferedWriter writer;
    private BufferedReader reader;
    private boolean stayLogedIn = true;
    private Socket socket;

    public IrcClient(String server, int port, String nick, String loginName, String realName) {
        this.server = server;
        this.port = port;
        this.nick = nick;
        this.loginName = loginName;
        this.realName = realName;
    }

    public IrcClient(String server, String nick, String loginName, String realName) {
        this(server, defaultPort, nick, loginName, realName);
    }


    protected abstract void handleMessage(String msg);

    private void requestLogin() throws IOException {
        // Log on to the server.
        writer.write("NICK " + nick + "\r\n");
        writer.write("USER " + loginName + " 8 * : " + realName + "\r\n");
        writer.flush();
    }

    private void joinChannel() throws IOException {
        // Join the channel.
        writer.write("JOIN " + channel + "\r\n");
        writer.flush();
    }

    private void handlePing(String ping) throws IOException {
        writer.write("PONG " + ping.substring(5) + "\r\n");
        writer.flush();
    }

    public final boolean connect() throws IOException {
        //Initialize the socket and the I/O
        socket = new Socket(server, port);
        writer = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream()));
        reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));

        requestLogin();

        //Wait for server confirmation, return whether it was possible to log in
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
            if (line.contains("004"))
                return true;

            else if (line.contains("433")) {
                System.err.println("Nickname is already in use.");
                break;
            }
        }
        return false;
    }

    public final void startListening(String channel) throws IOException {
        // Ensure that the channel name begins with '#'
        if(!channel.startsWith("#"))
            channel = "#" + channel;

        this.channel = channel;
        joinChannel();

        while (stayLogedIn && socket.isConnected()) {
            String line;
            line = reader.readLine();
            if (line.startsWith("PING"))
                // We must respond to PINGs to avoid being disconnected.
                handlePing(line);
            else
                // The implementation of the Client must describe how messages are handled
                handleMessage(line);
        }
    }

    public final void say(String msg) throws IOException {
        writer.write("PRIVMSG " + channel + " " + msg + "\r\n");
        writer.flush();
    }

    public final void sayPrivately(String msg, String target) throws IOException {
        writer.write("PRIVMSG " + target + " " + msg + "\r\n");
        writer.flush();
    }

    public final void logOut() {
        stayLogedIn = false;
    }

    //Some useful static methods to handle the messages
    public static boolean isMessageFromServer(String msg) {
        return msg.matches(":[a-zA-Z0-9]*@[a-zA-Z0-9.]* [0-9]{3} [^\n]*");
    }

    public static String getCleanMessage(String rawMessage) {
        try {
            int tmp = rawMessage.indexOf(':');
            int begin = rawMessage.indexOf(':', tmp + 1);

            return rawMessage.substring(begin + 1);
        } catch (Exception e) {
            return "";
        }
    }

    public static String getSenderNick(String rawMessage) {
        try {
            int begin = rawMessage.indexOf(':');
            int end = rawMessage.indexOf('!');

            return rawMessage.substring(begin + 1, end);
        } catch (Exception e) {
            return "";
        }
    }

    public static String getSenderLoginName(String rawMessage) {
        try {
            int begin = rawMessage.indexOf('!');
            int end = rawMessage.indexOf('@');

            return rawMessage.substring(begin + 1, end);
        } catch (Exception e) {
            return "";
        }
    }

    //TODO create a method to get the target of the message
    //TODO use the method described above to tell whether a message is private or not
}
