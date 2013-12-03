<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
  <head>
    <title>User Administration Console - GoMonitor</title>
    <%@ include file = "imports.jsp" %>

	<%
		if(((HttpServletRequest) request).isUserInRole("CAU"))
		{
	%>

	<script type="text/javascript" src="/goma/web/js/jquery.pstrength-min.1.2.js"></script>
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
			 $.get("/goma/admin/user-ops", {mode: operation},  processResponse);
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
				aDataSet += '["<input type = \'checkbox\' name = \'user_id\' onClick = \'highlightRow(this);\' value = \''+data[i].user_id+'\'>'+
							'","<a href = \'#\' onClick = \'fetch('+data[i].user_id+');\' class = \'details\'>'+ data[i].email + '</a>' +
							'","'+data[i].first_name + ' ' + data[i].last_name +
							'","'+data[i].role +
							'","'+data[i].job_title +'"]';
				if(i != (data.length - 1))
						aDataSet += ",";
			}
			aDataSet += " ]";

			var colSet;

			colSet = "[ { \"sTitle\": \"<input type='checkbox' value = '0' name='selectAllB' id='selectAllB' onClick='jqCheckAll(this.id)'>\"}, { \"sTitle\": \"Email\" }, { \"sTitle\": \"Name\" }, { \"sTitle\": \"Role\" }, { \"sTitle\": \"Title\" } ]";

//                        alert(aDataSet);
//                        alert(colSet);
			$('#dynamic').html( '<table cellpadding="0" cellspacing="0" border="0" class="display" id="consoleTable"></table>' );
			oTable = $('#consoleTable').dataTable( {
					"aaData": eval(aDataSet),
					"aoColumns": eval(colSet),
					"sDom": '<"top"fp<"clear">>rt<"bottom"p<"clear">>',
					"iDisplayLength": 20,
					"aLengthMenu": [[25, 50, 100], [25, 50, 100]],
					"aaSorting": [[2,'asc']]
			} );
			$("#mode").val("delete");
			
		}
		
		
		/**
		 * Function to Function to save the changes made to the user form
		 *
		 * 
		 */
		function update()
		{
			if(!checkPassword())
				return false;
			var inputs = $("#dynamicForm").validator();
			$('input, textarea').each(function(){
			    $(this).val(jQuery.trim($(this).val()));
			    $(this).val($(this).val().replace(/\n/g,"<br>"));
			    $(this).val($(this).val().replace(/\"/g,"'"));
			});
			if(inputs.data("validator").checkValidity())		
				$.getJSON("/goma/admin/user-ops", $("#dynamicForm").serialize(), processSave);
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
		 * Function to request the user data from the servlet
		 *
		 * @param (Number) The id of the user to be fetched for editing
		 */
		function fetch(user_id)
		{
			$.get("/goma/admin/user-ops", {mode: "fetch", user_id: user_id}, load);
		}
		
		
		/**
		 * Function to receive the HTTP request response from user fetch request and render it as a form
		 *
		 * @param (HTTPResponse) The object containing the user
		 */		 
		function load(response)
		{
			$("#currentOp").text("/ Editing User");
			var formString = "";
			var data = response.data;
			/*
			for(var i = 0; i < data.length; i++)
			{
				for (var key in data[i]) {
					  if(data[i].hasOwnProperty(key)) {
					    if(data[i][key] == null || data[i][key] == "")
					    	data[i][key] = "N/A";
					  }
					}
			}
			*/
			for(var i = 0; i < data.length; i++)
			{
				formString += "<table class = 'formTable'>";
				formString += "<tr><td class = 'fieldname'>Email:</td><td>"+data[i].email+"</td><td></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Password:</td><td><span id = 'password_well'>Saved. Click the edit button to change.</span></td><td><a title = 'Click to edit this field' class = 'edit-control' href = '#' onClick = 'activatePasswordField();'></a></td></tr>";
				formString += "<tr><td class = 'fieldname'>User Role:</td><td><span id = 'roles_well'>"+data[i].role+"<input type = 'hidden' id = 'role' name = 'role' value = '"+data[i].role+"'></span></td><td><a title = 'Click to edit this field' class = 'edit-control' href = '#' onClick = 'fetchUserRoleList();'></a></td></tr>";
				formString += "<tr><td class = 'fieldname required'>First Name:</td><td><input title = 'Enter the first name of the user' type = 'text' id = 'first_name' name = 'first_name' required = 'required' maxlength = '40' value = '"+data[i].first_name+"'></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Last Name:</td><td><input title = 'Enter the last name of the user' type = 'text' id = 'last_name' name = 'last_name' required = 'required' maxlength = '50' value = '"+data[i].last_name+"'></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Job Title:</td><td><input title = 'Enter the job title of the user in their parent organization' type = 'text' id = 'job_title' name = 'job_title' required = 'required' maxlength = '40' value = '"+data[i].job_title+"'></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Organization:</td><td><span id = 'organization_well'>"+data[i].organization+"<input type = 'hidden' id = 'organization_id' name = 'organization_id' value = '"+data[i].organization_id+"'></span></td><td><a title = 'Click to edit this field' class = 'edit-control' href = '#' onClick = 'fetchOrganizationList();'></a></td></tr>";
				formString += "<tr><td class = 'fieldname'>Address:</td><td><input title = 'Enter the street address of the user' type = 'text' id = 'address' name = 'address' maxlength = '200' value = '"+data[i].address+"'></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Phone:</td><td><input title = 'Enter the phone number of the user including area code. Do not include dashes or other separators' type = 'text' id = 'phone' name = 'phone' required = 'required' maxlength = '14' value = '"+data[i].phone+"'></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname'>Fax:</td><td><input title = 'Enter the fax number of the user' type = 'text' id = 'fax' name = 'fax' maxlength = '15' value = '"+data[i].fax+"'></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname'>Homepage:</td><td><input title = 'Enter the website address of the user beginning with http://...' type = 'url' id = 'website' name = 'website' maxlength = '1000' value = '"+data[i].website+"'></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname'>Date Created:</td><td>"+data[i].date_created+"</td><td></td></tr>";
				if(data[i].date_updated != null)
					formString += "<tr><td class = 'fieldname'>Date Updated:</td><td>"+data[i].date_updated+"</td><td></td></tr>";
				formString += "</table>";
				formString += "<input type = 'hidden' id = 'user_id' name = 'user_id' value = '"+data[i].user_id+"'>";
				formString += "<input type = 'hidden' id = 'user_role_id' name = 'user_role_id' value = '"+data[i].user_role_id+"'>";
				formString += "<input type = 'hidden' id = 'person_id' name = 'person_id' value = '"+data[i].person_id+"'>";
				formString += "<input type = 'hidden' id = 'created_by' name = 'created_by' value = '"+data[i].created_by+"'>";
			}
			
			formString += "<br><span class = 'operation'><a href = '#' onClick = 'update();' class = 'op-save'>Save Changes</a></span>";

			$("#dynamic").html(formString);
			$("#mode").val("update");
			registerTooltips('dynamic');
			registerButtons();
		}
		
		/**
		 * Function to display the form for new user creation
		 *
		 */
		 function create()
		{
			$("#currentOp").text("/ Adding New User");
			var formString = "";
		
			formString += "<table class = 'formTable'>";
			formString += "<tr><td class = 'fieldname'>Email:</td><td><input title = 'Enter the email address of this user' type = 'email' name = 'username' id = 'username' required = 'required' maxlength = '100'></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname required'>Password:</td><td><span id = 'password_well'></span></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname'>User Role:</td><td><span id = 'roles_well'></span></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname required'>First Name:</td><td><input title = 'Enter the first name of the user' type = 'text' id = 'first_name' name = 'first_name' required = 'required' maxlength = '40'></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname required'>Last Name:</td><td><input title = 'Enter the last name of the user' type = 'text' id = 'last_name' name = 'last_name' required = 'required' maxlength = '40'></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname required'>Job Title:</td><td><input title = 'Enter the job title of the user in their parent organization' type = 'text' id = 'job_title' name = 'job_title' required = 'required' maxlength = '40'></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname required'>Organization:</td><td><span id = 'organization_well'></span></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname'>Address:</td><td><input title = 'Enter the street address of the user' type = 'text' id = 'address' name = 'address' maxlength = '200'></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname required'>Phone:</td><td><input  title = 'Enter the phone number of the user including area code. Do not include dashes or other separators' type = 'text' id = 'phone' name = 'phone' required = 'required' maxlength = '15'></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname'>Fax:</td><td><input title = 'Enter the fax number of the user. Do not include dashes or other separators' type = 'number' id = 'fax' name = 'fax' maxlength = '15'></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname'>Website:</td><td><input  title = 'Enter the website address of the user beginning with http://...' type = 'url' id = 'website' name = 'website' maxlength = '1000'></td><td></td></tr>";
			formString += "</table>";
		
			formString += "<br><span class = 'operation'><a href = '#' onClick = 'update();' class = 'op-save'>Save Changes</a></span>";

			$("#dynamic").html(formString);
			$("#mode").val("create");
			activatePasswordField();
			registerTooltips();
			fetchUserRoleList();
			$("#dynamicForm").validator();
			setTimeout("fetchOrganizationList();", 500);
			
			registerButtons();
		}
		 
		/**
		 * Function to allow the administrator to change password for a user
		 *
		 */
		function activatePasswordField()
		{
			$("#password_well").html("<span class = 'nested-input'>Enter Once:</span><input  class = 'password' title = 'Must be between 7 and 15 characters in length<br>Must contain at least one uppercase letter<br>Must contain at least one special character<br>Must contain at least one number' type = 'password' id = 'password' name = 'password'><br><span class = 'nested-input'>Confirm:</span><input title = 'Retype the password exactly as above' type = 'password' id = 'passwordConfirm'>");
			//$("#passwordConfirm").blur(checkPassword());
			$('.password').pstrength();
			registerTooltips('dynamic');
			
		}
		 
		/**
		 * Function to check if the password and confirm password fields match
		 *
		 */
		function checkPassword() 
		{
			if (!$("#password").val())
				 return true;
			if($("#passwordConfirm").val() != $("#password").val())
			{
				setMessage("Warning: Password and Confirm Password Fields do not Match", "warning");
				return false;
			}
			//validate length
			if ( $("#password").val().length < 7 ) {
				setMessage("Warning: Password is less than 7 characters", "warning");
			 	return false;
			}
			//validate uppercase letter
			if ( !$("#password").val().match(/[A-Z]/) ) {
				setMessage("Warning: Password does not contain an uppercase letter", "warning");
			 	return false;
			}
			//validate number
			if ( !$("#password").val().match(/\d/) ) {
				setMessage("Warning: Password does not contain a number", "warning");
			 	return false;
			}			
			//validate special character
			if ( !$("#password").val().match(/\W/) ) {
				setMessage("Warning: Password does not contain a special character", "warning");
			 	return false;
			}
			//check if username is the same as the password
			if($("#username").val() == $("#password").val())
			{
				setMessage("Warning: Password and Username cannot be the same", "warning");
				return false;
			}
			return true;
		}
		/**
		 * Function to request the list of organizations from the servlet
		 *
		 */
		function fetchOrganizationList()
		{
			$.get("/goma/utilities", {mode: "listOrganizations"}, loadOrganizationList);
		}
		
		
		/**
		 * Function to receive the HTTP request response from organizations list request and render it as a select
		 *
		 * @param (HTTPResponse) The object containing the list of organizations
		 */
		function loadOrganizationList(response)
		{
			var orgStr = "<select class = 'picker' name = 'organization_id' id ='organization'>";
			var orgs = response.data;
			for(var i = 0; i < orgs.length; i++)
			{
				orgStr += "<option value = '"+orgs[i].organization_id+"'>"+orgs[i].name+"</option>";
			}
			orgStr += "</select>";
			$("#organization_well").html(orgStr);
			registerTooltips();
		}
		
		/**
		 * Function to request the list of user roles from the servlet
		 *
		 */
		function fetchUserRoleList()
		{
			$.get("/goma/utilities", {mode: "listUserRoles"}, loadUserRoleList);
			$("#roles_well").html("<img src = '/goma/web/images/loader.gif'>");
		}
		
		
		/**
		 * Function to receive the HTTP request response from user roles list request and render it as a select
		 *
		 * @param (HTTPResponse) The object containing the list of user roles
		 */
		function loadUserRoleList(response)
		{
			var rolesStr = "<select class = 'picker' name = 'role' id ='role'>";
			
			var roles = response.data;
			
			for(var i = 0; i < roles.length; i++)
			{
				rolesStr += "<option value = '"+roles[i]+"'>"+roles[i]+"</option>";	
				
			}
			rolesStr += "</select>";
			
			$("#roles_well").html(rolesStr);
			registerTooltips();
		}
    </script>
  </head>
<body>
		<%@ include file = "header.jsp" %>
		<%@ include file = "menu.jsp" %>
	<div class = "console">
		<div class = "page-title">Manage Users<span id = "currentOp"></span></div>
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