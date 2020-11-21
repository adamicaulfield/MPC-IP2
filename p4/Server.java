import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Random;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;

public class Server {

    public static ArrayList<Socket> parties = new ArrayList<Socket>();
    public static ArrayList<Integer> rshares = new ArrayList<Integer>();
    public static Set<String> returnedShares = new LinkedHashSet<String>();
    public static int[][] coefs;
    public static int t;
    public static int n;
    public static int p;
    public static int total_ab_shares = 0;
    public static int total_shares = 0;
    public static int total_zshares = 0;
    public static int total_diffs = 0;
    public static int closed_count = 0;
    public static int triple_index;
    public static int a;
    public static int b;
    public static int c;
    public static int[] a_coef;
    public static int[] b_coef;
    public static int[] c_coef;
    public static int[] a_shares;
    public static int[] b_shares;
    public static int[] c_shares;
    public static int[][] shares_sec;
    public static int[][] diff;
    public static int[] zshares;
    public static int x_prime;
    public static int y_prime;
    public static int x;
    public static int y;

    public static void setup(int xx, int yy){
        Random rand = new Random();
        x = xx;
        y = yy;
        int[] secrets = {x, y};
        diff = new int[2][n];
        zshares = new int[n];
        a_shares = new int[n];
        b_shares = new int[n];
        c_shares = new int[n];
        //Get polynomial of data
        int[][] coefs = new int[2][t+1];
        System.out.println("Polynomial of two data: ");
        for(int i=0; i<2; i++){
            for(int j=0; j<t+1; j++){
                if(j==0){
                    coefs[i][j] = secrets[i];
                }
                else{
                    coefs[i][j] = rand.nextInt(p-1)+1;
                }   
                System.out.print(coefs[i][j]);
                System.out.print(" ");
            }
            System.out.println("");
        }
        System.out.println(" ");

        // Generate Shares of data
        shares_sec = new int[2][n];
        System.out.println(" ");
        System.out.println("Secret shares: ");
        for(int party=0; party<n; party++){
            // System.out.println("Generate Secret Shares for Party #"+party);
            shares_sec[0][party] = 0;
            shares_sec[1][party] = 0;

            int big_i = party+1;
            for(int i=0; i<2; i++){
                for(int j=0; j<t+1; j++){
                    int x_i = (int)Math.pow(big_i,j);
                    shares_sec[i][party] = shares_sec[i][party] + coefs[i][j]*x_i;  
                }
                shares_sec[i][party] = shares_sec[i][party]%p;
                System.out.print(shares_sec[i][party]);
                System.out.print(" ");
            }
            System.out.println(" ");
        }
        System.out.println("Completed Setup");
    }

    public static void main(String[] args) throws Exception {

        try (var listener = new ServerSocket(59898)) {
            System.out.println("The server is running...");
            var pool = Executors.newFixedThreadPool(20);
            n = 3;
            p = 11;
            t = n/2;
            setup(2,3);
            rshares = partiesToReconstruct();
            Random rand = new Random();

            while (true) {
                Socket s = listener.accept();
                parties.add(s);
                pool.execute(new Party(s, parties.size()));
            }
        }
    }

    public static String reconstructProduct(){
        //Reconstruct Shares
        // System.out.println("");
        // System.out.println("Reconstruct Shares to reveal secret");
        double rshare0 = (double)(rshares.get(0));
        double rshare1 = (double)(rshares.get(1));

        double share0val = (double)zshares[rshares.get(0)-1];
        double share1val = (double)zshares[rshares.get(1)-1];

        int product = (int) ((rshare0*share1val - (rshare1*share0val))/(rshare0 - rshare1)) %p;
        // System.out.println(recoveredSecret+"="+"(("+rshare0+"*"+share1val+") - ("+rshare1+"*"+share0val+"))/("+rshare0+" - "+rshare1+")) %"+p);
        product = (product + x_prime*y_prime)%p;

        String msg = "S-C-PROD:"+product;
        
        return msg;
    }

    public static void reconstructTriple(){
        //Reconstruct Shares
        // System.out.println("");
        // System.out.println("Reconstruct Shares to reveal secret");
        double rshare0 = (double)(rshares.get(0));
        double rshare1 = (double)(rshares.get(1));

        double share0val = (double)a_shares[rshares.get(0)-1];
        double share1val = (double)a_shares[rshares.get(1)-1];

        a = (int) ((rshare0*share1val - (rshare1*share0val))/(rshare0 - rshare1)) %p;

        rshare0 = (double)(rshares.get(0));
        rshare1 = (double)(rshares.get(1));

        share0val = (double)b_shares[rshares.get(0)-1];
        share1val = (double)b_shares[rshares.get(1)-1];

        b = (int) ((rshare0*share1val - (rshare1*share0val))/(rshare0 - rshare1)) %p;        

        c = (a*b)%p;
    }

    public static ArrayList<Integer> partiesToReconstruct(){
        System.out.println("Reconstruct Shares to reveal secret");
        Random rand = new Random();
        Set<Integer> rshares_set = new LinkedHashSet<Integer>();
        while(rshares_set.size()<=t){
            rshares_set.add(rand.nextInt(n)+1);
        }
        ArrayList<Integer> rshares = new ArrayList<Integer>(rshares_set);
        System.out.println("Shares selected for reconstruction: "+rshares.toString());
        return rshares;
    }

    public static void processTriple(){
        c_coef = new int[t+1];

        c_coef[0] = c;
        Random rand = new Random();
        for(int i=1; i<t+1; i++){
            c_coef[i] = rand.nextInt(p-1)+1; 
        }
        System.out.println("Generated Polynomial of Triple");

        //Generate Shares of Triples
        c_shares = new int[n];
        for(int party=0; party<n; party++){
            c_shares[party] = 0;

            int big_i = party+1;

            for(int j = 0; j<t+1; j++){
                c_shares[party] = c_shares[party] +  c_coef[j]*((int)Math.pow(big_i,j));
            }
            c_shares[party] = c_shares[party]%p; 
        }
        System.out.println("Shares of c: ");
        for(int party=0; party<n; party++){
            System.out.print(c_shares[party]);
            System.out.print(" ");
        }
        System.out.println(" ");

        System.out.println("Processed Triple");
    }

    public static String getShares(int party){
        x_prime = x-a;
        y_prime = y-b;
        System.out.println("Getting shares for Party #"+party);
        int i = party-1;
        return "S-C-SHARES:"+c_shares[i]+","+shares_sec[0][i]+","+shares_sec[1][i]+","+x_prime+","+y_prime;
    }

    public static void logDiffs(String msg, int party){
        int i = party-1;
        diff[0][i] = Integer.parseInt((msg.split(":")[1]).split(",")[0])%p;
        diff[1][i] = Integer.parseInt((msg.split(":")[1]).split(",")[1])%p;
        System.out.println("Logged diffs from Party #"+i);
    }

    public static void logZShares(String msg, int party){
        int i = party-1;
        zshares[i] = Integer.parseInt((msg.split(":")[1]).split(",")[0])%p;
        System.out.println("Logged Z-Share from Party #"+i);
    }

    private static class Party implements Runnable {
        private Socket socket;
        int party;
        private int state = 0;

        Party(Socket socket, int party) {
            System.out.println("Beginning constructor");
            this.socket = socket;
            this.party = party;
            System.out.println("Finishing constructor");
        }

        @Override
        public void run() {
            System.out.println("Connected: " + socket);
            try{
                var in = new Scanner(socket.getInputStream());
                var out = new PrintWriter(socket.getOutputStream(), true);
                out.println("S-C-SEND-SHARES-AB");
                String m = in.nextLine();
                if(m.startsWith("C-S-SHARES-AB:")){
                    a_shares[party-1] = Integer.parseInt((m.split(":")[1]).split(",")[0]);
                    b_shares[party-1] = Integer.parseInt((m.split(":")[1]).split(",")[0]);
                    total_ab_shares++;
                    if(total_ab_shares == n){
                        reconstructTriple();
                        processTriple();
                        for(Socket s: parties){
                            var o_s = new PrintWriter(s.getOutputStream(), true);
                            o_s.println(getShares(party));
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error collecting shares of A and B:" + socket);
                System.out.println(e.toString());
                System.out.println(e.getStackTrace()[0].getLineNumber());
            } try{
                var in = new Scanner(socket.getInputStream());
                var out = new PrintWriter(socket.getOutputStream(), true);
                String m = in.nextLine();
                if(m.startsWith("C-S-Z-SHARE:")){
                    logZShares(m,party);
                    total_zshares++;
                    if(total_zshares==n){
                        System.out.println("Sending Product...");
                        for(Socket s: parties){
                            var o_s = new PrintWriter(s.getOutputStream(), true);
                            String msg = reconstructProduct();
                            o_s.println(msg);
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Collecting Z-Shares: Error:" + socket);
                System.out.println(e.toString());
                System.out.println(e.getStackTrace()[0].getLineNumber());
            } 
             finally {
                try {
                    var in = new Scanner(socket.getInputStream());
                    var out = new PrintWriter(socket.getOutputStream(), true);
                    String m = in.nextLine();
                    if(m.startsWith("DONE")){
                        socket.close();
                    }
                } catch (IOException e) {
                    System.out.println("Closing Socket: Error:" + socket);
                    System.out.println(e.toString());
                    System.out.println(e.getStackTrace()[0].getLineNumber());
                }
            }
        }
    }
}