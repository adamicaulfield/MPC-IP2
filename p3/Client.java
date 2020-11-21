import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static int a_i;
    public static int b_i;
    public static int c_i;
    public static int x_i;
    public static int y_i;

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Pass the server IP as the sole command line argument");
            return;
        }
        try (var socket = new Socket(args[0], 59898)) {
            System.out.println("Enter lines of text then Ctrl+D or Ctrl+C to quit");
            var scanner = new Scanner(System.in);
            var in = new Scanner(socket.getInputStream());
            var out = new PrintWriter(socket.getOutputStream(), true);
            String m;
            while (true) {
                // out.println(scanner.nextLine());
                m = in.nextLine();
                System.out.println(m);
                if(m.startsWith("S-C-SHARES:")){
                    processShares(m);
                } else{
                    out.println(scanner.nextLine());
                }

            }
        }
    }

    public static void processShares(String msg){
        a_i = Integer.parseInt((msg.split(":")[1]).split(",")[0]);
        b_i = Integer.parseInt((msg.split(":")[1]).split(",")[1]);
        c_i = Integer.parseInt((msg.split(":")[1]).split(",")[2]);
        x_i = Integer.parseInt((msg.split(":")[1]).split(",")[3]);
        y_i = Integer.parseInt((msg.split(":")[1]).split(",")[4]);
    }
}