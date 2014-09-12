package com.erick.irc;

import java.io.IOException;

/**
 * Created by erick on 9/11/14.
 */
public class SlaveClient extends IrcClient {
    public SlaveClient(int slaveNumber) {
        super("127.0.0.1", "BotSlave" + slaveNumber, "slave" + slaveNumber, "Mr. Slave" + slaveNumber);
    }

    @Override
    protected void handleMessage(String msg) {
        if(isMessageFromServer(msg))
            return;

        if(msg.toLowerCase().contains("attack"))
            try {
                say("I am attacking: " + getCleanMessage(msg).replaceAll("attack", "") + "!");
                say(getSenderLoginName(msg) + " told me to do this.");
                say("His nick is: " + getSenderNick(msg));
            } catch (IOException e) {
                e.printStackTrace();
            }

        else if(msg.toLowerCase().contains("get out"))
            logOut();
        else
            System.out.println(msg);
    }
}
