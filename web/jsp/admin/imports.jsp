<%@ page contentType="text/html; charset=UTF-8" %>

    <meta name="viewport"
        content="width=device-width, initial-scale=1.0, user-scalable=no">
    <meta charset="UTF-8">
	<link rel="stylesheet" href="/goma/web/css/jquery-ui-1.8.6.custom.css">
	<link rel="stylesheet" href="/goma/web/css/style.css">		
	<script src="http://cdn.jquerytools.org/1.2.6/full/jquery.tools.min.js"></script>
	<script type="text/javascript" src="/goma/web/js/jquery-ui-1.8.6.custom.min.js"></script>
	<script type="text/javascript" src="/goma/web/js/jquery.dataTables.min.js"></script>

	<script type="text/javascript" src="/goma/web/js/scripts.js"></script>
	<script type="text/javascript" src="/goma/web/js/statics.js"></script>
	<script type="text/javascript">
	$(function() {
		$("#dynamic").html("<div class = 'allCenter'><img src = '/goma/web/images/indicator.gif'></div>");
		$.getJSON("/goma/admin/login-interceptor", function(response){ 
				if(response.code != "success")
					setMessage(response.message, response.code);
				else
					init();
			});
	});
	</script>