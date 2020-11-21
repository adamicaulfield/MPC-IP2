import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Random;

public class TrustedDealer {
    //Get list of triples
    public static ArrayList<Integer> alist = new ArrayList<Integer>(1000000);
    public static ArrayList<Integer> blist = new ArrayList<Integer>(1000000);

    public static void main(String[] args) throws Exception {
        int p = 11;
        Random rand = new Random();
        for(int i=0; i<1000000; i++){
            alist.add(rand.nextInt(p-1)+1);
            blist.add(rand.nextInt(p-1)+1);
        }

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
                if(m.startsWith("S-D-REQ-TRIPLE:")){
                    String triple = processTripleRequest(m);
                    System.out.println(triple);
                    out.println(triple);
                }
            }
        }
    }

    public static String processTripleRequest(String msg){
        int index = Integer.parseInt(msg.split(":")[1]);
        int a = alist.get(index);
        int b = blist.get(index);
        return "D-S-RET-TRIPLE:"+a+","+b+","+(a*b);
    }
}