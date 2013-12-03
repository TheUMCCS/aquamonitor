<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
  <head>
    <title>Dictionary Term Administration Console - GoMonitor</title>
    <%@ include file = "imports.jsp" %>


    <script type="text/javascript">
    
    	//The current dictionary id, taken from the request
    	var dictionary_id = <%=request.getParameter("dictionary_id")%>;
		$(function() {
			registerButtons();
		});
		
		
		/**
		 * Initialize the display after the user session is augmented
		 *
		 */
		function init()
		{
			processQuery("listDictionaryTerms");	
		}
		

		
		/**
		 * Depending on the operation specified hit the appropriate servlet for data
		 *
		 * @param (String) The operation to be performed on the paging or noop if the first page is to be loaded
		 */
		function processQuery(operation)
		{
			 $.get("/goma/utilities", {mode: operation, dictionary_id: dictionary_id},  processResponse);
		}
		
		
		/**
		 * Function to receive the HTTP request response from search request and render it as a table
		 *
		 * @param (HTTPResponse) The object containing the search results
		 */
		function processResponse(response)
		{
			$("#specificTitle").text(response.dictionary);
			$("#dictionaryDesc").text(response.dictionary_description)
			var data = response.data;
			
			var aDataSet = "[ ";
			for(var i = 0; i < data.length; i++)
			{
				aDataSet += '["<input type = \'checkbox\' name = \'term_id\' onClick = \'highlightRow(this);\' value = \''+data[i].term_id+'\'>'+
							'","<a href = \'#\' onClick = \'fetch('+data[i].term_id+');\' class = \'details\'>'+ data[i].name + '</a>'+ 
							'"]';
				if(i != (data.length - 1))
						aDataSet += ",";
			}
			aDataSet += " ]";

			var colSet;

			colSet = "[ { \"sTitle\": \"<input type='checkbox' value = '0' name='selectAllB' id='selectAllB' onClick='jqCheckAll(this.id)'>\"}, { \"sTitle\": \"Dictionary Term Name (Click record to edit)\" }]";

//                        alert(aDataSet);
//                        alert(colSet);
			$('#dynamic').html( '<table cellpadding="0" cellspacing="0" border="0" class="display" id="consoleTable"></table>' );
			oTable = $('#consoleTable').dataTable( {
					"aaData": eval(aDataSet),
					"aoColumns": eval(colSet),
					"sDom": '<"top"fp<"clear">>rt<"bottom"p<"clear">>',
					"iDisplayLength": 20,
					"aLengthMenu": [[25, 50, 100], [25, 50, 100]],
					"aaSorting": [[1,'asc']]
			} );
			$("#mode").val("delete");
			
			if(data.length == 0)
				$('#consoleTable').after("NOTE: There may be additional records that have not been approved yet");
			
		}
		
		
		/**
		 * Function to save the data entered in the organization form
		 *
		 * 
		 */
		function update()
		{
			$.trim($(":input").val());
			var inputs = $("#dynamicForm").validator();
			$('input, textarea').each(function(){
			    $(this).val(jQuery.trim($(this).val()));
			    $(this).val($(this).val().replace(/\n/g,"<br>"));
			    $(this).val($(this).val().replace(/\"/g,"'"));
			});
			if(inputs.data("validator").checkValidity())	
				$.getJSON("/goma/admin/term-ops", $("#dynamicForm").serialize(), processSave);
		}
		
		/**
		 * Function to receive the HTTP request response from user fetch request and render it as a form
		 *
		 * @param (HTTPResponse) The object containing the user
		 */
		 function processSave(response)
		 {
			if(response.code == "success")
				processQuery("listDictionaryTerms");
			setMessage(response.message, response.code);
		 }
		
		
		/**
		 * Function to request the organization data from the servlet
		 *
		 * @param (Number) The id of the organization to be fetched for editing
		 */
		function fetch(term_id)
		{
			$.get("/goma/admin/term-ops", {mode: "fetch", term_id: term_id}, load);
		}
		
		
		/**
		 * Function to receive the HTTP request response from organization fetch request and render it as a form
		 *
		 * @param (HTTPResponse) The object containing the organization
		 */		 
		function load(response)
		{
			
			var formString = "";
			var data = response.data;
			/*
			for(var i = 0; i < data.length; i++)
			{
				for (var key in data[i]) {
					  if(data[i].hasOwnProperty(key)) {
					    if(data[i][key] == "null")
					    	data[i][key] = "N/A";
					  }
					}
			}
			*/
			for(var i = 0; i < data.length; i++)
			{
				formString += "<table class = 'formTable'>";
				formString += "<tr><td class = 'fieldname required'>Term:</td><td><input  title = 'Enter term name here' type = 'text' id = 'name' name = 'name' required = 'required' value = '"+data[i].name+"'></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Description:</td><td><textarea title = 'Enter a brief description of this term' id = 'description' name = 'description' required = 'required'>"+data[i].description+"</textarea></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname'>Date Created:</td><td>"+data[i].date_created+"</td><td></td></tr>";
				if(data[i].date_updated != null)
					formString += "<tr><td class = 'fieldname'>Date Updated:</td><td>"+data[i].date_updated+"</td><td></td></tr>";
				formString += "</table>";
				formString += "<input type = 'hidden' id = 'term_id' name = 'term_id' value = '"+data[i].term_id+"'>";
			}
			
			
			formString += "<br><span class = 'operation'><a href = '#' onClick = 'update();' class = 'op-save'>Save Changes</a></span>";

			$("#dynamic").html(formString);
			$("#mode").val("update");
			registerTooltips('dynamic');	
			registerButtons();		
		}
		
		/**
		 * Function to display the form for new organization creation
		 *
		 */
		 function create()
		{
			var formString = "";
			
			formString += "<table class = 'formTable'>";
			formString += "<tr><td class = 'fieldname required'>Term:</td><td><input  title = 'Enter term name here' type = 'text' id = 'name' name = 'name' required = 'required'></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname required'>Description:</td><td><textarea  title = 'Enter a brief description of the term' id = 'description' name = 'description' required = 'required'></textarea></td><td></td></tr>";
			formString += "</table>";
					
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
		<div class = "page-title">Manage Dictionary Terms - <span id = "specificTitle"></span></div>
		<div class = "operations">
			<ul>
				<li class = "operation"><a href = "dictionaries" class = "op-list button">Dictionary List</a></li>
				<li class = "operation"><a href = "#" class = "op-list button" onClick = "processQuery('listDictionaryTerms');">Term List</a></li>
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
		<div><b>Definition: </b><span id = "dictionaryDesc"></span></div>
		<form id = 'dynamicForm'>
			<input type = 'hidden' id = 'dictionary_id' name = 'dictionary_id' value = '<%=request.getParameter("dictionary_id")%>'>
			<input type = 'hidden' id = 'mode' name = 'mode' value = 'delete'>
			<div id = "dynamic"></div>
		</form>
	</div>
  </body>

</html>