<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<html>
  <head>
    <title>Announcement Administration Console - GoMonitor</title>
    <%@ include file = "imports.jsp" %>

	<%
		if(((HttpServletRequest) request).isUserInRole("CAU"))
		{
	%>
    <script type="text/javascript">
		$(function() {
			registerButtons();
		});
		
		
		/**
		 * Initialize the display after the user session is augmented
		 *
		 */
		function init()
		{
			processQuery("list");	
		}
		


		
		/**
		 * Depending on the operation specified hit the appropriate servlet for data
		 *
		 * @param (String) The operation to be performed on the paging or noop if the first page is to be loaded
		 */
		function processQuery(operation)
		{
			 $.get("/goma/admin/announcement-ops", {mode: operation},  processResponse);
		}
		
		
		/**
		 * Function to receive the HTTP request response from search request and render it as a table
		 *
		 * @param (HTTPResponse) The object containing the search results
		 */
		function processResponse(response)
		{
			var data = response.data;
		
			var aDataSet = "[ ";
			for(var i = 0; i < data.length; i++)
			{
				aDataSet += '["<input type = \'checkbox\' name = \'announcement_id\' onClick = \'highlightRow(this);\' value = \''+data[i].announcement_id+'\'>",'+
							'"'+ data[i].name + '",' +
							'"'+ data[i].description + '",' +
							'"'+ data[i].created_by + '",' +
							'"'+ data[i].date_created + '"';
				aDataSet += ']';
				if(i != (data.length - 1))
						aDataSet += ",";
			}
			aDataSet += ' ]';

			var colSet;

			colSet = "[ { \"sTitle\": \"Select\" },{ \"sTitle\": \"Title\" }, { \"sTitle\": \"Announcement Description\" }, { \"sTitle\": \"Posted By\" }, { \"sTitle\": \"Posted On\" } ]";

            //alert(aDataSet);
            //alert(colSet);
			$('#dynamic').html( '<table cellpadding="0" cellspacing="0" border="0" class="display" id="consoleTable"></table>' );
			oTable = $('#consoleTable').dataTable( {
					"aaData": eval(aDataSet),
					"aoColumns": eval(colSet),
					"sDom": '<"top"fp<"clear">>rt<"bottom"p<"clear">>',
					"iDisplayLength": 20,
					"aLengthMenu": [[25, 50, 100], [25, 50, 100]]
			} );
			$("#mode").val("delete");
			
		}
		
		
		/**
		 * Function to save the data entered in the announcement form
		 *
		 * 
		 */
		function update()
		{
			var inputs = $("#dynamicForm").validator();
			if(inputs.data("validator").checkValidity())
			{
				$('input, textarea').each(function(){
				    $(this).val(jQuery.trim($(this).val()));
				    $(this).val($(this).val().replace(/\n/g,"<br>"));
				    $(this).val($(this).val().replace(/\"/g,"'"));
				});
				$.getJSON("/goma/admin/announcement-ops", $("#dynamicForm").serialize(), processSave);
			}
		}
		
		/**
		 * Function to receive the HTTP request response from user fetch request and render it as a form
		 *
		 * @param (HTTPResponse) The object containing the user
		 */
		 function processSave(response)
		 {
			if(response.code == "success")
				processQuery("list");
			setMessage(response.message, response.code);
		 }
		
		
		/**
		 * Function to display the form for new announcement creation
		 *
		 */
		 function create()
		{
			var formString = "";
			
			formString += "<table class = 'formTable'>";
			formString += "<tr><td class = 'fieldname required'>Title:</td><td><input  title = 'Enter announcement title here' type = 'text' id = 'name' name = 'name' required = 'required'></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname required'>Description:</td><td><textarea  title = 'Enter a brief description' id = 'description' name = 'description' required = 'required'></textarea></td><td></td></tr>";
			formString += "</table>";
			formString += "<input type = 'hidden' id = 'mode' name = 'mode' value = 'create'>";
			
			
			formString += "<br><span class = 'operation'><a href = '#' onClick = 'update();' class = 'op-save'>Save Changes</a></span>";
	
			$("#dynamic").html(formString);
			$("#mode").val("create");
			registerTooltips('dynamic');
			registerButtons();
		}
		
    </script>
  </head>
<body>
		<%@ include file = "header.jsp" %>
		<%@ include file = "menu.jsp" %>
	<div class = "console">
		<div class = "page-title">Manage Announcements - Recent Posts</div>
		<div class = "operations">
			<ul>
				<li class = "operation"><a href = "#" class = "op-list button" onClick = "processQuery('list');">List</a></li>
				<li class = "operation"><a href = "#" class = "op-add button" onClick = "create();">Add New</a></li>
				<li class = "operation"><a href = "#" class = "op-delete button" onClick = "update();">Delete</a></li>
			</ul>
		</div>
		<form id = 'dynamicForm'>
			<input type = 'hidden' id = 'mode' name = 'mode' value = 'delete'>
			<div id = "dynamic"></div>
		</form>
	</div>
  </body>
    	<%
		}
		else
		{
			
	%>
		</head>
		<body>
			<%@ include file = "header.jsp" %>
			<div class = "violation">Access to this page is forbidden with your current credentials. Please login with the appropriate credentials. If you feel you are getting this message in error, please contact the system administrator.</div>
		</body>
	<%
		}
  	%>
</html>