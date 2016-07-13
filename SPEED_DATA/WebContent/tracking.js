      angular
        .module('chartApp', ['n3-line-chart','ui.bootstrap.datetimepicker'])
          .controller('ChartControl',['$http','$scope','$window', function($http,$scope,$window) {
 
//function to load the list of drivers        	  
             getDrivers = function() {
                 $http.get('/SPEED_DATA/getDrivers')
                   .success(function(data){
                   	console.log(data);
                     $scope.driverList = data;
                   });
                 };

//loading list of drivers on page load                 
             getDrivers();

//function to load the list of vehicles        	  
             getVehicles = function() {
                 $http.get('/SPEED_DATA/getVehicles')
                   .success(function(data){
                   	console.log(data);
                     $scope.vehicleList = data;
                   });
                 };

//loading list of vehicles on page load                 
             getVehicles();

//updates Vehicle on changing Driver 
          $scope.updateDriver = function(data){
                $scope.data.selectedVehicle = {'id':$scope.data.selectedDriver};
                $scope.data.selectedDriver = {'id':$scope.data.selectedDriver};
		};

//updates Driver on changing Vehicle 
          $scope.updateVehicle = function(data){
              $scope.data.selectedDriver = {'id':$scope.data.selectedVehicle};
              $scope.data.selectedVehicle = {'id':$scope.data.selectedVehicle};
          };

//get logout
          $scope.logout = function() {
              $http.get('/SPEED_DATA/getLogout')
              	.success(function(data){
            	  $window.location.href = '/SPEED_DATA';
              });
          };

//function to trigger both chart and map at the same time          
          $scope.loadChartAndMap = function(data,filter) {
				if(angular.isUndefined($scope.data) || angular.isUndefined($scope.data.selectedVehicle) 
						|| angular.isUndefined($scope.data.dateDropDownInput1)
						 || angular.isUndefined($scope.data.dateDropDownInput2))
					$window.alert("Please select a driver, from-datetime and upto-datetime");
				else if ($scope.data.dateDropDownInput2 <= $scope.data.dateDropDownInput1)
					$window.alert("Please select upto-datetime after from-datetime");
				else {
		        	  $scope.loadChart(data,filter);
		        	  $scope.loadMap(data,filter);					
				}
          };

//function to load chart with data from servlet
          $scope.loadChart = function(data,filter) {
			if(angular.isUndefined($scope.data) || angular.isUndefined($scope.data.selectedVehicle) 
					|| angular.isUndefined($scope.data.dateDropDownInput1)
					 || angular.isUndefined($scope.data.dateDropDownInput2))
				$window.alert("Please select a driver, from-datetime and upto-datetime");
			else if ($scope.data.dateDropDownInput2 <= $scope.data.dateDropDownInput1)
				$window.alert("Please select upto-datetime after from-datetime");
            else{
            console.log(data);
            $http({
                  method  : 'POST',
                  url     : '/SPEED_DATA/getChartPoints',
                  data    : { 'driverid' : data.selectedDriver.id,
                              'fromDate' : data.dateDropDownInput1,
                              'uptoDate' : data.dateDropDownInput2,
                              'filter' : filter}, 
                  headers : {'Content-Type': 'application/x-www-form-urlencoded'} 
                 })
                  .success(function(data) {
                    if (data.errors) {
                      // Showing errors.
                      $scope.errorName = data.errors.name;
                      $scope.errorUserName = data.errors.username;
                      $scope.errorEmail = data.errors.email;
                    } else {
                      console.log(data);
                      $scope.data2 =[];
                      for(i=0;i<data.chartList.length;i++)
                   	  {
                          $scope.chartElement = {datetime:'',speed:0};
                          $scope.chartElement.speed = data.chartList[i].speed;
                          $scope.chartElement.datetime = new Date(data.chartList[i].datetime);
						  $scope.data2.push($scope.chartElement);
                   	  }
                      $scope.data3 = {chartList:$scope.data2};
                      $scope.chartData = $scope.data3;
                      console.log($scope.data2);
                    }
                  });
            }
          };

//options for maps
          var mapOptions = {
              zoom: 6,
              center: new google.maps.LatLng(40.0000, -98.0000),
              mapTypeId: google.maps.MapTypeId.TERRAIN
          }          

//initializing the parameters          
          $scope.map = new google.maps.Map(document.getElementById('map'), mapOptions);
          $scope.markers = [];
          var infoWindow = new google.maps.InfoWindow();

          var poly = new google.maps.Polyline({
        	    strokeColor: '#00F600',
        	    strokeOpacity: 1.0,
        	    strokeWeight: 2
        	  });
          
//function which creates markers from lat,lng data            
          var createMarker = function (info){
        	  var path = poly.getPath();
              var marker = new google.maps.Marker({
                  map: $scope.map,
                  position: new google.maps.LatLng(info.lat, info.lng),
                  animation: google.maps.Animation.DROP,
                  title: info.date
              });
	        	 path.push(new google.maps.LatLng(info.lat, info.lng));
              	 marker.content = '<div class="infoWindowContent">' + info.date + ' ' + info.minutes + '</div>';
          		 google.maps.event.addListener(marker, 'click', function(){
        	     toggleBounce(marker);
                 infoWindow.setContent('<h2> Time </h2>' + marker.content);
                 infoWindow.open($scope.map, marker);
                 
             });
              $scope.markers.push(marker);
          }  

//bounce animation on clicking a marker
          function toggleBounce(marker) {
        	  if (marker.getAnimation() !== null) {
        	    marker.setAnimation(null);
        	  } else {
        	    marker.setAnimation(google.maps.Animation.BOUNCE);
        	  }
       	  }

//clearing the existing markers on map
          function clearMarkers() {
        	  setMapOnAll(null);
       	  }

//setting the map with provided markers
          function setMapOnAll(map) {
        	  for (var i = 0; i < $scope.markers.length; i++) {
        		  $scope.markers[i].setMap(map);
        	  }
          }

//removing existing markers          
          function deleteMarkers() {
        	  clearMarkers();
        	  $scope.markers = [];
       	  }

//loads markers received from servlet on maps
          $scope.loadMap = function(data,filter) {
            console.log(data);
            $http({
                  method  : 'POST',
                  url     : '/SPEED_DATA/getLatLngValues',
                  data    : { 'driverid' : data.selectedDriver.id,
                              'fromDate' : data.dateDropDownInput1,
                              'uptoDate' : data.dateDropDownInput2,
                              'filter'	 : filter}, 
                  headers : {'Content-Type': 'application/x-www-form-urlencoded'} 
                 })
                  .success(function(data) {
                    if (data.errors) {
                      // Showing errors.
                      $scope.errorName = data.errors.name;
                      $scope.errorUserName = data.errors.username;
                      $scope.errorEmail = data.errors.email;
                    } else {
                      console.log(data);
			          deleteMarkers();
                      var cities = data;
                      poly = new google.maps.Polyline({
                  	    strokeColor: '#00F600',
                  	    strokeOpacity: 1.0,
                  	    strokeWeight: 2
                  	  });
                      $scope.map = new google.maps.Map(document.getElementById('map'), mapOptions);
                      $scope.markers = [];
                      path = [];
                      for (i = 0; i < cities.length; i++){
                        createMarker(cities[i]);
                      }
                      poly.setMap($scope.map);
                    }
                  });
          };
          
          
//Dummy marker shown on page load          
          var cities = [
                        {
                        	date : '2016-02-01',
                            minutes : '02:34',
                            lat : 40.7000,
                            lng : -98.4000
                        }];          
          for (i = 0; i < cities.length; i++){
              createMarker(cities[i]);
          }

//Displays Info window on clicking marker       
          $scope.openInfoWindow = function(e, selectedMarker){
              e.preventDefault();
              google.maps.event.trigger(selectedMarker, 'click');
          }

//Options for n3-charts  
          $scope.options = {
              margin: {top: 5},
              series: [
                {
                  axis: "y",
                  dataset: "chartList",
                  key: "speed",
                  label: "speed",
                  color: "hsla(88, 48%, 48%, 1)",
                  type: ["line"],
                  visible : true
                }
              ],
              axes: {x: {key: "datetime", type: "date", ticks:6}}
            };

//Data shown on dummy button click
/*           $scope.change =function() {
                $scope.chartData = {
                chartList: [
                  {time: 10, speed: 0},
                  {time: 11, speed: 10},
                  {time: 12, speed: 11},
                  {time: 13, speed: 25},
                  {time: 14, speed: 36}
                ]
              };
	      }
 */
 
//Dummy data shown on page load
           $scope.chartData = {
                  chartList: [
                      {time:0 , datetime : new Date('2016-03-01 18:17:23'), speed:0},
                      {time:1 , datetime : new Date("2016-03-02 23:45:42"), speed:6},
                      {time:2 , datetime : new Date("2016-03-03 21:51:20"), speed:20},
                      {time:3 , datetime : new Date("2016-03-04 02:12:34"), speed:10},
                      {time:4 , datetime : new Date("2016-03-05 20:15:57"), speed:16}
                  ]
          };
//         $scope.chartData.chartList.time = new Date($scope.chartData.chartList.datetime);
          }]);