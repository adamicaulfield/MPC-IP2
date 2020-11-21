
import java.io.*;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Random;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;

public class Prob1 {

    public static void main(String args[]) {

        String secret = "3";
        int n = 5;
        int t = n/2;
        // BigInteger p = new BigInteger("499999999699"); //largest prime less than 500000000000
        BigInteger p = new BigInteger("11"); //largest prime less than 500000000000
        //random num generation for BigInteger inspired from from https://www.tutorialspoint.com/how-to-generate-a-random-biginteger-value-in-java
        Random rand = new Random();

        //Field Z_(p-1)
        // BigInteger maxLimit = new BigInteger("499999999698"); //p-1
        BigInteger maxLimit = new BigInteger("10"); //p-1
        BigInteger minLimit = new BigInteger("1");
        BigInteger bigInteger = maxLimit.subtract(minLimit);
        int length = maxLimit.bitLength();
        BigInteger res;


        BigInteger[] coefs = new BigInteger[t+1];
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

        // for(int i=0; i<t+1; i++){
           // System.out.print(coefs[i]);
           // System.out.print(", ");
        // }
        // System.out.println();

        //Generate Shares 
        // System.out.println("Generate Secret Shares");
        BigInteger[] shares_int = new BigInteger[n];
        shares_int[0] = new BigInteger("0");
        shares_int[1] = new BigInteger("0");
        shares_int[2] = new BigInteger("0");
        shares_int[3] = new BigInteger("0");
        shares_int[4] = new BigInteger("0");
        for(int i=1; i<=n; i++){
            BigInteger big_i = new BigInteger(Integer.toString(i));
            for(int j=0; j<t+1; j++){
                // BigInteger big_j = new BigInteger(Integer.toString(j));
                // System.out.println(i+", "+j+", "+(i^j)+", "+x_i);
                // System.out.print(shares[i-1]);
                BigInteger x_i = big_i.pow(j);
                if(j==0){
                    shares_int[i-1] = shares_int[i-1].add(coefs[j]);
                }
                else{
                    shares_int[i-1] = shares_int[i-1].add( coefs[j].multiply(x_i));    
                }
                // System.out.print("+"+coefs[j].toString()+"*"+x_i+"= ");
                // System.out.print(shares[i-1]);
                // System.out.println(" ");
            }
            System.out.print("share: ");
            // System.out.print(i);
            System.out.print("\t before mod: ");
            System.out.print(shares_int[i-1]);
            shares_int[i-1] = shares_int[i-1].mod(p);
            // System.out.println("");
            System.out.print("\t result: ");
            System.out.println(shares_int[i-1]);
        }

        BigDecimal[] shares = new BigDecimal[n];
        for(int i=0; i<n; i++){
            shares[i] = new BigDecimal(shares_int[i]);
        }

        //Reconstruct Shares
        System.out.println("Reconstruct Shares to reveal secret");
        Set<Integer> rshares_set = new LinkedHashSet<Integer>();
        while(rshares_set.size()<=t){
            rshares_set.add(rand.nextInt(n)+1);
        }
        List<Integer> rshares = new ArrayList<Integer>(rshares_set);

        System.out.println("Shares selected for reconstruction: "+rshares.toString());
        BigDecimal[] lb = new BigDecimal[t+1];

        BigDecimal rshare0 = new BigDecimal(Integer.toString(rshares.get(0)));
        BigDecimal rshare1 = new BigDecimal(Integer.toString(rshares.get(1)));
        BigDecimal rshare2 = new BigDecimal(Integer.toString(rshares.get(2)));

        System.out.println(rshare0+", "+shares[rshares.get(0)-1]);
        System.out.println(rshare1+", "+shares[rshares.get(1)-1]);
        System.out.println(rshare2+", "+shares[rshares.get(2)-1]);

        MathContext mc = new MathContext(10);
        lb[0] = (shares[rshares.get(0)-1].multiply((rshare1.multiply(rshare2)))).divide((rshare0.subtract(rshare1)).multiply((rshare0.subtract(rshare2))), mc);
        lb[1] = (shares[rshares.get(1)-1].multiply((rshare0.multiply(rshare2)))).divide((rshare1.subtract(rshare0)).multiply((rshare1.subtract(rshare2))), mc);
        lb[2] = (shares[rshares.get(2)-1].multiply((rshare1.multiply(rshare0)))).divide((rshare2.subtract(rshare1)).multiply((rshare2.subtract(rshare0))), mc);

        BigInteger recoveredSecret = (lb[0].add(lb[1].add(lb[2]))).toBigInteger().mod(p);
        
        System.out.println("Lagrange Basis Results:");
        System.out.println("0\t "+lb[0].toString()+"= ("+(shares[rshares.get(0)-1]).toString()+" x ("+rshare1.toString()+"x"+rshare2.toString()+")) / (("+rshare0.toString()+"-"+rshare1.toString()+") x ("+rshare0.toString()+"-"+rshare2.toString()+"))");
        System.out.println("1\t "+lb[1].toString()+"= ("+(shares[rshares.get(1)-1]).toString()+" x ("+rshare0.toString()+"x"+rshare2.toString()+")) / (("+rshare1.toString()+"-"+rshare0.toString()+") x ("+rshare1.toString()+"-"+rshare2.toString()+"))");
        System.out.println("2\t "+lb[2].toString()+"= ("+(shares[rshares.get(2)-1]).toString()+" x ("+rshare1.toString()+"x"+rshare0.toString()+")) / (("+rshare2.toString()+"-"+rshare1.toString()+") x ("+rshare2.toString()+"-"+rshare0.toString()+"))");

        System.out.println("Recovered secret:");
        System.out.println(recoveredSecret.toString()+"="+lb[0].toString()+" "+lb[1].toString()+" "+lb[2].toString());

        // System.out.println(recoveredSecret.toString());
    }
}

