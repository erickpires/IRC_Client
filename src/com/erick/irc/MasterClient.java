package com.erick.irc;

/**
 * Created by erick on 9/11/14.
 */
public class MasterClient extends IrcClient {
    public MasterClient() {
        super("127.0.0.1", "Master0", "master", "I'm the Master");
    }

    @Override
    protected void handleMessage(String msg) {
        if (isMessageFromServer(msg))
            return;

        System.out.println(getSenderNick(msg) + " said: " + getCleanMessage(msg));
    }
}
