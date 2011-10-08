<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
<style type="text/css">
  html { height: 100% }
  body { height: 100%; margin: 0px; padding: 0px }
  #map_canvas { height: 100% }
</style>
<script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=false&libraries=geometry"></script>
<script>
function initialize() {
    var latlng = new google.maps.LatLng(51.50, -0.12);
    var myOptions = {
      zoom: 13,
      center: latlng,
      mapTypeId: google.maps.MapTypeId.ROADMAP
    };
    map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
	google.maps.event.addListener(map, 'click', function(event) {
		document.upload.lat.value = event.latLng.lat();
		document.upload.lng.value = event.latLng.lng();
	});
  }
</script>
<title>Upload a photo</title>
</head>
<body onload="initialize()">
<div style="position: relative; float: left; width:100%; height: 25%; font-family: Arial; margin: 10px">
<a href="index.jsp">Edit neighbourhoods</a> | Upload a photo | <a href="photos.jsp">View photos</a>
<form enctype="multipart/form-data" action="api/app/photo" name="upload" method="POST">
<br>Select photo <input type="file" name="myPhoto"></input><br>
Click on map to set latitude <input type="text" name="lat" size="10"></input> and longitude <input type="text" name="lng" size="10"></input><br>
Caption <input type="text" name="caption"></input><br>
User <input type="text" name="user"></input><br>
<input type="submit" name="submit" value="submit"></input>
</form>
</div>
<div id="map_canvas" style="position: relative; float: left; width:100%; height:75%"></div>
</body>
</html>