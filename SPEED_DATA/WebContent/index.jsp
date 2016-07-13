<%@ page import="com.sun.xml.internal.bind.v2.schemagen.xmlschema.Import"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="java.io.*,java.util.*,javax.servlet.http.*,speed.*"%>
<!DOCTYPE html>
<html>
<%	HttpSession existingSession=request.getSession(true);  
			session.invalidate();	%>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>SPEED DATA</title>
		<link rel="shortcut icon" type="image/png" href="img/favicon2.png">
		<link rel="stylesheet" href="node_modules/bootstrap/dist/css/bootstrap.css">
		<script type="text/javascript" src="node_modules/jquery/dist/jquery.min.js"></script>
		<script type="text/javascript" src="node_modules/bootstrap/dist/js/bootstrap.min.js"></script>
		<style>
			.header {
			    margin-top : 5%;
			    float : right;
			    font-family: Comic Sans MS!important;
			    font-weight: bold!important;
			}
			.signin {
			    margin-top : 22%;
			    float : left;
			}
			.wrapper {
			    background: url("img/bg.jpg") no-repeat center center fixed;
			    -webkit-background-size: cover;
			    -moz-background-size: cover;
			    -o-background-size: cover;
			    background-size: cover;
			    color : white;
			}   
			.headerFont {
			    font-family : lora!important;
			    font-size : 50px;
			}
			#response {
				font-family: Comic Sans MS!important;
			}
			.form-control {
				width : 60%;
			}
		</style>
	</head>
	<body class="wrapper">
		<div>
			<div class="col-md-7">
			</div>
			<div class="col-md-4 header">
				<h1 class="headerFont">SPEED DATA</h1>
				<h4>know your vehicle like never before..</h4>
			</div>
			<div class="col-md-1">
			</div>
		</div>
		<div>
			<div class="col-md-2">
			</div>
			<div class="col-md-3 signin form-group">
				<form action="/SPEED_DATA/login" method="post" id="login_details">
					<br>
					<input class="form-control" type="text" name="username" placeholder="Username" required>
					<br>
					<input class="form-control" type="password" name="password" placeholder="Password" required>
					<br>
					<input class="btn btn-primary" type="submit" value="Submit">
					<br>
					<br>
					<div id="response">
						<% if(request.getAttribute("response") != null) 
							out.println(request.getAttribute("response")); %>
					</div>
				</form>
			</div>
			<div class="col-md-7">
			</div>
		</div>
	</body>
</html>
