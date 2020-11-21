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

    public static ArrayList<String> secrets = new ArrayList<String>();
    public static ArrayList<Socket> party_sockets = new ArrayList<Socket>();
    public static ArrayList<Party> parties = new ArrayList<Party>();
    public static ArrayList<Integer> rshares = new ArrayList<Integer>();
    public static Set<String> returnedShares = new LinkedHashSet<String>();
    public static BigInteger[][] coefs;
    public static int t;
    public static int n;
    public static BigInteger p;
    public static int total_shares = 0;
    public static int closed_count = 0;
    public static int lastParty = 0;

    public static void setupPolynomial(int n, String modulus){
        System.out.println("Beginning setupPolynomial()");
        t = n/2;
        p = new BigInteger(modulus);

        Random rand = new Random();
        BigInteger maxLimit = new BigInteger("30"); //p-1
        BigInteger minLimit = new BigInteger("1");
        BigInteger bigInteger = maxLimit.subtract(minLimit);
        int length = maxLimit.bitLength();
        BigInteger res;

        coefs = new BigInteger[n][t+1];
        // for(int s=0; s<secrets.size(); s++){
            for(int i=0; i<n; i++){
                for(int j=0; j<t+1; j++){
                    if(j==0){
                        coefs[i][j] = new BigInteger(secrets.get(i));
                    }
                    else{
                        res = new BigInteger(length, rand);
                        if (res.compareTo(minLimit) < 0)
                            res = res.add(minLimit);
                        if (res.compareTo(bigInteger) > 0)
                            res = res.mod(bigInteger).add(minLimit);

                        res = res.mod(p);
                        coefs[i][j] = res;   
                    }
                }
            }
        // }
        System.out.println("Completed setupPolynomial()");
    }

    public static String generateShare(int party){
        System.out.println("Beginning generateShare()");
        //Generate Share for party i
        BigInteger big_i = new BigInteger(Integer.toString(party));
        BigInteger[] share = new BigInteger[n];
        share[0] = new BigInteger("0");
        share[1] = new BigInteger("0");
        share[2] = new BigInteger("0");
        share[3] = new BigInteger("0");
        share[4] = new BigInteger("0");
        String returnString = "";
        for(int i=0; i<n; i++){
            for(int j=0; j<t+1; j++){
                BigInteger x_i = big_i.pow(j);
                if(j==0){
                    share[i] = share[i].add(coefs[i][j]);
                }
                else{
                    share[i] = share[i].add(coefs[i][j].multiply(x_i));    
                }
            }
            share[i] = share[i].mod(p);
            returnString = returnString+share[i].toString();
            if(i!=n-1){
                returnString = returnString+",";
            }            
        }
        System.out.println("Completed generateShare()");
        return returnString;
    }

    public static void main(String[] args) throws Exception {
        try (var listener = new ServerSocket(59898)) {
            System.out.println("The server is running...");
            var pool = Executors.newFixedThreadPool(20);
            n = 5;

            while (true) {
                Socket s = listener.accept();
                party_sockets.add(s);
                Party p = new Party(s, party_sockets.size());
                parties.add(p);
                pool.execute(p);
            }
        }
    }

    public static String reconstruct(){
        BigDecimal[] tShares = new BigDecimal[t+1];
        ArrayList<String> t_parties = new ArrayList<String>(t+1);
        ArrayList<String> tShares_string = new ArrayList<String>(returnedShares);
        for(int i=0; i<t+1; i++){
            t_parties.add((tShares_string.get(i).split(":")[0]).split("-")[1]);
            tShares[i] = new BigDecimal(new BigInteger(tShares_string.get(i).split(":")[1]).mod(p));
        }

        BigDecimal rshare0 = new BigDecimal(t_parties.get(0));
        BigDecimal rshare1 = new BigDecimal(t_parties.get(1));
        BigDecimal rshare2 = new BigDecimal(t_parties.get(2));

        MathContext mc = new MathContext(10);
        BigDecimal[] lb = new BigDecimal[t+1];
        lb[0] = (tShares[0].multiply((rshare1.multiply(rshare2)))).divide((rshare0.subtract(rshare1)).multiply((rshare0.subtract(rshare2))), mc);
        lb[1] = (tShares[1].multiply((rshare0.multiply(rshare2)))).divide((rshare1.subtract(rshare0)).multiply((rshare1.subtract(rshare2))), mc);
        lb[2] = (tShares[2].multiply((rshare1.multiply(rshare0)))).divide((rshare2.subtract(rshare1)).multiply((rshare2.subtract(rshare0))), mc);
        BigInteger recoveredSecret = (lb[0].add(lb[1].add(lb[2]))).toBigInteger().mod(p);

        System.out.println("");
        System.out.println("Lagrange Basis Results:");
        System.out.println("0\t "+lb[0].toString()+"= ("+(tShares[0]).toString()+" x ("+rshare1.toString()+"x"+rshare2.toString()+")) / (("+rshare0.toString()+"-"+rshare1.toString()+") x ("+rshare0.toString()+"-"+rshare2.toString()+"))");
        System.out.println("1\t "+lb[1].toString()+"= ("+(tShares[1]).toString()+" x ("+rshare0.toString()+"x"+rshare2.toString()+")) / (("+rshare1.toString()+"-"+rshare0.toString()+") x ("+rshare1.toString()+"-"+rshare2.toString()+"))");
        System.out.println("2\t "+lb[2].toString()+"= ("+(tShares[2]).toString()+" x ("+rshare1.toString()+"x"+rshare0.toString()+")) / (("+rshare2.toString()+"-"+rshare1.toString()+") x ("+rshare2.toString()+"-"+rshare0.toString()+"))");
        System.out.println("");
        System.out.println("Sum of Secrets:");
        System.out.println(recoveredSecret.toString()+"="+lb[0].toString()+" "+lb[1].toString()+" "+lb[2].toString());

        
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

    public static void triggerPartTwo(){
        System.out.println("Secrets: "+secrets.toString());
        setupPolynomial(n, "31");
        System.out.println("Polynomials: ");
        for(int i=0; i<n; i++){
            System.out.print("#"+(i+1)+": ");
            for(int j=0; j<t+1; j++){
                System.out.print(coefs[i][j]);
                System.out.print(" ");  
            }
            System.out.println("");
        }

        rshares = partiesToReconstruct();

        for(int i=0; i<parties.size(); i++){
            String returnshare = "SHARES-"+(i+1)+":"+generateShare(i+1);
           try{
               var out = new PrintWriter((party_sockets.get(i)).getOutputStream(), true);
               out.println(returnshare);
           } catch(Exception e) {
                System.out.println(e);
           }
        }

    }

    private static class Party implements Runnable {
        private Socket socket;
        int party;
        private int state=0;
        public boolean allSecretsCollected = false;

        Party(Socket socket, int party) {
            System.out.println("Beginning constructor");
            this.socket = socket;
            this.party = party;
            System.out.println("Finishing constructor");
        }

        @Override
        public void run() {
            System.out.println("Connected: " + socket);
            try {
                var in = new Scanner(socket.getInputStream());
                var out = new PrintWriter(socket.getOutputStream(), true);
                System.out.println("Party #"+party+" has entered while state=0 loop");
                while(state==0){
                    out.println("Enter \"SECRET:\" followed by your integer");
                    if(in.hasNextLine()){
                        String secret_i = in.nextLine();
                        // synchronized(secrets){
                        if(secret_i.startsWith("SECRET:")){
                            secrets.add(secret_i.split(":",2)[1]);
                            out.println("RECEIVED SECRET");
                            System.out.println("Secrets size: "+secrets.size()+", n: "+n);
                            if(secrets.size()==n){
                                triggerPartTwo();
                            }
                            state++;
                            break;  
                        }
                    } else{
                        out.println("STATE="+state);
                    }
                }
                System.out.println("Party #"+party+" has exited while state=0 loop");
            } catch (Exception e) {
                System.out.println("PHASE 1 Error:" +party+ ":" + socket);
                System.out.println(e.toString());
                System.out.println("Server.java line:"+e.getStackTrace()[0].getLineNumber());
            } try {
                var in = new Scanner(socket.getInputStream());
                var out = new PrintWriter(socket.getOutputStream(), true);
                System.out.println("Party #"+party+" is ready for part 2");
                String m = in.nextLine();
                while(!(m.startsWith("SUM"))){
                    System.out.println("Received from "+party+": "+m);
                    m = in.nextLine();
                }
                System.out.println("Party #"+party+" is continuing to part 2");

                int i=0;
                boolean selected = false;
                
                while(i<rshares.size()){
                    if(rshares.get(i)==party){
                        selected = true;
                        lastParty = rshares.get(i);
                    }
                    i++;
                }
                System.out.println("Shares needed to reconstruct: "+rshares);
                if(selected){
                    // out.println("Send your share to reconstruct the secret");    
                    returnedShares.add(m);
                    out.println("RECEIVED: \""+m+"\""); 
                    // closed_count++;
                    // return;
                }

            } catch (Exception e) {
                System.out.println("PHASE 2 Error:" +party+ ":" + socket);
                System.out.println(e.toString());
                System.out.println("Server.java line:"+e.getStackTrace()[0].getLineNumber());
            }



            try{
                var in = new Scanner(socket.getInputStream());
                var out = new PrintWriter(socket.getOutputStream(), true);
                if(party == lastParty){
                    String recoveredSecret = reconstruct();
                    for(int i=0; i<parties.size(); i++){
                       try{
                        System.out.println("Sending sum to "+i);
                           var out_i = new PrintWriter((party_sockets.get(i)).getOutputStream(), true);
                           out_i.println("FINISHED: Sum = "+recoveredSecret);
                           // party_sockets.get(i).close();
                       } catch(Exception e) {
                            System.out.println(e+" Server.java line:262-272");
                       }
                    }
                } else{
                    boolean wait = true;
                    while(wait){
                        String m = in.nextLine();
                        if(m.startsWith("ACK")){
                            wait = false;
                        }    
                    }
                    
                }
                
            }  catch (Exception e) {
                System.out.println("PHASE 3 Error:" + socket);
                System.out.println(e.toString());
                System.out.println(e.getStackTrace()[0].getLineNumber());
            }  
            // finally {
            //     try {
            //         System.out.println("Closing connection with "+party);
            //         socket.close();
            //     } catch (IOException e) {
            //         System.out.println("CLOSING Error: "+e.toString());
            //     }
            // }
        }
    }
}