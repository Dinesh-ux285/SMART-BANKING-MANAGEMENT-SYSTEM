import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class AccountManager {
    Connection connection;
    Scanner scanner;

    public AccountManager(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    public void credit_money(long account_number) throws SQLException {
        scanner.nextLine(); // Clear buffer if needed from previous inputs
        System.out.println("Enter Amount: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();
        System.out.println("Enter Security Pin");
        String pin = scanner.next();
        scanner.nextLine();

        if (account_number == 0) {
            System.out.println("Invalid account number.");
            return;
        }

        try {
            connection.setAutoCommit(false);

            // 1. Verify PIN and Account
            String query = "select * from accounts where account_number = ? and security_pin = ?";
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setLong(1, account_number);
                ps.setString(2, pin);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Invalid pin");
                        connection.rollback();
                        return;
                    }
                }
            }

            // 2. Update Balance (Fixed SQL syntax: removed 'and')
            String creditQuery = "update accounts set balance = balance + ? where account_number = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(creditQuery)) {
                preparedStatement.setDouble(1, amount);
                preparedStatement.setLong(2, account_number);

                int affected = preparedStatement.executeUpdate();
                if (affected > 0) {
                    connection.commit();
                    System.out.println("Rs. " + amount + " credited successfully.");
                } else {
                    System.out.println("Rs. " + amount + " deposit failed.");
                    connection.rollback();
                }
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.out.println("Rollback failed: " + rollbackEx.getMessage());
            }
        } finally {
            // Always ensure auto-commit is reset back to true
            try {
                connection.setAutoCommit(true);
            } catch (SQLException autoCommitEx) {
                System.out.println("Failed to reset auto-commit: " + autoCommitEx.getMessage());
            }
        }
    }

    public void debit_money(long account_number) throws SQLException {
        scanner.nextLine();
        System.out.println("Enter amount to withdraw: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();
        System.out.println("Enter security pin: ");
        String pin = scanner.next();
        scanner.nextLine();

        try {
            connection.setAutoCommit(false);
            if (account_number != 0) {
                String query = "select * from accounts where account_number = ? and security_pin = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setLong(1, account_number);
                preparedStatement.setString(2, pin);
                ResultSet rs = preparedStatement.executeQuery();

                if (rs.next()) {
                    String query1 = "update accounts set balance = balance - ?  where account_number = ?";
                    PreparedStatement ps = connection.prepareStatement(query1);
                    ps.setDouble(1, amount);
                    ps.setLong(2, account_number);
                    int result = ps.executeUpdate();
                    if (result > 0) {
                        System.out.println("Rs. " + amount + " withdrawal Successful");
                        connection.commit();
                        connection.setAutoCommit(true);
                        return;
                    } else {
                        System.out.println("Rs. " + amount + " withdrawal Failed");
                        connection.rollback();
                        connection.setAutoCommit(true);
                    }
                } else {
                    System.out.println("Insufficient balance");
                }

            } else {
                System.out.println("Invalid pin");
            }
        } catch (SQLException e) {
            System.out.println("Withdrawal Failed");
        }
        connection.setAutoCommit(true);
    }

    public void transfer_money(long account_number) throws SQLException {
        scanner.nextLine(); // Clear buffer
        System.out.println("Enter receiver's account number: ");
        long receiver_account_number = scanner.nextLong();
        scanner.nextLine();
        System.out.println("Enter amount to transfer: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();
        System.out.println("Enter security pin: ");
        String pin = scanner.nextLine();

        if (account_number == 0 || receiver_account_number == 0) {
            System.out.println("Invalid account number.");
            return;
        }

        if (account_number == receiver_account_number) {
            System.out.println("You cannot transfer money to your own account.");
            return;
        }

        try {
            connection.setAutoCommit(false);

            // 1. Verify Sender's Account, PIN, and check balance
            String query = "select balance from accounts where account_number = ? and security_pin = ?";
            double senderBalance = -1;

            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setLong(1, account_number);
                ps.setString(2, pin);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        senderBalance = rs.getDouble("balance");
                    } else {
                        System.out.println("Invalid Security Pin or Account Number.");
                        connection.rollback();
                        return;
                    }
                }
            }

            // 2. Check if sender has enough balance
            if (amount > senderBalance) {
                System.out.println("Insufficient balance.");
                connection.rollback();
                return;
            }

            // 3. Verify if the receiver's account exists
            String checkReceiver = "select account_number from accounts where account_number = ?";
            try (PreparedStatement psReceiver = connection.prepareStatement(checkReceiver)) {
                psReceiver.setLong(1, receiver_account_number);
                try (ResultSet rsReceiver = psReceiver.executeQuery()) {
                    if (!rsReceiver.next()) {
                        System.out.println("Receiver account does not exist.");
                        connection.rollback();
                        return;
                    }
                }
            }

            String creditQuery = "update accounts set balance = balance + ? where account_number = ?";
            String debitQuery = "update accounts set balance = balance - ? where account_number = ?";

            try (PreparedStatement psc = connection.prepareStatement(creditQuery);
                 PreparedStatement psd = connection.prepareStatement(debitQuery)) {

                // Credit Receiver
                psc.setDouble(1, amount);
                psc.setLong(2, receiver_account_number);

                // Debit Sender
                psd.setDouble(1, amount);
                psd.setLong(2, account_number);

                int creditAffected = psc.executeUpdate();
                int debitAffected = psd.executeUpdate();

                if (creditAffected > 0 && debitAffected > 0) {
                    connection.commit();
                    System.out.println("Rs. " + amount + " transferred successfully from " + account_number + " to " + receiver_account_number);
                } else {
                    System.out.println("Transfer Failed.");
                    connection.rollback();
                }
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.out.println("Rollback failed: " + rollbackEx.getMessage());
            }
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException autoCommitEx) {
                System.out.println("Failed to reset auto-commit: " + autoCommitEx.getMessage());
            }
        }
    }

    public void getBalance(long account_number) {
        scanner.nextLine();
        System.out.println("Enter security pin: ");
        String pin = scanner.nextLine();

        try {
            if(account_number!=0){
                String query = "select balance from accounts where security_pin = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1,pin);
                ResultSet rs = preparedStatement.executeQuery();
                if(rs.next()){
                    double balance = rs.getDouble("balance");
                    System.out.println("Balance: " + balance);
                }
                else {
                    System.out.println("Invalid Pin");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
