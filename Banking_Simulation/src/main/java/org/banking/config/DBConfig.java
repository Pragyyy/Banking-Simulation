package org.banking.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

@WebListener
public class DBConfig implements ServletContextListener {
    private static String DB_URL;
    private static String DB_NAME ;
    private static String DB_USER ;
    private static String DB_PASSWORD ;
    private static Connection connection;

    static {
        try (InputStream input = DBConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            Properties prop = new Properties();
            prop.load(input);

            DB_URL = prop.getProperty("db.url");
            DB_USER = prop.getProperty("db.username");
            DB_PASSWORD = prop.getProperty("db.password");
            DB_NAME= prop.getProperty("db.name");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("WebApp STARTING UP: Initializing database connection...");
        try {
            initializeDatabase();
            System.out.println("WebApp STARTED SUCCESSFULLY");
        } catch (Exception e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed successfully.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initializeDatabase() throws SQLException, ClassNotFoundException {
        // Load MySQL driver
        Class.forName("com.mysql.cj.jdbc.Driver");

        // First connect without database to create it if needed
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

            // Create database if not exists
            String createDb = "CREATE DATABASE IF NOT EXISTS " + DB_NAME;
            stmt.executeUpdate(createDb);
            System.out.println("Database '" + DB_NAME + "' is ready.");
        }

        // Now connect to the specific database
        connection = DriverManager.getConnection(DB_URL + DB_NAME + "?useSSL=false&serverTimezone=UTC",
                DB_USER, DB_PASSWORD);
        connection.setAutoCommit(true);

        // Create tables
        createTables();
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {

            // Customer table
            String customerTable = """
                CREATE TABLE IF NOT EXISTS Customer (
                    customer_id VARCHAR(50) PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    phone_number VARCHAR(10) NOT NULL UNIQUE,
                    email VARCHAR(100) NOT NULL UNIQUE,
                    address TEXT,
                    customer_pin VARCHAR(6) NOT NULL,
                    aadhar_number VARCHAR(12) NOT NULL UNIQUE,
                    dob DATE NOT NULL,
                    status VARCHAR(20) DEFAULT 'Active'
                )
            """;


            String accountTable = "CREATE TABLE IF NOT EXISTS account (" +
                    "account_id VARCHAR(50) PRIMARY KEY, " +
                    "customer_id VARCHAR(50) NOT NULL, " +
                    "created_at DATETIME NOT NULL, " +
                    "modified_at DATETIME NOT NULL, " +
                    "balance DECIMAL(15,2) DEFAULT 50.00, " +
                    "account_type VARCHAR(20) NOT NULL, " +
                    "account_name VARCHAR(100) NOT NULL, " +
                    "account_number VARCHAR(20) UNIQUE NOT NULL, " +
                    "phone_number_linked VARCHAR(15) NOT NULL, " +
                    "ifsc_code VARCHAR(20) NOT NULL, " +
                    "bank_name VARCHAR(100) NOT NULL, " +
                    "status VARCHAR(20) DEFAULT 'ACTIVE', " +
                    "CONSTRAINT fk_customer FOREIGN KEY (customer_id) REFERENCES customer(customer_id)" +
                    ")";


            // Transaction table
            String transactionsTable = """
                CREATE TABLE IF NOT EXISTS Transactions (
                    transaction_id VARCHAR(50) PRIMARY KEY,
                    account_id VARCHAR(50) NOT NULL,
                    transaction_amount DECIMAL(15,2) NOT NULL,
                    transaction_type VARCHAR(20) NOT NULL,
                    transaction_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    transaction_mode VARCHAR(50) NOT NULL,
                    sender_account_number VARCHAR(20),
                    receiver_account_number VARCHAR(20),
                    description TEXT,
                    FOREIGN KEY (account_id) REFERENCES Account(account_id) ON DELETE CASCADE
                )
            """;

            stmt.executeUpdate(customerTable);
            System.out.println(" -> Table 'Customer' is ready.");
            stmt.executeUpdate(accountTable);
            System.out.println(" -> Table 'Account' is ready.");
            stmt.executeUpdate(transactionsTable);
            System.out.println(" -> Table 'Transactions' is ready.");

            System.out.println("All tables are ready.");
        } catch (SQLException e) {
            System.err.println("!!! ERROR: Could not create tables in the database !!!");
            e.printStackTrace();
            throw e;
        }
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(
                        DB_URL + DB_NAME + "?useSSL=false&serverTimezone=UTC",
                        DB_USER,
                        DB_PASSWORD
                );
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL Driver not found", e);
            }
        }
        return connection;
    }

    // Helper method to get next ID for auto-generation
    public static synchronized int getNextId(String tableName, String idColumn, String prefix) {
        String query = "SELECT " + idColumn + " FROM " + tableName +
                " WHERE " + idColumn + " LIKE ? ORDER BY " + idColumn + " DESC LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, prefix + "%");
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String lastId = rs.getString(1);
                String numPart = lastId.substring(prefix.length());
                return Integer.parseInt(numPart) + 1;
            }
            return 1; // First ID

        } catch (SQLException e) {
            e.printStackTrace();
            return 1;
        }
    }
}