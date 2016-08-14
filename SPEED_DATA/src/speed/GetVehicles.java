package speed;

import java.io.*;
import java.util.Properties;
import javax.servlet.*;
import javax.servlet.http.*;
import org.json.JSONArray;
import org.json.simple.JSONObject;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;

public class GetVehicles extends HttpServlet {

	private static final long serialVersionUID = -7913012034304166616L;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Properties prop = new Properties();
		String propFileName = "resources/config.properties";
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
		if (inputStream != null) {
			prop.load(inputStream);
		}

		Cluster cluster;
		Session session;
		ResultSet results;

		cluster = Cluster.builder().addContactPoint(prop.getProperty("database")).build();
		session = cluster.connect(prop.getProperty("keyspace"));

		Statement select = QueryBuilder.select().all().from(prop.getProperty("keyspace"),
				prop.getProperty("vehicletable"));
		results = session.execute(select);

		JSONArray jArray = new JSONArray();

		for (Row row : results) {
			JSONObject jObject = new JSONObject();
			jObject.put("id", row.getInt("vehicleid"));
			jObject.put("title", row.getString("vehicle_brand"));
			jArray.put(jObject);
			// System.out.println(jArray);
		}

		JSONObject json = new JSONObject();
		json.put("vehicleList", jArray);

		cluster.close();

		response.setContentType("application/json");
		response.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();
		out.print(json.get("vehicleList").toString());
		out.flush();
	}
}