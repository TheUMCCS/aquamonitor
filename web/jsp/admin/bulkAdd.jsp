<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
  <head>
    <title>Bulk Station Creation - GoMonitor</title>
    <%@ include file = "imports.jsp" %>
    <%@ page import = "edu.miami.ccs.goma.pojos.User" %>

    <script type="text/javascript">
    
    	//The current program that we are working on
    	var project_id = <%=request.getParameter("project_id")%>;
    	var parent_name = "<%=request.getParameter("project_name")%>";
		$(function() {
			registerButtons();			
			registerTooltips();
		});
		
		function init()
		{
		<%
	   		if(request.getParameter("project_id") == null)
	   		{
	   	%>
	    		getProgram();
	    		$(".operations").hide();
	   	<%
	   		}
	   		else
	   		{
	   	%>
		    	processQuery("list");
	   	<%
	   		}
	   	%>
		}
		
		/**
		 * If there is no program ID specified, show a form to get a program
		 *
		 */
		function getProgram()
		{
			<%
			if(((HttpServletRequest) request).isUserInRole("CAU"))
			{
			%>
			$.get("/goma/utilities", {mode: "listPrograms"},  loadPrograms);
			<%
			}
			else
			{
				User curr_user =  (User) session.getAttribute("curr_user"); 
			%>
			$.get("/goma/utilities", {mode: "listPrograms", organization_id: <%=curr_user.getOrganization().getOrganizationId()%>},  loadPrograms);
			<%
			}
			%>	
		}
		
		/**
		 * Function to receive the HTTP request response from the program list request and render it as a select
		 *
		 * @param (HTTPResponse) The object containing the search results
		 */
		function loadPrograms(response)
		{
			var data = response.data;
			var selStr = "<select name = 'curr_prog' id = 'curr_prog' onChange = 'loadProjects();'><option value = '0' selected>Select Program</option>";
			for(var i = 0; i < data.length; i++)
			{
				selStr += "<option value = '"+data[i].program_id+"'>"+data[i].name+"</option>";
			}
			selStr += "</select>";
			$('#dynamic').html(selStr);
		}
		
		/**
		 * Function to receive the HTTP request response from the project list request and render it as a select
		 *
		 * @param (HTTPResponse) The object containing the search results
		 */
		function loadProjects(response)
		{
			$.get("/goma/utilities", {mode: "listProjects", program_id: $("#curr_prog").val()},  function(response)
				{
					var data = response.data;
					var selStr = "<select name = 'curr_proj' id = 'curr_proj' onChange = 'setProject();'><option value = '0' selected>Select Project</option>";
					for(var i = 0; i < data.length; i++)
					{
						selStr += "<option value = '"+data[i].project_id+"'>"+data[i].name+"</option>";
					}
					selStr += "</select>";
					$('#dynamic').html(selStr);
				}		
			);
		}
		
		/**
		 * Set the project_id as selected by the user and call to display station list
		 *
		 */
		 function setProject()
		 {
			project_id = $("#curr_proj").val();
			parent_name = $("#curr_proj option:selected").text();
			$("#project_id").val(($("#curr_proj").val()));
			processQuery("list");
			$(".operations").show();
		 }
		 
		/**
		 * Depending on the operation specified hit the appropriate servlet for data
		 *
		 * @param (String) The operation to be performed on the paging or noop if the first page is to be loaded
		 */
		function processQuery(operation)
		{
			 $.get("/goma/admin/station-ops", {mode: operation, project_id: project_id},  processResponse);
		}
		
		
		/**
		 * Function to receive the HTTP request response from the list request and render it as a table
		 *
		 * @param (HTTPResponse) The object containing the search results
		 */
		function processResponse(response)
		{
			var data = response.data;
			$("#parentName").text(parent_name + ": ");
			$("#currentOp").text(" / Station Listing");
			var aDataSet = "[ ";
			for(var i = 0; i < data.length; i++)
			{
				aDataSet += '["<a href = \'#\' onClick = \'fetch('+data[i].station_id+');\' class = \'details\'>'+ data[i].name + '</a>' +
							'","'+data[i].status;
				if(data[i].website != "")
					aDataSet+='","<a href = \''+data[i].website +'\' class = \'external\' target = \'blank\' title = \'Open Website in New Window\'></a>"';
				else
					aDataSet+='","N/A"';
				aDataSet += ']';			
				if(i != (data.length - 1))
						aDataSet += ",";
			}
			aDataSet += " ]";

			var colSet;

			colSet = "[ { \"sTitle\": \"Name (Click to select)\" }, { \"sTitle\": \"Status\" }, { \"sTitle\": \"Website\" } ]";

//                        alert(aDataSet);
//                        alert(colSet);
			$('#dynamic').html( '<table cellpadding="0" cellspacing="0" border="0" class="display" id="consoleTable"></table>' );
			oTable = $('#consoleTable').dataTable( {
					"aaData": eval(aDataSet),
					"aoColumns": eval(colSet),
					"sDom": '<"top"fp<"clear">>rt<"bottom"p<"clear">>',
					"iDisplayLength": 25,
					"aLengthMenu": [[25, 50, 100], [25, 50, 100]],
					"aaSorting": [[2,'asc']]
			} );
			$("#mode").val("delete");
			
		}
		
		/**
		 * Function to request the station data from the servlet
		 *
		 * @param (Number) The id of the station to be fetched for editing
		 */
		function fetch(station_id)
		{
			$.get("/goma/admin/station-ops", {mode: "fetch", station_id: station_id}, load);
		}
		
		
		/**
		 * Function to receive the HTTP request response from station fetch request and render it as a form
		 *
		 * @param (HTTPResponse) The object containing the station
		 */		 
		function load(response)
		{
			$("#currentOp").text(" ");
			var formString = "";
			var data = response.data;
			
			for(var i = 0; i < data.length; i++)
			{
				formString += "<div id = 'basicOpts'><table class = 'formTable'>";
				formString += "<tr><td class = 'fieldname required'>Name:</td><td>"+data[i].name+"</td><td></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Description:</td><td>"+data[i].description+"</td><td></td></tr>";
				formString += "<tr><td class = 'fieldname'>Website:</td><td>"+data[i].website+"</td><td></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Status:</td><td>"+data[i].status+"</td><td></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Location:</td><td>"+data[i].location+"</td><td></td></tr>";
				formString += "<tr><td class = 'fieldname'>Start Date:</td><td>"+data[i].start_date+"</td><td></td></tr>";
				formString += "<tr><td class = 'fieldname'>End Date:</td><td>"+data[i].end_date+"</td><td></td></tr>";
				formString += "<tr><td class = 'fieldname'>Date Created:</td><td>"+data[i].date_created+"</td><td></td></tr>";
				formString += "<tr><td class = 'fieldname'>Approval Status:</td><td>"+data[i].approval+"</td><td></td></tr>";
				if(data[i].date_updated != null)
					formString += "<tr><td class = 'fieldname'>Date Updated:</td><td>"+data[i].date_updated+"</td><td></td></tr>";
				formString += "</table>";
				formString += "<input type = 'hidden' name = 'station_id' id = 'station_id' value = '"+data[i].station_id+"'/>";
				formString += "<input type = 'submit' value = 'Bulk Add'>";
			}
			
			
			formString += "</div>";


			
			$("#dynamic").html(formString);
			$("#mode").val("update");
			registerTooltips('basicOpts');
			registerButtons();
			$( "#start_date" ).datepicker();
			$( "#end_date" ).datepicker();
		}

		
		function bulkCreate()
		{
			if($('input:checkbox[name=station_id]:checked').length != 1)
			{
				setMessage("Please select exactly one station", "warning");
				return;
			}
			
		
		}
		
    </script>
  </head>
<body>
		<%@ include file = "header.jsp" %>
		<%@ include file = "menu.jsp" %>
	<div class = "console">
		<div class = "page-title"><span id = "parentName"></span>Manage Stations - Bulk Creation<span id = "currentOp"></span></div>
		<div class = "operations">
			<ul>

			</ul>
		</div>
		<div class = "blurb">
			Bulk create multiple stations for a project using 3 easy steps:
			<ol>
				<li>Download the CSV template file from <a href = "/goma/web/downloads/station_template.csv">here</a> and fill in your data</li>
				<li>Select a template station from below</li>
				<li>Upload the filled-in CSV file and click 'Bulk Add' below</li> 
			</ol>
			You only need to fill in the name, description and location for each station in the CSV file. The system will automatically populate all other fields (including observations) for each station in your list from the template station you selected.
			<br/>
		</div>
		<form id = 'dynamicForm' enctype="multipart/form-data" method="post" action = 'bulk-upload'>
			<input type = "file" name = "stationList"/>
			<input type = 'hidden' id = 'mode' name = 'mode' value = 'delete'>
			<input type = 'hidden' id = 'project_id' name = 'project_id' value = '<%=request.getParameter("project_id")%>'>
			<div id = "dynamic"></div>
		</form>	
	</div>
	
  </body>
</html>