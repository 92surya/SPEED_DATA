package speed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;

public class GetLatLngValues extends HttpServlet {

	private static final long serialVersionUID = 2718670849200378114L;

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Properties prop = new Properties();
		String propFileName = "resources/config.properties";
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
		if (inputStream != null) {
			prop.load(inputStream);
		}

		StringBuffer stringBuffer = new StringBuffer();
		String line = null;
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
				stringBuffer.append(line);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			JSONObject jsonObject = new JSONObject(stringBuffer.toString());
			// System.out.println(stringBuffer.toString());

			int driverid = jsonObject.getInt("driverid");
			String fromDateString = jsonObject.getString("fromDate");
			String uptoDateString = jsonObject.getString("uptoDate");
			String filter = jsonObject.getString("filter");

			SimpleDateFormat datetimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date fromDatetime = datetimeFormatter.parse(fromDateString);
			Date uptoDatetime = datetimeFormatter.parse(uptoDateString);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(fromDatetime);
			int fromHours = calendar.get(Calendar.HOUR_OF_DAY);
			int fromMinutes = calendar.get(Calendar.MINUTE);
			calendar.setTime(uptoDatetime);
			int uptoHours = calendar.get(Calendar.HOUR_OF_DAY);
			int uptoMinutes = calendar.get(Calendar.MINUTE);
			String fromDate = fromDateString.substring(0, 10);
			String uptoDate = uptoDateString.substring(0, 10);

			int fromTime = fromHours * 60 + fromMinutes;
			int uptoTime = uptoHours * 60 + uptoMinutes;

			int queryStartMinutes = 0;
			int queryEndMinutes = 0;
			int hourDisplay = 0;
			int minuteDisplay = 0;
			int difference = 0;

			JSONObject json = null;
			JSONArray jArray = null;
			JSONObject jObject = null;

			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date startDate = formatter.parse(fromDate);
			Date endDate = formatter.parse(uptoDate);

			LocalDate start = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			List<String> dates = new ArrayList<String>();

			Cluster cluster;
			Session session;
			ResultSet results;

			cluster = Cluster.builder().addContactPoint(prop.getProperty("database")).build();
			session = cluster.connect(prop.getProperty("keyspace"));

			jArray = new JSONArray();
			Statement select = null;

			for (LocalDate date = start; (!date.isAfter(end)); date = date.plusDays(1)) // loop
																						// for
																						// number
																						// of
																						// days
				dates.add(date.toString());

			if (filter.equals("")) {
				if (dates.size() == 1)
					filter = "day";
				else if (dates.size() == 2)
					if (uptoTime + (1440 - fromTime) > 1440)
						filter = "week";
					else
						filter = "day";
				else if (dates.size() <= 8)
					if (dates.size() == 8 && (uptoTime + (1440 - fromTime) > 1440))
						filter = "month";
					else
						filter = "week";
				else if (dates.size() <= 30)
					if (dates.size() == 30 && (uptoTime + (1440 - fromTime) > 1440))
						filter = "year";
					else
						filter = "month";
				else
					filter = "year";

			}
			// System.out.println(filter);

			if (filter.equals("day") || filter.equals("daypart") || filter.equals("hour") || filter.equals("min")) {
				jArray = new JSONArray();
				minuteDisplay = fromMinutes;
				hourDisplay = fromHours;
				if (uptoTime > fromTime)
					difference = uptoTime - fromTime;
				else
					difference = uptoTime - fromTime + 1440;
				// System.out.println(difference+" "+uptoTime+" "+fromTime);
				for (int minutes = fromMinutes; minutes <= fromMinutes + difference; minutes = minutes + 4) {
					minuteDisplay = minutes % 60;
					if (minutes >= 60)
						hourDisplay = fromHours + (minutes - minuteDisplay) / 60;
					if (hourDisplay >= 24) {
						hourDisplay = hourDisplay - 24;
						if (hourDisplay == 0 && minuteDisplay == 0)
							start = start.plusDays(1);
					}
					queryStartMinutes = hourDisplay * 60 + minuteDisplay;
					queryEndMinutes = hourDisplay * 60 + minuteDisplay + 2;
					select = QueryBuilder.select().column("date").column("minutes").column("lat").column("lng")
							.from(prop.getProperty("keyspace"), prop.getProperty("locationtable"))
							.where(QueryBuilder.eq("dvb_id", driverid)).and(QueryBuilder.eq("date", start.toString()))
							.and(QueryBuilder.eq("minutes", queryStartMinutes));
					results = session.execute(select);

					for (Row row : results) {
						jObject = new JSONObject();
						jObject.put("date", row.getString("date"));
						jObject.put("minutes",
								String.format("%02d", hourDisplay) + ":" + String.format("%02d", minuteDisplay));
						jObject.put("lat", row.getFloat("lat"));
						jObject.put("lng", row.getFloat("lng"));
						jArray.put(jObject);
					}
				}
				// System.out.println(jArray);
			}

			if (filter.equals("week")) {
				jArray = new JSONArray();
				minuteDisplay = fromMinutes;
				hourDisplay = fromHours;
				if (uptoTime > fromTime)
					difference = uptoTime - fromTime + (dates.size() - 1) * 1440;
				else
					difference = uptoTime - fromTime + (dates.size()) * 1440;
				for (int minutes = fromMinutes; minutes <= fromMinutes + difference; minutes = minutes + 30) {
					minuteDisplay = minutes % 60;
					if (minutes >= 60)
						hourDisplay = fromHours + (minutes - minuteDisplay) / 60;
					if (hourDisplay >= 24) {
						hourDisplay = hourDisplay % 24;
						if (hourDisplay == 0 && minuteDisplay < 30)
							start = start.plusDays(1);
					}
					queryStartMinutes = hourDisplay * 60 + minuteDisplay;
					queryEndMinutes = hourDisplay * 60 + minuteDisplay + 2;
					select = QueryBuilder.select().column("date").column("minutes").column("lat").column("lng")
							.from(prop.getProperty("keyspace"), prop.getProperty("locationtable"))
							.where(QueryBuilder.eq("dvb_id", driverid)).and(QueryBuilder.eq("date", start.toString()))
							.and(QueryBuilder.eq("minutes", queryStartMinutes));
					results = session.execute(select);

					for (Row row : results) {
						jObject = new JSONObject();
						jObject.put("date", row.getString("date"));
						jObject.put("minutes",
								String.format("%02d", hourDisplay) + ":" + String.format("%02d", minuteDisplay));
						jObject.put("lat", row.getFloat("lat"));
						jObject.put("lng", row.getFloat("lng"));
						jArray.put(jObject);
					}
				}
				// System.out.println(jArray);
			}

			if (filter.equals("month")) {
				jArray = new JSONArray();
				minuteDisplay = fromMinutes;
				hourDisplay = fromHours;
				if (uptoTime > fromTime)
					difference = uptoTime - fromTime + (dates.size() - 1) * 1440;
				else
					difference = uptoTime - fromTime + (dates.size()) * 1440;
				for (int minutes = fromMinutes; minutes <= fromMinutes + difference; minutes = minutes + 120) {
					minuteDisplay = minutes % 60;
					if (minutes >= 60)
						hourDisplay = fromHours + (minutes - minuteDisplay) / 60;
					if (hourDisplay >= 24) {
						hourDisplay = hourDisplay % 24;
						if (hourDisplay < 2)
							start = start.plusDays(1);
					}
					queryStartMinutes = hourDisplay * 60 + minuteDisplay;
					queryEndMinutes = hourDisplay * 60 + minuteDisplay + 2;
					select = QueryBuilder.select().column("date").column("minutes").column("lat").column("lng")
							.from(prop.getProperty("keyspace"), prop.getProperty("locationtable"))
							.where(QueryBuilder.eq("dvb_id", driverid)).and(QueryBuilder.eq("date", start.toString()))
							.and(QueryBuilder.eq("minutes", queryStartMinutes));
					results = session.execute(select);

					for (Row row : results) {
						jObject = new JSONObject();
						jObject.put("date", row.getString("date"));
						jObject.put("minutes",
								String.format("%02d", hourDisplay) + ":" + String.format("%02d", minuteDisplay));
						jObject.put("lat", row.getFloat("lat"));
						jObject.put("lng", row.getFloat("lng"));
						jArray.put(jObject);
					}
				}
				// System.out.println(jArray);
			}

			if (filter.equals("year")) {
				jArray = new JSONArray();
				for (int i = 0; i < dates.size(); i++) {
					start = start.plusDays(1);
					queryStartMinutes = fromHours * 60 + fromMinutes;
					select = QueryBuilder.select().column("date").column("minutes").column("lat").column("lng")
							.from(prop.getProperty("keyspace"), prop.getProperty("locationtable"))
							.where(QueryBuilder.eq("dvb_id", driverid)).and(QueryBuilder.eq("date", start.toString()))
							.and(QueryBuilder.eq("minutes", queryStartMinutes));
					results = session.execute(select);

					for (Row row : results) {
						jObject = new JSONObject();
						jObject.put("date", row.getString("date"));
						jObject.put("minutes",
								String.format("%02d", fromHours) + ":" + String.format("%02d", fromMinutes));
						jObject.put("lat", row.getFloat("lat"));
						jObject.put("lng", row.getFloat("lng"));
						jArray.put(jObject);
					}
				}
			}
			// System.out.println(jArray.length());
			json = new JSONObject();
			json.put("cities", jArray);

			cluster.close();

			response.setContentType("application/json");
			response.setCharacterEncoding("utf-8");
			PrintWriter out = response.getWriter();
			out.print(json.get("cities").toString());
			out.flush();

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}