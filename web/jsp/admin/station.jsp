<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
  <head>
    <title>Station Administration Console - GoMonitor</title>
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
		
		
		/**
		 * Initialize the display after the user session is augmented
		 *
		 */
		function init()
		{
			<%
	   		if(request.getParameter("station_id") != null)
	   		{
	   	%>
		    	fetch(<%=request.getParameter("station_id")%>);
		<%
	   		}
		   	else if(request.getParameter("project_id") == null)
	   		{
	   	%>
	    		
	    		$(".operations").hide();
	    		$(".wizardStage").hide();
	    		getProgram();
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
			var selStr = "<br><br>Please select a program first: <br>"+
						"<select name = 'curr_prog' id = 'curr_prog' onChange = 'loadProjects();'><option value = '0' selected>Select Program</option>";
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
					var selStr;
					if(data.length == 0)
						selStr = "<br>There are no projects associated with that program. Please <a href = 'stations'>click here</a> to try again. <br>Note: There may be projects associated with this program that have not been approved yet.";
					else
					{
						selStr ="<br><br>Now, select a project from the list below:<br>" +
									"<select name = 'curr_proj' id = 'curr_proj' onChange = 'setProject();'><option value = '0' selected>Select Project</option>";
						for(var i = 0; i < data.length; i++)
						{
							selStr += "<option value = '"+data[i].project_id+"'>"+data[i].name+"</option>";
						}
						selStr += "</select>";
					}
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
			$("#linked_project_id").val(($("#curr_proj").val()));
			$("#linkStn").attr("href", "link-stations?project_id="+project_id+"&project_name="+parent_name);
			processQuery("list");
			$(".operations").show();
			$(".wizardStage").show();
		 }
		 
		/**
		 * Depending on the operation specified hit the appropriate servlet for data
		 *
		 * @param (String) The operation to be performed on the paging or noop if the first page is to be loaded
		 */
		function processQuery(operation)
		{
			 $.get("/goma/utilities", {mode: "listStations", project_id: project_id},  processResponse);
			 
		}
		
		
		/**
		 * Function to receive the HTTP request response from the list request and render it as a table
		 *
		 * @param (HTTPResponse) The object containing the search results
		 */
		function processResponse(response)
		{
    		$(".wizardStage").show();
			$("#linkedStations").show();
			var data = response.data;
			$("#parentName").text(parent_name + ": ");
			$("#currentOp").text(" / Station Listing");
			var aDataSet = "[ ";
			for(var i = 0; i < data.length; i++)
			{
				aDataSet += '["<input type = \'checkbox\' name = \'station_id\' onClick = \'highlightRow(this);\' value = \''+data[i].station_id+'\'>'+
							'","<a href = \'#\' onClick = \'fetch('+data[i].station_id+');\' class = \'details\'>'+ data[i].name + '</a>' +
							'","'+data[i].status;
				if(data[i].website != "null" && data[i].website.length != 0)
					aDataSet+='","<a href = \''+data[i].website +'\' class = \'external\' target = \'blank\' title = \'Open Website in New Window\'></a>"';
				else
					aDataSet+='","N/A"';
				aDataSet += ']';			
				if(i != (data.length - 1))
						aDataSet += ",";
			}
			aDataSet += " ]";

			var colSet;

			colSet = "[ { \"sTitle\": \"<input type='checkbox' value = '0' name='selectAllB' id='selectAllB' onClick='jqCheckAll(this.id)'>\"}, { \"sTitle\": \"Station Name (Click record to edit)\" }, { \"sTitle\": \"Status\" }, { \"sTitle\": \"Website\" } ]";

//                        alert(aDataSet);
//                        alert(colSet);
			$('#dynamic').html( '<table cellpadding="0" cellspacing="0" border="0" class="display" id="consoleTable"></table>' );
			oTable = $('#consoleTable').dataTable( {
					"aaData": eval(aDataSet),
					"aoColumns": eval(colSet),
					"sDom": '<"top"fp<"clear">>rt<"bottom"p<"clear">>',
					"iDisplayLength": 25,
					"aLengthMenu": [[25, 50, 100], [25, 50, 100]],
					"aaSorting": [[1,'asc']]
			} );
			$("#mode").val("delete");
			$.get("/goma/utilities", {mode: "listLinkedStations", project_id: project_id},  processLinkedStations);
			if(data.length == 0)
				$('#consoleTable').after("NOTE: There may be additional records that have not been approved yet");
		}
		
		/**
		 * Function to receive the HTTP request response from the list request and render it as a table
		 *
		 * @param (HTTPResponse) The object containing the search results
		 */
		function processLinkedStations(response)
		{

			var data = response.data;
			var aDataSet = "[ ";
			for(var i = 0; i < data.length; i++)
			{
				aDataSet += '["<input type = \'radio\' name = \'station_id\' value = \''+data[i].station_id+'\'>'+
							'","'+ data[i].name +
							'","'+data[i].status;
				if(data[i].website != "null" && data[i].website.length != 0)
					aDataSet+='","<a href = \''+data[i].website +'\' class = \'external\' target = \'blank\' title = \'Open Website in New Window\'></a>"';
				else
					aDataSet+='","N/A"';
				aDataSet += ']';			
				if(i != (data.length - 1))
						aDataSet += ",";
			}
			aDataSet += " ]";

			var colSet;

			colSet = "[ { \"sTitle\": \"Select\"}, { \"sTitle\": \"Station Name\" }, { \"sTitle\": \"Status\" }, { \"sTitle\": \"Website\" } ]";

//                        alert(aDataSet);
//                        alert(colSet);
			$('#linkedStations').html( '<table cellpadding="0" cellspacing="0" border="0" class="display" id="linkedStnTable"></table>' );
			oTable = $('#linkedStnTable').dataTable( {
					"aaData": eval(aDataSet),
					"aoColumns": eval(colSet),
					"sDom": '<"top"fp<"clear">>rt<"bottom"p<"clear">>',
					"iDisplayLength": 25,
					"aLengthMenu": [[25, 50, 100], [25, 50, 100]],
					"aaSorting": [[1,'asc']]
			} );
			$("linkedMode").val("deleteLinked");
			
		}
		
		/**
		 * Function to save the data entered in the station form
		 *
		 * 
		 */
		function update()
		{		
			var inputs = $("#dynamicForm").validator();
			$('input, textarea').each(function(){
			    $(this).val(jQuery.trim($(this).val()));
			    $(this).val($(this).val().replace(/\n/g,"<br>"));
			    $(this).val($(this).val().replace(/\"/g,"'"));
			});
			if(!inputs.data("validator").checkValidity())
				return false;	
			$.getJSON("/goma/admin/station-ops", $("#dynamicForm").serialize(), processSave);
		}
		
		
		/**
		 * Function to receive the HTTP request response from user fetch request and render it as a form
		 *
		 * @param (HTTPResponse) The object containing the user
		 */
		 function processSave(response)
		 {
			if(response.code == "success")
			{
				if(response.message != "Delete Successful")
				{
					//processQuery("list");
					fetch(response.station_id);
					$("#currentOp").text(" / Review and Save");
				}
				else
					processQuery("listStations");
			}
			setMessage(response.message, response.code);
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
				formString += "<tr><td class = 'fieldname required'>Name:</td><td><input title = 'Enter the name of the station here' type = 'text' id = 'name' name = 'name' required = 'required' maxlength = '255' value = '"+data[i].name+"'></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Description:</td><td><textarea  title = 'Enter a brief description, no HTML tags' id = 'description' name = 'description' required = 'required'>"+data[i].description+"</textarea></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname'>Website:</td><td><input  title = 'Enter the full address of the station data website starting with http://...' type = 'url' id = 'website' name = 'website' maxlength = '1000' value = '"+data[i].website+"'></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Status:</td><td><span id = 'status_well'>"+data[i].status+"<input type = 'hidden' id = 'status_id' name = 'status_id' value = '"+data[i].status_id+"'></span></td><td><a class = 'edit-control' href = '#' onClick = 'fetchStatusList();' title = 'Click to edit station status'></a></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Geographic Location:<br><a href = 'http://en.wikipedia.org/wiki/Well-known_text' target = '_blank'>More Information about WKT</a></td><td><textarea  title = 'Well Known Text (WKT) format coordinates for the feature of interest(e.g., Station, Geographical Zone).  WKT coordinates are accepted for points, lines and polygons.  Coordinates should be entered in decimal degrees with respect to the WGS 84 datum.  For example point coordinate for a feature of interest in Tallahassee is POINT(-84.3062 30.4496), which is equivalent to W84.3062 N30.4496. Examples of linestring and polygon formats are:<br>LINESTRING(-84.3062 30.4496, -80.05656 26.88135, -80.13451 25.76994)<br>POLYGON((-84.3062 30.4496,-80.05656 26.88135,-80.13451 25.76994,-82.4493 27.0733,-84.3062 30.4496))' id = 'location' name = 'location' required = 'required'>"+data[i].location+"</textarea></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname'>Start Date:</td><td><input title = 'Enter the start date of data collection at this station, if different (e.g. later) than the owning project' type = 'text' id = 'start_date' name = 'start_date' value = '"+data[i].start_date+"'></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname'>End Date:</td><td><input title = 'Enter the end date of data collection at this station, if different (e.g. earlier) than the owning project' type = 'text' id = 'end_date' name = 'end_date' value = '"+data[i].end_date+"'></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Observation Parameters:</td><td>Saved. Click edit icon to view or modify.</td><td><a class = 'edit-control' href = 'edit-obs-params?station_id="+data[i].station_id+"&station_name="+data[i].name+"' title = 'Click to edit observation parameters'></a></td></tr>";
				formString += "<tr><td class = 'fieldname'>Date Created:</td><td>"+data[i].date_created+"</td><td></td></tr>";
				formString += "<tr><td class = 'fieldname'>Approval Status:</td><td>"+data[i].approval+"</td><td></td></tr>";
				if(data[i].date_updated != null)
					formString += "<tr><td class = 'fieldname'>Date Updated:</td><td>"+data[i].date_updated+"</td><td></td></tr>";
				formString += "</table>";
				formString += "<input type = 'hidden' name = 'station_id' id = 'station_id' value = '"+data[i].station_id+"'/>";
			}
			
			
			formString += "<br><span class = 'operation'><a href = '#' onClick = 'update();' class = 'op-save'>Save Changes</a></span></div>";


    		$(".wizardStage").hide();
    		$("#linkedStations").hide();
			$("#dynamic").html(formString);
			$("#mode").val("update");
			registerTooltips('basicOpts');
			registerButtons();
			$( "#start_date" ).datepicker();
			$( "#end_date" ).datepicker();
		}
		
		/**
		 * Function to display the form for new station creation
		 *
		 */
		 function create()
		{
			$("#currentOp").text(" / Adding New");
			var formString = "";
			
			formString += "<div id = 'basicOpts'><span class = 'wizardStage'>Basic Station Information</span><table class = 'formTable'>";
			formString += "<tr><td class = 'fieldname required'>Name:</td><td><input title = 'Enter the name of the station here' type = 'text' id = 'name' name = 'name' required = 'required' maxlength = '255'></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname required'>Description:</td><td><textarea  title = 'Enter a brief description, no HTML tags' id = 'description' name = 'description' required = 'required'></textarea></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname'>Website:</td><td><input  title = 'Enter the full address of the station data website starting with http://...' type = 'url' id = 'website' name = 'website' maxlength = '1000'></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname required'>Status:</td><td><span id = 'status_well'></span></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname required'>Geographic Location:<br><a href = 'http://en.wikipedia.org/wiki/Well-known_text' target = '_blank'>More Information about WKT</a></td><td><textarea  title = 'Well Known Text (WKT) format coordinates for the feature of interest(e.g., Station, Geographical Zone).  WKT coordinates are accepted for points, lines and polygons.  Coordinates should be entered in decimal degrees with respect to the WGS 84 datum.  For example point coordinate for a feature of interest in Tallahassee is POINT(-84.3062 30.4496), which is equivalent to W84.3062 N30.4496. Examples of linestring and polygon formats are:<br>LINESTRING(-84.3062 30.4496, -80.05656 26.88135, -80.13451 25.76994)<br>POLYGON((-84.3062 30.4496,-80.05656 26.88135,-80.13451 25.76994,-82.4493 27.0733,-84.3062 30.4496))' id = 'location' name = 'location' required = 'required'></textarea></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname'>Start Date:</td><td><input title = 'Enter the start date of the station here' type = 'text' id = 'start_date' name = 'start_date'></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname'>End Date Type:</td><td><input type = 'checkbox' id = 'end_date_type' name = 'end_date_type' value = 'unknown' onChange = 'insertEndDate();'/>Unknown/Ongoing</td><td></td></tr>";
			formString += "<tr><td class = 'fieldname'>End Date:</td><td><input title = 'Enter the end date of the station here' type = 'text' id = 'end_date' name = 'end_date'></td><td></td></tr>";
			formString += "</table>";
			formString += "<br><span class = 'operation'><a href = '#' onClick = 'showStationObservationOptions();' class = 'op-next'>Next</a></span></div>";
			
    		$(".wizardStage").hide();
    		$("#linkedStations").hide();
			$("#dynamic").html(formString);
			$("#mode").val("create");
			registerTooltips('basicOpts');
			registerButtons();

			setTimeout("fetchStatusList();", 500);

			$( "#start_date" ).datepicker();
			$( "#end_date" ).datepicker();

		}
		 
		/**
		 * Function to set an arbitrary far away date as the end date for ongoing stations
		 * 
		 */
		function insertEndDate()
		{
			if($("#end_date_type").is(':checked'))
			{
				//Launch date of the Starship Enterprise-D
				$("#end_date").val("05/10/2363");
				//$("#end_date").prop("disabled", true);
				$("#end_date").toggle();
			}
			else
			{
				$("#end_date").val("");
				//$("#end_date").prop("disabled", false);
				$("#end_date").toggle();
			}
		}
		 	
			
		/**
		 * Function to request the list of status values from the servlet
		 *
		 */
		function fetchStatusList()
		{
			$.get("/goma/utilities", {mode: "listStatusValues", status_type_id: Statics.PROGRAM_STATUS}, loadStatusList);
		}
		
		
		/**
		 * Function to receive the HTTP request response from status list request and render it as a select
		 *
		 * @param (HTTPResponse) The object containing the list of status values			 
		 */
		function loadStatusList(response)
		{
			var statusStr = "<select class = 'picker' name = 'status_id' id ='status_id'>";
			var statuses = response.data;
			for(var i = 0; i < statuses.length; i++)
			{
				statusStr += "<option value = '"+statuses[i].status_id+"'>"+statuses[i].value+"</option>";
			}
			statusStr += "</select>";
			$("#status_well").html(statusStr);
		}
		
		/**
		 * Function to request the list of dictionary terms from the servlet
		 *
		 */
		function fetchTermsList(dictionary_id)
		{
			$.get("/goma/admin/term-ops", {mode: "list", dictionary_id: dictionary_id}, loadTermsList);
		}
		
		
		/**
		 * Function to receive the HTTP request response from dictionary terms request and render it as a select
		 *
		 * @param (HTTPResponse) The object containing the list of terms			 
		 */
		function loadTermsList(response)
		{
			var termsStr = "<select class = 'picker' name = '"+response.code.toLowerCase()+"_id' id ='"+response.code.toLowerCase()+"_id'>";
			var terms = response.data;
			for(var i = 0; i < terms.length; i++)
			{
				termsStr += "<option value = '"+terms[i].term_id+"'>"+terms[i].name+"</option>";
			}
			termsStr += "</select>";
			$("#"+response.code.toLowerCase()+"_well").html(termsStr);
		}
		
		/**
		 * Function to show the observation tuple selection options
		 *		 
		 */
		function showStationObservationOptions()
		{
			var inputs = $("#dynamicForm").validator();
			
			if(!inputs.data("validator").checkValidity())
				return false;	
			$('input, textarea').each(function(currIdx, val){
			    $(this).val(jQuery.trim($(this).val()));
			    $(this).val($(this).val().replace(/\n/g,"<br>"));
			    $(this).val($(this).val().replace(/\"/g,"'"));
			    
			    if(currIdx == 6)
			    	redirectPage();
			});
			
		}
		
		function redirectPage()
		{
			$.getJSON("/goma/admin/station-ops", $("#dynamicForm").serialize(), function(response) {
				if(response.code == "success")
					{
						window.location.replace("add-obs-params?station_id="+response.station_id+"&station_name="+$("#dynamicForm #name").val());
					}
				else
					{
						setMessage(response.message, response.code);
						return;
					}
			});
			
		}
		
		function updateLinkedStations()
		{
			$.getJSON("/goma/admin/station-ops", $("#linkedStnForm").serialize(), function(response) {
				if(response.code == "success")
					{
						processQuery("list");
					}
				else
					{
						setMessage(response.message, response.code);
						return;
					}
			});
		}
		
		
    </script>
  </head>
<body>
		<%@ include file = "header.jsp" %>
		<%@ include file = "menu.jsp" %>
	<div class = "console">
		<div class = "page-title"><span id = "parentName"></span>Manage Stations<span id = "currentOp"></span></div>
		<div class = "operations">
			<ul>
				<li class = "operation"><a href = "#" class = "op-list button" onClick = "processQuery('list');">List</a></li>
				<li class = "operation"><a href = "#" class = "op-add button" onClick = "create();">Add New</a></li>
			<%
			if(((HttpServletRequest) request).isUserInRole("CAU"))
			{
			%>
				<li class = "operation"><a href = "#" class = "op-delete button" onClick = "update();">Delete</a></li>
			<%
			}
			%>	
			</ul>
		</div>
		<form id = 'dynamicForm'>
			<span class = "wizardStage"><span class = "operation"><a href = "#" class = "op-toggle button" onClick = "$('#dynamic').toggle();">Expand/Collapse</a></span>&nbsp;&nbsp;Stations Owned by This Project</span>
			<input type = 'hidden' id = 'mode' name = 'mode' value = 'delete'>
			<input type = 'hidden' id = 'project_id' name = 'project_id' value = '<%=request.getParameter("project_id")%>'>
			<div id = "dynamic"></div>
		</form>	
		
		<form id = 'linkedStnForm'>
			<span class = "wizardStage">
				<span class = "operation"><a href = "#" class = "op-toggle button" onClick = "$('#linkedStations').toggle();">Expand/Collapse</a></span>
				&nbsp;&nbsp;All Stations Linked to This Project
				<span style = "display: inline-block; float: right;">
					<span class = "operation"><a id = "linkStn" href = "link-stations?project_id=<%=request.getParameter("project_id")%>&project_name=<%=request.getParameter("project_name")%>" class = "op-add button span">Link New</a></span>
					&nbsp;
					<span class = "operation"><a href = "#" class = "op-delete button span" onClick = "updateLinkedStations();">Delete Link</a></span>
				</span>
			</span>
			<input type = 'hidden' id = 'linkedMode' name = 'mode' value = 'deleteLinked'>
			<input type = 'hidden' id = 'linked_project_id' name = 'project_id' value = '<%=request.getParameter("project_id")%>'>
			<div id = "linkedStations"></div>
		</form>	
	</div>
	
  </body>
</html>