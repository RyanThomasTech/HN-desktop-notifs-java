import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.*;
import java.util.*;
import ds.desktop.notify.DesktopNotify;
import com.google.gson.*;


public class HNInquisitor {
	
	LocalDateTime currentTime = LocalDateTime.now();
	public static void main(String[] args) throws IOException{
		int minPoints = getMinPointThreshold();
		inquireOnce(minPoints);
	}
	
	private static int getMinPointThreshold(){
		Scanner reader = new Scanner(System.in);
		System.out.println("Enter a minimum point threshold.");
		int minimumPoints = reader.nextInt();
				
		return minimumPoints;
	}
	
	public static void inquireOnce(int minPoints) throws IOException{
		long now = Instant.now().getEpochSecond();
		long then = getLastWeekTimestamp();
		parseJson(retrieveJson(now, then, minPoints));
		int intervalTime = getAlertInterval();
		inquirePeriodically(minPoints, intervalTime);
	}
	
	public static int getAlertInterval(){
		Scanner reader = new Scanner(System.in);
		System.out.println(
				"Enter an interval (in minutes) after which you'd like to be alerted to stories.");
		int intervalMinutes = reader.nextInt();
		while (intervalMinutes<=1){
			System.out.println("Please select an interval greater than 1 minute.");
			intervalMinutes = reader.nextInt();
		}
		reader.close();
		
		return intervalMinutes;
	}
	
	public static void inquirePeriodically(int minPoints, int intervalTime){
		
		Thread everyTwentyMinutes = new Thread(){
			public void run(){
				while(true){
					try {
						Thread.sleep(1000*60*intervalTime);
						long now = Instant.now().getEpochSecond();
						long then = getLastWeekTimestamp();
						try {
							parseJson(retrieveJson(now, then, minPoints));
						}
						catch (IOException e){
							System.out.println(e);
						}

					}
					catch (InterruptedException ie){
						
					}
				}
			}
		};
		everyTwentyMinutes.start();
	}
	
	public static long getLastWeekTimestamp(){
		Instant now = Instant.now();
		Instant nowMinusSevenDays = now.minus(Duration.ofDays(7));
		long unixTimestamp = nowMinusSevenDays.getEpochSecond();
		
		return unixTimestamp;
	}
	
	/*
	 * HTTP GET request style supplied by mkyong.com tutorials for restful java client
	 */
	public static String retrieveJson(long now, long then, int pointMinimum) throws IOException{
		String json = null;

		URL url = new URL(
				"http://hn.algolia.com/api/v1/search_by_date?tags=story&numericFilters="
				+ "created_at_i>" + then
				+ ",created_at_i<" + now
				+ ",points>=" + pointMinimum
				);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "application/json");

		if (conn.getResponseCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode());
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(
			(conn.getInputStream())));

		String output;
		//System.out.println("Output from Server .... \n");
		while ((output = br.readLine()) != null) {
			json = output;
			//System.out.println(output);
		}

		conn.disconnect();

		 
		return json;
	}
	
	public static void parseJson(String json) throws JsonSyntaxException{
		Gson gson = new GsonBuilder().create();
		Data data = gson.fromJson(json,Data.class);
		runDesktopNotifications(data);
	}
	
	public static void runDesktopNotifications(Data data){
		Iterator<Hit> hitsIterator = data.hits.listIterator();
		while (hitsIterator.hasNext()){
			Hit currentHit = hitsIterator.next();
			String currentHitTitle = currentHit.title;
			int currentHitPoints = (int)currentHit.points;
			DesktopNotify.showDesktopMessage(
					"On Hacker News with points score of " + currentHitPoints, currentHitTitle);
		}
	}
}