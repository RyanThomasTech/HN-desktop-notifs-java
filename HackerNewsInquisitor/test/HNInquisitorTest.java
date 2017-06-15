import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import java.io.*;
import java.time.Instant;
import java.util.Scanner;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HNInquisitorTest {

	@Test
	public void testInquireOnce() throws IOException{
		long now = Instant.now().getEpochSecond();
		long then = HNInquisitor.getLastWeekTimestamp();
		assertTrue (HNInquisitor.retrieveJson(now, then, 200) != null);
	}

	@Test
	public void testGetAlertInterval() {
		String fakeInput = "20";
		System.setIn(new ByteArrayInputStream(fakeInput.getBytes()));

		Scanner scanner = new Scanner(System.in);
		int intervalMinutes = scanner.nextInt();
		while (intervalMinutes<=1){
			System.out.println("Please select an interval greater than 1 minute.");
			intervalMinutes = scanner.nextInt();
		}
		scanner.close();
		assertTrue (1 < intervalMinutes);
	}

	@Test
	public void testRetrieveJson() throws IOException {
		long now = Instant.now().getEpochSecond();
		long then = HNInquisitor.getLastWeekTimestamp();
		assertFalse (HNInquisitor.retrieveJson(now, then, 200) == null);
	}

	@Test
	public void testGetLastWeekTimestamp() {
		assertThat (Instant.now().getEpochSecond(), is(not(HNInquisitor.getLastWeekTimestamp())));
	}

}
