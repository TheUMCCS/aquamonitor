
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
  <head>
    <title>Administration Console Dashboard - GoMonitor</title>
    <%@ include file = "imports.jsp" %>

        <script type="text/javascript">
		$(function() {
			
		});
		
		
		/**
		 * Initialize the display after the user session is augmented
		 *
		 */
		function init()
		{
			var prefix = "";
			<%
			if(!((HttpServletRequest) request).isUserInRole("CAU"))
			{
			%>
			prefix = "My ";
			<%
			}
			%>
			var statStr = '<div class = "dashboard-widget" id = "dwData"><span class = "widget-title">'+prefix+'Data</span>'
							+'<div class = "stat-elem"><a href = "programs"><span class = "subhead">Programs</span><span class = "stat-count" id = "prgStats"></span></a></div>'
							+'<div class = "stat-elem"><a href = "projects"><span class = "subhead">Projects</span><span class = "stat-count" id = "prjStats"></span></a></div>'
							+'<div class = "stat-elem"><a href = "stations"><span class = "subhead">Stations</span><span class = "stat-count" id = "stnStats"></span></a></div>'
							+'<div class = "stat-elem"><a href = "gz"><span class = "subhead">Geographic Zones</span><span class = "stat-count" id = "gzStats"></span></a></div>'
							+'<div class = "stat-elem"><a href = "dictionaries"><span class = "subhead">Dictionary Terms</span><span class = "stat-count" id = "termStats"></span></a></div>'
						+'</div>'
						+'<div class = "dashboard-widget" id = "dwApprovals"><span class = "widget-title">'+prefix+'Pending Approvals</span><div id = "approvalsWell"></div></div>'
						<%
						if(((HttpServletRequest) request).isUserInRole("CAU"))
						{
						%>
						+ '<div class = "dashboard-widget" id = "dwUser"><span class = "widget-title">Users</span><div id = "usersWell"></div></div>'
						<%
						}
						%>
						+ '<div class = "dashboard-widget" id = "dwAnnouncements"><span class = "widget-title">Recent Announcements</span><div id = "announcementsWell"></div></div>';
			$("#dynamic").html(statStr);
			<%
			if(((HttpServletRequest) request).isUserInRole("CAU"))
			{
			%>
			$.getJSON("/goma/admin/user-ops", {mode: "statistics"},  function(response) {
				$("#usersWell").html('<div class = "stat-elem"><a href = "users"><span class = "subhead">Total System Users</span><span class = "stat-count">'+response.count+'</span></a></div>');
			});			
			<%
			}
			%>
			$.getJSON("/goma/admin/program-ops", {mode: "statistics"},  function(response) {
				$("#prgStats").text(response.count);
			});
			$.getJSON("/goma/admin/project-ops", {mode: "statistics"},  function(response) {
				$("#prjStats").text(response.count);
			});
			$.getJSON("/goma/admin/station-ops", {mode: "statistics"},  function(response) {
				$("#stnStats").text(response.count);
			});
			$.getJSON("/goma/admin/term-ops", {mode: "statistics"},  function(response) {
				$("#termStats").text(response.count);
			});
			$.getJSON("/goma/admin/gz-ops", {mode: "statistics"},  function(response) {
				$("#gzStats").text(response.count);
			});
			$.getJSON("/goma/admin/approval-ops", {mode: "statistics"},  function(response) {
				var appStr = "";
				for(var i = 0; i < response.statistics.length; i++)
				{
					appStr += "<div class = 'stat-elem'><a href = 'approvals'><span class = 'subhead'>"+response.statistics[i].name+"</span><span class = 'stat-count'>"+response.statistics[i].count+"</span></a></div>";	
				}
				if(response.statistics.length == 0)
					appStr += "<div class = 'stat-elem'><span class = 'left'>No Pending Approvals</span></div>";
				$("#approvalsWell").html(appStr);
			});
			$.getJSON("/goma/admin/announcement-ops", {mode: "list"},  function(response) {
				var appStr = "";
				for(var i = 0; i < response.data.length && i < 3; i++)
				{
					appStr += "<div class = 'stat-elem'><span class = 'subhead'>"+response.data[i].name+"</span><br>"+response.data[i].description+"<div class = 'post-footer'><span class = 'left'>"+response.data[i].created_by+"</span><span class = 'right'>"+response.data[i].date_created+"</span></div></div>";
				}
				$("#announcementsWell").html(appStr);
			});
		}
		
		</script>
    
  </head>
  <body>
		<%@ include file = "header.jsp" %>
		<%@ include file = "menu.jsp" %>
	<div class = "console">
		<div class = "page-title">Administration Dashboard</div>
		<div class = "operations">

		</div>
		<div id = "dynamic">

		</div>
	</div>
  </body>
</html>

