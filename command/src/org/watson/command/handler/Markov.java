package org.watson.command.handler;

import org.watson.command.CommandActor;
import org.watson.command.CommandManager;
import org.watson.command.io.MarkovDatabaseAdapter;
import org.watson.module.user.UserAccess;
import org.watson.protocol.IRCServer;
import org.watson.protocol.io.IncomingMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Kyle Richards
 * @version 1.0
 *          <p>
 *          Watson's Markov machine learning
 *          <p>
 *          He will randomly say something, and respond to being asked to talk about something
 */
public final class Markov implements CommandActor {


    private static final Pattern COMMAND_PATTERN = Pattern.compile("(\\S+):?\\s*(\\S+)?(?: (\\S+))?", Pattern.CASE_INSENSITIVE);
    public static int REPLY_RATE = 1;
    public static int REPLY_NICK = 100;

    @Override
    public final void handleCommand(IRCServer server, String command, IncomingMessage message) {
        if (!message.hasMessage()) {
            message.sendChatf(getHelp());
            return;
        }
        Matcher m = COMMAND_PATTERN.matcher(message.getMessage());
        if (m.find()) {
            String cmd = m.group(1);
            if (cmd.equalsIgnoreCase("about")) {
                if (m.group(2) == null || m.group(2).isEmpty()) {
                    message.sendChat("Need context");
                } else if (CommandManager.RANDOM.nextFloat() * 100 <= REPLY_NICK) {

                    String markov = MarkovDatabaseAdapter.markovFind(m.group(2), m.group(3));
                    if (markov == null) {
                        message.sendChat("I can't :(");
                    } else {
                        message.sendChat(markov);
                    }
                }
            } else if (server.getUserProperties().users.get(message.getHostName()) == UserAccess.FULL_USER) {
                if (cmd.equalsIgnoreCase("replyrate")) {
                    if (m.group(2) == null) {
                        message.sendChatf("Reply rate is: %d%%", REPLY_RATE);
                    } else {
                        REPLY_RATE = stringToInt(m.group(2), 0, 100);
                        message.sendChatf("Reply rate set to: %d%%", REPLY_RATE);
                    }
                } else if (cmd.equalsIgnoreCase("replynick")) {
                    if (m.group(2) == null) {
                        message.sendChatf("Reply nick is: %d%%", REPLY_NICK);
                    } else {
                        REPLY_NICK = stringToInt(m.group(2), 0, 100);
                        message.sendChatf("Reply nick set to: %d%%", REPLY_NICK);
                    }
                } else {
                    message.sendChatf("Unknown chat command: %s", cmd);
                }
            } else {
                message.sendChatf("Unknown chat command: %s", cmd);
            }
        }
    }

    @Override
    public final String getHelp() {
        return "Usage: chat [about|replynick|replyrate] <context>";
    }

    @Override
    public final String getCommands() {
        return "chat";
    }


    private int stringToInt(String s, int min, int max) {
        try {
            int i = Integer.parseInt(s);
            if (i < min) {
                return min;
            } else if (i > max) {
                return max;
            } else {
                return i;
            }
        } catch (Exception ignored) {

        }
        return min;
    }

}
