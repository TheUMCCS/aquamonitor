<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
  <head>
    <title>Dictionary Administration Console - GoMonitor</title>
    <%@ include file = "imports.jsp" %>


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
			 $.get("/goma/admin/dictionary-ops", {mode: operation},  processResponse);
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
				aDataSet += '["<input type = \'checkbox\' name = \'dictionary_id\' onClick = \'highlightRow(this);\' value = \''+data[i].dictionary_id+'\'>'+
							'","<a href = \'dictionary?dictionary_id='+data[i].dictionary_id+'\' class = \'list-control\' title = \'Click to view all terms\'> </a>' +
							<%
							if(((HttpServletRequest) request).isUserInRole("CAU"))
							{
							%>			
							'","<a href = \'#\' onClick = \'fetch('+data[i].dictionary_id+');\' class = \'details\'>'+ data[i].name + '</a>' +
							<%
							}
							else
							{
							%>
							'","'+ data[i].name +
							<%
							}
							%>	
							'"]';
				if(i != (data.length - 1))
						aDataSet += ",";
			}
			aDataSet += " ]";

			var colSet;

			colSet = "[ { \"sTitle\": \"<input type='checkbox' value = '0' name='selectAllB' id='selectAllB' onClick='jqCheckAll(this.id)'>\"},"+ 
			            "{ \"sTitle\": \"View Terms\" }, "+
			            "{ \"sTitle\": \"Dictionary Name\" } ]";

//          alert(aDataSet);
//          alert(colSet);
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
			if($("#opList li").length > 1)
				$("#opList li:last").remove();
			
		}
		
		
		/**
		 * Function to save the data entered in the dictionary form
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
				$.getJSON("/goma/admin/dictionary-ops", $("#dynamicForm").serialize(), processSave);
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
		 * Function to request the dictionary data from the servlet
		 *
		 * @param (Number) The id of the dictionary to be fetched for editing
		 */
		function fetch(dictionary_id)
		{
			$.get("/goma/admin/dictionary-ops", {mode: "fetch", dictionary_id: dictionary_id}, load);
		}
		
		
		/**
		 * Function to receive the HTTP request response from dictionary fetch request and render it as a form
		 *
		 * @param (HTTPResponse) The object containing the dictionary
		 */		 
		function load(response)
		{
			
			var formString = "";
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
				formString += "<tr><td class = 'fieldname required'>Name:</td><td><input  title = 'Enter dictionary name here' type = 'text' id = 'name' name = 'name' required = 'required' value = '"+data[i].name+"'></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Description:</td><td><textarea title = 'Enter a brief description of the dictionary' id = 'description' name = 'description' required = 'required'>"+data[i].description+"</textarea></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname'>Date Created:</td><td>"+data[i].date_created+"</td><td></td></tr>";
				if(data[i].date_updated != null)
					formString += "<tr><td class = 'fieldname'>Date Updated:</td><td>"+data[i].date_updated+"</td><td></td></tr>";
				formString += "</table>";
				formString += "<input type = 'hidden' id = 'user_id' name = 'dictionary_id' value = '"+data[i].dictionary_id+"'>";
			}
			
			
			formString += "<br><span class = 'operation'><a href = '#' onClick = 'update();' class = 'op-save'>Save Changes</a></span>";

			$("#dynamic").html(formString);
			$("#mode").val("update");

			$(".operations ul").append('<li class = "operation"><a href = "dictionary?dictionary_id='+data[0].dictionary_id+'" class = "op-list button">Term List</a></li>');
			registerTooltips('dynamic');	
			registerButtons();		
		}
		
		/**
		 * Function to display the form for new dictionary creation
		 *
		 */
		 function create()
		{
			var formString = "";
			
			formString += "<table class = 'formTable'>";
			formString += "<tr><td class = 'fieldname required'>Name:</td><td><input  title = 'Enter dictionary name here' type = 'text' id = 'name' name = 'name' required = 'required'></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname required'>Description:</td><td><textarea  title = 'Enter a brief description of the dictionary' id = 'description' name = 'description' required = 'required'></textarea></td><td></td></tr>";
			formString += "</table>";
			formString += "<input type = 'hidden' id = 'mode' name = 'mode' value = 'create'>";
			
			
			formString += "<br><span class = 'operation'><a href = '#' onClick = 'update();' class = 'op-save'>Save Changes</a></span>";
	
			$("#dynamic").html(formString);
			$("#mode").val("create");
			registerTooltips();
			registerButtons();
		}
		 
		
    </script>
  </head>
<body>
		<%@ include file = "header.jsp" %>
		<%@ include file = "menu.jsp" %>
	<div class = "console">
		<div class = "page-title">Manage Dictionaries</div>
		<div class = "operations">
			<ul id = "opList">
				<li class = "operation"><a href = "#" class = "op-list button" onClick = "processQuery('list');">Dictionary List</a></li>
			</ul>
		</div>
		<form id = 'dynamicForm'>
			<input type = 'hidden' id = 'mode' name = 'mode' value = 'delete'>
			<div id = "dynamic"></div>
		</form>
	</div>
  </body>

</html>