import java.sql.*;
import java.util.Scanner;

public class Accounts {
    private final Connection connection;
    private final Scanner scanner;

    public Accounts(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    public long open_account(String email) throws SQLException {
        if (!account_exists(email)) {
            String query = "Insert into accounts (account_number,full_name,email,balance,security_pin) VALUES (?,?,?,?,?)";
            scanner.nextLine();
            System.out.println("Enter full_name: ");
            String full_name = scanner.next();
            System.out.println("Enter the Initial Balance:");
            double balance = scanner.nextDouble();
            scanner.nextLine();
            System.out.println("Enter Security Pin: ");
            String security_pin = scanner.next();

            try {
                long account_number = generateAccountNumber();
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setLong(1, account_number);
                ps.setString(2, full_name);
                ps.setString(3,email);
                ps.setDouble(4, balance);
                ps.setString(5, security_pin);
                int result = ps.executeUpdate();
                if (result > 0) {
                    return account_number;
                } else {
                    throw new RuntimeException("Account opening failed");
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        throw new RuntimeException("Account already exists");
    }

    public long generateAccountNumber() throws SQLException {
        try {
            Statement ps = connection.createStatement();
            ResultSet rs = ps.executeQuery("select account_number from accounts order by account_number DESC LIMIT 1");
            if (rs.next()) {
                long last_account_Number = rs.getLong("account_number");
                return last_account_Number+1;
            } else {
                return 100001001;
            }
        }
            catch(SQLException e){
                System.out.println(e.getMessage());
            }
            return 100001001;
    }

    public long getAccount_Number(String email){
        String query = "select account_number from accounts where email = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong("account_number");
            }
        }catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        throw new RuntimeException("Account doesn't exist");
    }

    public  boolean account_exists(String email) {
        String query = "select * from accounts where email = ?";
        try {
            long last_account_number = generateAccountNumber();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1,email);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return true;
            }
            else {
                return false;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }
}
