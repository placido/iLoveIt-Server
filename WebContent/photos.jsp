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
<script type="text/javascript">
 


  function initialize() {
    var latlng = new google.maps.LatLng(51.50, -0.12);
    var myOptions = {
      zoom: 13,
      center: latlng,
      mapTypeId: google.maps.MapTypeId.ROADMAP
    };
    map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);

	
	$(document).ready(function() { 
		// override JSON file type for local files only
		$.ajaxSetup({'beforeSend': function(xhr){
    	if (xhr.overrideMimeType)
        	xhr.overrideMimeType("application/json");
    	}
		})                   
		$.getJSON('api/app/nearby?'+ new Date().getTime(), function(data) {
			$.each(data.photos,function(i,photo){
			   var myLatlng = new google.maps.LatLng(photo.location[0], photo.location[1]);
			   var thumbnailUrl = "http://d1w26viojcn1vr.cloudfront.net/<%= pageContext.getServletContext().getInitParameter("environment") %>/uploads/"+photo._id.$oid+"/small.jpeg";   
			   var marker = new google.maps.Marker({
					position: myLatlng, 
					map: map,
					draggable: false, 
					title: photo.caption,
					icon: thumbnailUrl,
					dbId: photo._id.$oid
				});
			    google.maps.event.addListener(marker, 'click', function(event) {
				    var url = "http://d1w26viojcn1vr.cloudfront.net/<%= pageContext.getServletContext().getInitParameter("environment") %>/uploads/"+marker.dbId+"/large.jpeg";
					window.location = url;
				});   
			});
		});
   }); 
  }	
</script>
<script type="text/javascript" src="jquery/jquery-1.6.2.min.js"></script> 
 
<title>View photos</title>
</head>
<body onload="initialize()">
<div style="position: relative; float: left; width:100%; height: 25%; font-family: Arial; margin: 10px">
<a href="index.jsp">Edit neighbourhoods</a> | <a href="upload.jsp">Upload a photo</a> | View photos
<ul>
	<li>Click on marker to view photo</li>
</ul>
</div>
  <div id="map_canvas" style="position: relative; float: left; width:100%; height:75%"></div>
</body>
</html>
