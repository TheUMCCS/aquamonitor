<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
  <head>
    <title>Geographic Zone Administration Console  GoMonitor</title>
    <%@ include file = "imports.jsp" %>

    <script type="text/javascript">
    
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
			processQuery("listGZ");	
		}
		

		
		/**
		 * Depending on the operation specified hit the appropriate servlet for data
		 *
		 * @param (String) The operation to be performed on the paging or noop if the first page is to be loaded
		 */
		function processQuery(operation)
		{
			 $.get("/goma/utilities", {mode: operation, type: "name"},  processResponse);
		}
		
		
		/**
		 * Function to receive the HTTP request response from the list request and render it as a table
		 *
		 * @param (HTTPResponse) The object containing the search results
		 */
		function processResponse(response)
		{
			var data = response.data;
			$("#currentOp").text(" / Geographic Zone Listing");
			var aDataSet = "[ ";
			for(var i = 0; i < data.length; i++)
			{
				aDataSet += '["<input type = \'checkbox\' name = \'gz_id\' onClick = \'highlightRow(this);\' value = \''+data[i].gz_id+'\'>'+
							'","<a href = \'#\' onClick = \'fetch('+data[i].gz_id+');\' class = \'details\'>'+ data[i].name + '</a>' + '"';
				aDataSet += ']';			
				if(i != (data.length - 1))
						aDataSet += ",";
			}
			aDataSet += " ]";

			var colSet;

			colSet = "[ { \"sTitle\": \"<input type='checkbox' value = '0' name='selectAllB' id='selectAllB' onClick='jqCheckAll(this.id)'>\"}, { \"sTitle\": \"Name (Click record to edit)\" } ]";

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
			$('input, textarea').each(function(){
			    $(this).val(jQuery.trim($(this).val()));
			    $(this).val($(this).val().replace(/\n/g,"<br>"));
			    $(this).val($(this).val().replace(/\"/g,"'"));
			});
			if(!inputs.data("validator").checkValidity())
				return false;	
			$.post("/goma/admin/gz-ops", $("#dynamicForm").serialize(), processSave, 'json');

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
				//processQuery("list");
				fetch(response.gz_id);
				$("#currentOp").text(" / Review and Save");
			}
			setMessage(response.message, response.code);
		 }
		
		
		/**
		 * Function to request the station data from the servlet
		 *
		 * @param (Number) The id of the station to be fetched for editing
		 */
		function fetch(gz_id)
		{
			$.get("/goma/admin/gz-ops", {mode: "fetch", gz_id: gz_id}, load);
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
				formString += "<tr><td class = 'fieldname required'>Name:</td><td><input title = 'Enter the name of the geographic zone here' type = 'text' id = 'name' name = 'name' required = 'required' maxlength = '255' value = '"+data[i].name+"'></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Description:</td><td><textarea  title = 'Enter a brief description, no HTML tags' id = 'description' name = 'description' required = 'required'>"+data[i].description+"</textarea></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Geographic Location:<br><a href = 'http://en.wikipedia.org/wiki/Well-known_text' target = '_blank'>More Information about WKT</a></td><td><textarea  title = 'Enter the geographic zone polygon in Well-Known Text format. Coordinates should be entered in decimal degrees with respect to the WGS 84 datum. e.g.<br>POLYGON((long1 lat1, long2 lat2, long3 lat3,long4 lat4, long1 lat1)). Please note that polygons must be entered as closed linear rings, i.e. the first vertex must match the last vertex' id = 'location' name = 'location' required = 'required'>"+data[i].location+"</textarea></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname'>Date Created:</td><td>"+data[i].date_created+"</td><td></td></tr>";
				formString += "<tr><td class = 'fieldname'>Approval Status:</td><td>"+data[i].approval+"</td><td></td></tr>";
				if(data[i].date_updated != null)
					formString += "<tr><td class = 'fieldname'>Date Updated:</td><td>"+data[i].date_updated+"</td><td></td></tr>";
				formString += "</table>";
				formString += "<input type = 'hidden' name = 'gz_id' id = 'gz_id' value = '"+data[i].gz_id+"'/>";
			}		
			
			formString += "<br><span class = 'operation'><a href = '#' onClick = 'update();' class = 'op-save'>Save Changes</a></span></div>";

			
			$("#dynamic").html(formString);
			$("#mode").val("update");
			registerTooltips('basicOpts');
			registerButtons();
		}
		
		/**
		 * Function to display the form for new station creation
		 *
		 */
		 function create()
		{
			$("#currentOp").text(" / Adding New");
			var formString = "";
			
			formString += "<div id = 'basicOpts'><table class = 'formTable'>";
			formString += "<tr><td class = 'fieldname required'>Name:</td><td><input title = 'Enter the name of the geographic zone here' type = 'text' id = 'name' name = 'name' required = 'required' maxlength = '255'></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname required'>Description:</td><td><textarea  title = 'Enter a brief description, no HTML tags' id = 'description' name = 'description' required = 'required'></textarea></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname required'>Geographic Location:<br><a href = 'http://en.wikipedia.org/wiki/Well-known_text' target = '_blank'>More Information about WKT</a></td><td><textarea  title = 'Enter the geographic zone polygon in Well-Known Text format. Coordinates should be entered in decimal degrees with respect to the WGS 84 datum. e.g.<br>POLYGON((long1 lat1, long2 lat2, long3 lat3,long4 lat4, long1 lat1)). Please note that polygons must be entered as closed linear rings, i.e. the first vertex must match the last vertex' id = 'location' name = 'location' required = 'required'></textarea></td><td></td></tr>";
			formString += "</table>";
			formString += "<br><span class = 'operation'><a href = '#' onClick = 'update();' class = 'op-save'>Save</a></span></div>";
			
			$("#dynamic").html(formString);
			$("#mode").val("create");
			registerTooltips('basicOpts');
			registerButtons();
		}
		 	
		
    </script>
  </head>
<body>
		<%@ include file = "header.jsp" %>
		<%@ include file = "menu.jsp" %>
	<div class = "console">
		<div class = "page-title"><span id = "parentName"></span>Manage Geographic Zones<span id = "currentOp"></span></div>
		<div class = "operations">
			<ul>
				<li class = "operation"><a href = "#" class = "op-list button" onClick = "processQuery('listGZ');">List</a></li>
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
			<input type = 'hidden' id = 'mode' name = 'mode' value = 'delete'>
			<div id = "dynamic"></div>
		</form>	
	</div>
	
  </body>
</html>