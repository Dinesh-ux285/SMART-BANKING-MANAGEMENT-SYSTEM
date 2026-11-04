
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import static java.lang.Class.forName;
import com.mysql.cj.jdbc.Driver;


public class BankingApp {
    private static final String url = "jdbc:mysql://localhost:3306/bankingSystem";
    private static final String username = "root";
    private static final String password = "Dinu@sql#45";

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }

        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            System.out.println("-------WELCOME TO BANKING SYSTEM-------");
            Scanner scanner = new Scanner(System.in);
            Users users = new Users(connection, scanner);
            Accounts accounts = new Accounts(connection, scanner);
            AccountManager accountManager = new AccountManager(connection, scanner);
            while (true) {
                System.out.println("1. Register");
                System.out.println("2. Login");
                System.out.println("3. Exit");

                String email;
                long accountNumber;

                System.out.println("Enter your Choice:");
                int choice = scanner.nextInt();
                switch (choice) {
                    case 1:
                        users.register(connection, scanner);
                        break;
                    case 2:
                        email = users.login();
                        if (email != null) {
                            System.out.println();
                            System.out.println("User Logged In");
                            if (!accounts.account_exists(email)) {
                                System.out.println();
                                System.out.println("1.Open a new account");
                                System.out.println("2.Exit");
                                if (scanner.nextInt() == 1) {
                                    System.out.println("Account created successfully");
                                    System.out.println("Your Account Number is : " + accounts.open_account(email));
                                } else {
                                    break;
                                }
                            }
                            accountNumber = accounts.getAccount_Number(email);
                            int option1 = 0;
                            while (option1 != 5) {
                                System.out.println("1.Credit Money");
                                System.out.println("2.Debit Money");
                                System.out.println("3.Transfer Money");
                                System.out.println("4.Check Money");
                                System.out.println("5.Log Out");

                                System.out.println("Enter your Option");
                                option1 = scanner.nextInt();
                                switch (option1) {
                                    case 1:
                                        accountManager.credit_money(accountNumber);
                                        break;
                                    case 2:
                                        accountManager.debit_money(accountNumber);
                                        break;
                                    case 3:
                                        accountManager.transfer_money(accountNumber);
                                        break;
                                    case 4:
                                        accountManager.getBalance(accountNumber);
                                        break;
                                    case 5:
                                        System.out.println("Logging out");
                                        break;
                                    default:
                                        System.out.println("Invalid choice");
                                        break;
                                }
                            }
                        } else {
                            System.out.println("Invalid email or password");
                            break;
                        }

                    case 3:
                        try {
                            users.exit();
                            break;
                        } catch (InterruptedException e) {
                            System.out.println(e.getMessage());
                        }
                    default:
                        System.out.println("Invalid choice. Enter Again");
                        break;
                }
            }
        }catch(SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}