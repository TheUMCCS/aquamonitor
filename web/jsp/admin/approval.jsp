<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
  <head>
    <title>Approvals - GoMonitor</title>
    <%@ include file = "imports.jsp" %>

 
    <script type="text/javascript">

		$(function() {
			//processQuery("list");
  			registerButtons();
			registerTooltips();
		});
	
		/**
		 * Initialize the display after the user session is augmented
		 *
		 */
		function init()
		{
			$("#dynamic").html("");
		}
		
		/**
		 * Depending on the operation specified hit the appropriate servlet for data
		 *
		 * @param (String) The operation to be performed on the paging or noop if the first page is to be loaded
		 */
		function list()
		{
			 $.get("/goma/admin/approval-ops", {mode: "list", type: $('input:radio[name=type]:checked').val()},  processResponse);
		}
		
		
		/**
		 * Function to receive the HTTP request response from the list request and render it as a table
		 *
		 * @param (HTTPResponse) The object containing the search results
		 */
		function processResponse(response)
		{
			
			var data = response.data;
			$("#currentOp").text(" / "+$('input[name=type]:checked + label').text());
			var colSet = "[";
			<%
			if(((HttpServletRequest) request).isUserInRole("CAU"))
			{
			%>
			colSet += "{ \"sTitle\": \"<input type='checkbox' value = '0' name='selectAllB' id='selectAllB' onClick='jqCheckAll(this.id)'>\"}, ";
			<%
			}
			%>
			colSet += "{ \"sTitle\": \"Click to View Details\" }, { \"sTitle\": \"Comment\" }, { \"sTitle\": \"Requestor\" }, { \"sTitle\": \"Date Requested\" }, { \"sTitle\": \"Status\" }]";
			var aDataSet = "[ ";
			for(var i = 0; i < data.length; i++)
			{
				<%
				if(((HttpServletRequest) request).isUserInRole("CAU"))
				{
				%>
				aDataSet += '["<input type = \'checkbox\' name = \'request_id\' onClick = \'highlightRow(this);\' value = \''+data[i].request_id+'\'>",'+
				<%
				}
				else
				{
				%>
				colSet = "[ { \"sTitle\": \"Click to View Details\" }, { \"sTitle\": \"Comment\" }, { \"sTitle\": \"Requestor\" }, { \"sTitle\": \"Date Requested\" }, { \"sTitle\": \"Status\" }]";
				aDataSet += '['+
				<%
				}
				%>
							'"<a href = \'#\' onClick = \'fetch('+data[i].parent_id+');\' class = \'details\'>Details</a>' +
							'","'+data[i].comment +
							'","'+data[i].requestor +
							'","'+data[i].date_created +
							'","'+data[i].approval_status + '"';
				aDataSet += ']';			
				if(i != (data.length - 1))
						aDataSet += ",";
			}
			aDataSet += " ]";

			$('#dynamic').html( '<table cellpadding="0" cellspacing="0" border="0" class="display" id="consoleTable"></table>' );
			oTable = $('#consoleTable').dataTable( {
					"aaData": eval(aDataSet),
					"aoColumns": eval(colSet),
					"sDom": '<"top"fp<"clear">>rt<"bottom"p<"clear">>',
					"iDisplayLength": 25,
					"aLengthMenu": [[25, 50, 100], [25, 50, 100]],
					"aaSorting": [[2,'asc']]
			} );
		}
		
		/**
		 * Function to save the data entered in the station form
		 *
		 * 
		 */
		function update(status_id)
		{		
			$("#status_id").val(status_id);
			var inputs = $("#dynamicForm").validator();
			if(!inputs.data("validator").checkValidity())
				return false;	
			$('input, textarea').each(function(){
			    $(this).val(jQuery.trim($(this).val()));
			    $(this).val($(this).val().replace(/\n/g,"<br>"));
			    $(this).val($(this).val().replace(/\"/g,"'"));
			});
			if($('input:checkbox[name=request_id]:checked').length < 1)
			{
				setMessage("At least one request must be selected", "warning");
				return;
			}
			$.getJSON("/goma/admin/approval-ops", $("#dynamicForm").serialize(), function(response)
				{
					setMessage(response.message, response.code);
					list();
				 }		
			);
			$("#floatForm").remove();
		}

		/**
		 * Function to request the parent data from the servlet
		 *
		 * @param (Number) The id of the parent to be fetched for editing
		 */
		function fetch(parent_id)
		{
			var type = $('input:radio[name=type]:checked').val();
			if (type == "program")
				window.open("/goma/details?type=program&program_id="+parent_id,'name','height=600,width=650');
			else if (type == "project")
				window.open("/goma/details?type=project&project_id="+parent_id,'name','height=600,width=650');
			else if (type == "station")
				window.open("/goma/details?type=station&station_id="+parent_id,'name','height=600,width=650');
			else if (type == "obsParam")
				window.open("/goma/details?type=obsParam&obs_param_id="+parent_id,'name','height=600,width=650');
			else if (type == "obsTuple")
				window.open("/goma/details?type=obsTuple&obs_tuple_id="+parent_id,'name','height=600,width=650');
			else if (type == "dictionaryTerm")
				window.open("/goma/details?type=dictionaryTerm&term_id="+parent_id,'name','height=600,width=650');
			else if (type == "gz")
				window.open("/goma/details?type=gz&gz_id="+parent_id,'name','height=600,width=650');
		}

		//This function is here in case we want to show the item details in the page itself
		function load(response)
		{
			
		}
		
		/**
		 * Function to allow the user to set a comment if the status is "Incomplete" or "Rejected" 
		 *
		 * @param (Number) The id of the approval status
		 */
		function setComment(elem, approval_status_id)
		{
			 $("#floatForm").remove();
			 $(elem).after("<div id = 'floatForm'>Enter Comment:<br><input type = 'text' name = 'comment' id = 'comment'><br><span class = 'operation'><a href = '#' class = 'op-save button' onClick = 'update("+approval_status_id+");'>Save</a></span></div>");
		}
    </script>
  </head>
<body>
		<%@ include file = "header.jsp" %>
		<%@ include file = "menu.jsp" %>
	<div class = "console">
		<div class = "page-title">Approvals<span id = "currentOp"></span></div>
		<div class = "operations">
			<ul>
				
			</ul>
		</div>
		<form id = 'dynamicForm'>
			<input type = "radio" name = "type" id = "rb_program" value = "program" checked = "checked"><label for "rb_program">Program</label>
			<input type = "radio" name = "type" id = "rb_project" value = "project"><label for "rb_project">Project</label>
			<input type = "radio" name = "type" id = "rb_station" value = "station"><label for "rb_station">Station</label>
			<input type = "radio" name = "type" id = "rb_gz" value = "gz"><label for "rb_gz">Geographical Zone</label>
			<input type = "radio" name = "type" id = "r_obs_param" value = "obsParam"><label for "rb_obs_param">Observation Parameter</label>
			<input type = "radio" name = "type" id = "r_obs_tuple" value = "obsTuple"><label for "rb_obs_tuple">Observed Parameter</label>
			<input type = "radio" name = "type" id = "rb_dt" value = "dictionaryTerm"><label for "rb_dt">Dictionary Term</label>
			<br/><br/>
			<div class = "nestedOps">
			<span class = "operation"><a href = "#" class = "op-list button" onClick = "list();">List Approvals</a></span>

			<%
			if(((HttpServletRequest) request).isUserInRole("CAU"))
			{
			%>

			<span class = "operation"><a href = "#" class = "op-check button" onClick = "update(4);">Approve</a></span>
			<span class = "operation"><a href = "#" class = "op-info button" onClick = "setComment(this, 5)">Incomplete</a></span>
			<span class = "operation"><a href = "#" class = "op-denied button" onClick = "setComment(this, 6);">Reject</a></span>
			</div>
			<%
			}
			%>
			<input type = 'hidden' id = 'mode' name = 'mode' value = 'update'>
			<input type = 'hidden' id = 'status_id' name = 'status_id' value = '4'>
			<input type = 'hidden' id = 'project_id' name = 'project_id' value = '<%=request.getParameter("project_id")%>'>
			<div id = "dynamic"></div>
		</form>	
	</div>
	
  </body>
</html>