package speed;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.querybuilder.QueryBuilder;

public class LoginDao {

	public static String validate(String name, String pass) throws IOException {
		String status = "false";
		try {
			Properties prop = new Properties();
			String propFileName = "/resources/config.properties";
			InputStream inputStream = LoginDao.class.getResourceAsStream(propFileName);
			if (inputStream != null) {
				prop.load(inputStream);
			}

			Cluster cluster;
			Session session;
			ResultSet results;
			// Row rows;

			cluster = Cluster.builder().addContactPoint(prop.getProperty("database")).build();
			session = cluster.connect(prop.getProperty("keyspace"));

			Statement select = QueryBuilder.select().from(prop.getProperty("keyspace"), prop.getProperty("usertable"))
					.where(QueryBuilder.eq("username", name)).and(QueryBuilder.eq("password", pass));
			results = session.execute(select);
			int i = 0;
			i = results.all().size();
			if (i > 0) {
				status = "true";
			}
			cluster.close();
		} catch (NoHostAvailableException e) {
			status = "Make Sure Cassandra is up and running..";
		}
		return status;
	}
}