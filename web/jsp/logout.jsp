<%
	((HttpServletRequest) request).getSession().invalidate();
	response.sendRedirect("home");
%>
