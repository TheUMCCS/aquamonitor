<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
  <head>
    <title>Link Stations - GoMonitor</title>
    <%@ include file = "imports.jsp" %>

   	<%
  		if(request.getParameter("project_id") == null)
  		{
		out.print("<div class = 'violation'>Select a project first from the project page</div>");
		return;
  		}
	%>
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
			processQuery("listStations");	
		}
		

	
		/**
		 * Depending on the operation specified hit the appropriate servlet for data
		 *
		 * @param (String) The operation to be performed on the paging or noop if the first page is to be loaded
		 */
		function processQuery(operation)
		{
			 $.get("/goma/utilities", {mode: operation},  processResponse);
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
				aDataSet += '["<input type = \'checkbox\' name = \'station_id\' onClick = \'highlightRow(this);\' value = \''+data[i].station_id+'\'>'+
							'","<a href = \'#\' onClick = \'fetch('+data[i].station_id+');\' class = \'details\'>'+ data[i].name + '</a>' +
							'","'+data[i].status;
				if(data[i].website != null)
					aDataSet+='","<a href = \''+data[i].website +'\' class = \'external\' target = \'blank\' title = \'Open Website in New Window\'></a>"';
				else
					aDataSet+='","N/A"';
				aDataSet += ']';			
				if(i != (data.length - 1))
						aDataSet += ",";
			}
			aDataSet += " ]";

			var colSet;

			colSet = "[ { \"sTitle\": \"<input type='checkbox' value = '0' name='selectAllB' id='selectAllB' onClick='jqCheckAll(this.id)'>\"}, { \"sTitle\": \"Name (Click record to edit)\" }, { \"sTitle\": \"Status\" }, { \"sTitle\": \"Website\" } ]";

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
			$("#mode").val("link");
			if(data.length == 0)
				$('#consoleTable').after("NOTE: There may be additional records that have not been approved yet");
		}
		
		/**
		 * Function to save the data entered in the station form
		 *
		 * 
		 */
		function update()
		{		
			var inputs = $("#dynamicForm").validator();
			if(!inputs.data("validator").checkValidity())
				return false;	
			$.getJSON("/goma/admin/station-ops", $("#dynamicForm").serialize(), function(response)
				{
					if(response.code == "success")
					{
						window.location.replace("stations?project_id="+project_id+"&project_name="+parent_name);
					}
					setMessage(response.message, response.code);
				 }		
			);
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
				<li class = "operation"><a href = "#" class = "op-station button" onClick = "update();">Create Link</a></li>
			</ul>
		</div>
		<form id = 'dynamicForm'>
			<input type = 'hidden' id = 'mode' name = 'mode' value = 'link'>
			<input type = 'hidden' id = 'project_id' name = 'project_id' value = '<%=request.getParameter("project_id")%>'>
			<div id = "dynamic"></div>
		</form>	
	</div>
	
  </body>
</html>