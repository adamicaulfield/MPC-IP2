import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.math.BigInteger;

public class Client {
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
                // System.out.println(m);
                if(m.startsWith("Enter")){
                    System.out.println(m);
                    out.println(scanner.nextLine());
                } else if(m.startsWith("R")){
                    System.out.println(m);
                    out.println("WAITING");
                } else if(m.startsWith("SHARES")){
                    System.out.println("Received Shares: "+m);
                    String returnMsg = sumOfShares(m);
                    System.out.println("Sending sum of shares");
                    System.out.println(returnMsg);
                    out.println(returnMsg);
                } else if(m.startsWith("FINISHED")){
                    System.out.println(m);
                    // System.out.println("Sending ACK");
                    out.println("ACK");
                }
                // if(in.nextLine().startsWith("RECEIVED")){
                //     out.println("ACK");
                // } else if(in.nextLine().startsWith("ENTER")){
                //     out.println(scanner.nextLine());
                // }
            }
        }
    }

    public static String sumOfShares(String m){
        //Expecting format SHARES-i:s_1,s_2,s_3,s_4,s_5
        //Goal is to return SUM-i:sum_i

        String returnMsg = "SUM-";
        String[] twohalves = m.split(":");
        String[] shares = twohalves[1].split(",");
        BigInteger sum = new BigInteger("0");
        for(String s: shares){
            sum = sum.add(new BigInteger(s));
        }
        returnMsg = returnMsg + twohalves[0].split("-")[1] + ":" + sum.toString();
        return returnMsg;
    }
}