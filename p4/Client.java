import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.Random;

public class Client {
    public static int a_i;
    public static int b_i;
    public static int c_i;
    public static int x_i;
    public static int y_i;
    public static int x_prime_i;
    public static int y_prime_i;
    public static int x_prime;
    public static int y_prime;
    public static int z_i;

    public static void main(String[] args) throws Exception {
        //Each client generates share of a and b on their own
        int p = 11;
        Random rand = new Random();
        a_i = rand.nextInt(p-1)+1;
        b_i = rand.nextInt(p-1)+1;

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
                if(m.startsWith("S-C-SEND-SHARES-AB")){
                    System.out.println("Sending \"C-S-SHARES-AB:"+a_i+","+b_i+"\"");
                    out.println("C-S-SHARES-AB:"+a_i+","+b_i);
                }
                else if(m.startsWith("S-C-SHARES:")){
                    c_i = Integer.parseInt((m.split(":")[1]).split(",")[0]);
                    x_i = Integer.parseInt((m.split(":")[1]).split(",")[1]);
                    y_i = Integer.parseInt((m.split(":")[1]).split(",")[2]);
                    x_prime = Integer.parseInt((m.split(":")[1]).split(",")[3]);
                    y_prime = Integer.parseInt((m.split(":")[1]).split(",")[4]);

                    z_i = c_i + x_prime*b_i + y_prime*a_i; //x'*y' is added once by Server
                    System.out.println("Sending \"C-S-Z-SHARE:"+z_i+"\"");
                    out.println("C-S-Z-SHARE:"+z_i);
                } 
                else if(m.startsWith("S-C-PROD")){
                    System.out.println(m);
                    out.println("DONE");
                }
                else{
                    out.println(scanner.nextLine());
                }

            }
        }
    }
}