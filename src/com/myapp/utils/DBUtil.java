package com.myapp.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtil {
    private static final String URL = "jdbc:sqlite:services.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initialize() {
        String createUsers = """
            CREATE TABLE IF NOT EXISTS Users (
              userId        INTEGER PRIMARY KEY AUTOINCREMENT,
              name          TEXT NOT NULL,
              email         TEXT UNIQUE NOT NULL,
              password      TEXT NOT NULL,
              userType      TEXT NOT NULL,
              averageRating REAL DEFAULT 0.0,
              totalRatings  INTEGER DEFAULT 0
            );
            """;

        String createServices = """
            CREATE TABLE IF NOT EXISTS Services (
              serviceId     INTEGER PRIMARY KEY AUTOINCREMENT,
              providerId    INTEGER NOT NULL,
              category      TEXT,
              title         TEXT,
              description   TEXT,
              price         REAL,
              thumbnailPath TEXT,
              FOREIGN KEY(providerId) REFERENCES Users(userId)
            );
            """;

        String createBookings = """
            CREATE TABLE IF NOT EXISTS Bookings (
              bookingId             INTEGER PRIMARY KEY AUTOINCREMENT,
              customerId            INTEGER NOT NULL,
              serviceId             INTEGER NOT NULL,
              status                TEXT,
              bookingDate           TEXT,
              paymentStatus         TEXT DEFAULT 'Unpaid',
              isCompletedByProvider BOOLEAN DEFAULT 0,
              hasCustomerReviewed   BOOLEAN DEFAULT 0,
              FOREIGN KEY(customerId) REFERENCES Users(userId),
              FOREIGN KEY(serviceId)   REFERENCES Services(serviceId)
            );
            """;

        String createPayments = """
            CREATE TABLE IF NOT EXISTS Payments (
              paymentId     INTEGER PRIMARY KEY AUTOINCREMENT,
              bookingId     INTEGER NOT NULL,
              amountPaid    REAL NOT NULL,
              platformFee   REAL NOT NULL,
              payoutAmount  REAL NOT NULL,
              paymentMethod TEXT NOT NULL,
              transactionId TEXT,
              paymentDate   TEXT NOT NULL,
              FOREIGN KEY(bookingId) REFERENCES Bookings(bookingId)
            );
            """;

        String createReviews = """
            CREATE TABLE IF NOT EXISTS Reviews (
                reviewId    INTEGER PRIMARY KEY AUTOINCREMENT,
                bookingId   INTEGER NOT NULL,
                providerId  INTEGER NOT NULL,
                customerId  INTEGER NOT NULL,
                rating      INTEGER NOT NULL,
                comment     TEXT,
                reviewDate  TEXT NOT NULL,
                FOREIGN KEY(bookingId) REFERENCES Bookings(bookingId),
                FOREIGN KEY(providerId) REFERENCES Users(userId),
                FOREIGN KEY(customerId) REFERENCES Users(userId)
            );
        """;


        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createUsers);
            stmt.execute(createServices);
            stmt.execute(createBookings);
            stmt.execute(createPayments);
            stmt.execute(createReviews);

            System.out.println("✅ Database with Review System initialized.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("❌ Failed to initialize database: " + e.getMessage());
        }
    }
}
