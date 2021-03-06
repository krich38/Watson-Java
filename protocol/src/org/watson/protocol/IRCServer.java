package org.watson.protocol;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.watson.module.ServerProperties;


/**
 * @author Kyle Richards
 * @version 1.0
 *          <p>
 *          Represents a server that Watson is connected to
 */
public final class IRCServer {
    private static final AttributeKey<ServerProperties> USERPROPS_ATTR = AttributeKey.valueOf("UserProperties.attr");
    private static final AttributeKey<IRCClient> CLIENT_ATTR = AttributeKey.valueOf("IRClient.attr");
    public static final int MAX_LENGTH = 500;

    public static void setProperties(Channel channel, ServerProperties up, IRCClient client) {
        channel.attr(USERPROPS_ATTR).set(up);
        channel.attr(CLIENT_ATTR).set(client);
    }

    private final Channel channel;

    public IRCServer(Channel channel) {
        this.channel = channel;
    }

    public final Channel getChannel() {
        return channel;
    }

    public final IRCClient getIrcClient() {
        return channel.attr(CLIENT_ATTR).get();
    }

    public final ServerProperties getUserProperties() {
        return channel.attr(USERPROPS_ATTR).get();
    }

    public final void sendMessage(String target, String msg) {
        getChannel().writeAndFlush("PRIVMSG " + target + " :" + msg);
    }

    public final void write(String text) {
        channel.writeAndFlush(text);
    }
}
