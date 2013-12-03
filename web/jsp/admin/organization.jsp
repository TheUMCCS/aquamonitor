<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<html>
  <head>
    <title>Organization Administration Console - GoMonitor</title>
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
			processQuery("listOrganizations");	
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
				aDataSet += '["<input type = \'checkbox\' name = \'organization_id\' onClick = \'highlightRow(this);\' value = \''+data[i].organization_id+'\'>'+
							'","<a href = \'#\' onClick = \'fetch('+data[i].organization_id+');\' class = \'details\'>'+ data[i].name + '</a>' +
							'","'+data[i].organization_type;
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

			colSet = "[ { \"sTitle\": \"<input type='checkbox' value = '0' name='selectAllB' id='selectAllB' onClick='jqCheckAll(this.id)'>\"}, { \"sTitle\": \"Name (Click record to edit)\" }, { \"sTitle\": \"Type\" }, { \"sTitle\": \"Website\" } ]";

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
			
		}
		
		
		/**
		 * Function to save the data entered in the organization form
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
				$.getJSON("/goma/admin/org-ops", $("#dynamicForm").serialize(), processSave);
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
				processQuery("listOrganizations");
			setMessage(response.message, response.code);
		 }
		
		
		/**
		 * Function to request the organization data from the servlet
		 *
		 * @param (Number) The id of the organization to be fetched for editing
		 */
		function fetch(organization_id)
		{
			$.get("/goma/admin/org-ops", {mode: "fetch", organization_id: organization_id}, load);
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
				formString += "<tr><td class = 'fieldname required'>Name:</td><td><input  title = 'Enter full organization name here' type = 'text' id = 'name' name = 'name' required = 'required' value = '"+data[i].name+"'></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Description:</td><td><textarea title = 'Enter a brief description of the organization' id = 'description' name = 'description' required = 'required'>"+data[i].description+"</textarea></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Type:</td><td><span id = 'type_well'>"+data[i].type+"<input type = 'hidden' id = 'type_id' name = 'type_id' value = '"+data[i].type_id+"'></span></td><td><a title = 'Click to edit this field' class = 'edit-control' href = '#' onClick = 'fetchOrganizationTypes();'></a></td></tr>";
				formString += "<tr><td class = 'fieldname'>Website:</td><td><input title = 'Enter the homepage address beginning with http://' type = 'url' id = 'website' name = 'website' value = '"+data[i].website+"'></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname'>Date Created:</td><td>"+data[i].date_created+"</td><td></td></tr>";
				if(data[i].date_updated != null)
					formString += "<tr><td class = 'fieldname'>Date Updated:</td><td>"+data[i].date_updated+"</td><td></td></tr>";
				formString += "</table>";
				formString += "<input type = 'hidden' id = 'user_id' name = 'organization_id' value = '"+data[i].organization_id+"'>";
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
			formString += "<tr><td class = 'fieldname required'>Name:</td><td><input  title = 'Enter full organization name here' type = 'text' id = 'name' name = 'name' required = 'required'></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname required'>Description:</td><td><textarea  title = 'Enter a brief description of the organization' id = 'description' name = 'description' required = 'required'></textarea></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname required'>Type:</td><td><span id = 'type_well'></span></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname'>Website:</td><td><input  title = 'Enter the homepage address beginning with http://'  type = 'url' id = 'website' name = 'website'></td><td></td></tr>";
			formString += "</table>";
			formString += "<input type = 'hidden' id = 'mode' name = 'mode' value = 'create'>";
			
			
			formString += "<br><span class = 'operation'><a href = '#' onClick = 'update();' class = 'op-save'>Save Changes</a></span>";
	
			$("#dynamic").html(formString);
			$("#mode").val("create");
			setTimeout("fetchOrganizationTypes();", 500);
			registerTooltips('dynamic');
			registerButtons();
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
		<div class = "page-title">Manage Organizations</div>
		<div class = "operations">
			<ul>
				<li class = "operation"><a href = "#" class = "op-list button" onClick = "processQuery('listOrganizations');">List</a></li>
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