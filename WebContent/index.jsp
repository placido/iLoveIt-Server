<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
<style type="text/css">
  html { height: 100% }
  body { height: 100%; margin: 0px; padding: 0px }
  #map_canvas { height: 100% }
  .style1 {background-color:#ffffff;font-weight:bold;border:2px #006699 solid;}
</style>
<script type="text/javascript" src="jquery/jquery-1.6.2.min.js"></script> 
<script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=false&libraries=geometry"></script>
<script type="text/javascript">
  var circle = new Array();
  var colourOf = new Array();
  var map;
  
  function leftClick(event) {
	var name = prompt("Please enter a neighbourhood name for this marker","");
	if (name!=null && name!="") {
		// HTTP POST the details of this new marker to create it
		$.post('api/cms/marker?name='+name+'&lat='+event.latLng.lat()+'&lng='+event.latLng.lng(), function(data) {
			addMarker(map, event.latLng, name, data._id.$oid);
		})
	}
  }

	function rightClick(event) {
		$.getJSON('api/cms/near?lat='+event.latLng.lat()+'&lng='+event.latLng.lng(), function(data) {
			var myLatlng = new google.maps.LatLng(data.location[0], data.location[1]);
    		var distance = Math.floor(google.maps.geometry.spherical.computeDistanceBetween(event.latLng, myLatlng));
    		if (distance <= 500) {
	    		alert('I am in '+data.name);
    		} else {
    			alert('The closest neighbourhood is '+data.name+' at '+(distance - 500)+'m');
    		}
		})
	}
  
  function getRandomColour() {
	    var letters = '0123456789ABCDEF'.split('');
	    var color = '#';
	    for (var i = 0; i < 6; i++ ) {
	        color += letters[Math.round(Math.random() * 15)];
	    }
	    return color;
	}
	
  function drawCircle(map, myLatlng, name, dbId) {

	  	if (colourOf[name] == null) {
		  	colourOf[name] = getRandomColour();
	  	}
		 var circleOptions = {
			strokeColor: colourOf[name],
			strokeOpacity: 0.9,
			strokeWeight: 2,
			fillColor: colourOf[name],
			fillOpacity: 0.2,
			map: map,
			center: myLatlng,
			radius: 500
		};
		circle[dbId] = new google.maps.Circle(circleOptions);
		google.maps.event.addListener(circle[dbId], 'click', leftClick);
		google.maps.event.addListener(circle[dbId], 'rightclick', rightClick);
		
  }
  
  function addMarker(map, myLatlng, name, dbId) {
	   var marker = new google.maps.Marker({
		position: myLatlng, 
		map: map,
		draggable: true, 
		title: name,
		dbId: dbId
		});
	      
	    // on dragEnd of this marker, HTTP PUT its new position
		google.maps.event.addListener(marker, 'dragend', function(event) {			  			
  			$.ajax({
	  			   url: 'api/cms/marker?dbId='+marker.dbId+'&name='+marker.title+'&lat='+event.latLng.lat()+'&lng='+event.latLng.lng(),
	  			   type: 'PUT',
	  			   success: function( response ) {
	  			   }
	  			});  	
  			circle[marker.dbId].setMap(null);
  			drawCircle(map, event.latLng, marker.title, marker.dbId);
		});
		
		// on left-click of this marker, HTTP DELETE its record
		google.maps.event.addListener(marker, 'click', function(event) {
			var r = confirm ("Remove "+marker.title+ " marker?");
			if (r == true) {
	  			$.ajax({
	  			   url: 'api/cms/marker?dbId='+marker.dbId,
	  			   type: 'DELETE',
	  			   success: function( response ) {
	  			   }
	  			});  			
				removeMarker(marker);
			}
		});   

		drawCircle(map, myLatlng, name, dbId)
  }
  
  function removeMarker(marker) {
  		marker.setMap(null);
  		circle[marker.dbId].setMap(null);
  }
  
  function initialize() {
    var latlng = new google.maps.LatLng(51.50, -0.12);
    var myOptions = {
      zoom: 13,
      center: latlng,
      mapTypeId: google.maps.MapTypeId.ROADMAP
    };
    map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
  
	google.maps.event.addListener(map, 'click', leftClick);
	google.maps.event.addListener(map, 'rightclick', rightClick);
	$(document).ready(function() { 
		$(document).ajaxError(function (event, request, settings, thrownError) {
    		alert('error: '+thrownError);
		});
		// override JSON file type for local files only
		$.ajaxSetup({'beforeSend': function(xhr){
    	if (xhr.overrideMimeType)
        	xhr.overrideMimeType("application/json");
    	}
		})    
		$.getJSON('api/cms/marker?'+ new Date().getTime(), function(data) {
			$.each(data.neighbourhoods,function(i,hood){
			   var myLatlng = new google.maps.LatLng(hood.location[0], hood.location[1]);
			   addMarker(map, myLatlng, hood.name, hood._id.$oid);
			});
		});
   }); 
  }
</script>

 
<title>Edit neighbourhoods</title>
</head>
<body onload="initialize()">
<div style="position: relative; float: left; width:100%; height: 25%; font-family: Arial; margin: 10px">
Edit neighbourhoods | <a href="upload.jsp">Upload a photo</a> | <a href="photos.jsp">View photos</a>
<ul>
	<li>Hover over a marker to see the name of the neighbourhood</li>
	<li>Left-click on map to create a marker</li>
	<li>Left-click on marker to delete it</li>
	<li>Right-click on map to find the closest neighbourhood</li>
	<li>Drag marker to move it</li>
</ul>
</div>
  <div id="map_canvas" style="position: relative; float: left; width:100%; height:75%"></div>
</body>
</html>
