<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
  <head>
    <title>Geospatial Search - GoMonitor</title>
    <meta name="viewport"
        content="width=device-width, initial-scale=1.0, user-scalable=no">
    <meta charset="UTF-8">
	<link rel="stylesheet" href="/goma/web/css/jquery-ui-1.8.6.custom.css">
    <link rel="stylesheet" href="/goma/web/css/search.css">
    <link rel="stylesheet" type="text/css" href="/goma/web/css/jquery.multiselect.css" />
	<link rel="stylesheet" type="text/css" href="/goma/web/css/jquery.multiselect.filter.css" />
	<link rel="stylesheet" type="text/css" href="/goma/web/css/tipTip.css" />	
	<script type="text/javascript" language="javascript" src="/goma/web/js/jquery-1.7.1.min.js"></script>
	<script type="text/javascript" language="javascript" src="/goma/web/js/jquery-ui-1.8.6.custom.min.js"></script>
	<script type="text/javascript" src="/goma/web/js/jquery.dataTables.min.js"></script>
	<script type="text/javascript" src="/goma/web/js/statics.js"></script>
	<script type="text/javascript" src="/goma/web/js/jquery.multiselect.min.js"></script>
	<script type="text/javascript" src="/goma/web/js/jquery.multiselect.filter.js"></script>
	<script type="text/javascript" src="/goma/web/js/jquery.tipTip.minified.js"></script>
		
    <script type="text/javascript"
        src="http://maps.googleapis.com/maps/api/js?sensor=false"></script>

    <script type="text/javascript">
    
	    var map, gzList, poly;
	    var markersArray = [];
	    var markers = [];
	    var path = new google.maps.MVCArray;
	    var stationResults = "";
	    
	    var requestCount = 0;
	    function initialize() {
	      var myOptions = {
				zoom: 6,
				center: new google.maps.LatLng(28.26568, -90.00),
				mapTypeId: google.maps.MapTypeId.TERRAIN,
				panControl: false,
				zoomControl: true,
				zoomControlOptions: {
					position: google.maps.ControlPosition.RIGHT_TOP
				},
				scaleControl: true,
				streetViewControl: false,
				mapTypeControl: true,
				mapTypeControlOptions: {
					style: google.maps.MapTypeControlStyle.DROPDOWN_MENU,
					position: google.maps.ControlPosition.TOP_RIGHT
				}
	      };
	      map = new google.maps.Map(document.getElementById('map_canvas'),
	          myOptions);
	      
	      poly = new google.maps.Polygon({
	          strokeWeight: 1,
	          fillColor: '#5555FF'
	        });
	        poly.setMap(map);
	        poly.setPaths(new google.maps.MVCArray([path]));


	    }
	    
	    function addPoint(event) {
	        path.insertAt(path.length, event.latLng);

	        var marker = new google.maps.Marker({
	          position: event.latLng,
	          map: map,
	          draggable: true
	        });
	        markers.push(marker);
	        marker.setTitle("#" + path.length);

	        google.maps.event.addListener(marker, 'click', function() {
	          marker.setMap(null);
	          for (var i = 0, I = markers.length; i < I && markers[i] != marker; ++i);
	          markers.splice(i, 1);
	          path.removeAt(i);
	          }
	        );

	        google.maps.event.addListener(marker, 'dragend', function() {
	          for (var i = 0, I = markers.length; i < I && markers[i] != marker; ++i);
	          path.setAt(i, marker.getPosition());
	          }
	        );
	      }

	 /**
	  * Deletes all markers in the array by removing references to them
	  *
	  */
	    function deleteOverlays() {
	      if (markersArray) {
	        for (i in markersArray) {
	          markersArray[i].setMap(null);
	        }
	        markersArray.length = 0;
	      }
	    }
	 
		google.maps.event.addDomListener(window, 'load', initialize);
    
		$(function() {
			
			$("a").tipTip();
			
			$.get("/goma/utilities", {mode: "listOrganizations"},  loadOrganizations);
			$( "#start_date" ).datepicker();
			$( "#end_date" ).datepicker();
			$( ".formControl" ).click(
				function()
				{
					$(".target").hide('fast');
					$(".formControl").removeClass("formControlActive");
					$("#"+ this.id).addClass("formControlActive");
					$("#"+ this.id + "-target").toggle('fast');
				}
			);
			$("input[name=gzSource]:radio").click(
				function()
				{
					if(this.id == "gzDraw")
					{
						$("#searchPane").hide();
						$("#drawGZ").show("slide", {direction: "left"}, 600);
				        google.maps.event.addListener(map, 'click', addPoint);
					}
					$(".well").hide();
					$("#"+ this.id + "Well").show();
				}
			);
		});
		
		/**
		 * Function to receive the HTTP request response from the organization list request and render it as a select
		 *
		 * @param (HTTPResponse) The object containing the search results
		 */
		function loadOrganizations(response)
		{
			var data = response.data;
			var selStr = "<select multiple = 'multiple'  class = 'picker' name = 'organization_id' id = 'organization'>";
			for(var i = 0; i < data.length; i++)
			{
				selStr += "<option value = '"+data[i].organization_id+"'>"+data[i].name+"</option>";
			}
			selStr += "</select>";
			
			$('#organizationWell').html(selStr);	
			$.get('/goma/utilities', {mode: 'listPrograms'},  loadPrograms);
		}
		
		/**
		 * Function to receive the HTTP request response from the program list request and render it as a select
		 *
		 * @param (HTTPResponse) The object containing the search results
		 */
		function loadPrograms(response)
		{
			var data = response.data;
			var selStr = "<select multiple = 'multiple'  class = 'picker' name = 'program_id' id = 'program'>";
			for(var i = 0; i < data.length; i++)
			{
				selStr += "<option value = '"+data[i].program_id+"'>"+data[i].name+"</option>";
			}
			selStr += "</select>";
			$('#programWell').html(selStr);
			$.get('/goma/utilities', {mode: 'listProjects'},  loadProjects);
		}
		
		/**
		 * Function to receive the HTTP request response from the project list request and render it as a select
		 *
		 * @param (HTTPResponse) The object containing the search results
		 */
		function loadProjects(response)
		{
			var data = response.data;
			var selStr = "<select multiple = 'multiple'  class = 'picker' name = 'project_id' id = 'project'>";			
			for(var i = 0; i < data.length; i++)
			{
				selStr += "<option value = '"+data[i].project_id+"'>"+data[i].name+"</option>";
			}
			selStr += "</select>";
			$('#projectWell').html(selStr);
			$.get("/goma/utilities", {mode: "listTuples"},  loadTuples);		
		}
		
		function populateSearchOptions()
		{

			termIds = new Array(Statics.PROPRIETARY_RESTRICTIONS, Statics.AVAILABILITY, Statics.PURPOSE_CATEGORY, Statics.SOURCE_MEDIUM, Statics.OBSERVATION_CATEGORY, Statics.OBSERVATION_TYPE, Statics.METHOD, Statics.PROJECT_METHODOLOGY);
			if(requestCount < termIds.length)
			{
				fetchTermsList(termIds[requestCount]);
				requestCount++;
			}
			else
			{
				$.get("/goma/utilities", {mode: "listStatusValues", status_type_id: Statics.PROGRAM_STATUS}, loadStatusLists);
			}
		}
		
		/**
		 * Function to request the list of dictionary terms from the servlet
		 *
		 */
		function fetchTermsList(dictionary_id)
		{
			$.get("/goma/utilities", {mode: "listDictionaryTerms", dictionary_id: dictionary_id}, loadTermsList);
		}
		
		
		/**
		 * Function to receive the HTTP request response from dictionary terms request and render it as a select
		 *
		 * @param (HTTPResponse) The object containing the list of terms			 
		 */
		function loadTermsList(response)
		{
			var termsStr = "<select multiple = 'multiple'  class = 'picker' name = '"+response.code.toLowerCase()+"_id' id ='"+response.code.toLowerCase()+"_id'>";
			var terms = response.data;
			for(var i = 0; i < terms.length; i++)
			{
				termsStr += "<option value = '"+terms[i].term_id+"'>"+terms[i].name+"</option>";
			}
			termsStr += "</select>";
			$("#"+response.code.toLowerCase()+"_well").html(termsStr);
			populateSearchOptions();
			
		}
		
		/**
		 * Function to receive the HTTP request response from status list request and render it as a select
		 *
		 * @param (HTTPResponse) The object containing the list of status values			 
		 */
		function loadStatusLists(response)
		{
			var statusStr = "";
			var statuses = response.data;
			for(var i = 0; i < statuses.length; i++)
			{
				statusStr += "<option value = '"+statuses[i].status_id+"'>"+statuses[i].value+"</option>";
			}
			statusStr += "</select>";
			$("#program_status_well").html("<select multiple = 'multiple' class = 'picker' name = 'program_status_id' id ='program_status_id'>"+statusStr);
			$("#project_status_well").html("<select multiple = 'multiple' class = 'picker' name = 'project_status_id' id ='project_status_id'>"+statusStr);
			$("#station_status_well").html("<select multiple = 'multiple'  class = 'picker' name = 'station_status_id' id ='station_status_id'>"+statusStr);
			fetchOrganizationTypes();
		}
	  	
		/**
		 * Function to request the list of organization types from the servlet
		 *
		 */
		function fetchOrganizationTypes()
		{
			$.get("/goma/utilities", {mode: "listOrganizationTypes"}, loadOrganizationTypes);
		}
		
		
		/**
		 * Function to receive the HTTP request response from organization types request and render it as a select
		 *
		 * @param (HTTPResponse) The object containing the list of organization types
		 */
		function loadOrganizationTypes(response)
		{
			var orgTypeStr = "<select multiple = 'multiple' class = 'picker' name = 'organization_type_id' id ='organization_type_id'>";
			var orgTypes = response.data;
			for(var i = 0; i < orgTypes.length; i++)
			{
				orgTypeStr += "<option value = '"+orgTypes[i].type_id+"'>"+orgTypes[i].type+"</option>";
			}
			orgTypeStr += "</select>";
			$("#organization_type_well").html(orgTypeStr);
			$.get("/goma/utilities", {mode: "listGZ", type: "name"}, loadGZList);
		}
		
		function loadGZList(response)
		{
			var data = response.data;
			gzList = data; 
			var selStr = "<select name = 'gz' id = 'gzPreDefId'>";			
			for(var i = 0; i < data.length; i++)
			{
				selStr += "<option value = '"+data[i].gz_id+"'>"+data[i].name+"</option>";
			}
			selStr += "</select>";
			$("#gzPreDefWell").html(selStr);
			$("#status").hide();
		}
		
		function fetchStationsByAdHocGZ(wkt)
		{
			if(wkt == "false")
			{
				gz_str = "POLYGON((";
				for(var i = 0; i < markers.length; i++)
				{
					gz_str += markers[i].getPosition().lng() + " " + markers[i].getPosition().lat();
					if(i != markers.length - 1)
						gz_str += ",";
					else
						gz_str += "," + markers[0].getPosition().lng() + " " + markers[0].getPosition().lat();
				}
				gz_str += "))";
				$("#gzWKTtext").val(gz_str);
			}
			$("#drawGZ").hide();
			$("#searchPane").show(); 
		}
		
		function fetchStationsByGZ()
		{
			var selGZ = $("#gzPreDefId").val();
			var polygonCoords;
			for(var i = 0; i < gzList.length; i++)
			{
				if(gzList[i].gz_id == selGZ)
				{
					polygonCoords = new Array(gzList[i].location.coordinates.length);
					for(var j = 0; j < gzList[i].location.coordinates.length; j++)
					{
						polygonCoords[j] = new google.maps.LatLng(gzList[i].location.coordinates[j].latitude, gzList[i].location.coordinates[j].longitude);
					}
				}
			}	
			var currPolygon = new google.maps.Polygon({
			    paths: polygonCoords,
			    strokeColor: "#0000FF",
			    strokeOpacity: 0.8,
			    strokeWeight: 1,
			    fillColor: "#0000AA",
			    fillOpacity: 0.25
			  });
			  currPolygon.setMap(map);
			$.get("/goma/utilities", {mode: "listStationsByGZ", gz_id: selGZ}, loadResults);
		}
		
		function fetchStations()
		{
			$.get("/goma/utilities", {mode: "listStations", project_id: $("#project").val()},  loadResults);
			$('#browsePane').hide('fast');
		}
		
		function fetchResults()
		{
			/*
			if($('input:radio[name=gzSource]:checked').val() == "wkt")
			{
				if($("#gzWKTText").val().length < 10)
				{
						alert("WKT String is Too Short");
						return false;
				}
			}*/
			stationResults = "";
			$.get("/goma/utilities", $("#searchForm").serialize(),  loadResults);
			$('#searchPane').hide('fast');
			return false;
		}
		
		function loadResults(response)
		{
			clearMap();
			
			var infowindow = null;
			var data = response.data;
			var aDataSet = "[ ";
		    infowindow = new google.maps.InfoWindow({
		        content: "loading..."
		    });
			for(var i = 0; i < response.data.length; i++)
			{
				stationResults += data[i].station_id;
				if(i != (data.length - 1))
					stationResults += ",";
				aDataSet += '["<a target = \'_blank\' href = \'/goma/details?type=station&station_id='+data[i].station_id+'\'>'+ data[i].name  + '</a>' +
							'","'+data[i].status +
							'","'+data[i].project +
							'","'+data[i].program +
							'","'+data[i].organization + '"';
				aDataSet += ']';
				if(i != (data.length - 1))
						aDataSet += ",";
				
				$("#dlLink").attr("href", "download-results?stationList="+stationResults);
				
				//resultString += "<tr><td>"+response.data[i].name+"</td><td>"+response.data[i].project+"</td><td>"+response.data[i].program+"</td></tr>";
				var coords = new google.maps.LatLng(data[i].location.coordinates.latitude, data[i].location.coordinates.longitude);
				var contentString = "<b>"+data[i].name+"</b><br>Project: "+data[i].project+"<br>Program: "+data[i].program+"<br><a target = '_blank' href = '/goma/details?type=station&station_id="+data[i].station_id+"'>View Details</a>";

			    
				var marker = new google.maps.Marker({
					  position: coords,
					  map: map,
					  title: data[i].name,
					  icon: "/goma/web/images/marker.png",
					  html: contentString
				  });
			    google.maps.event.addListener(marker, 'click', function() {
			    	infowindow.setContent(this.html);
			        infowindow.open(map,this);
			      });
			    markersArray.push(marker);
			    //alert("adding marker: "+data[i].name)
			}
			aDataSet += " ]";
			var colSet;
			colSet = "[ { \"sTitle\": \"Station\"}, { \"sTitle\": \"Status\" }, { \"sTitle\": \"Project\" }, { \"sTitle\": \"Program\" }, { \"sTitle\": \"Organization\" } ]";

			$('#dynamic').html( '<table cellpadding="0" cellspacing="0" border="0" class="display" id="stationTable"></table>' );
			var oTable2 = $('#stationTable').dataTable( {
					"aaData": eval(aDataSet),
					"aoColumns": eval(colSet),
					"sDom": '<"top"flp<"clear">>rt<"bottom"<"clear">>',
					"aLengthMenu": [[10, 25, 50, -1], [10, 25, 50, "All"]]
			} );


			//We have a polygon based query
			if(response.polygon)
			{
				for(var j = 0; j < response.polygon.length; j++)
				{
					var polygonCoords = new Array(response.polygon[j].coords.length);
					for(var k = 0; k < response.polygon[j].coords.length; k++)
					{
						polygonCoords[k] = new google.maps.LatLng(response.polygon[j].coords[k].latitude, response.polygon[j].coords[k].longitude);
					}
					poly = new google.maps.Polygon({
				    paths: polygonCoords,
				    strokeColor: "#0000FF",
				    strokeOpacity: 0.8,
				    strokeWeight: 1,
				    fillColor: "#0000AA",
				    fillOpacity: 0.25
				  });
				  poly.setMap(map);
				}
				
			}
			
			$("#obs-table").show("slide", {direction: "down"}, 600);
			return false;
		}
		
		/**
		 * Function to display observation tuples as a table
		 *		 
		 */
		function loadTuples(response)
		{			
			var data = response.data;
			
			var aDataSet = "[ ";
			for(var i = 0; i < data.length; i++)
			{
				aDataSet += '["<input type = \'checkbox\' name = \'tuple\' value = \''+data[i].tuple_id+'\'>'+
							'","'+ data[i].medium  +
							'","'+data[i].category +
							'","'+data[i].type +
							'","'+data[i].method + '"';
				aDataSet += ']';
				if(i != (data.length - 1))
						aDataSet += ",";
			}
			aDataSet += " ]";

			var colSet;

			colSet = "[ { \"sTitle\": \"Select\"}, { \"sTitle\": \"Medium\" }, { \"sTitle\": \"Category\" }, { \"sTitle\": \"Type\" }, { \"sTitle\": \"Method Of Analysis\" } ]";

 //                       alert(aDataSet);
//                        alert(colSet);
			$('#tupleContainer').html( '<table cellpadding="0" cellspacing="0" border="0" class="display" id="tupleTable"></table>' );
			var oTable1 = $('#tupleTable').dataTable( {
					"aaData": eval(aDataSet),
					"aoColumns": eval(colSet),
					"sDom": '<"top"<"clear">>rt<"bottom"<"clear">>',
					"iDisplayLength": 20,
					"aLengthMenu": [[25, 50, 100], [25, 50, 100]]
			} );

			var filterStr = "<tr><th>&nbsp;</th>";
			filterStr += '<th><input type="text" name="filterMedium" value="Filter" class="search_init1 dt1" /></th>';
			filterStr += '<th><input type="text" name="filterCategory" value="Filter" class="search_init1 dt1" /></th>';
			filterStr += '<th><input type="text" name="filterType" value="Filter" class="search_init1 dt1" /></th>';
			filterStr += '<th><input type="text" name="filterMethod" value="Filter" class="search_init1 dt1" /></th>';
			filterStr += "</tr>";
			
			$("table#tupleTable thead").append(filterStr);
			var asInitVals = new Array();
			$("thead input").keyup( function () {
				//alert("searching for "+this.value+" in column "+($("thead input").index(this)+2));
				if ( $("thead input").index(this) < 5 ) 
					oTable1.fnFilter( this.value, ($("thead input").index(this)+1) );
			} );
			
			$("thead input").each( function (i) {
				if ( $(this).hasClass("dt1") )
					asInitVals[this.name] = this.value;
			} );
			
			$("thead input").focus( function () {
				
				if ( $(this).hasClass("search_init1") )
				{
					$(this).removeClass("search_init1");
					this.value = "";
				}
			} );

			$("thead input").blur( function (i) {
				if ( $(this).hasClass("dt1") )
			    {
			        $(this).addClass("search_init1");
					this.value = asInitVals[this.name];
				}
			} );
			populateSearchOptions();
		}
		
		function clearMap()
		{
			deleteOverlays();
			poly.setMap(null);
			for(var i = 0; i < markers.length; i++)
				markers[i].setMap(null);
			markers = [];
			path = null;
			path = new google.maps.MVCArray;
		}
		
		
    </script>
  </head>
<body>
	<div id = "status"><img src = "/goma/web/images/loader.gif" style = "float: left;"><span id = "loading">Loading...</span></div>
    <div id="map_canvas"></div>
    <div id = "title"></div>
	<div id = "control">
		<a class = "menu search" href = "#" onClick = "$('#searchPane').toggle('slow');$('select.picker').multiselect().multiselectfilter();" title = "Search Catalog">Search</a>
		<a class = "menu grid" href = "#" onClick = "$('#obs-table').toggle('slow');" title = "Show/Hide Search Results Table">Results</a>
		<a class = "menu home" href = "/goma" title = "Go to the home page">Home</a>
	</div>

	<div id = "drawGZ" class = "helpDescription">
		<div class = "paneControl">
			<a class = "help" href = "#" onClick="$('#drawHelp').toggle();" title = "Show/Hide Help">Help</a>
			<a class = "minimize" href = "#" onClick="$(this).parent().parent().hide();" title = "Minimize">Hide</a>
		</div>
		<b>Draw Ad-hoc Polygon</b>
		<br>
		Click on the map to start drawing, click NEXT when finished.

		<input type = "button" onClick="fetchStationsByAdHocGZ('false');" value = "NEXT">
		<span id = "drawHelp">
			<p>1. Click to start drawing a polygon<br>2. The polygon completes automatically<br>3. Drag marker to change location<br>4. Click an existing marker to remove it<br>5. Once complete, click the NEXT button to begin querying<br></p>
		</span>
	</div>
	</div>
	<div id = "searchPane">
		<div class = "paneControl">
	 		<a class = "reset" href = "#" onClick="clearMap();" title = "Clear Map">Clear Map</a>
			<a class = "refresh" href = "#" onClick="document.location.reload(true);" title = "Reset Search">Reset</a>
			<a class = "help" href = "#" onClick="$('#searchHelp').toggle();" title = "Show Help for this Pane">Help</a>
			<a class = "minimize" href = "#" onClick="$(this).parent().parent().hide();" title = "Hide">Hide</a>
		</div>
		<form id = "searchForm" onSubmit = "return fetchResults();">
			<div class = "formControlContainer">
				<span class = "controlTitle">Search Stations: </span>	
				<div class = "formControl"></div>
				<input type = "text" name = "q" id = "q"/><input type = "submit" value = "GO">
			</div>
			
			<input type = "hidden" name = "mode" value = "search">
			<div class = "searchOpts">
				
				<br>
				<b>Search Options:</b>
				<table class = 'formTable'>
					<tr>
						<td>Organization Type: </td><td><span id = "organization_type_well"></span> AND </td>
					</tr>
					<tr>
						<td>Organization Name: </td><td><span id = "organizationWell"></span> AND </td>
					</tr>
					<tr>
						<td>Program Status:</td><td><span id = "program_status_well"></span> AND </td>
					</tr>
					<tr>
						<td>Program Name:</td><td><span id = "programWell"></span> AND </td>
					</tr>
					<tr>
						<td>Project Status:</td><td><span id = "project_status_well"></span> AND </td>
					</tr>
					<tr>
						<td>Project Name:</td><td><span id = "projectWell"></span> AND </td>
					</tr>					
					<tr>
						<td>Project Methodology:</td><td><span id = "project_methodology_well"></span> AND </td>
					</tr>
					<tr>
						<td>Data Availability:</td><td><span id = "availability_well"></span> AND </td>
					</tr>
					<tr>
						<td>Proprietary Restrictions: </td><td><span id = "proprietary_restriction_well"></span> AND </td>
					</tr>
					<tr>
						<td>Purpose Category:</td><td><span id = "purpose_category_well"></span> AND </td>
					</tr>
					<tr>
						<td>Station Status:</td><td><span id = "station_status_well"></span> AND </td>
					</tr>
					<tr>
						<td>Source Medium:</td><td><span id = "source_medium_well"></span> AND </td>
					</tr>
					<tr>
						<td>Observation Category:</td><td><span id = "observation_category_well"></span> AND </td>
					</tr>
					<tr>
						<td>Observation Type:</td><td><span id = "observation_type_well"></span> AND </td>
					</tr>
					<tr>
						<td>Analysis Method:</td><td><span id = "method_well"></span> AND </td>
					</tr>					
					<tr>
						<td>Date Range:</td><td><input title = 'Enter the start date of the station here' type = 'text' id = 'start_date' name = 'start_date' class = 'date'> to <input title = 'Enter the end date of the station here' type = 'text' id = 'end_date' name = 'end_date' class = 'date'> AND </td>
					</tr>					
				</table>
				<div id = "geo-target">
					Search within a geographical zone:<br/><br/>
					<input type = "radio" name = "gzSource" id = "gzPreDef" value = "preDef"/>Select Predefined<br/>
					<div id = "gzPreDefWell" class = "well"></div>
					<input type = "radio" name = "gzSource" id = "gzWKT" value = "wkt"/>Enter Polygon in WKT Format<br/>
					<div id = "gzWKTWell" class = "well">
						<textarea rows = "4" cols = "32" id = "gzWKTtext" name = "gz_str"></textarea>
					</div>
					<input type = "radio" name = "gzSource" id = "gzDraw" value = "adHoc">Draw Ad-hoc Polygon<br/>
					<table id ="featuretable">
					     <tbody id="featuretbody"></tbody>
				    </table>
				</div>		
			</div>
		</form>
	</div>
	<div id = "obs-table">		
		<div class = "paneControl">
			<a id = "dlLink" class = "download" href = "#" title = "Download">Download</a>
			<a class = "help" href = "#" onClick="$('#resultsHelp').toggle();" title = "Show Help for this Pane">Help</a>
			<a class = "minimize" href = "#" onClick="$(this).parent().parent().hide();" title = "Minimize">Hide</a>
		</div>

		<span id = "resultsTitle">Search Results</span>
		<div id = "dynamic"></div>
	</div>
	<div id = "goma-logo"><img src = "/goma/web/images/goma-logo.png"></div>
	<div id = "resultsHelp" class = "helpDescription">
		<div class = "paneControl">
			<a class = "minimize" href = "#" onClick="$(this).parent().parent().hide();" title = "Minimize">Hide</a>
		</div>
		<b>Results Help</b>
		<p>Description Goes Here</p>
	</div>
	<div id = "searchHelp" class = "helpDescription">
		<div class = "paneControl">
			<a class = "minimize" href = "#" onClick="$(this).parent().parent().hide();" title = "Minimize">Hide</a>
		</div>
		<b>Search Help</b>
		<p>Select one or more options (e.g,. Organization Type) from the available lists to use to search for stations.  Options are combined using a logical AND, meaning that only stations which meet all criteria will be displayed.</p>
	</div>
	<div id = "browseHelp" class = "helpDescription">
		<div class = "paneControl">
			<a class = "minimize" href = "#" onClick="$(this).parent().parent().hide();" title = "Minimize">Hide</a>
		</div>
		<b>Browse Help</b>
		<p>Select the tabs at at the top to browse stations by Organization, Geographic Zone or Observation Parameters.  Observation Parameters selected as criteria for browsing are combined using a logical OR, meaning that stations that match any of the select Parameters will be displayed.</p>
	</div>
  </body>
</html>
