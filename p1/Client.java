import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static int i;
    public static int share;

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
                m = in.nextLine();
                System.out.println(m);
                if(m.startsWith("SHARE:")){
                    i = Integer.parseInt((m.split(":")[1]).split(",")[0]);
                    share = Integer.parseInt((m.split(":")[1]).split(",")[1]);
                } else if(m.startsWith("SEND")){
                    out.println("RETSHARE:"+i+","+share);
                } else if(m.startsWith("GOODBYE")){
                    out.println(m);
                }
            }
        }
    }
}