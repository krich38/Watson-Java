package org.watson.protocol;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.watson.module.ServerProperties;
import org.watson.protocol.event.ProtocolEvent;


/**
 * @author Kyle Richards
 * @version 1.0
 *          <p>
 *          Watsons client to the server
 */
public final class IRCClient {
    private final NioEventLoopGroup group;
    private final String IP;
    private final int PORT;
    private final DefaultChannelGroup connections;
    private ServerProperties config;
    private ProtocolEvent onConnected;

    private boolean logging;

    public IRCClient(ServerProperties config) {
        group = new NioEventLoopGroup(4);
        this.config = config;
        this.IP = config.ip;
        this.PORT = config.port;
        connections = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    }

    /**
     * Establish I/O connection here
     */
    public final void connect() {
        final Bootstrap b = new Bootstrap();
        b.group(group).channel(NioSocketChannel.class).handler(new IRCInitializer(true));
        final ChannelFuture cf = b.connect(IP, PORT);
        final Channel channel = cf.channel();
        IRCServer.setProperties(channel, config, this);
        channel.closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture chan) throws Exception {
                System.out.println("DISCONNECT: " + chan.channel().remoteAddress());
                ServerProperties properties = new IRCServer(chan.channel()).getUserProperties();
                if (properties.isReconnect()) {
                    connect();
                }
            }
        });
        connections.add(channel);
    }

    public final void setOnConnected(ProtocolEvent onConnected) {
        this.onConnected = onConnected;
    }

    public final ProtocolEvent getOnConnected() {
        return onConnected;
    }

    public final void attachMessageHandler(IRCMessageHandler messageHandler) {
        for (Channel channel : connections) {
            ((IRCHandler) channel.pipeline().get("handler")).attachMessageHandler(messageHandler);
        }
    }

    public final void sendMessage(String target, String msg) {

    }

    public final ServerProperties getConfig() {
        return config;
    }

    public final void setLogging(boolean logging) {
        this.logging = logging;
    }

    public final boolean isLogging() {
        return logging;
    }
}
