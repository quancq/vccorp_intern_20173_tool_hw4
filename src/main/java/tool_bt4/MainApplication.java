/* Chinh sua tren server github */
package tool_bt4;

import static spark.Spark.get;
import static spark.Spark.port;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;

public class MainApplication {
	// private int count;
	public static final int DEFAULT_PORT = 8080;

	public static void main(String[] args) {
		port(DEFAULT_PORT);
		
		// Declare cache
		CacheLoader<Integer, ArrayList<Integer>> primeLoader = new CacheLoader<Integer, ArrayList<Integer>>() {
			@Override
			public ArrayList<Integer> load(Integer key) throws Exception {
				return getPrimes(1, key);
			}
		};
		
		int maxTimeAfterAccess = 5;
		int maxTimeAfterWrite = 10;
		
		// Build cache
		LoadingCache<Integer, ArrayList<Integer>> primeCache = CacheBuilder.newBuilder()
				.expireAfterAccess(maxTimeAfterAccess, TimeUnit.SECONDS)
				.expireAfterWrite(maxTimeAfterWrite, TimeUnit.SECONDS)
				.recordStats()
				.build(primeLoader);
		
		
		// Define route
		get("/prime", (req, res) -> {

			int n = Integer.parseInt(req.queryParams("n"));
			System.out.println("n = " + n);

			String result = "";
			ArrayList<Integer> primes = primeCache.get(n);
			
			result += "<div>Number prime is : " + primes.size() + "</div>";
			result += getCacheStats(primeCache.stats());
			result += "<div>List prime is : </div><div>";
			result += primes.toString();
			result += "</div>";
			return result;
		});

	}

	/**
	 * Calculate all prime numbers in range from min to max with include at two
	 * anchor
	 * 
	 * @param min
	 * @param max
	 * @return ArrayList contains prime numbers in range
	 */
	private static ArrayList<Integer> getPrimes(int min, int max) {
		ArrayList<Integer> primes = new ArrayList<>();

		// Initial boolean array check if prime number
		boolean[] isPrime = new boolean[max + 1];
		for (int i = 0; i < isPrime.length; ++i) {
			isPrime[i] = true;
		}
		isPrime[0] = isPrime[1] = false;

		int maxIndex = (int) Math.floor(Math.sqrt(max));
		for (int p = 2; p <= maxIndex; ++p) {
			if (isPrime[p] == true) {
				// Set true for each multiplier of p : p^2, p^2 + p,... <= max
				for (int i = p * p; i < isPrime.length; i += p) {
					isPrime[i] = false;
				}
			}
		}

		for (int i = min; i <= max; ++i) {
			if (isPrime[i]) {
				primes.add(i);
			}
		}
		System.out.printf("Found %d number primes : in range [%d, %d]\n", primes.size(), min, max);
		return primes;
	}
	
	private static String getCacheStats(CacheStats cacheStats) {
		long hitCount = cacheStats.hitCount();
		long missCount = cacheStats.missCount();
		long requestCount = cacheStats.requestCount();
		double hitRate = cacheStats.hitRate();
		
		String stats = String.format("<div>Cache stats : (hit count, miss count, request count) = (%d, %d, %d) - Hit rate = %.2f</div>",
				hitCount, missCount, requestCount, hitRate);
		
		return stats;
	}

}
