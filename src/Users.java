import java.sql.*;
import java.util.Scanner;

public class Users {
    Connection connection;
    Scanner scanner;

    public Users(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    public void register(Connection connection, Scanner scanner) throws SQLException, ClassNotFoundException {

        System.out.print("Enter Full_Name: ");
        String full_name = scanner.next();
        System.out.print("Enter email: ");
        String email = scanner.next();
        System.out.print("Enter Password: ");
        String password = scanner.next();
        if (user_exists(email)) {
            System.out.println("Email Already Exists proceed to login");
            return;
        }

        String query = "INSERT INTO user (full_name,email,password) VALUES (?,?,?)";

        try (PreparedStatement ps = connection.prepareStatement(query)){
            ps.setString(1, full_name);
            ps.setString(2, email);
            ps.setString(3, password);
            int rows = ps.executeUpdate();
            System.out.println((rows > 0) ? "Registration Successful" : "Registration Failed ");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public String login() {
        System.out.println("Enter email: ");
        String email = scanner.next();
        System.out.println("Enter Password: ");
        String password = scanner.next();

        String query = "select * from user where email = ? and password = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return email;
            } else {
                return null;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public boolean user_exists(String email) {
        String query = "select * from user where email = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)){

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
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

    public void exit() throws InterruptedException {
        int i = 3;
        System.out.print("Exiting");
        while(i>=0){
            Thread.sleep(1000);
            System.out.print(".");
            i--;
        }
        System.out.println();
        System.out.println("Thank you for Choosing us.");
        System.out.println("---------------------------------------");
    }
}

