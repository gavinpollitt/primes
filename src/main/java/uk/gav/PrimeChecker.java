package uk.gav;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.stream.LongStream;

public class PrimeChecker {

	public static void main(String[] args) {
		
		long start = 100000L;
		long range = 3000L;
		
		if (args.length == 2) {
			try {
				long s = Long.parseLong(args[0]);
				long r = Long.parseLong(args[1]);
				
				start = s;
				range = r;
			}
			catch (Exception e) {}
		}

		final RunParameters rp = new RunParameters(start, range);
		System.out.println("Processing Input Stream seqentially");
		rp.start(System.nanoTime());
		rp.getSourceNumbers().filter(PrimeChecker::isPrime).forEach(rp::prime);
		rp.end(System.nanoTime());
		System.out.println(rp.toString());

		System.out.println("Processing Input Stream in parallel");
		rp.start(System.nanoTime());
		rp.getSourceNumbers().parallel().filter(PrimeChecker::isPrime)
				.forEach(rp::prime);
		rp.end(System.nanoTime());
		System.out.println(rp.toString());

		System.out.println();

		System.out.println("Processing Input Stream in parallel with collection before print");
		rp.start(System.nanoTime());
		rp.getSourceNumbers().parallel().filter(PrimeChecker::isPrime)
				.collect(ArrayList<Long>::new, ArrayList::add, ArrayList::addAll).stream()
				.forEach(rp::prime);
		rp.end(System.nanoTime());
		System.out.println(rp.toString());

		System.out.println();

		System.out.println("Processing Input Stream in parallel with sort");
		rp.start(System.nanoTime());
		rp.getSourceNumbers().parallel().filter(PrimeChecker::isPrime)
				.collect(TreeSet<Long>::new, TreeSet::add, TreeSet::addAll).stream()
				.forEach(rp::prime);
		;
		rp.end(System.nanoTime());
		System.out.println(rp.toString());

	}

	// Uses the Rabin-Miller Primality Test
	private static boolean isPrime(Long p) {
		if (p % 2 == 0 || p < 2) {
			return false;
		} else if (p == 2 || p == 3) {
			return true;
		}

		boolean prime = false;
		if (!prime) {
			// Isolate the variables required for the test...
			for (int i = 0; i < 10; i++) {
				Factors factors = new Factors(p);

				if (factors.s != null) {
					prime = mrTest(factors);
				}
			}
		}

		return prime;

	}

	
	private static BigDecimal bigPow(double n, double p) {
		BigDecimal bN = new BigDecimal(n);
		return bN.pow((int) p);
	}

	private static double bigMod(BigDecimal d, double mod) {
		BigDecimal bM = BigDecimal.valueOf(mod);

		return d.remainder(bM).doubleValue();
	}

	private static boolean mrTest(Factors f) {
		double X = bigMod(bigPow(f.a, f.d), f.n);

		boolean p = (X == 1 || X == f.n - 1);

		if (!p) {

			for (int i = 1; (i <= f.s) && !p; i++) {
				X = bigMod(bigPow(X, 2), f.n);

				p = (X == f.n - 1);
			}
		}

		return p;
	}

	private static class Factors {
		private Long n;
		private Long s;
		private Long d;
		private Long a;

		Factors(long n) {
			this.n = n;
			long n1 = n - 1;

			boolean ex = false;
			for (int i = 1; !ex; i++) {
				double pow = Math.pow(2, i);

				double temp = n1 / pow;

				if (temp < 2) {
					ex = true;
				} else if (Math.round(temp) == temp) {
					s = (long) i;
					d = (long) temp;
				}
			}

			if (s != null && d != null) {
				a = ((long) (Math.random() * (n1 - 2))) + 2;
			}
		}

		public String toString() {
			if (s == null) {
				return "No Factors";
			} else {
				return "Number:" + n + "::s = " + s + ",d = " + d + ", a = " + a;
			}
		}
	}

	private static class RunParameters {
		private long startRange;
		private long endRange;
		private long startTime;
		private double timeTaken;
		private long primes;

		RunParameters(long sr, long nums) {
			this.startRange = sr;
			this.endRange = sr + nums;
		}

		LongStream getSourceNumbers() {
			return LongStream.rangeClosed(this.startRange, this.endRange);
		}

		void start(long time) {
			this.timeTaken = 0d;
			this.primes = 0l;
			this.startTime = time;
		}

		void end(long end) {
			this.timeTaken = end - this.startTime;
		}
		
		void prime(long p) {
			System.out.println(p + " is Prime");
			this.primes++;
		}

		public String toString() {
			double seconds = Math.floor(this.timeTaken / Math.pow(10, 9));
			double milli = Math.floor(this.timeTaken / Math.pow(10, 6)) - (seconds * Math.pow(10, 3));
			double minutes = Math.floor(seconds/60);
			seconds = minutes > 0?(seconds - minutes*60d):seconds;
			return "Time to identify " + this.primes + " primes in the range (" + this.startRange + "," + this.endRange + ")\n"
					+ (minutes>0?(((long)minutes) + " minute(s), "):"") + (long) seconds + " second(s), " + (long) milli + " millisecond(s)\n";
		}
	}

}
