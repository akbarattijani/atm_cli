package Util;

import Constant.Command;
import Util.Listener.DatabaseListener;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author AKBAR
 */
public class Controller implements DatabaseListener {
    private Database database = new Database(this);
    
    private Scanner sc;
    private String command = "";
    
    public void start() throws IOException {
        System.out.println("Welcome ATM Command Line Interface...\n");
        sc = new Scanner(System.in);
        
        nextCommand();
        run(command);
    }
    
    private void nextCommand() {
        System.out.print("$");
        command = sc.nextLine();
        
        try {
            run(command);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        System.out.println();
    }
    
    private void run(String command) throws IOException {
        String[] arguments = command.split(" ");
        
        if (arguments[0].toUpperCase().equals(Command.LOGIN.toString())) {
            if (arguments.length < 2) {
                System.out.println("Command Login not have arguments, try again...\n");
                nextCommand();
            } else if (arguments.length > 2) {
                System.out.println("Argumnets Login not valis, try again...\n");
                nextCommand();
            } else if (database.isLogin()) {
                System.out.println("Your Already Login..\n");
                nextCommand();
            } else {
                database.login(arguments[1]);
            }
        } else if (arguments[0].toUpperCase().equals(Command.LOGOUT.toString())) {
            if (arguments.length > 1) {
                System.out.println("Logout can't need arguments, try again...\n");
                nextCommand();
            } else {
                database.logout();
            }
        } else if (arguments[0].toUpperCase().equals(Command.DEPOSIT.toString())) {
            if (arguments.length < 2) {
                System.out.println("Amount is empty, try again...\n");
                nextCommand();
            } else if (arguments.length > 2) {
                System.out.println("Argumnets Deposit not valid, try again...\n");
                nextCommand();
            } else {
                database.update(Double.parseDouble(arguments[1]), Command.DEPOSIT);
            }
        } else if (arguments[0].toUpperCase().equals(Command.WITHDRAW.toString())) {
            if (arguments.length < 2) {
                System.out.println("Amount is empty, try again...\n");
                nextCommand();
            } else if (arguments.length > 2) {
                System.out.println("Argumnets Withdraw not valid, try again...\n");
                nextCommand();
            } else {
                database.update(Double.parseDouble(arguments[1]), Command.WITHDRAW);
            }
        } else if (arguments[0].toUpperCase().equals(Command.TRANSFER.toString())) {
            if (arguments.length < 2) {
                System.out.println("Command Transfer not have arguments, try again...\n");
                nextCommand();
            } else if (arguments.length < 3) {
                System.out.println("Please input amount!\n");
                nextCommand();
            } else if (arguments.length > 3) {
                System.out.println("Argumnets Transefer not valid, try again...\n");
                nextCommand();
            } else {
                try {
                    database.update(Double.parseDouble(arguments[2]), Command.TRANSFER, arguments[1]);
                } catch (Exception e) {
                    System.out.println("Command Transfer not valid, try again...\n");
                    e.printStackTrace();
                    nextCommand();
                }
            }
        } else {
            System.out.println("Command not found\n");
            nextCommand();
        }
    }
    
    @Override
    public void onInvalidLogin() {
        System.out.println("Your not login, please login first!\n");
        nextCommand();
    }

    @Override
    public void onSuccess(Command command, String... arguments) {
        if (command == Command.LOGIN) {
            System.out.println("Hello, " + arguments[0] + "!");
            try {
                database.balance(Command.ROUTE_OWED.toString());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else if (command == Command.BALANCE) {
            try {
                System.out.println("Your balance is $" + arguments[0] + (arguments.length > 1 ? "" : "\n"));  
            
                if (arguments.length > 1) {
                    if (arguments[1].equals(Command.ROUTE_OWED.toString())) {
                        database.getFromToOwed();
                    } else {
                        System.out.println(arguments[1]);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (command == Command.ROUTE_OWED) {
            if (arguments[0] != null && !arguments[0].equals("null")) {
                System.out.println("Owed $" + arguments[2] + " from " + arguments[0]);
            } else {
                if (arguments[1] == null || arguments[1].equals("null")) {
                    System.out.println("");
                }
            }
            
            if (arguments[1] != null && !arguments[1].equals("null")) {
                System.out.println("Owed $" + arguments[3] + " to " + arguments[1] + "\n");
            } else {
                System.out.println("");
            }
        } else if (command == Command.LOGOUT) {
            System.out.println("Goodbye, " + arguments[0] + "!\n");
        } else if (command == Command.DEPOSIT) {
            try {
                if (arguments != null && arguments.length > 2) {
                    System.out.println("Transferred $" + arguments[0] + " to " + arguments[1]);
                    database.balance("Owed $" + arguments[2] + " to " + arguments[1] + "\n");
                } else {
                    database.balance();   
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else if (command == Command.WITHDRAW) {
            try {
                database.balance();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else if (command == Command.TRANSFER) {
            try {
                database.checkOwed("Transferred $" + arguments[0] + " to " + arguments[1], arguments[1], command.toString());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else if (command == Command.OWED) {
            String owed = arguments[0];
            if (owed == null || Double.parseDouble(owed) < 0.1) {
                try {
                    System.out.println(arguments[2]);
                    database.balance(Command.ROUTE_OWED.toString());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                try {
                    if (arguments[4] == Command.TRANSFER.toString()) {
                        System.out.println(arguments[2]);
                        database.balance("Owed $" + owed + " to " + arguments[3] + "\n");
                    } else {
                        database.update(Double.parseDouble(owed), Command.TRANSFER, arguments[1], Command.OWED.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        nextCommand();
    }

    @Override
    public void onFail(Command command, String... arguments) {
        if (command == Command.LOGIN) {
            try {
                if (database.insert(arguments[0], 0.0)) {
                    System.out.println("Created New User");
                    System.out.println("Hello, " + arguments[0]);
                    
                    try {
                        database.balance();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    System.out.println("Failed Create New User\n");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else if (command == Command.LOGOUT) {
            System.out.println("Logout failed, try again!\n");
        } else if (command == Command.DEPOSIT) {
            System.out.println("Failed Deposit\n");
        } else if (command == Command.WITHDRAW) {
            System.out.println("Failed Withdraw, " + arguments[0] + "\n");
        } else if (command == Command.TRANSFER) {
            System.out.println("Failed Transfer, " + arguments[0] + "\n");
        }
        
        nextCommand();
    }   
}
