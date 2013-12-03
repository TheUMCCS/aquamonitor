<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
	<head>
    	<title>Administration Login - GoMonitor</title>
    	
    	<link rel="stylesheet" href="/goma/web/css/style.css">	
    	<script src="http://cdn.jquerytools.org/1.2.6/full/jquery.tools.min.js"></script>
   	    <script type="text/javascript">
		   	$(function() {
					$("#dynamic").hide();
			});
		   	
   	    	function resetPassword()
   	    	{
   	    		$.getJSON("/goma/reset-password", {username: $("#j_username").val()}, function(response){$("#dynamic").html(response.message);});
  	    	}
   	    </script>
    </head>
	<body class = "login">
		<div id = "control"></div>
		<div class = "console">
			<div class = "page-title">Administration Login</div>
			<%
			if (((HttpServletRequest) request).getParameter("error") != null) 
			{
			%>
			    <p class="error" style = "width: 200px; padding-left: 20px; height: 30px;">Login failed. Please try again.</p>
			<%
			}
			%>
			<form method="POST" action="j_security_check">
				<table>
					<tr>
						<td>Email:</td>
						<td><input type="text" name="j_username" id = "j_username"/></td>
					</tr>
					<tr>
						<td>Password:</td>
						<td><input type="password" name="j_password"/ ></td>
					</tr>
					<tr>
						<td colspan="2"><input type="submit" value="Sign In" /></td>
					</tr>
					<tr>
						<td colspan="2">Note: Email and Password are Case-sensitive</td>
					</tr>
				</table>
			</form>
			<a href = "#" onClick="$('#dynamic').show();">I Forgot My Password</a>
			<div id = "dynamic">Enter your username above and click the button below. A new password will be mailed to your email address.<br><input type = 'button' value = 'Reset Password' onClick="resetPassword();"></div>
		</div>
	</body>    
</html>
    