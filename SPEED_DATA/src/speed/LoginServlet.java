package speed;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Properties;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginServlet extends HttpServlet {

	private static final long serialVersionUID = 1683357958905576261L;

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		Properties prop = new Properties();
		String propFileName = "resources/config.properties";
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
		if (inputStream != null) {
			prop.load(inputStream);
		}

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		String n = request.getParameter("username");
		String p = request.getParameter("password");

		String status = LoginDao.validate(n, p);
		if (status.equals("true")) {
			HttpSession newSession = request.getSession(true);
			newSession.setAttribute("user", n);
			response.sendRedirect("tracking.jsp");
		} else {
			if (status.equals("false"))
				status = "Sorry, username or password incorrect!";
			// out.print(status);
			request.setAttribute("response", status);
			RequestDispatcher rd = request.getRequestDispatcher("index.jsp");
			rd.include(request, response);
		}
		out.close();
	}
}
