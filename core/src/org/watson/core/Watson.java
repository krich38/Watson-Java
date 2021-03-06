package org.watson.core;

import org.watson.command.CommandManager;
import org.watson.core.handler.message.CommandListener;
import org.watson.module.ServerProperties;
import org.watson.module.util.ClassEnumerator;
import org.watson.protocol.IRCClient;
import org.watson.protocol.IRCMessageHandler;
import org.watson.protocol.io.DatabaseAdapter;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kyle Richards
 * @version 1.0
 */
public class Watson {


    private List<IRCClient> CONNECTED;
    private List<ServerProperties> UNCONNECTED;
    public static final boolean LOGGING_RAW = true;


    public static Watson getInstance() {

        return INSTANCE;
    }

    private boolean loadAndSetup() throws FileNotFoundException {
        File[] files = new File("servers/").listFiles();
        if (files != null) {
            UNCONNECTED = new ArrayList<>(files.length);
            Yaml yaml = new Yaml();
            for (File f : files) {
                ServerProperties server = yaml.loadAs(new FileInputStream(f), ServerProperties.class);
                UNCONNECTED.add(server);
            }
        }
        return true;
    }

    public Watson() throws FileNotFoundException {
        if (DatabaseAdapter.establishConnection()) {
            if (loadAndSetup()) {
                if (CommandManager.getCommandManager().load()) {
                    connectAll(LOGGING_RAW);

                }
            }

        }
    }

    private void connectAll(boolean logRaw) {
        CONNECTED = new ArrayList<>();
        for (ServerProperties sc : UNCONNECTED) {

            final IRCClient client = new IRCClient(sc);
            client.setLogging(logRaw);
            client.connect();
            client.setOnConnected(() -> {
                for (Class c : ClassEnumerator.getClassesForPackage(CommandListener.class.getPackage())) {
                    try {
                        IRCMessageHandler message = (IRCMessageHandler) c.newInstance();
                        client.attachMessageHandler(message);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                UNCONNECTED.remove(sc);
                CONNECTED.add(client);
            });
        }
    }

    private static Watson INSTANCE;

    public static void main(String[] args) {
        try {
            INSTANCE = new Watson();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public void save() {
        for (IRCClient irc : CONNECTED) {
            ServerProperties up = irc.getConfig();
            try {
                Yaml yaml = new Yaml();
                yaml.dump(up, new FileWriter("servers/" + up.file));
                System.out.println(up.users);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
