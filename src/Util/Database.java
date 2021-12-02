package Util;

import Constant.Command;
import Util.Listener.DatabaseListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author AKBAR
 */
public class Database {
    private DatabaseListener listener;
    private final String path = getClass().getResource("/database/Database").getPath();
    private String user = "";
    
    public Database(DatabaseListener listener) {
        this.listener = listener;
    }
    
    public boolean isLogin() {
        return !user.equals("");
    }
    
    public void balance(String... packet) throws FileNotFoundException, IOException {
        if (user.equals("")) {
            listener.onInvalidLogin();
            return;
        }
        
        FileReader fr = new FileReader(path);
        String line,
               input = "";
        
        try(BufferedReader br = new BufferedReader(fr)) {
            
            while((line = br.readLine()) != null) {  
                String name = getName(line);
                if(name != null && name.equals(user)) {
                    if (packet != null && packet.length > 0) {
                        String[] fixPacket = new String[packet.length + 1];
                        fixPacket[0] = line.split(":")[1].trim();

                        System.arraycopy(packet, 0, fixPacket, 1, packet.length);
                        listener.onSuccess(Command.BALANCE, fixPacket);
                    } else {
                        listener.onSuccess(Command.BALANCE, line.split(":")[1].trim());
                    }
                    
                    return;
                }
            }
        }
        
        listener.onFail(Command.BALANCE, "0.0");
    }
    
    public void getFromToOwed() throws FileNotFoundException, IOException {
        if (user.equals("")) {
            listener.onInvalidLogin();
            return;
        }
        
        FileReader fr = new FileReader(path);
        FileReader fr2 = new FileReader(path);
        String line,
               line2,
               input = "",
               fromOwed = null,
               toOwed = null;
        double amountFromOwed = 0.0, amountToOwed = 0.0;
        
        try(BufferedReader br = new BufferedReader(fr)) {
            
            while((line = br.readLine()) != null) { 
                String name = getName(line);
                
                if(name != null && name.equals(user)) {
                    toOwed = line.substring(line.indexOf("=>") + 2, line.indexOf(")")).trim();
                    amountToOwed = Double.parseDouble(line.substring(line.indexOf("(") + 1, line.indexOf("=>")).trim());
                    
                    try(BufferedReader br2 = new BufferedReader(fr2)) {
            
                        while((line2 = br2.readLine()) != null) {
                            if (line2.substring(line2.indexOf("=>") + 2, line2.indexOf(")")).trim().equals(name)) {
                                fromOwed = line2.substring(0, line2.indexOf("(")).trim();
                                amountFromOwed = Double.parseDouble(line2.substring(line2.indexOf("(") + 1, line2.indexOf("=>")).trim());
                                
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        listener.onSuccess(Command.ROUTE_OWED, fromOwed, toOwed, String.valueOf(amountFromOwed), String.valueOf(amountToOwed));
    }
    
    public void checkOwed(String... packet) throws FileNotFoundException, IOException {
        if (user.equals("")) {
            listener.onInvalidLogin();
            return;
        }
        
        FileReader fr = new FileReader(path);
        String line,
               input = "";
        
        try(BufferedReader br = new BufferedReader(fr)) {
            
            while((line = br.readLine()) != null) {  
                if(line.contains(user)) {
                    String owed = line.substring(line.indexOf("(") + 1, line.indexOf(")"));
                    String[] fixPacket = new String[packet.length + 2];
                    fixPacket[0] = owed.split("=>")[0].trim();
                    fixPacket[1] = owed.split("=>")[1].trim();
                    
                    for (int i = 0; i < packet.length; i++) {
                        fixPacket[i + 2] = packet[i];
                    }
                    
                    listener.onSuccess(Command.OWED, fixPacket);
                    return;
                }
            }
        }
        
        String[] fixPacket = new String[packet.length];
        for (int i = 0; i < packet.length; i++) {
            fixPacket[i] = packet[i];
        }
        listener.onFail(Command.OWED, fixPacket);
    }
    
    public void login(String user) throws FileNotFoundException, IOException {
        FileReader fr = new FileReader(path);
        String line,
               input = "";
        
        try(BufferedReader br = new BufferedReader(fr)) {
            
            while((line = br.readLine()) != null) {  
                if(line.contains(user)) {
                    this.user = user;
                    listener.onSuccess(Command.LOGIN, user);
                    return;
                }
            }
        }
        
        listener.onFail(Command.LOGIN, user);
    }
    
    public void logout() throws FileNotFoundException, IOException {
        if (!isLogin()) {
            listener.onInvalidLogin();
        }
        
        FileReader fr = new FileReader(path);
        String line,
               input = "";
        
        try(BufferedReader br = new BufferedReader(fr)) {
            
            while((line = br.readLine()) != null) {  
                if(line.contains(user)) {
                    this.user = "";
                    listener.onSuccess(Command.LOGOUT, line.split("\\(")[0].trim());
                    return;
                }
            }
        }
        
        listener.onFail(Command.LOGOUT);
    }
    
    private String updateUserForTransfer(String user, Double amount) throws FileNotFoundException, IOException {
        FileReader fr = new FileReader(path);
        String line,
               input = "";
        
        try(BufferedReader br = new BufferedReader(fr)) {
            
            while((line = br.readLine()) != null) { 
                String name = getName(line);
                if(name != null && name.equals(user)) {
                    // Check is reciver have owed or not
                    String owedReciver = line.substring(line.indexOf("=>") + 2, line.indexOf(")")).trim();
                    if (!owedReciver.equals("null")) {
                        double owed = Double.parseDouble(line.substring(line.indexOf("(") + 1, line.indexOf("=>")).trim());
                        if (owed >= amount) {
                            double originalOwed = owed;
                            owed = owed - amount;
                            return user + "(" + String.valueOf(owed) + " => " + owedReciver + ") : 0 @" + originalOwed;
                        } else {
                            double balance = amount - owed;
                            return user + "(0 => null) : " + balance + " @" + owed;
                        }
                    } else {
                        double balance = Double.parseDouble(line.split(":")[1].trim());
                        balance += amount;

                        return line.split(":")[0].trim() + " : " + String.valueOf(balance) + '\n';   
                    }
                }
            }
        }
        
        return null;
    }
    
    public void update(Double amount, Command command, String... userForTransfer) throws FileNotFoundException, IOException {
        if (user.equals("")) {
            listener.onInvalidLogin();
            return;
        }
        
        FileReader fr = new FileReader(path);
        String line,
               input = "",
               lineForTransferReciver = null,
               userReciver = null;
        
        double balance = 0.0;
        double owedForDepositOnly = 0.0; // Onlu for deposit command
        double transferedAmount = 0.0; // Only for transfer command
        
        try(BufferedReader br = new BufferedReader(fr)) {
            
            while((line = br.readLine()) != null) { 
                String name = getName(line);
                if(name != null && name.equals(user)) {
                    balance = Double.parseDouble(line.split(":")[1].trim());
                    if (command == Command.DEPOSIT) {
                        balance += amount;
                        
                        //Check owed
                        double owed = Double.parseDouble(line.substring(line.indexOf("(") + 1, line.indexOf("=>")).trim());
                        if (owed > 0.0) {
                            if (balance <= owed) {
                                owedForDepositOnly = owed - balance;
                                userReciver = line.substring(line.indexOf("=>") + 2, line.indexOf(")")).trim();
                                line = user + "(" + owedForDepositOnly + " => " + (owedForDepositOnly == 0.0 ? "null" : userReciver) + ") : 0";
                                lineForTransferReciver = updateUserForTransfer(userReciver, balance);
                            } else {
                                double fixBalance = balance - owed;
                                owedForDepositOnly = 0.0;
                                line = user + "(" + owedForDepositOnly + " => null) : " + String.valueOf(fixBalance);
                            }
                        } else {
                            line = line.substring(0, line.indexOf(":") - 1) + " : " + String.valueOf(balance);
                        }
                    } else if (command == Command.WITHDRAW) {
                        balance -= amount;
                        
                        if (balance < 0.0) {
                            listener.onFail(command, "Your balance is not enough..\n");
                            return;
                        }
                        
                        line = line.substring(0, line.indexOf(":") - 1) + " : " + String.valueOf(balance);
                    } else if (command == Command.TRANSFER) {
                        //Check owed
                        double owed = Double.parseDouble(line.substring(line.indexOf("(") + 1, line.indexOf("=>")).trim());
                        if (owed > 0.0) {
                            listener.onFail(command, "Your have Owed $" + owed + " to " + line.substring(line.indexOf(">"), line.indexOf(")")).trim() + "\n");
                            return;
                        }
                        
                        // Transfering
                        userReciver = userForTransfer[0];
                        double originalBalance = balance;
                        transferedAmount = balance;
                        balance -= amount;
                        
                        if (transferedAmount >= amount) {
                            transferedAmount = amount;
                        }
                        
                        lineForTransferReciver = updateUserForTransfer(userReciver, transferedAmount);
                        if (lineForTransferReciver == null) {
                            listener.onFail(command, "User Reciver not found!\n");
                            return;
                        }
                        
                        double owedTotal = 0.0;
                        if (balance < 0.0) {
                            owedTotal = Math.abs(balance);
                            balance = 0.0;
                        } else {
                            userReciver = "null";
                        }
                        
                        String oweded = lineForTransferReciver.split("@").length > 1 ? lineForTransferReciver.split("@")[1].trim() : "0";
                        lineForTransferReciver = lineForTransferReciver.split("@")[0].trim();
                        
                        if (Double.parseDouble(oweded) > amount) {
                            line = user + "(" + owedTotal + " => " + userReciver + ") : " + line.substring(line.indexOf(":") + 1).trim();
                        } else {
                            double updateBalance = Double.parseDouble(line.substring(line.indexOf(":") + 1).trim()) - (amount - Double.parseDouble(oweded));
                            if (updateBalance < 0.0) {
                                updateBalance = 0.0;
                            }
                            
                            line = user + "(" + owedTotal + " => " + userReciver + ") : " + String.valueOf(updateBalance);
                        }
                    } else {
                        listener.onFail(command, "Command not found!\n");
                        return;
                    }
                }
                
                input += line + '\n';
            }
        }
        
        if (command == Command.TRANSFER) {
            String lines[] = input.split("\\r?\\n");
            input = "";
            for (String value : lines) {
                String name = getName(value);
                if (name != null && name.equals(userForTransfer[0])) {
                    input += lineForTransferReciver + '\n';
                } else {
                    input += value + '\n';
                }
            }
            
            write(input);
            listener.onSuccess(command, String.valueOf(transferedAmount), (userForTransfer != null && userForTransfer.length > 0 ? userForTransfer[0] : null));
            return;
        } else if (command == Command.DEPOSIT && userReciver != null) {
            String lines[] = input.split("\\r?\\n");
            input = "";
            for (String value : lines) {
                String name = getName(value);
                if (name != null && name.equals(userReciver)) {
                    input += lineForTransferReciver + '\n';
                } else {
                    input += value + '\n';
                }
            }
            
            write(input);
            listener.onSuccess(command, String.valueOf(balance), userReciver, String.valueOf(owedForDepositOnly));
            return;
        }
        
        write(input);
        listener.onSuccess(command, String.valueOf(amount), (userForTransfer != null && userForTransfer.length > 0 ? userForTransfer[0] : null));
    }
    
    private String getName(String line) {
        if (line != null && line.trim().length() > 0) {
            return line.substring(0, line.indexOf("(")).trim();
        } else {
            return null;
        }
    }
    
    public boolean insert(String user, Double amount) throws FileNotFoundException, IOException {
        FileReader fr = new FileReader(path);
        String line,
               input = "";
        
        try(BufferedReader br = new BufferedReader(fr)) {
            
            while((line = br.readLine()) != null) {  
                input += line + '\n';
            }
        }
        
        input += user + "(0 => null) : " + String.valueOf(amount) + '\n';
        write(input);
        
        this.user = user;
        return true;
    }
    
    private void write(String data) throws FileNotFoundException, IOException {
        // Cleasing
        String lines[] = data.split("\\r?\\n");
        data = "";
        for (String value : lines) {
            if (value != null && !value.trim().equals("")) {
                data += value + '\n';
            }
        }
        
        try (FileOutputStream fileOut = new FileOutputStream(path)) {
            fileOut.write(data.getBytes());
        }
    }
}
