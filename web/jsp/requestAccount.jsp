
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
  <head>
    <title>New Account Request - GoMonitor</title>
   
	<link rel="stylesheet" href="/goma/web/css/style.css">		
	<script src="http://cdn.jquerytools.org/1.2.6/full/jquery.tools.min.js"></script>
	<script type="text/javascript" src="/goma/web/js/scripts.js"></script>
    <script type="text/javascript">
   
		$(function() {
			registerTooltips();
			registerButtons();
		});
		/**
		 * Function to submit the data in the account request form for approval
		 *
		 * 
		 */
		function request()
		{
			var inputs = $("#accountRequest").validator();
			if(inputs.data("validator").checkValidity())
				$.getJSON("/goma/process-account", $("#accountRequest").serialize(), processRequest);
		}
		/**
		 * Function to receive the HTTP request response from account request and show the result
		 *
		 * @param (HTTPResponse) The object containing the user
		 */
		 function processRequest(response)
		 {
			if(response.code == "success")
				$("#dynamic").html("Your request has been submitted successfully. You will be contacted at the email address provided once it has been processed. Thank you.");
			else
				$("#dynamic").html("There was a problem cummunicating with the server. Please try again later.");
		 }
	</script>
    
  </head>
  <body class = "splash">
  	<div id = "splash-menu">

  			<form name = "accountRequest" id = "accountRequest" method = "POST" action = "process-request">
		  		<table class = "formTable">
			  		<tr><td class="fieldname required">Email:</td><td><input type="email" required="required" id="email" name="email" title="Enter your email address"/></td><td/></tr>
		  			<tr><td class="fieldname required">First Name:</td><td><input type="text" required="required" name="first_name" id="first_name" pattern="[a-zA-Z ]{2,}" title="Enter your first name"/></td><td/></tr>
		  			<tr><td class="fieldname required">Last Name:</td><td><input type="text" required="required" name="last_name" id="last_name" pattern="[a-zA-Z ]{2,}" title="Enter your last name"/></td><td/></tr>
		  			<tr><td class="fieldname required">Job Title:</td><td><input type="text" required="required" name="job_title" id="job_title" pattern="[a-zA-Z ]{3,}" title="Enter your current job title"/></td><td/></tr>
		  			<tr><td class="fieldname required">Organization:</td><td><input type="text" required="required" name="organization" id="organization" pattern="[a-zA-Z ]{3,}" title="Enter the name of your organization"/></td><td/></tr>
		  			<tr><td class="fieldname required">Phone:</td><td><input type="number" required="required" name="phone" id="phone" title="Enter your phone number without any hyphens or spaces"/></td><td/></tr>
		  		</table>
		  		<br/>
		  		<span class="operation"><a class="op-request" onclick="request();" href="#">Submit Request</a></span>
	  		</form>
  	</div>
  	

  </body>
</html>

