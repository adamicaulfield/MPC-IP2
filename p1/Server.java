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
    public static BigInteger[] coefs;
    public static int t;
    public static int n;
    public static BigInteger p;
    public static int total_shares = 0;
    public static int closed_count = 0;

    public static void setupPolynomial(String secret, int n, String modulus){
        System.out.println("Beginning setupPolynomial()");
        t = n/2;
        p = new BigInteger(modulus);

        Random rand = new Random();
        BigInteger maxLimit = new BigInteger("10"); //p-1
        BigInteger minLimit = new BigInteger("1");
        BigInteger bigInteger = maxLimit.subtract(minLimit);
        int length = maxLimit.bitLength();
        BigInteger res;

        coefs = new BigInteger[t+1];
        for(int i=0; i<t+1; i++){
            if(i==0){
                coefs[i] = new BigInteger(secret);
            }
            else{
                res = new BigInteger(length, rand);
                if (res.compareTo(minLimit) < 0)
                    res = res.add(minLimit);
                if (res.compareTo(bigInteger) > 0)
                    res = res.mod(bigInteger).add(minLimit);

                res = res.mod(p);
                coefs[i] = res;   
            }
        }
        System.out.println("Completed setupPolynomial()");
    }

    public static String generateShare(int i){
        System.out.println("Beginning generateShare()");
        //Generate Share for party i
        BigInteger big_i = new BigInteger(Integer.toString(i));
        BigInteger share = new BigInteger("0");
        for(int j=0; j<t+1; j++){
            BigInteger x_i = big_i.pow(j);
            if(j==0){
                share = share.add(coefs[j]);
            }
            else{
                share = share.add(coefs[j].multiply(x_i));    
            }
        }
        System.out.println("Completing generateShare()");
        return (share.mod(p)).toString();
    }

    public static void main(String[] args) throws Exception {
        try (var listener = new ServerSocket(59898)) {
            System.out.println("The server is running...");
            var pool = Executors.newFixedThreadPool(20);
            n = 5;
            setupPolynomial("3", n, "11");
            rshares = partiesToReconstruct();
            for(int i=0; i<t+1; i++){
                System.out.println(coefs[i]);
            }

            while (true) {
                Socket s = listener.accept();
                parties.add(s);
                pool.execute(new Party(s, parties.size()));
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

        BigInteger recoveredSecret = (lb[0].add(lb[1].add(lb[2]))).toBigInteger().mod(p);
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
            try {
                var in = new Scanner(socket.getInputStream());
                var out = new PrintWriter(socket.getOutputStream(), true);
                String returnshare = "SHARE:"+party+","+generateShare(party);
                out.println(returnshare);
                total_shares++;
                if(total_shares==n){
                	int count = 1;
                	System.out.println("Shares needed to reconstruct: "+rshares);
                	for(Socket s: parties){
                		var o_s = new PrintWriter(s.getOutputStream(),true);
                		boolean selected = false;
                		int i=0;
                		while(i<rshares.size()){
                		    if(rshares.get(i)==count){
                		        selected = true;
                		    }
                		    i++;
                		}
                		if(selected){
                			o_s.println("SENDSHARE");
                		}
                		else{
                			o_s.println("GOODBYE");
                		}
                		count++;
                	}
                }
            } catch (Exception e) {
                System.out.println("Error:" + socket);
                System.out.println(e.toString());
                System.out.println(e.getStackTrace()[0].getLineNumber());
            }
            try{
                var in = new Scanner(socket.getInputStream());
                var out = new PrintWriter(socket.getOutputStream(), true);
                String m = in.nextLine();
                if(m.startsWith("GOODBYE")){
					closed_count++;
                    socket.close();
                    return;
                }
                else if(m.startsWith("RETSHARE:")){
                    String shareMsg = m.split(":")[1];   
                    returnedShares.add(shareMsg);
                    closed_count++;
                    if(closed_count == n){
                        String recoveredSecret = reconstruct();
                        System.out.println("Recovered Secret: "+recoveredSecret);
                    }
                }
                
                //get their shares
            }  catch (Exception e) {
                System.out.println("Error:" + socket);
                System.out.println(e.toString());
                System.out.println(e.getStackTrace()[0].getLineNumber());
            }  finally {
                try {
                	var out = new PrintWriter(socket.getOutputStream(), true);
                	out.println("GOODBYE");
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}