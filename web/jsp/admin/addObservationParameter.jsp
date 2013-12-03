<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
  <head>
    <title>Station Observed Parameters - GoMonitor</title>
    <%@ include file = "imports.jsp" %>

   	<%
   		if(request.getParameter("station_id") == null)
   		{
			out.print("<div class = 'violation'>Select a station first from the station page</div>");
			return;
   		}
   	%>
    <script type="text/javascript">
    
    	//The current program that we are working on
    	var station_id = <%=request.getParameter("station_id")%>;
    	var station_name = "<%=request.getParameter("station_name")%>";
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
		 	//$.get("/goma/utilities", {mode: "listTuples"},  loadTuples);
			 $.get("/goma/utilities", {mode: "listStnObservations" , station_id: station_id},  processResponse);
		}
		
		
		/**
		 * Depending on the operation specified hit the appropriate servlet for data
		 *
		 * @param (String) The operation to be performed on the paging or noop if the first page is to be loaded
		 */
		function processQuery(operation)
		{
			 
			 $.get("/goma/utilities", {mode: operation, station_id: station_id},  processResponse);
			
		}
		
		
		/**
		 * Function to receive the HTTP request response from the list request and render it as a table
		 *
		 * @param (HTTPResponse) The object containing the search results
		 */
		function processResponse(response)
		{
			var data = response.data;
			$("#parentName").text(station_name + ": ");
			$("#currentOp").text("");
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
			            +"{ \"sTitle\": \"Method\" },"
			            +" { \"sTitle\": \"Sampling Frequency\" },"
			            +" { \"sTitle\": \"Sampling Depth\" },"
			            +" { \"sTitle\": \"Start Date\" },"
			            +" { \"sTitle\": \"End Date\" } ]";
			
//                        alert(aDataSet);
//                        alert(colSet);
			$('#dynamic').html( '<table cellpadding="0" cellspacing="0" border="0" class="display" id="consoleTable"></table>' );
			oTable = $('#consoleTable').dataTable( {
					"aaData": eval(aDataSet),
					"aoColumns": eval(colSet),
					"sDom": '<"top"p<"clear">>rt<"bottom"p<"clear">>',
					"iDisplayLength": 25,
					"aLengthMenu": [[25, 50, 100], [25, 50, 100]],
					"aaSorting": [[2,'asc']]
			} );
			$("#mode").val("delete");
			
			$.get("/goma/utilities", {mode: "listTuples"},  loadTuples);
		}		

		/**
		 * Function to request the list of status values from the servlet
		 *
		 */
		 //TODO: NEED TO FIND A BETTER WAY OF PASSING IN STATUS_TYPE_ID
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
			var termsStr = "<select class = 'picker' name = '"+response.code.toLowerCase()+"_id' id ='"+response.code.toLowerCase()+"_id'><option value = '-1'>Pick From List</option>";
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
			$.getJSON("/goma/admin/station-ops", $("#dynamicForm").serialize(), function(response) {
				if(response.code == "success")
					{
						$("#currentOp").text(" / Add Observed Parameter");
						$("#obsOpts #obs_station_id").val(response.station_id);
					}
				else
					{
						setMessage(response.message, response.code);
						return;
					}
			});
			$("#basicOpts").hide();
			$("#obsOpts").show();
			
			$.get("/goma/admin/obs-ops", {mode: "list"},  loadTuples);
			registerTooltips('obsForm');
			registerButtons();
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
				aDataSet += '["<input type = \'radio\' name = \'tuple_id\' onClick = \'addObsParameters();\' value = \''+data[i].tuple_id+'\'>'+
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
			oTable = $('#tupleTable').dataTable( {
					"aaData": eval(aDataSet),
					"aoColumns": eval(colSet),
					"sDom": '<"top"p<"clear">>rt<"bottom"p<"clear">>',
					"iDisplayLength": 20,
					"aLengthMenu": [[25, 50, 100], [25, 50, 100]]
			} );

			
			var filterStr = "<tr><th>&nbsp;</th>";
			filterStr += '<th><input type="text" name="filterMedium" value="Filter" class="search_init" /></th>';
			filterStr += '<th><input type="text" name="filterCategory" value="Filter" class="search_init" /></th>';
			filterStr += '<th><input type="text" name="filterType" value="Filter" class="search_init" /></th>';
			filterStr += '<th><input type="text" name="filterMethod" value="Filter" class="search_init" /></th>';
			filterStr += "</tr>";
			
			$("table#tupleTable thead").append(filterStr);

			var asInitVals = new Array();
			$("thead input").keyup( function () {
				//alert("searching for "+this.value+" in column "+($("thead input").index(this)));
				oTable.fnFilter( this.value, ($("thead input").index(this)+1) );
			} );
			

			/*
			 * Support functions to provide a little bit of 'user friendlyness' to the textboxes in 
			 * the header
			 */
			$("thead input").each( function (i) {
				asInitVals[this.name] = this.value;
			} );
			
			$("thead input").focus( function () {
				if ( this.className == "search_init" )
				{
					this.className = "";
					this.value = "";
				}
			} );

			$("thead input").blur( function (i) {
				if ( this.value == "" )
				{
					this.className = "search_init";
					this.value = asInitVals[this.name];
				}
			} );
			
		}		
		
		function addObsParameters()
		{
			var paramStr = "<table class = 'formTable'>";
			 
			paramStr += "<tr><td class = 'fieldname required'>Sampling Frequency:</td><td><span id = 'sampling_frequency_well'></span></td><td></td></tr>";
			paramStr += "<tr><td class = 'fieldname'>Sampling Depth:</td><td><span id = 'sampling_depth_well'></span></td><td></td></tr>";
			paramStr += "<tr><td class = 'fieldname'>Start Date:</td><td><input title = 'Enter the sampling start date of this parameter' type = 'text' id = 'start_date' name = 'start_date' maxlength = '20'></td><td></td></tr>";
			paramStr += "<tr><td class = 'fieldname'>End Date:</td><td><input title = 'Enter the sampling end date of this parameter' type = 'text' id = 'end_date' name = 'end_date' maxlength = '20'></td><td></td></tr>";
			paramStr += "</table>"
			paramStr += "<span class = 'operation'><a href = '#' onClick = 'saveObsParameter()' class = 'op-save button'>Save</a></span><br/><br/>"
			$("#paramForm").html(paramStr);
			fetchTermsList(Statics.SAMPLING_FREQUENCY);
			setTimeout("fetchTermsList("+Statics.SAMPLING_DEPTH+");", 1500);
			$( "#start_date" ).datepicker();
			$( "#end_date" ).datepicker();
		}
		
		function saveObsParameter()
		{
			var inputs = $("#obsForm").validator();
			if(!inputs.data("validator").checkValidity())
				return false;	
			$.getJSON("/goma/admin/obs-param-ops", $("#obsForm").serialize(), processObsSave);
		}
		
		function processObsSave(response)
		{
			setMessage(response.message, response.code);
			if(response.code == "success")
			{
				$("#paramForm").html("");
				processQuery("listStnObservations");
			}
		}
		
    </script>
  </head>
<body>
		<%@ include file = "header.jsp" %>
		<%@ include file = "menu.jsp" %>
	<div class = "console">
		<div class = "page-title"><span id = "parentName"></span>Add Observed Parameters<span id = "currentOp"></span></div>
		<div class = "operations">
			<ul>
				<li class = "operation"><a href = "edit-obs-params?station_id=<%=request.getParameter("station_id")%>&station_name=<%=request.getParameter("station_name")%>" class = "op-edit button">Edit</a></li>
				<li class = "operation"><a href = "stations?station_id=<%=request.getParameter("station_id")%>&station_name=<%=request.getParameter("station_name")%>" class = "op-station button">View Station</a></li>
		
			</ul>
		</div>	
		<form name = 'obsForm' id = 'obsForm'>
			<div id = "paramForm"></div>
			<span class = "wizardStage"><span class = "operation"><a href = "#" class = "op-toggle button" onClick = "$('#tupleContainer').toggle();">Expand/Collapse</a></span>&nbsp;&nbsp;Select Observation Parameters to be Added</span>
			<div id = "tupleContainer"></div>
			<input type = 'hidden' name = 'mode' id = 'obsMode' value = 'create'>
			<input type = 'hidden' name = 'station_id' id = 'obs_station_id' value = '<%=request.getParameter("station_id")%>'>
		</form>		
		
		<form id = 'dynamicForm'>
			<span class = "wizardStage"><span class = "operation"><a href = "#" class = "op-toggle button" onClick = "$('#dynamic').toggle();">Expand/Collapse</a></span>&nbsp;&nbsp;Current Observation Parameters for this Station</span>
			<div id = "dynamic"></div>
			<input type = 'hidden' id = 'mode' name = 'mode' value = 'delete'>
			<input type = 'hidden' id = 'project_id' name = 'station_id' value = '<%=request.getParameter("station_id")%>'>
			
		</form>

	
	</div>
	
  </body>
</html>