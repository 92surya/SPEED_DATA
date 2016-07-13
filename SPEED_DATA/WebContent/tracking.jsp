<%@page import="com.sun.xml.internal.bind.v2.schemagen.xmlschema.Import"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="java.io.*,java.util.*,javax.servlet.http.*" %>
<html>
  <head>
		<%	HttpSession existingSession=request.getSession(true);  
			if (existingSession.getAttribute("user") == null) 
				response.sendRedirect("/SPEED_DATA");
		%>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>SPEED_DATA</title>
    <link rel="shortcut icon" type="image/png" href="img/favicon2.png">

    <link rel="stylesheet" href="node_modules/n3-charts/build/LineChart.css">

    <link rel="stylesheet" href="node_modules/bootstrap/dist/css/bootstrap.css">
    <link rel="stylesheet" href="node_modules/angular-bootstrap-datetimepicker/src/css/datetimepicker.css"/>

    <script type="text/javascript" src="node_modules/angular/angular.js"></script>
    <script type="text/javascript" src="node_modules/d3/d3.min.js"></script>
    <script type="text/javascript" src="node_modules/n3-charts/build/LineChart.js"></script>
	
	<script src="https://maps.googleapis.com/maps/api/js?sensor=false" type="text/javascript"></script>
	<script type="text/javascript" src="node_modules/angular-google-maps/dist/angular-google-maps.min.js"></script>
  	<script type="text/javascript" src="node_modules/jquery/dist/jquery.min.js"></script>
  	<script type="text/javascript" src="node_modules/bootstrap/dist/js/bootstrap.min.js"></script>
    <script type="text/javascript" src="node_modules/moment/moment.js"></script>
  	<script type="text/javascript" src="node_modules/angular-bootstrap-datetimepicker/src/js/datetimepicker.js"></script>
  	<script type="text/javascript" src="node_modules/angular-bootstrap-datetimepicker/src/js/datetimepicker.templates.js"></script>
  	<script type="text/javascript" src="tracking.js"></script>

    <style>
      .body {
      	background-color : #FAFAFA!important;
      }
      .logout {
           margin : 1%;
           float : right;
      }
      .chart-legend {
        display : none;
      }
      .chart {
	    height : 420px;
      }
      .span {
	    font-weight: bold;
	    margin-top: 25px;
      }
      .dropdown {
	    margin-top: 20px;
      }
      .my-chart {
		margin-top : 5%;
      }
      #map {
		margin : 5%;
	    height : 420px;
	    width : 90%;
	    border-style : double;
	  }
	  .button {
	  	margin : 2%;
	  }
    </style>
  </head> 
  <body ng-app="chartApp" class="body" ng-strict-di>
    <div align="center">
      <div ng-controller="ChartControl">
        <form ng-submit=loadChartAndMap(data,'')>
		  <div class="col-md-12">
			<div class="col-md-11">
			</div>
			<div class="col-md-1 logout">
		       <input class="btn btn-primary" type="button" value="Logout" ng-model="data" ng-click="logout()">				
			</div>
		  </div>
          <div class="col-md-6 span">
          	<div class="col-md-3">
          	</div>
          	<div class="col-md-3">
          		<span>Select a Driver </span>
            </div>
          	<div class="col-md-3">
            	<select data-ng-model="data.selectedDriver" ng-options="driver.id as driver.title for driver in driverList | orderBy : 'id' track by driver.id" ng-change="updateDriver(data)" required></select>
			</div>
          	<div class="col-md-3">
          	</div>
          </div>
          <div class="col-md-6 span">
          	<div class="col-md-2">
          	</div>
          	<div class="col-md-3">
	            <span>Select a Vehicle</span>
	        </div>
          	<div class="col-md-4">
	             <select data-ng-model="data.selectedVehicle" ng-options="vehicle.id as vehicle.title for vehicle in vehicleList | orderBy : 'id' track by vehicle.id" ng-change="updateVehicle(data)" required></select>
	        </div>
          	<div class="col-md-2">
          	</div>
          </div>
          <div class="col-md-6">
          	  <div class="col-md-2">
          	  </div>
          	  <div class="col-md-3 span">
		          <span>Select from Date</span>
		      </div>
	       		<div class="col-md-6 dropdown">
	       		  <a class="dropdown-toggle" id="dropdown1" role="button" data-toggle="dropdown" data-target="#" href="#">
	       		    <div class="input-group">
	       		    	<input type="text" class="form-control" data-ng-model="data.dateDropDownInput1" required disabled>
	       			    <span class="input-group-addon">
	       			    	<i class="glyphicon glyphicon-calendar"></i>
	       			    </span>
	       		    </div>
	       		  </a>
	       		  <ul class="dropdown-menu" role="menu" aria-labelledby="dLabel">
	       		    <datetimepicker data-ng-model="data.dateDropDownInput1" data-datetimepicker-config="{ dropdownSelector: '#dropdown1' , minuteStep : 2, modelType : 'YYYY-MM-DD HH:mm:ss'}"/>
	       		  </ul>
	       		</div>
          	  <div class="col-md-1">
          	  </div>
	       	</div>
          <div class="col-md-6">
          	  <div class="col-md-1">
          	  </div>
          	  <div class="col-md-3 span">
		          <span>Select upto Date</span>
		      </div>
	          <div class="col-md-6 dropdown">
	            <a class="dropdown-toggle" id="dropdown2" role="button" data-toggle="dropdown" data-target="#" href="#">
	              <div class="input-group">
	                <input type="text" class="form-control" data-ng-model="data.dateDropDownInput2" required disabled>
	                <span class="input-group-addon">
	                  <i class="glyphicon glyphicon-calendar"></i>
	                </span>
	              </div>
	            </a>
	            <ul class="dropdown-menu" role="menu" aria-labelledby="dLabel">
	              <datetimepicker data-ng-model="data.dateDropDownInput2" data-datetimepicker-config="{ dropdownSelector: '#dropdown2' , minuteStep : 2,  modelType : 'YYYY-MM-DD HH:mm:ss'}"/>
	            </ul>
	          </div>
          	  <div class="col-md-2">
          	  </div>
        	</div>
			<br>	
			<br>	
           <div class="col-md-6">
	       	 <div class="my-chart">
	            <linechart data="chartData" options="options"></linechart>
	          </div>
			</div>
            <div class="col-md-6">
	          <div id="map">
	          </div>
			</div>
          <br>
      	  <br>
      	  <div>
	      	  <div class="col-md-6">
<!--  	      	  <input class="btn btn-default" type="button" value="DUMMY" ng-click="change()"> -->
		          <input class="btn btn-default" type="button" value="MIN" ng-model="data" ng-click="loadChart(data,'min')">
		          <input class="btn btn-default" type="button" value="HOUR" ng-model="data" ng-click="loadChart(data,'hour')">
		          <input class="btn btn-default" type="button" value="DAYPART" ng-model="data" ng-click="loadChart(data,'daypart')">
		          <input class="btn btn-default" type="button" value="DAY" ng-model="data" ng-click="loadChart(data,'day')">
		          <input class="btn btn-default" type="button" value="WEEK" ng-model="data" ng-click="loadChart(data,'week')">
		          <input class="btn btn-default" type="button" value="MONTH" ng-model="data" ng-click="loadChart(data,'month')">
		          <input class="btn btn-default" type="button" value="YEAR" ng-model="data" ng-click="loadChart(data,'year')">
		      </div>
	      	  <div class="col-md-6">
	      	  </div>
		  </div>
          <br>
          <br>
      	  <div class="col-md-12 button">
	          <input class="btn btn-primary" type="submit" value="Plot Speed & Location">
	      </div>
        </form>
      </div>
    </div>
  </body>
</html>