<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<html>
  <head>
    <title>Manage Observation Parameters - GoMonitor</title>
    <%@ include file = "imports.jsp" %>


    <script type="text/javascript">
    
		var method, category, obsType, medium;

		$(function() {
			$.get("/goma/admin/term-ops", {mode: "list", dictionary_id: Statics.SOURCE_MEDIUM},  loadMedium);
			registerButtons();

		});
		
		
		/**
		 * Initialize the display after the user session is augmented
		 *
		 */
		function init()
		{
			processQuery("listTuples");	
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
				aDataSet += '["<input type = \'checkbox\' name = \'tuple_id\' onClick = \'highlightRow(this);\' value = \''+data[i].tuple_id+'\'>'+
							'","'+ data[i].medium  +
							'","'+data[i].category +
							'","'+data[i].type +
							'","'+data[i].method +
							'","'+data[i].approval_status + '"';
				aDataSet += ']';
				if(i != (data.length - 1))
						aDataSet += ",";
			}
			aDataSet += " ]";

			var colSet;

			colSet = "[ { \"sTitle\": \"<input type='checkbox' value = '0' name='selectAllB' id='selectAllB' onClick='jqCheckAll(this.id)'>\"}, { \"sTitle\": \"Medium\" }, { \"sTitle\": \"Category\" }, { \"sTitle\": \"Type\" }, { \"sTitle\": \"Method Of Analysis\" }, { \"sTitle\": \"Status\" } ]";

//                        alert(aDataSet);
//                        alert(colSet);
			$('#dynamic').html( '<table cellpadding="0" cellspacing="0" border="0" class="display" id="consoleTable"></table>' );
			oTable = $('#consoleTable').dataTable( {
					"aaData": eval(aDataSet),
					"aoColumns": eval(colSet),
					"sDom": '<"top"fp<"clear">>rt<"bottom"p<"clear">>',
					"iDisplayLength": 20,
					"aLengthMenu": [[25, 50, 100], [25, 50, 100]]
			} );
			$("#mode").val("delete");
			if(data.length == 0)
				$('#consoleTable').after("NOTE: There may be additional records that have not been approved yet");
		}
		
		function loadMedium(response)
		{
			if(response == null)
			{
				setMessage("There was an error communicating with the server", "failure");
				return;
			}
			$.get("/goma/admin/term-ops", {mode: "list", dictionary_id: Statics.OBSERVATION_CATEGORY},  loadCategory);
			var data = response.data;
			medium = "<select id = 'medium' name = 'medium'>";
			for(var i = 0; i < data.length; i++)
			{
				medium += "<option value = '"+data[i].term_id+"'>"+data[i].name+"</option>";
			}
			medium += "</select>";

		}
		
		
		function loadCategory(response)
		{
			if(response == null)
			{
				setMessage("There was an error communicating with the server", "failure");
				return;
			}
			
			$.get("/goma/admin/term-ops", {mode: "list", dictionary_id: Statics.OBSERVATION_TYPE},  loadObsType);
			var data = response.data;
			category = "<select id = 'category' name = 'category'>";
			for(var i = 0; i < data.length; i++)
			{
				category += "<option value = '"+data[i].term_id+"'>"+data[i].name+"</option>";
			}
			category += "</select>";
		}
		
		function loadObsType(response)
		{
			if(response == null)
			{
				setMessage("There was an error communicating with the server", "failure");
				return;
			}

			$.get("/goma/admin/term-ops", {mode: "list", dictionary_id: Statics.METHOD},  loadMethod);
			var data = response.data;
			
			obsType = "<select id = 'type' name = 'type'>";
			
			for(var i = 0; i < data.length; i++)
			{
				
				obsType += "<option value = '"+data[i].term_id+"'>"+data[i].name+"</option>";
				
			}			
			obsType += "</select>";
			
		}
		
		function loadMethod(response)
		{
			if(response == null)
			{
				setMessage("There was an error communicating with the server", "failure");
				return;
			}
			
			var data = response.data;
			method = "<select id = 'method' name = 'method'>";
			for(var i = 0; i < data.length; i++)
			{
				method += "<option value = '"+data[i].term_id+"'>"+data[i].name+"</option>";
			}
			method += "</select>";
			

			$("#mediumList").html(medium);
			$("#categoryList").append(category);
			$("#typeList").append(obsType);
			$("#methodList").append(method);
		}
		

		
		
		/**
		 * Function to receive the HTTP request response from user fetch request and render it as a form
		 *
		 * @param (HTTPResponse) The object containing the user
		 */
		 function processSave(response)
		 {
			if(response.code == "success")
				processQuery("listTuples");
			setMessage(response.message, response.code);
		 }
		
		

		/**
		 * Function to display the form for new organization creation
		 *
		 */
		 function create()
		{
			$("#mode").val("create");
			$.getJSON("/goma/admin/obs-ops", $("#dynamicForm").serialize(), processSave);

		}
		 
		 
		/**
		 * Function to request the list of organization types from the servlet
		 *
		 */
		function fetchOrganizationTypes()
		{
			$.get("/goma/utilities", {mode: "listOrganizationTypes"}, loadOrganizationTypes);
		}
		
		
		/**
		 * Function to receive the HTTP request response from organization types request and render it as a select
		 *
		 * @param (HTTPResponse) The object containing the list of organization types
		 */
		function loadOrganizationTypes(response)
		{
			var orgTypeStr = "<select class = 'picker' name = 'type_id' id ='type_id'>";
			var orgTypes = response.data;
			for(var i = 0; i < orgTypes.length; i++)
			{
				orgTypeStr += "<option value = '"+orgTypes[i].type_id+"'>"+orgTypes[i].type+"</option>";
			}
			orgTypeStr += "</select>";
			$("#type_well").html(orgTypeStr);
		}
		
    </script>
  </head>
<body>
		<%@ include file = "header.jsp" %>
		<%@ include file = "menu.jsp" %>
	<div class = "console">
		<div class = "page-title">Manage Observation Parameters</div>
		<div class = "operations">
			<ul>
			</ul>
		</div>
		<form id = 'dynamicForm'>
			<div id = "obs-container">
				<table class = 'formTable'>
					<tr><td class='fieldname required'>Medium:</td><td><span id = "mediumList"></span></td></tr>
					<tr><td class='fieldname required'>Observation Category:</td><td><span id = "categoryList"></span></td></tr>
					<tr><td class='fieldname required'>Observation Type:</td><td><span id = "typeList"></span></td></tr>
					<tr><td class='fieldname required'>Method of Analysis:</td><td><span id = "methodList"></span></td></tr>
				</table>
				<li class = "operation"><a href = "#" class = "op-add button" onClick = "create();">Add New</a></li>
			</div>
			<input type = 'hidden' id = 'mode' name = 'mode' value = 'create'>
		</form>
		
		<div id = "dynamic"></div>
		
	</div>
  </body>
    
</html>