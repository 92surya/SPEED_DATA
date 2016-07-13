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

public class GetChartPoints extends HttpServlet {

private static final long serialVersionUID = 2718670849200378114L;


public void doPost(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException
	{
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
		} catch (Exception e) 
			{ 
			e.printStackTrace();
		}
		try {
			JSONObject jsonObject = new JSONObject(stringBuffer.toString());
//			System.out.println(stringBuffer.toString());

			int driverid = jsonObject.getInt("driverid");
			String fromDateString = jsonObject.getString("fromDate");
			String uptoDateString = jsonObject.getString("uptoDate");
			String filter = jsonObject.getString("filter");
						
			SimpleDateFormat datetimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date fromDatetime = datetimeFormatter.parse(fromDateString);
			Date uptoDatetime = datetimeFormatter.parse(uptoDateString);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(fromDatetime);
//			System.out.println(calendar.toString());
			int fromHours = calendar.get(Calendar.HOUR_OF_DAY);
			int fromMinutes = calendar.get(Calendar.MINUTE);
			int fromSeconds = calendar.get(Calendar.SECOND);
			calendar.setTime(uptoDatetime);
			int uptoHours = calendar.get(Calendar.HOUR_OF_DAY);
			int uptoMinutes = calendar.get(Calendar.MINUTE);
			int uptoSeconds = calendar.get(Calendar.SECOND);
			String fromDate = fromDateString.substring(0, 10);
			String uptoDate = uptoDateString.substring(0, 10);

			int fromTime = fromHours*3600+fromMinutes*60+fromSeconds;
			int uptoTime = uptoHours*3600+uptoMinutes*60+uptoSeconds;
			
			int queryStartSeconds = 0;
			int queryEndSeconds = 0;
			int hourDisplay = 0;
			int minuteDisplay = 0;

			JSONObject json = null;
			JSONArray jArray = null;
			JSONObject jObject = null;
			
			List<String> dates = new ArrayList<String>();
			
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date startDate = formatter.parse(fromDate);
			Date endDate = formatter.parse(uptoDate);
			
			LocalDate start = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			
			for (LocalDate date = start; (!date.isAfter(end)); date = date.plusDays(1)) //loop for number of days
				dates.add(date.toString());
//			System.out.println(dates);

			if(filter.equals(""))
			{
				if(dates.size()==1)
					if((uptoTime-fromTime)>21600)
						filter="day";
					else if((uptoTime-fromTime)>3600)
						filter="daypart";
					else if((uptoTime-fromTime)>120)
						filter="hour";
					else
						filter="min";
				else if(dates.size()==2)
					if(uptoTime+(86400-fromTime)>86400)
						filter="week";				
					else if(uptoTime+(86400-fromTime)>21600)
						filter="day";				
					else if((uptoTime+(86400-fromTime))>3600)
						filter="daypart";
					else if((uptoTime+(86400-fromTime))>120)
						filter="hour";
					else
						filter="min";
				else if(dates.size()<=8)
					if(dates.size()==8 && (uptoTime+(86400-fromTime)>86400))
						filter="month";
					else 
						filter="week";
				else if(dates.size()<=30)
					if(dates.size()==30 && (uptoTime+(86400-fromTime)>86400))
						filter="year";
					else 
						filter="month";
				else
					filter="year";
				
			}
//			System.out.println(filter);
			
			Cluster cluster;
			Session session;
			ResultSet results;

			cluster = Cluster
				.builder()
				.addContactPoint(prop.getProperty("database"))
				.build();
			session = cluster.connect(prop.getProperty("keyspace"));
			
			Statement select = null;
			
//			if(filter.equals(""))
//			{
//				select = QueryBuilder.select().column("time").column("speed").from(prop.getProperty("keyspace"), prop.getProperty("speedtable"))
//						.where(QueryBuilder.eq("dvb_id",driverid)).and(QueryBuilder.eq("date",fromDate))
//						.and(QueryBuilder.gte("time",fromTime))
//						.and(QueryBuilder.lt("time",uptoTime));
//				results = session.execute(select);
//
//				jArray = new JSONArray();
//
//				for (Row row : results) {
//					jObject = new JSONObject();
//					jObject.put("time",jArray.length());
//					jObject.put("speed",row.getInt("speed"));
//					jArray.put(jObject);
//					System.out.println(jArray);
//				}
//
//				json = new JSONObject();
//				json.put ("chartList",jArray);
//			}
			
			if(filter.equals("min"))
			{
				select = QueryBuilder.select().column("time").column("speed").from(prop.getProperty("keyspace"), prop.getProperty("speedtable"))
						.where(QueryBuilder.eq("dvb_id",driverid)).and(QueryBuilder.eq("date",fromDate))
						.and(QueryBuilder.gte("time",fromTime))
						.and(QueryBuilder.lt("time",fromTime+120));
				results = session.execute(select);

				jArray = new JSONArray();
				
				int i=0;
				for (Row row : results) {
					if(i==60)
						fromMinutes=fromMinutes+1;
					if(i%2==0)
					{
						jObject = new JSONObject();
//						jObject.put("time",jArray.length());
						jObject.put("datetime",fromDate+" "+fromHours+":"+fromMinutes+":"+(i%60));
						jObject.put("speed",row.getInt("speed"));
						jArray.put(jObject);
					}
					i++;
				}
//				System.out.println(jArray);
				json = new JSONObject();
				json.put ("chartList",jArray);
			}
			
			else if(filter.equals("hour"))
			{
				jArray = new JSONArray();
				minuteDisplay = fromMinutes;
				hourDisplay = fromHours;
				for(int minutes = fromMinutes; minutes < fromMinutes+60; minutes++)
				{
					queryStartSeconds = hourDisplay*3600+minuteDisplay*60+fromSeconds;
					queryEndSeconds = queryStartSeconds+60;
					select = QueryBuilder.select().column("time").raw("max(speed) as max_speed").from(prop.getProperty("keyspace"), prop.getProperty("speedtable"))
							.where(QueryBuilder.eq("dvb_id",driverid)).and(QueryBuilder.eq("date",start.toString()))
							.and(QueryBuilder.gte("time",queryStartSeconds))
							.and(QueryBuilder.lt("time",queryEndSeconds));
					results = session.execute(select);
					minuteDisplay = minutes%60;
					if(minutes>=60)
						hourDisplay = fromHours+(minutes-minuteDisplay)/60;
					if(hourDisplay>=24)
					{
						hourDisplay = hourDisplay-24;
						if(hourDisplay==0 && minuteDisplay==0)
							start = start.plusDays(1);
					}
					for (Row row : results) {
						jObject = new JSONObject();
//						jObject.put("time",jArray.length());
						jObject.put("datetime",start.toString()+" "+hourDisplay+":"+minuteDisplay+":"+fromSeconds);
						jObject.put("speed",row.getInt("max_speed"));
						jArray.put(jObject);
					}										
				}
//				System.out.println(jArray);
				json = new JSONObject();
				json.put ("chartList",jArray);
			}
			
			else if(filter.equals("daypart"))
			{
				jArray = new JSONArray();
				minuteDisplay = fromMinutes;
				hourDisplay = fromHours;
				for(int minutes = fromMinutes; minutes < fromMinutes+360; minutes=minutes+6)
				{
					queryStartSeconds = hourDisplay*3600+minuteDisplay*60+fromSeconds;
					queryEndSeconds = queryStartSeconds+360;
					select = QueryBuilder.select().column("time").raw("max(speed) as max_speed").from(prop.getProperty("keyspace"), prop.getProperty("speedtable"))
							.where(QueryBuilder.eq("dvb_id",driverid)).and(QueryBuilder.eq("date",start.toString()))
							.and(QueryBuilder.gte("time",queryStartSeconds))
							.and(QueryBuilder.lt("time",queryEndSeconds));
					results = session.execute(select);
					minuteDisplay = minutes%60;
					if(minutes>=60)
						hourDisplay = fromHours+(minutes-minuteDisplay)/60;
					if(hourDisplay>=24)
					{
						hourDisplay = hourDisplay%24;
						if(hourDisplay==0 && minuteDisplay<6)
							start = start.plusDays(1);
					}
					for (Row row : results) {
						jObject = new JSONObject();
//						jObject.put("time",jArray.length());
						jObject.put("datetime",start.toString()+" "+hourDisplay+":"+minuteDisplay+":"+fromSeconds);
						jObject.put("speed",row.getInt("max_speed"));
						jArray.put(jObject);
					}	
				}
//				System.out.println(jArray);
				json = new JSONObject();
				json.put ("chartList",jArray);
			}
			
			else if(filter.equals("day"))
			{
				jArray = new JSONArray();
				minuteDisplay = fromMinutes;
				hourDisplay = fromHours;
				for(int minutes = fromMinutes; minutes < fromMinutes+1440; minutes=minutes+30)
				{
					queryStartSeconds = hourDisplay*3600+minuteDisplay*60+fromSeconds;
					queryEndSeconds = queryStartSeconds+1800;
					select = QueryBuilder.select().column("time").raw("max(speed) as max_speed").from(prop.getProperty("keyspace"), prop.getProperty("speedtable"))
							.where(QueryBuilder.eq("dvb_id",driverid)).and(QueryBuilder.eq("date",start.toString()))
							.and(QueryBuilder.gte("time",queryStartSeconds))
							.and(QueryBuilder.lt("time",queryEndSeconds));
					results = session.execute(select);
					minuteDisplay = minutes%60;
					if(minutes>=60)
						hourDisplay = fromHours+(minutes-minuteDisplay)/60;
					if(hourDisplay>=24)
					{
						hourDisplay = hourDisplay%24;
						if(hourDisplay==0 && minuteDisplay<30)
							start = start.plusDays(1);
					}
					for (Row row : results) {
						jObject = new JSONObject();
//						jObject.put("time",jArray.length());
						jObject.put("datetime",start.toString()+" "+hourDisplay+":"+minuteDisplay+":"+fromSeconds);
						jObject.put("speed",row.getInt("max_speed"));
						jArray.put(jObject);
					}	
				}
//				System.out.println(jArray);
				json = new JSONObject();
				json.put ("chartList",jArray);
			}
			
			else if(filter.equals("week"))
			{
				jArray = new JSONArray();
				hourDisplay = fromHours;
				for(int hours = fromHours; hours < fromHours+168; hours=hours+4)
				{
					queryStartSeconds = hourDisplay*3600+fromMinutes*60+fromSeconds;
					queryEndSeconds = queryStartSeconds+14400;
					select = QueryBuilder.select().column("time").raw("max(speed) as max_speed").from(prop.getProperty("keyspace"), prop.getProperty("speedtable"))
							.where(QueryBuilder.eq("dvb_id",driverid)).and(QueryBuilder.eq("date",start.toString()))
							.and(QueryBuilder.gte("time",queryStartSeconds))
							.and(QueryBuilder.lt("time",queryEndSeconds));
					results = session.execute(select);
					hourDisplay = hours;
					if(hourDisplay>=24)
					{
						hourDisplay = hourDisplay%24;
						if(hourDisplay<=3)
							start = start.plusDays(1);
					}
					for (Row row : results) {
						jObject = new JSONObject();
//						jObject.put("time",jArray.length());
						jObject.put("datetime",start.toString()+" "+hourDisplay+":"+fromMinutes+":"+fromSeconds);
						jObject.put("speed",row.getInt("max_speed"));
						jArray.put(jObject);
					}	
				}
//				System.out.println(jArray);
				json = new JSONObject();
				json.put ("chartList",jArray);
			}
			
			else if(filter.equals("month"))
			{
				jArray = new JSONArray();
				hourDisplay = fromHours;
				for(int hours = fromHours; hours < fromHours+720; hours=hours+12)
				{
					queryStartSeconds = hourDisplay*3600+fromMinutes*60+fromSeconds;
					queryEndSeconds = queryStartSeconds+43200;
					select = QueryBuilder.select().column("time").raw("max(speed) as max_speed").from(prop.getProperty("keyspace"), prop.getProperty("speedtable"))
							.where(QueryBuilder.eq("dvb_id",driverid)).and(QueryBuilder.eq("date",start.toString()))
							.and(QueryBuilder.gte("time",queryStartSeconds))
							.and(QueryBuilder.lt("time",queryEndSeconds));
					results = session.execute(select);
					hourDisplay = hours;
					if(hourDisplay>=24)
					{
						hourDisplay = hourDisplay%24;
						if(hourDisplay<=11)
							start = start.plusDays(1);
					}
					for (Row row : results) {
						jObject = new JSONObject();
//						jObject.put("time",jArray.length());
						jObject.put("datetime",start.toString()+" "+hourDisplay+":"+fromMinutes+":"+fromSeconds);
						jObject.put("speed",row.getInt("max_speed"));
						jArray.put(jObject);
					}	
				}
//				System.out.println(jArray);
				json = new JSONObject();
				json.put ("chartList",jArray);
			}

			else if(filter.equals("year"))
			{
				jArray = new JSONArray();
				for(int i = 0; i < 365; i=i+6)
				{
					for(int j = 0; j < 6; j=j+1)
					{
						dates.clear();
						dates.add(start.plusDays(i+j).toString());
					}
					queryStartSeconds = fromHours*3600+fromMinutes*60+fromSeconds;
					queryEndSeconds = queryStartSeconds+43200;
					select = QueryBuilder.select().column("time").raw("max(speed) as max_speed").from(prop.getProperty("keyspace"), prop.getProperty("speedtable"))
							.where(QueryBuilder.eq("dvb_id",driverid)).and(QueryBuilder.in("date",dates));
					results = session.execute(select);
					for (Row row : results) {
						jObject = new JSONObject();
//						jObject.put("time",jArray.length());
						jObject.put("datetime",start.plusDays(i).toString()+" "+fromHours+":"+fromMinutes+":"+fromSeconds);
						jObject.put("speed",row.getInt("max_speed"));
						jArray.put(jObject);
					}	
				}
//				System.out.println(jArray);
				json = new JSONObject();
				json.put ("chartList",jArray);
			}
			
			cluster.close();
				
			response.setContentType("application/json");
			response.setCharacterEncoding("utf-8");
			PrintWriter out = response.getWriter();
			out.print(json.toString());
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