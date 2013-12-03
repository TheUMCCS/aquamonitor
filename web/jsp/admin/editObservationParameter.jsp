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
			processQuery("listStnObservations");	
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
				aDataSet += '["<input type = \'radio\' name = \'obs_param_id\' onClick = \'editObsParameter();\' value = \''+data[i].obs_param_id+'\'>'+
							'","'+ data[i].medium  +
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

			colSet = "[ { \"sTitle\": \"Edit\"}, "
			            +"{ \"sTitle\": \"Medium\" },"
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
					"sDom": '<"top"fp<"clear">>rt<"bottom"p<"clear">>',
					"iDisplayLength": 25,
					"aLengthMenu": [[25, 50, 100], [25, 50, 100]],
					"aaSorting": [[2,'asc']]
			} );
			if(data.length == 0)
				$('#consoleTable').after("NOTE: There may be additional records that have not been approved yet");
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
			$.get("/goma/utilities", {mode: "listDictionaryTerms", dictionary_id: dictionary_id}, loadTermsList);
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
		
		function saveObsParameter()
		{
			var inputs = $("#dynamicForm").validator();
			if(!inputs.data("validator").checkValidity())
				return false;	
			$.getJSON("/goma/admin/obs-param-ops", $("#dynamicForm").serialize(), processObsSave);
		}
		
		function processObsSave(response)
		{
			setMessage(response.message, response.code);
			if(response.code == "success")
			{
				$("#paramForm").html("");
			}
		}
		
		function deleteObsParameter()
		{
			$.getJSON("/goma/admin/obs-param-ops", { obs_param_id: $('input:radio[name=obs_param_id]:checked').val(), mode: "delete" }, processQuery("list"));
		}
		
		function editObsParameter()
		{
			if($('input:radio[name=obs_param_id]:checked').length > 1)
			{
				setMessage("Please select only one observed parameter", "warning");
				return;
			}
			else if($('input:radio[name=obs_param_id]:checked').length == 1)
				$.getJSON("/goma/admin/obs-param-ops", {mode: "fetch", obs_param_id: $('input:radio[name=obs_param_id]:checked').val()}, loadObsParameter);
		}
		
		function loadObsParameter(response)
		{
			var data = response.data;
			var paramStr = "<table class = 'formTable'>";
			
			for(var i = 0; i < data.length; i++)
			{
				paramStr += "<tr><td class = 'fieldname required'>Sampling Frequency:</td><td><span id = 'sampling_frequency_well'></span></td><td></td></tr>";
				paramStr += "<tr><td class = 'fieldname'>Sampling Depth:</td><td><span id = 'sampling_depth_well'></span></td><td></td></tr>";
				paramStr += "<tr><td class = 'fieldname'>Start Date:</td><td><input title = 'Enter the sampling start date of this parameter' type = 'text' id = 'start_date' name = 'start_date' maxlength = '20' value = '"+data[i].start_date+"'></td><td></td></tr>";
				paramStr += "<tr><td class = 'fieldname'>End Date:</td><td><input title = 'Enter the sampling end date of this parameter' type = 'text' id = 'end_date' name = 'end_date' maxlength = '20' value = '"+data[i].end_date+"'></td><td></td></tr>";
			}
			paramStr += "</table>"
			paramStr += "<span class = 'operation'><a href = '#' onClick = 'saveObsParameter()' class = 'op-save button'>Save</a></span><br><br>"
	
			$("#obsMode").val("update");
			$("#paramForm").html(paramStr);
			fetchTermsList(Statics.SAMPLING_FREQUENCY);
			setTimeout("fetchTermsList("+Statics.SAMPLING_DEPTH+");", 1500);
			$( "#start_date" ).datepicker();
			$( "#end_date" ).datepicker();
		}
		
    </script>
  </head>
<body>
		<%@ include file = "header.jsp" %>
		<%@ include file = "menu.jsp" %>
	<div class = "console">
		<div class = "page-title"><span id = "parentName"></span>Edit Observed Parameters<span id = "currentOp"></span></div>
		<div class = "operations">
			<ul>
				<li class = "operation"><a href="add-obs-params?station_id=<%=request.getParameter("station_id")%>&station_name=<%=request.getParameter("station_name")%>" class = "op-add button" >Add New</a></li>
				<li class = "operation"><a href = "stations?station_id=<%=request.getParameter("station_id")%>&station_name=<%=request.getParameter("station_name")%>" class = "op-station button">View Station</a></li>	
			<%
			if(((HttpServletRequest) request).isUserInRole("CAU"))
			{
			%>
				<li class = "operation"><a href = "#" class = "op-delete button" onClick = "deleteObsParameter();">Delete</a></li>
			<%
			}
			%>	
			</ul>
		</div>
		
		<form id = 'dynamicForm'>
			<div id = "paramForm"></div>
			<span class = "wizardStage">Current Observed Parameters for this Station</span>
			<input type = 'hidden' id = 'mode' name = 'mode' value = 'update'>
			<input type = 'hidden' id = 'project_id' name = 'station_id' value = '<%=request.getParameter("station_id")%>'>
			<div id = "dynamic"></div>
		</form>

	
	</div>
	
  </body>
</html>