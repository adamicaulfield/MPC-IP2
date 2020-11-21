
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

public class Prob3 {

    public static void main(String args[]) {
        //Parameters
        int num_triples = 1000000;
        int p = 11;
        
        //Get list of triples
        ArrayList<Integer> alist = new ArrayList<Integer>(num_triples);
        ArrayList<Integer> blist = new ArrayList<Integer>(num_triples);
        Random rand = new Random();
        for(int i=0; i<1000000; i++){
            alist.add(rand.nextInt(p));
            blist.add(rand.nextInt(p));
        }

        //Select one set of triples, generate c
        int r = rand.nextInt(num_triples);
        int a = alist.get(r);
        int b = blist.get(r);
        int c = (a*b)%p;
        System.out.println("Beaver triple: a="+a+", b="+b+", c="+c);
        System.out.println(" ");
        //Get polynomial of triples
        int n = 3;
        int t = n/2;
        int[] a_coef = new int[t+1];
        int[] b_coef = new int[t+1];
        int[] c_coef = new int[t+1];
        a_coef[0] = a;
        b_coef[0] = b;
        c_coef[0] = c;
        for(int i=1; i<t+1; i++){
            a_coef[i] = rand.nextInt(p-1)+1; 
            b_coef[i] = rand.nextInt(p-1)+1; 
            c_coef[i] = rand.nextInt(p-1)+1; 
        }

        System.out.println("Polynomial of a: ");
        for(int i=0; i<t+1; i++){
            System.out.print(a_coef[i]);
            System.out.print(" ");
        }
        System.out.println();
        System.out.println(" ");
        System.out.println("Polynomial of b: ");
        for(int i=0; i<t+1; i++){
            System.out.print(b_coef[i]);
            System.out.print(" ");
        }
        System.out.println();
        System.out.println(" ");
        System.out.println("Polynomial of c: ");
        for(int i=0; i<t+1; i++){
            System.out.print(c_coef[i]);
            System.out.print(" ");
        }
        System.out.println();
        System.out.println(" ");
        //Assume 2 secret data, shared amongst 5 parties
        int x = 2;
        int y = 3; //expected product = 450;
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

        System.out.println("Shares of a: ");
        for(int party=0; party<n; party++){
            System.out.print(a_shares[party]);
            System.out.print(" ");
        }
        System.out.println(" ");
        System.out.println(" ");
        System.out.println("Shares of b: ");
        for(int party=0; party<n; party++){
            System.out.print(b_shares[party]);
            System.out.print(" ");
        }
        System.out.println(" ");
        System.out.println(" ");
        System.out.println("Shares of c: ");
        for(int party=0; party<n; party++){
            System.out.print(c_shares[party]);
            System.out.print(" ");
        }
        System.out.println(" ");

        // Generate Shares of data
        int[][] shares_sec = new int[2][n];
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

        System.out.println("Secret shares");
        for(int i=0; i<2; i++){
            for(int j=0; j<n; j++){
                System.out.print(shares_sec[i][j]);
                System.out.print(" ");
            }
            System.out.println(" ");
        }
        System.out.println(" ");

        System.out.println("Compute x' and y' ");
        int[][] diff = new int[2][n];
        for(int i=0; i<n; i++){
            diff[0][i] = (shares_sec[0][i]-a_shares[i])%p;
            diff[1][i] = (shares_sec[1][i]-b_shares[i])%p;
        }

        System.out.println("Difference of shares");
        for(int i=0; i<2; i++){
            for(int j=0; j<n; j++){
                System.out.print(diff[i][j]);
                System.out.print(" ");
            }
            System.out.println(" ");
        }
        System.out.println(" ");

        int x_prime = reconstruct(diff[0], p, n, t);
        System.out.println("Reconstructed x\'="+x_prime);

        int y_prime = reconstruct(diff[1], p, n, t);
        System.out.println("Reconstructed y\'="+y_prime);

        //Use shares along with x' and y' to construct the product
        int z = 0;
        // x_prime = x-a;
        // y_prime = y-b;
        for(int i=0; i<t+1; i++){
            z = z + c_shares[i]+x_prime*b_shares[i]+y_prime*a_shares[i];
        }
        z = (z + x_prime+y_prime)%p;
        System.out.println("Product: "+z+" = "+x+"*"+y);
    }

    public static int reconstruct(int shares[], int p, int n, int t){
        //Reconstruct Shares
        // System.out.println("");
        // System.out.println("Reconstruct Shares to reveal secret");
        Random rand = new Random();
        Set<Integer> rshares_set = new LinkedHashSet<Integer>();
        while(rshares_set.size()<=t){
            rshares_set.add(rand.nextInt(n)+1);
        }
        List<Integer> rshares = new ArrayList<Integer>(rshares_set);
        System.out.println("");
        System.out.println("Shares selected for reconstruction: "+rshares.toString());

        double rshare0 = (double)(rshares.get(0));
        double rshare1 = (double)(rshares.get(1));

        double share0val = (double)shares[rshares.get(0)-1];
        double share1val = (double)shares[rshares.get(1)-1];

        int recoveredSecret = (int) ((rshare0*share1val - (rshare1*share0val))/(rshare0 - rshare1)) %p;
        System.out.println(recoveredSecret+"="+"(("+rshare0+"*"+share1val+") - ("+rshare1+"*"+share0val+"))/("+rshare0+" - "+rshare1+")) %"+p);

        return recoveredSecret;
    }
}

