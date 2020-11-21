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
    public static int total_shares = 0;
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

    public static void setup(int x, int y){
        Random rand = new Random();
        int[] secrets = {x, y};
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
        // System.out.println("Secret shares: ");
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
                // System.out.print(shares_sec[i][party]);
                // System.out.print(" ");
            }
            // System.out.println(" ");
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
            triple_index = rand.nextInt(1000000);
            int count = 0;
            while (true) {
                Socket s = listener.accept();
                parties.add(s);
                if(count == 0){
                    pool.execute(new Dealer(s));
                    count++;    
                } else {
                    pool.execute(new Party(s, parties.size()));
                }
                
            }
        }
    }

    public static String reconstruct(){
        BigDecimal[] tShares = new BigDecimal[t+1];
        ArrayList<String> t_parties = new ArrayList<String>(t+1);
        ArrayList<String> tShares_string = new ArrayList<String>(returnedShares);
        for(int i=0; i<t+1; i++){
            t_parties.add(tShares_string.get(i).split(",",2)[0]);
            tShares[i] = new BigDecimal(tShares_string.get(i).split(",",2)[1]);
        }

        BigDecimal rshare0 = new BigDecimal(t_parties.get(0));
        BigDecimal rshare1 = new BigDecimal(t_parties.get(1));
        BigDecimal rshare2 = new BigDecimal(t_parties.get(2));

        MathContext mc = new MathContext(10);
        BigDecimal[] lb = new BigDecimal[t+1];
        lb[0] = (tShares[0].multiply((rshare1.multiply(rshare2)))).divide((rshare0.subtract(rshare1)).multiply((rshare0.subtract(rshare2))), mc);
        lb[1] = (tShares[1].multiply((rshare0.multiply(rshare2)))).divide((rshare1.subtract(rshare0)).multiply((rshare1.subtract(rshare2))), mc);
        lb[2] = (tShares[2].multiply((rshare1.multiply(rshare0)))).divide((rshare2.subtract(rshare1)).multiply((rshare2.subtract(rshare0))), mc);

        BigInteger recoveredSecret = (lb[0].add(lb[1].add(lb[2]))).toBigInteger();//.mod(p);
        return recoveredSecret.toString();
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

    public static void processTriple(String msg){
        a = Integer.parseInt((msg.split(":")[1]).split(",")[0]);
        b = Integer.parseInt((msg.split(":")[1]).split(",")[1]);
        c = Integer.parseInt((msg.split(":")[1]).split(",")[2]);

        a_coef = new int[t+1];
        b_coef = new int[t+1];
        c_coef = new int[t+1];

        a_coef[0] = a;
        b_coef[0] = b;
        c_coef[0] = c;
        Random rand = new Random();
        for(int i=1; i<t+1; i++){
            a_coef[i] = rand.nextInt(p-1)+1; 
            b_coef[i] = rand.nextInt(p-1)+1; 
            c_coef[i] = rand.nextInt(p-1)+1; 
        }
        System.out.println("Generated Polynomial of Triple");

        //Generate Shares of Triples
        int[] a_shares = new int[n];
        int[] b_shares = new int[n];
        int[] c_shares = new int[n];
        for(int party=0; party<n; party++){
            a_shares[party] = 0;
            b_shares[party] = 0;
            c_shares[party] = 0;

            int big_i = party+1;

            for(int j = 0; j<t+1; j++){
                a_shares[party] = a_shares[party] +  a_coef[j]*((int)Math.pow(big_i,j));
                b_shares[party] = b_shares[party] +  b_coef[j]*((int)Math.pow(big_i,j));
                c_shares[party] = c_shares[party] +  c_coef[j]*((int)Math.pow(big_i,j));
            }

            a_shares[party] = a_shares[party]%p;
            b_shares[party] = b_shares[party]%p;
            c_shares[party] = c_shares[party]%p; 
        }
        System.out.println("Generated Shares of Triple");

        System.out.println("Processed Triple");
    }

    public static String getShares(int party){
        return "S-C-SHARES:"+a_shares[party]+","+b_shares[party]+","+c_shares[party]+","+shares_sec[0][party]+","+shares_sec[1][party];
    }

    private static class Dealer implements Runnable {
        private Socket socket;

        Dealer(Socket socket) {
            System.out.println("Beginning constructor");
            this.socket = socket;
            System.out.println("Finishing constructor");
        }

        @Override
        public void run(){
             try {
                var in = new Scanner(socket.getInputStream());
                var out = new PrintWriter(socket.getOutputStream(), true);
                
                out.println("S-D-REQ-TRIPLE:"+triple_index);
                String m = in.nextLine();
                if(m.startsWith("D-S-RET-TRIPLE:")){
                    System.out.println("Received: "+m);
                    processTriple(m);
                    socket.close();
                }
            } catch (Exception e) {
                System.out.println("GET TRIPLE Error:" + socket);
                System.out.println(e.toString());
                System.out.println(e.getStackTrace()[0].getLineNumber());
            } 
        }
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
                String m = in.nextLine();
                if(m.startsWith("C-S-READY")){
                    System.out.println("Received: "+m);
                    String all_shares = getShares(party);
                    out.println(all_shares);
                }

            } catch (Exception e) {
                System.out.println("part 2 Error:" + socket);
                System.out.println(e.toString());
                System.out.println(e.getStackTrace()[0].getLineNumber());
            }
            // try{
            //     var in = new Scanner(socket.getInputStream());
            //     var out = new PrintWriter(socket.getOutputStream(), true);
            //     //pick random t parties
            //     // while(state==1){
            //     int i=0;
            //     boolean selected = false;
            //     while(i<rshares.size()){
            //         if(rshares.get(i)==party){
            //             selected = true;
            //         }
            //         i++;
            //     }
            //     System.out.println("Shares needed to reconstruct: "+rshares);
            //     if(!selected){
            //         out.println("Your share is not needed for reconstruction. Please press CTRL+D. Goodbye");
            //         closed_count++;
            //         socket.close();
            //         return;
            //     }
            //     else{
            //         out.println("Send your share to reconstruct the secret");    
            //         returnedShares.add(in.nextLine());
            //         closed_count++;
            //         // return;
            //     }
                
            //     //get their shares
            // }  catch (Exception e) {
            //     System.out.println("Error:" + socket);
            //     System.out.println(e.toString());
            //     System.out.println(e.getStackTrace()[0].getLineNumber());
            // }  
             finally {
                try {
                    socket.close();
                } catch (IOException e) {
                }
                if(closed_count == n){
                    String recoveredSecret = reconstruct();
                    System.out.println("Recovered Secret: "+recoveredSecret);
                }
            }
        }
    }
}