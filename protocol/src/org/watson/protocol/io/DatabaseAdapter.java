package org.watson.protocol.io;

import org.watson.module.user.UserAccess;

import java.sql.*;

/**
 * @author Kyle Richards
 * @version 1.0
 *          <p>
 *          Our database connection. Just a basic front end to the sqlite database
 */
public class DatabaseAdapter {
    private static Connection connection;
    private static Statement statement;

    public static boolean establishConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:./watson.db");
            statement = connection.createStatement();
            return true;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return false;

        }

    }

    public static UserAccess authenticateUser(String username, String password) {
        UserAccess access = UserAccess.ANYONE;
        try {
            ResultSet result = statement.executeQuery("SELECT * FROM users WHERE username='" + username + "'");
            if (result.next()) {
                if (result.getString("password").equals(password)) {
                    UserAccess temp = UserAccess.getByOrdinal(result.getInt("access"));
                    if (temp != null) {
                        access = temp;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return access;
    }

    public static void executeUpdate(String sql) {
        try {
            statement.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        return connection;
    }
}
