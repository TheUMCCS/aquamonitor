<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<meta charset="UTF-8" />
<html>
  <head>
    <title>Details - GOMA Aqua Monitor</title>
	<link rel="stylesheet" href="/goma/web/css/details.css">		
	<script src="http://cdn.jquerytools.org/1.2.6/full/jquery.tools.min.js"></script>

	<script type="text/javascript" src="/goma/web/js/jquery.dataTables.min.js"></script>
	<script type="text/javascript" src="/goma/web/js/scripts.js"></script>


    <script type="text/javascript">
		$(function() {
		<%
		if(request.getParameter("type").equals("organization"))
			out.print("fetchOrganization("+request.getParameter("organization_id")+");");
		else if(request.getParameter("type").equals("program"))
			out.print("fetchProgram("+request.getParameter("program_id")+");");
		else if(request.getParameter("type").equals("project"))
			out.print("fetchProject("+request.getParameter("project_id")+");");
		else if(request.getParameter("type").equals("station"))
			out.print("fetchStation("+request.getParameter("station_id")+");");	
		else if(request.getParameter("type").equals("gz"))
			out.print("fetchGZ("+request.getParameter("gz_id")+");");
		else if(request.getParameter("type").equals("obsParam"))
			out.print("fetchObsParam("+request.getParameter("obs_param_id")+");");
		else if(request.getParameter("type").equals("obsTuple"))
			out.print("fetchObsTuple("+request.getParameter("obs_tuple_id")+");");
		else if(request.getParameter("type").equals("dictionaryTerm"))
			out.print("fetchDictionaryTerm("+request.getParameter("term_id")+");");
		%>
			registerButtons();
		});

		/**
		 * Function to request the organization data from the servlet
		 *
		 * @param (Number) The id of the organization to be fetched
		 */
		function fetchOrganization(organization_id)
		{
			$.get("/goma/utilities", {mode: "fetchOrganization", organization_id: organization_id}, loadOrganization);
		}
		
		
		/**
		 * Function to receive the HTTP request response from organization fetch request and render it
		 *
		 * @param (HTTPResponse) The object containing the organization
		 */		 
		function loadOrganization(response)
		{
			
			var formString = "<div class = 'page-title'>Organization Details</div>";
			var data = response.data;
			
			for(var i = 0; i < data.length; i++)
			{
				for (var key in data[i]) {
					  if(data[i].hasOwnProperty(key)) {
					    if(data[i][key] == "null")
					    	data[i][key] = "N/A";
					  }
					}
			}			
			for(var i = 0; i < data.length; i++)
			{
				formString += "<table class = 'formTable'>";
				formString += "<tr><td class = 'fieldname required'>Name:</td><td>"+data[i].organization+"</td></tr>";
				formString += "<tr><td class = 'fieldname required'>Description:</td><td>"+data[i].description+"</td></tr>";
				formString += "<tr><td class = 'fieldname required'>Type:</td><td>"+data[i].type+"</td></tr>";
				formString += "<tr><td class = 'fieldname'>Website:</td><td>"+data[i].website+"</td></tr>";
				formString += "</table>";
			}
			
			$("#dynamic").html(formString);
			registerButtons();		
		}

		/**
		 * Function to request the program data from the servlet
		 *
		 * @param (Number) The id of the program to be fetched
		 */
		function fetchProgram(program_id)
		{
			$.get("/goma/utilities", {mode: "fetchProgram", program_id: program_id}, loadProgram);
		}
		
		
		/**
		 * Function to receive the HTTP request response from program fetch request and render it
		 *
		 * @param (HTTPResponse) The object containing the program
		 */		 
		function loadProgram(response)
		{
			var formString = "<div class = 'page-title'>Program Details</div>";
			var data = response.data;
			
			for(var i = 0; i < data.length; i++)
			{
				formString += "<table class = 'formTable'>";
				formString += "<tr><td class = 'fieldname required'>Organization:</td><td>"+data[i].organization+"</td></tr>";
				formString += "<tr><td class = 'fieldname required'>Name:</td><td>"+data[i].name+"</td></tr>";
				formString += "<tr><td class = 'fieldname required'>Description:</td><td>"+data[i].description+"</td></tr>";
				formString += "<tr><td class = 'fieldname'>Website:</td><td>"+data[i].website+"</td></tr>";
				
				formString += "<tr><td class = 'fieldname required'>Program Manager:</td><td>"+data[i].pm_first_name+" "+data[i].pm_last_name+"</td></tr>";
				formString += "<tr><td class = 'fieldname'>Contact Title:</td><td>"+data[i].pm_job_title+"</td></tr>";
				formString += "<tr><td class = 'fieldname'>Contact Email:</td><td>"+data[i].pm_email+"</td></tr>";
				formString += "<tr><td class = 'fieldname'>Contact Phone:</td><td>"+data[i].pm_phone+"</td></tr>";
				formString += "<tr><td class = 'fieldname'>Contact Address:</td><td>"+data[i].pm_address+"</td></tr>";
				formString += "</table>";
			}
					
			$("#dynamic").html(formString);
			
			registerButtons();
		}
		 
		/**
		 * Function to request the project data from the servlet
		 *
		 * @param (Number) The id of the project to be fetched
		 */
		function fetchProject(project_id)
		{
			$.get("/goma/utilities", {mode: "fetchProject", project_id: project_id}, loadProject);
		}
		
		
		/**
		 * Function to receive the HTTP request response from project fetch request and render it
		 *
		 * @param (HTTPResponse) The object containing the project
		 */		 
		function loadProject(response)
		{
			var formString = "<div class = 'page-title'>Project Details</div>";
			var data = response.data;
			
			for(var i = 0; i < data.length; i++)
			{
				formString += "<table class = 'formTable'>";
				formString += "<tr><td class = 'fieldname required'>Organization:</td><td>"+data[i].organization+"</td></tr>";
				formString += "<tr><td class = 'fieldname required'>Program:</td><td>"+data[i].program+"</td></tr>";
				formString += "<tr><td class = 'fieldname required'>Name:</td><td>"+data[i].name+"</td></tr>";
				formString += "<tr><td class = 'fieldname required'>Description:</td><td>"+data[i].description+"</td></tr>";
				formString += "<tr><td class = 'fieldname'>Website:</td><td>"+data[i].website+"</td></tr>";
				formString += "<tr><td class = 'fieldname required'>Availability:</td><td>"+data[i].availability+"</td></tr>";
				formString += "<tr><td class = 'fieldname required'>Proprietary Restrictions:</td><td>"+data[i].proprietary_restriction+"</td></tr>";
				formString += "<tr><td class = 'fieldname'>Proprietary Restriction Type:</td><td>"+data[i].proprietary_restriction_text+"</td></tr>";
				formString += "<tr><td class = 'fieldname required'>Project Methodology:</td><td>"+data[i].project_methodology+"</td></tr>";
				formString += "<tr><td class = 'fieldname required'>Purpose Category:</td><td>"+data[i].purpose_category+"</td></tr>";
				formString += "<tr><td class = 'fieldname required'>Purpose Description:</td><td>"+data[i].purpose_text+"</td></tr>";
				formString += "<tr><td class = 'fieldname'>Geographic Boundary:</td><td>"+data[i].geo_boundary+"</td></tr>";
				formString += "<tr><td class = 'fieldname required'>Data Quality Objective:</td><td>"+data[i].data_quality_obj+"</td></tr>";
				formString += "<tr><td class = 'fieldname required'>Data Usage Limitations:</td><td>"+data[i].usage_limitations+"</td></tr>";
				formString += "<tr><td class = 'fieldname'>Data Website:</td><td>"+data[i].data_link_website+"</td></tr>";
				formString += "<tr><td class = 'fieldname required'>Start Date:</td><td>"+data[i].start_date+"</td></tr>";
				formString += "<tr><td class = 'fieldname required'>End Date:</td><td>"+data[i].end_date+"</td></tr>";
				formString += "<tr><td class = 'fieldname required'>Project Manager:</td><td>"+data[i].pm_first_name+" "+data[i].pm_last_name+"</td></tr>";
				formString += "<tr><td class = 'fieldname'>Contact Title:</td><td>"+data[i].pm_job_title+"</td></tr>";
				formString += "<tr><td class = 'fieldname'>Contact Email:</td><td>"+data[i].pm_email+"</td></tr>";
				formString += "<tr><td class = 'fieldname'>Contact Phone:</td><td>"+data[i].pm_phone+"</td></tr>";
				formString += "<tr><td class = 'fieldname'>Contact Address:</td><td>"+data[i].pm_address+"</td></tr>";
				formString += "<tr><td class = 'fieldname required'>Data Distributor:</td><td>"+data[i].dd_first_name+" "+data[i].dd_last_name+"</td></tr>";
				formString += "<tr><td class = 'fieldname'>Distributor Title:</td><td>"+data[i].dd_job_title+"</td></tr>";
				formString += "<tr><td class = 'fieldname'>Distributor Email:</td><td>"+data[i].dd_email+"</td></tr>";
				formString += "<tr><td class = 'fieldname'>Distributor Phone:</td><td>"+data[i].dd_phone+"</td></tr>";
				formString += "<tr><td class = 'fieldname'>Distributor Address:</td><td>"+data[i].dd_address+"</td></tr>";
				formString += "<tr><td class = 'fieldname'>Date Created:</td><td>"+data[i].date_created+"</td></tr>";
				if(data[i].date_updated != null)
					formString += "<tr><td class = 'fieldname'>Date Updated:</td><td>"+data[i].date_updated+"</td></tr>";
				formString += "</table>";					
			}
			$("#dynamic").html(formString);	
			registerButtons();
		}
		
		/**
		 * Function to request the station data from the servlet
		 *
		 * @param (Number) The id of the station to be fetched
		 */
		function fetchStation(station_id)
		{
			$.get("/goma/utilities", {mode: "fetchStation", station_id: station_id}, loadStation);
		}
		
		
		/**
		 * Function to receive the HTTP request response from station fetch request and render it
		 *
		 * @param (HTTPResponse) The object containing the station
		 */		 
		function loadStation(response)
		{
			var formString = "<div class = 'page-title'>Station Details</div>";
			var data = response.data;
			
			for(var i = 0; i < data.length; i++)
			{
				formString += "<div id = 'basicOpts'><table class = 'formTable'>";
				formString += "<tr><td class = 'fieldname required'>Organization:</td><td><a href = '/goma/details?type=organization&organization_id="+data[i].organization_id+"'>"+data[i].organization+"</a></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Program:</td><td><a href = '/goma/details?type=program&program_id="+data[i].program_id+"'>"+data[i].program+"</a></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Project:</td><td><a href = '/goma/details?type=project&project_id="+data[i].project_id+"'>"+data[i].project+"</a></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Name:</td><td>"+data[i].name+"</td></tr>";
				formString += "<tr><td class = 'fieldname required'>Description:</td><td>"+data[i].description+"</td></tr>";
				formString += "<tr><td class = 'fieldname'>Website:</td><td>"+data[i].website+"</td></tr>";
				formString += "<tr><td class = 'fieldname required'>Status:</td><td>"+data[i].status+"</td></tr>";
				formString += "<tr><td class = 'fieldname required'>Location:</td><td>"+data[i].location+"</td></tr>";
				formString += "<tr><td class = 'fieldname'>Start Date:</td><td>"+data[i].start_date+"</td></tr>";
				formString += "<tr><td class = 'fieldname'>End Date:</td><td>"+data[i].end_date+"</td></tr>";
				formString += "<tr><td class = 'fieldname'>Date Created:</td><td>"+data[i].date_created+"</td></tr>";
				if(data[i].date_updated != null)
					formString += "<tr><td class = 'fieldname'>Date Updated:</td><td>"+data[i].date_updated+"</td></tr>";
				formString += "</table>";
			}
			$("#dynamic").html(formString);
			registerButtons();
			fetchStnObservations(data[0].station_id);

		}
		
		
		
		/**
		 * Function to request the station data from the servlet
		 *
		 * @param (Number) The id of the station to be fetched
		 */
		function fetchGZ(gz_id)
		{
			$.get("/goma/admin/gz-ops", {mode: "fetch", gz_id: gz_id}, loadGZ);
		}
		
		
		/**
		 * Function to receive the HTTP request response from station fetch request and render it
		 *
		 * @param (HTTPResponse) The object containing the station
		 */		 
		function loadGZ(response)
		{
			var formString = "<div class = 'page-title'>Geographic Zone Details</div>";
			var data = response.data;
			
			for(var i = 0; i < data.length; i++)
			{
				formString += "<table class = 'formTable'>";
				formString += "<tr><td class = 'fieldname required'>Name:</td><td>"+data[i].name+"</td></tr>";
				formString += "<tr><td class = 'fieldname required'>Description:</td><td>"+data[i].description+"</td></tr>";
				formString += "<tr><td class = 'fieldname required'>Location:</td><td>"+data[i].location+"</td></tr>";
				formString += "<tr><td class = 'fieldname'>Date Created:</td><td>"+data[i].date_created+"</td></tr>";
				if(data[i].date_updated != null)
					formString += "<tr><td class = 'fieldname'>Date Updated:</td><td>"+data[i].date_updated+"</td></tr>";
				formString += "</table>";
			}		
			
			$("#dynamic").html(formString);
			registerButtons();
		}
		
		
		/**
		 * Function to request the observation parameter data from the servlet
		 *
		 * @param (Number) The id of the observation to be fetched
		 */
		function fetchObsParam(obs_param_id)
		{
			$.getJSON("/goma/admin/obs-param-ops", {mode: "fetch", obs_param_id: obs_param_id}, loadObsParameter);
		}
		
		
		/**
		 * Function to receive the HTTP request response from obs parameter fetch request and render it
		 *
		 * @param (HTTPResponse) The object containing the observation parameter
		 */			
		function loadObsParameter(response)
		{
			var data = response.data;
			var paramStr = "<div class = 'page-title'>Observation Parameter Details</div><table class = 'formTable'>";
			
			for(var i = 0; i < data.length; i++)
			{
				paramStr += "<tr><td class = 'fieldname required'>Organization:</td><td>"+data[i].organization+"</td></tr>";
				paramStr += "<tr><td class = 'fieldname required'>Program:</td><td>"+data[i].program+"</td></tr>";
				paramStr += "<tr><td class = 'fieldname required'>Project:</td><td>"+data[i].project+"</td></tr>";
				paramStr += "<tr><td class = 'fieldname required'>Station:</td><td>"+data[i].station+"</td></tr>";
				paramStr += "<tr><td class = 'fieldname required'>Observation Medium:</td><td>"+data[i].medium+"</td></tr>";
				paramStr += "<tr><td class = 'fieldname required'>Observation Category:</td><td>"+data[i].category+"</td></tr>";
				paramStr += "<tr><td class = 'fieldname required'>Observation Type:</td><td>"+data[i].type+"</td></tr>";
				paramStr += "<tr><td class = 'fieldname required'>Method of Analysis:</td><td>"+data[i].method+"</td></tr>";
				paramStr += "<tr><td class = 'fieldname required'>Sampling Frequency:</td><td>"+data[i].sampling_freq+"</td></tr>";
				paramStr += "<tr><td class = 'fieldname'>Sampling Depth:</td><td>"+data[i].sampling_depth+"</td></tr>";
				paramStr += "<tr><td class = 'fieldname'>Date Created:</td><td>"+data[i].date_created+"</td></tr>";
				if(data[i].date_updated != null)
					paramStr += "<tr><td class = 'fieldname'>Date Updated:</td><td>"+data[i].date_updated+"</td></tr>";
				paramStr += "</table>";			}
			paramStr += "</table>"
			$("#dynamic").html(paramStr);
		}
		
		/**
		 * Function to request the observation tuple data from the servlet
		 *
		 * @param (Number) The id of the tuple to be fetched
		 */
		function fetchObsTuple(obs_tuple_id)
		{
			$.getJSON("/goma/admin/obs-ops", {mode: "fetch", obs_tuple_id: obs_tuple_id}, loadObsTuple);
		}
		
		
		/**
		 * Function to receive the HTTP request response from obs tuple fetch request and render it
		 *
		 * @param (HTTPResponse) The object containing the tuple
		 */			
		function loadObsTuple(response)
		{
			var data = response.data;
			var paramStr = "<div class = 'page-title'>Observed Parameter Details</div><table class = 'formTable'>";
			
			for(var i = 0; i < data.length; i++)
			{
				paramStr += "<tr><td class = 'fieldname required'>Medium:</td><td>"+data[i].medium+"</td></tr>";
				paramStr += "<tr><td class = 'fieldname required'>Observation Category:</td><td>"+data[i].category+"</td></tr>";
				paramStr += "<tr><td class = 'fieldname required'>Observation Type:</td><td>"+data[i].type+"</td></tr>";
				paramStr += "<tr><td class = 'fieldname required'>Analysis Method:</td><td>"+data[i].method+"</td></tr>";
				paramStr += "<tr><td class = 'fieldname'>Date Created:</td><td>"+data[i].date_created+"</td></tr>";
				if(data[i].date_updated != null)
					paramStr += "<tr><td class = 'fieldname'>Date Updated:</td><td>"+data[i].date_updated+"</td></tr>";
				paramStr += "</table>";
			}
			paramStr += "</table>"
			$("#dynamic").html(paramStr);
		}
		
		/**
		 * Function to request the dictionary term data from the servlet
		 *
		 * @param (Number) The id of the dictionary term to be fetched for editing
		 */
		function fetchDictionaryTerm(term_id)
		{
			$.get("/goma/admin/term-ops", {mode: "fetch", term_id: term_id}, loadTerm);
		}
		
		
		/**
		 * Function to receive the HTTP request response from dictionary term fetch request and render it
		 *
		 * @param (HTTPResponse) The object containing the dictionary term
		 */		 
		function loadTerm(response)
		{			
			var formString = "<div class = 'page-title'>Dictionary Term Details</div>";
			var data = response.data;

			for(var i = 0; i < data.length; i++)
			{
				formString += "<table class = 'formTable'>";
				formString += "<tr><td class = 'fieldname required'>Dictionary:</td><td>"+data[i].dictionary+"</td></tr>";
				formString += "<tr><td class = 'fieldname required'>Term:</td><td>"+data[i].name+"</td></tr>";
				formString += "<tr><td class = 'fieldname required'>Description:</td><td>"+data[i].description+"</td></tr>";
				formString += "<tr><td class = 'fieldname'>Date Created:</td><td>"+data[i].date_created+"</td></tr>";
				if(data[i].date_updated != null)
					formString += "<tr><td class = 'fieldname'>Date Updated:</td><td>"+data[i].date_updated+"</td></tr>";
				formString += "</table>";
			}
			$("#dynamic").html(formString);
			registerButtons();		
		}
		
		/**
		 * Depending on the operation specified hit the appropriate servlet for data
		 *
		 * @param (String) The operation to be performed on the paging or noop if the first page is to be loaded
		 */
		function fetchStnObservations(station_id)
		{
			 $.get("/goma/utilities", {mode: "listStnObservations", station_id: station_id},  loadStnObservations);
		}
		
		
		/**
		 * Function to receive the HTTP request response from the list request and render it as a table
		 *
		 * @param (HTTPResponse) The object containing the search results
		 */
		function loadStnObservations(response)
		{
			var data = response.data;
			var aDataSet = "[ ";
			for(var i = 0; i < data.length; i++)
			{
				aDataSet += '["'+ data[i].medium  +
							'","'+data[i].category +
							'","'+data[i].type +
							'","'+data[i].method +
							'","'+data[i].sampling_freq +
							'","'+data[i].sampling_depth +
							'","'+data[i].start_date +
							'","'+data[i].end_date + '"';
				aDataSet += ']';
				if(i != (data.length - 1))
						aDataSet += ",";
			}
			aDataSet += " ]";

			var colSet;

			colSet = "[ { \"sTitle\": \"Medium\" },"
			            +" { \"sTitle\": \"Category\" },"
			            +"{ \"sTitle\": \"Type\" },"
			            +" { \"sTitle\": \"Method Of Analysis\" },"
			            +" { \"sTitle\": \"Sampling Frequency\" },"
			            +" { \"sTitle\": \"Sampling Depth\" },"
            			+" { \"sTitle\": \"Start Date\" },"
            			+" { \"sTitle\": \"End Date\" } ]";
			
//                        alert(aDataSet);
//                        alert(colSet);
			$('#observations').html( '<table cellpadding="0" cellspacing="0" border="0" class="display" id="consoleTable"></table>' );
			oTable = $('#consoleTable').dataTable( {
					"aaData": eval(aDataSet),
					"aoColumns": eval(colSet),
					"sDom": '<"top"fp<"clear">>rt<"bottom"p<"clear">>',
					"iDisplayLength": 25,
					"aLengthMenu": [[25, 50, 100], [25, 50, 100]],
					"aaSorting": [[2,'asc']]
			} );
		}	
		
    </script>
  </head>
<body style = "background: #ffffff;">
    <div id = "title"></div>
	<div class = "console">
		<div class = "page-title"></div>
		<div class = "operations">
		</div>
		<form id = 'dynamicForm'>
			<input type = 'hidden' id = 'mode' name = 'mode' value = 'delete'>
			<div id = "dynamic"></div>
			<div id = "observations"></div>
			<span class = "operation"><a href = "#" onClick = "history.go(-1);">Back</a></span>
			<span class = "operation"><a href = "#" onClick = "window.close();">Close Details</a></span>
		</form>
	</div>
  </body>
    	
</html>