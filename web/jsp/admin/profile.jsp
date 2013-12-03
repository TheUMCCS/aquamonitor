<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
  <head>
    <title>User Profile - GoMonitor</title>
    <%@ include file = "imports.jsp" %>

	<script type="text/javascript" src="/goma/web/js/jquery.pstrength-min.1.2.js"></script>
    <script type="text/javascript">
		$(function() {

		});
		
		
		/**
		 * Initialize the display after the user session is augmented
		 *
		 */
		function init()
		{
			fetch();	
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
			$('input, textarea').each(function(){
			    $(this).val(jQuery.trim($(this).val()));
			    $(this).val($(this).val().replace(/\n/g,"<br>"));
			    $(this).val($(this).val().replace(/\"/g,"'"));
			});
			var inputs = $("#dynamicForm").validator();
			if(inputs.data("validator").checkValidity())
				$.getJSON("/goma/admin/person-ops", $("#dynamicForm").serialize(), processSave);
		}
		
		/**
		 * Function to receive the HTTP request response from user fetch request and render it as a form
		 *
		 * @param (HTTPResponse) The object containing the user
		 */
		 function processSave(response)
		 {
			setMessage(response.message, response.code);
		 }
		
		
		/**
		 * Function to request the user data from the servlet
		 *
		 * @param (Number) The id of the user to be fetched for editing
		 */
		function fetch(user_id)
		{
			$.get("/goma/admin/person-ops", {mode: "fetch", user_id: user_id}, load);
		}
		
		
		/**
		 * Function to receive the HTTP request response from user fetch request and render it as a form
		 *
		 * @param (HTTPResponse) The object containing the user
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
					    	data[i][key] = "";
					  }
					}
			}
			
			for(var i = 0; i < data.length; i++)
			{
				formString += "<table class = 'formTable'>";
				formString += "<tr><td class = 'fieldname'>Email:</td><td>"+data[i].email+"</td><td></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Password:</td><td><span id = 'password_well'>Saved. Click the edit button to change.</span></td><td><a title = 'Click to edit this field' class = 'edit-control' href = '#' onClick = 'activatePasswordField();'></a></td></tr>";
				formString += "<tr><td class = 'fieldname required'>First Name:</td><td><input  title = 'Enter your first name'type = 'text' id = 'first_name' name = 'first_name' required = 'required' maxlength = '40' value = '"+data[i].first_name+"'></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Last Name:</td><td><input  title = 'Enter your last name' type = 'text' id = 'last_name' name = 'last_name' required = 'required' maxlength = '50' value = '"+data[i].last_name+"'></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname'>Organization:</td><td>"+data[i].organization+"</td><td></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Job Title:</td><td><input title = 'Enter your job title' type = 'text' id = 'job_title' name = 'job_title' required = 'required' maxlength = '40' value = '"+data[i].job_title+"'></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname'>Biographical Information:</td><td><textarea title = 'Type in a brief biographical sketch' id = 'bio' name = 'bio'>"+data[i].bio+"</textarea></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname'>Address:</td><td><input title = 'Enter your official street address' type = 'text' id = 'address' name = 'address' maxlength = '200' value = '"+data[i].address+"'></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Phone:</td><td><input title = 'Enter your phone number including area code. Do not include dashes or other separators' type = 'number' id = 'phone' name = 'phone' maxlength = '15' value = '"+data[i].phone+"'></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname'>Fax:</td><td><input title = 'Enter your fax number including area code. Do not include dashes or other separators' type = 'number' id = 'fax' name = 'fax' maxlength = '15' value = '"+data[i].fax+"'></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname'>Website:</td><td><input title = 'Enter your homepage address beginning with http://...' type = 'url' id = 'website' name = 'website' maxlength = '1000' value = '"+data[i].website+"'></td><td></td></tr>";
				formString += "</table>";
			}
			
			formString += "<br><span class = 'operation'><a href = '#' onClick = 'update();' class = 'op-save'>Save Changes</a></span>";

			$("#dynamic").html(formString);
			$("#mode").val("update");
			registerTooltips('dynamic');
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
		 
    </script>
  </head>
<body>
		<%@ include file = "header.jsp" %>
		<%@ include file = "menu.jsp" %>
	<div class = "console">
		<div class = "page-title">My Profile</div>
		<div class = "operations">

		</div>
		<form id = 'dynamicForm'>
			<input type = 'hidden' id = 'mode' name = 'mode' value = 'update'>
			<div id = "dynamic"></div>
		</form>
	</div>
  </body>
</html>