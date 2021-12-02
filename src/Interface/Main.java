package Interface;

import Constant.Command;
import Util.Controller;
import Util.Database;
import java.io.IOException;

/**
 *
 * @author AKBAR
 */
public class Main {
    
    public Main() throws IOException {
        Controller controller = new Controller();
        controller.start();
    }
    
    public static void main (String[] args) throws IOException {
        new Main();
    }
}
