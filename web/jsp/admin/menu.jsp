	<div id = "menuPane">

		<div class = "pane-header">User Administration</div>
		<div class = "pane-well">
			<a href = "/goma/admin/profile">My Profile</a>
			<%
			if(((HttpServletRequest) request).isUserInRole("CAU"))
			{
			%>
			<a href = "/goma/admin/users">List Users</a>
			<%
			}
			%>
			<a href = "/goma/admin/">My Dashboard</a>
		</div>

		<div class = "pane-header">Data Administration</div>
		<div class = "pane-well">
			<%
			if(((HttpServletRequest) request).isUserInRole("CAU"))
			{
			%>
			<a href = "/goma/admin/announcements">Announcements</a>
			<a href = "/goma/admin/organizations">Organizations</a>
			<%
			}
			%>
			
			<a href = "/goma/admin/programs">Programs</a>
			<a href = "/goma/admin/projects">Projects</a>
			<a href = "/goma/admin/stations">Stations</a>
			<a href = "/goma/admin/bulk-add">Bulk Add Stations</a>
			<a href = "/goma/admin/observation-tuples">Observation Parameters</a>
			<a href = "/goma/admin/gz">Geographical Zones</a>
			<a href = "/goma/admin/dictionaries">Dictionaries </a>

			<a href = "/goma/admin/approvals">Approval Requests</a>
		</div>
		
		<div class = "pane-header">Public Pages</div>
		<div class = "pane-well">
			<a href = "/goma/search">Search</a>
			<a href = "/goma/about">About</a>
			<a href = "/goma/">Home</a>
		</div>
	</div>