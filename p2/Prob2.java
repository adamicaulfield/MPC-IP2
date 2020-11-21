
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

public class Prob2 {

    public static void main(String args[]) {

        String[] secrets = {"3","5","4","1","9"};
        int n = 5;
        int t = n/2;
        // BigInteger p = new BigInteger("499999999699"); //largest prime less than 500000000000
        BigInteger p = new BigInteger("23"); //largest prime less than 500000000000
        //random num generation for BigInteger inspired from from https://www.tutorialspoint.com/how-to-generate-a-random-biginteger-value-in-java
        Random rand = new Random();

        //Field Z_(p-1)
        // BigInteger maxLimit = new BigInteger("499999999698"); //p-1
        BigInteger maxLimit = new BigInteger("28"); //p-1
        BigInteger minLimit = new BigInteger("1");
        BigInteger bigInteger = maxLimit.subtract(minLimit);
        int length = maxLimit.bitLength();
        BigInteger res;


        BigInteger[][] coefs = new BigInteger[n][t+1];
        for(int i=0; i<n; i++){
            for(int j=0; j<t+1; j++){
            	if(j==0){
	                coefs[i][j] = new BigInteger(secrets[i]);
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

        System.out.println("Polynomials: ");
        for(int i=0; i<n; i++){
        	System.out.print("#"+(i+1)+": ");
            for(int j=0; j<t+1; j++){
        		System.out.print(coefs[i][j]);
            	System.out.print(" ");	
            }
            System.out.println("");
        }

        // Generate Shares 
        BigInteger[][] shares_int = new BigInteger[n][n];
        for(int party=0; party<n; party++){
	        
	        // System.out.println("Generate Secret Shares for Party #"+party);
	        shares_int[party][0] = new BigInteger("0");
	        shares_int[party][1] = new BigInteger("0");
	        shares_int[party][2] = new BigInteger("0");
	        shares_int[party][3] = new BigInteger("0");
	        shares_int[party][4] = new BigInteger("0");

	        BigInteger big_i = new BigInteger(Integer.toString(party+1));
	        for(int i=0; i<n; i++){
	            for(int j=0; j<t+1; j++){
	                BigInteger x_i = big_i.pow(j);


	                if(j==0){
	                    shares_int[party][i] = shares_int[party][i].add(coefs[i][j]);
	                }
	                else{
	                    shares_int[party][i] = shares_int[party][i].add( coefs[i][j].multiply(x_i));    
	                }
	            }
	            // System.out.print("share: ");
	            // System.out.print("\t before mod: ");
	            // System.out.print(shares_int[party][i]);
	            shares_int[party][i] = shares_int[party][i].mod(p);
	            // System.out.print("\t result: ");
	            // System.out.println(shares_int[party][i]);
	        }
	        System.out.println("");
	    }
        System.out.println("Shares held by each party");
        for(int party=0; party<n; party++){
        	System.out.print("#"+(party+1)+": ");
        	for(int i=0; i<n; i++){
				System.out.print(shares_int[party][i]);
		    	System.out.print(" ");	
        	}
        	System.out.println("");
        }
        BigInteger[] shares_summed = new BigInteger[n]; //Each party's sum of the share's they received for final result
        for(int party=0; party<n; party++){
        	shares_summed[party] = new BigInteger("0");
        	for(int i=0; i<n; i++){
        		shares_summed[party] = shares_summed[party].add(shares_int[party][i]);
        	}
        }
        System.out.println("");
        System.out.println("Each party takes sum (mod p) of their shares: ");
        for(int party=0; party<n; party++){
        	shares_summed[party] = shares_summed[party].mod(p);
        	System.out.println("#"+(party+1)+": "+shares_summed[party]);
        }

        BigDecimal[] shares = new BigDecimal[n];
        for(int party=0; party<n; party++){
        	shares[party] = new BigDecimal(shares_summed[party]);
        }

        //Reconstruct Shares
        System.out.println("");
        System.out.println("Reconstruct Shares to reveal secret");
        Set<Integer> rshares_set = new LinkedHashSet<Integer>();
        while(rshares_set.size()<=t){
            rshares_set.add(rand.nextInt(n)+1);
        }
        List<Integer> rshares = new ArrayList<Integer>(rshares_set);
        System.out.println("");
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
        System.out.println("");
        System.out.println("Lagrange Basis Results:");
        System.out.println("0\t "+lb[0].toString()+"= ("+(shares[rshares.get(0)-1]).toString()+" x ("+rshare1.toString()+"x"+rshare2.toString()+")) / (("+rshare0.toString()+"-"+rshare1.toString()+") x ("+rshare0.toString()+"-"+rshare2.toString()+"))");
        System.out.println("1\t "+lb[1].toString()+"= ("+(shares[rshares.get(1)-1]).toString()+" x ("+rshare0.toString()+"x"+rshare2.toString()+")) / (("+rshare1.toString()+"-"+rshare0.toString()+") x ("+rshare1.toString()+"-"+rshare2.toString()+"))");
        System.out.println("2\t "+lb[2].toString()+"= ("+(shares[rshares.get(2)-1]).toString()+" x ("+rshare1.toString()+"x"+rshare0.toString()+")) / (("+rshare2.toString()+"-"+rshare1.toString()+") x ("+rshare2.toString()+"-"+rshare0.toString()+"))");
        System.out.println("");
        System.out.println("Sum of Secrets:");
        System.out.println(recoveredSecret.toString()+"="+lb[0].toString()+" "+lb[1].toString()+" "+lb[2].toString());

        System.out.println(recoveredSecret.toString());
    }
}

