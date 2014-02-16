package org.streaminer.util.distribution;

import java.util.Random;

/**
 *
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class ZipfDistribution {
    private double theta;
    private double alpha;
    private double eta;
    private double zetan;
    private long n;
    private Random random = new Random();
    
    public ZipfDistribution(double theta, long n) {
        this(theta, n, n);
    }
    
    public ZipfDistribution(double theta, long n, long length) {
        this.theta = theta;
        this.n = n;
        
        alpha = 1. / (1. - theta);
        zetan = zeta(length, theta);
	eta = (1. - Math.pow(2.0/n, 1.0 - theta)) / (1.0 - zeta(2, theta)/zetan);
    }
    
    public double nextDouble() {
        double val;
        double u = random.nextDouble();
	double uz = u * zetan;
        
	if (uz < 1) val = 1;
	else if (uz < (1. + Math.pow(0.5, theta))) val = 2;
	else val = 1 + (long)(n * Math.pow(eta*u - eta + 1.0, alpha));

	return val;
    }
    
    public static double zeta(long n, double theta)  {
        double ans=0.0;

        for (int i=1; i <= n; i++)
            ans += Math.pow(1./(double)i, theta);
        
        return ans;
    }
}
