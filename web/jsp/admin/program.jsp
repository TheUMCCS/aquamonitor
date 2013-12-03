<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
  <head>
    <title>Program Administration Console  GoMonitor</title>
    <%@ include file = "imports.jsp" %>
    <%@ page import = "edu.miami.ccs.goma.pojos.User" %>

    <script type="text/javascript">
		$(function() {

			
			$( "#dialog-form" ).dialog({
				title: 'Program Manager',
				autoOpen: false,
				height: 400,
				width: 600,
				modal: true,
                open: function (event, ui) {
                    $('#dialog-form').css('overflow', 'hidden');
                },
				buttons: {
					"Save": function() {
							savePerson();
							$( this ).dialog( "close" );
					},
					Cancel: function() {
						$( this ).dialog( "close" );
					}
				},
				close: function() {
					$("#personForm input").val("");
					//allFields.val( "" ).removeClass( "ui-state-error" );
				}
			});
			registerTooltips();
			registerButtons();
		});
		
		
		/**
		 * Initialize the display after the user session is augmented
		 *
		 */
		function init()
		{
			processQuery("listPrograms");	
		}
		


		
		/**
		 * Depending on the operation specified hit the appropriate servlet for data
		 *
		 * @param (String) The operation to be performed on the paging or noop if the first page is to be loaded
		 */
		function processQuery(operation)
		{
			<%
			if(((HttpServletRequest) request).isUserInRole("CAU"))
			{
			%>
			$.get("/goma/utilities", {mode: "listPrograms"},  processResponse);
			<%
			}
			else
			{
				User curr_user =  (User) session.getAttribute("curr_user"); 
			%>
			$.get("/goma/utilities", {mode: operation, organization_id: <%=curr_user.getOrganization().getOrganizationId()%>},  processResponse);
			<%
			}
			%>				 
		}
		
		
		/**
		 * Function to receive the HTTP request response from search request and render it as a table
		 *
		 * @param (HTTPResponse) The object containing the search results
		 */
		function processResponse(response)
		{
			var data = response.data;
			$("#currentOp").text(" / Program Listing");
			$("#basicOpts").hide();
			var aDataSet = "[ ";
			for(var i = 0; i < data.length; i++)
			{
				aDataSet += '["<input type = \'checkbox\' name = \'program_id\' onClick = \'highlightRow(this);\' value = \''+data[i].program_id+'\'>'+
							'","<a href = \'projects?program_name='+data[i].name+'&program_id='+data[i].program_id+'\' class = \'list-control\' title = \'Click to view all projects for this program\'> </a>' +
							'","<a href = \'#\' onClick = \'fetch('+data[i].program_id+');\' class = \'details\'>'+ data[i].name + '</a>' +
							'","'+data[i].program_manager +
							'","'+data[i].status;
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

			colSet = "[ { \"sTitle\": \"<input type='checkbox' value = '0' name='selectAllB' id='selectAllB' onClick='jqCheckAll(this.id)'>\"}, { \"sTitle\": \"View Projects\" }, { \"sTitle\": \"Name (Click record to edit)\" }, { \"sTitle\": \"Program Manager\" }, { \"sTitle\": \"Status\" }, { \"sTitle\": \"Website\" } ]";

//                        alert(aDataSet);
//                        alert(colSet);
			$('#dynamic').html( '<table cellpadding="0" cellspacing="0" border="0" class="display" id="consoleTable"></table>' );
			oTable = $('#consoleTable').dataTable( {
					"aaData": eval(aDataSet),
					"aoColumns": eval(colSet),
					"sDom": '<"top"fp<"clear">>rt<"bottom"p<"clear">>',
					"iDisplayLength": 25,
					"aLengthMenu": [[25, 50, 100], [25, 50, 100]],
					"aaSorting": [[2,'asc']]
			} );
			$("#mode").val("delete");
			if(data.length == 0)
				$('#consoleTable').after("NOTE: There may be additional records that have not been approved yet");
			$("#organizationWell").html(response.organization);
			<%
			if(((HttpServletRequest) request).isUserInRole("CAU"))
			{
			%>
			$.get("/goma/utilities", {mode: "listOrganizations"},  function(response){
				var data = response.data;
				var selStr = "<select name = 'organization_id' id = 'organization_id'><option value = '0' selected>Select Organization</option>";
				for(var i = 0; i < data.length; i++)
				{
					selStr += "<option value = '"+data[i].organization_id+"'>"+data[i].name+"</option>";
				}
				selStr += "</select>";
				
				$('#organizationWell').html(selStr);	
			});
			<%
			}
			%>
			
		}
		
		/**
		 * Function to save the data entered in the program manager form
		 *
		 * 
		 */
		function savePerson()
		{
			var inputs = $("#personForm").validator();
			if(!inputs.data("validator").checkValidity())
				return false;	
			$.getJSON("/goma/admin/person-ops", $("#personForm").serialize(),  function(response){
				if(response.code == "success")
					{
						$("#person_id").val(response.data[0].person_id);
					}

				setMessage(response.message, response.code);
			});
			
		}
		
		/**
		 * Function to save the data entered in the program form
		 *
		 * 
		 */
		function update()
		{
			var inputs = $("#dynamicForm").validator();
			$('input, textarea').each(function(){
			    $(this).val(jQuery.trim($(this).val()));
			    $(this).val($(this).val().replace(/\n/g,"<br>"));
			    $(this).val($(this).val().replace(/\"/g,"'"));
			});
			if(inputs.data("validator").checkValidity())
				$.getJSON("/goma/admin/program-ops", $("#dynamicForm").serialize(), processSave);
		}
		
		/**
		 * Function to save the data entered in the program form
		 *
		 * 
		 */
		function saveProgram()
		{
			$("#pmOpts").hide();
			$.getJSON("/goma/admin/program-ops", $("#programForm").serialize(), processSave);
			$("#pmForm input").val("");
			$("#programForm input").val("");
			$("#programForm textarea").val("");
		}
		
		/**
		 * Function to receive the HTTP request response from user fetch request and render it as a form
		 *
		 * @param (HTTPResponse) The object containing the user
		 */
		 function processSave(response)
		 {
			if(response.code == "success")
			{
				if(response.message != "Delete Successful")
				{
					//processQuery("list");
					fetch(response.program_id);
					$("#currentOp").text(" / Review and Save");
				}
				else
					processQuery("listPrograms");
			}
			setMessage(response.message, response.code);
		 }
		
		
		/**
		 * Function to request the program data from the servlet
		 *
		 * @param (Number) The id of the program to be fetched for editing
		 */
		function fetch(program_id)
		{
			$.get("/goma/admin/program-ops", {mode: "fetch", program_id: program_id}, load);
		}
		
		
		/**
		 * Function to receive the HTTP request response from program fetch request and render it as a form
		 *
		 * @param (HTTPResponse) The object containing the program
		 */		 
		function load(response)
		{
			$("#currentOp").text(" / Editing Program");
			var formString = "";
			var data = response.data;
			
			for(var i = 0; i < data.length; i++)
			{
				formString += "<div id = 'basicOpts'><table class = 'formTable'>";
				formString += "<tr><td class = 'fieldname required'>Program Name:</td><td><input type = 'text' id = 'name' name = 'name' required = 'required' maxlength = '225' title = 'Enter the program name' value = '"+data[i].name+"'></td><td></td></tr>";
				
				<%
				if(((HttpServletRequest) request).isUserInRole("CAU"))
				{
				%>
				
				formString += "<tr><td class = 'fieldname required'>Organization:</td><td><span id = 'organization_well'>"+data[i].organization+"<input type = 'hidden' id = 'organization_id' name = 'organization_id' value = '"+data[i].organization_id+"'></span></td><td><a title = 'Click to edit this field' class = 'edit-control' href = '#' onClick = 'fetchOrganizationList();'></a></td></tr>";
				<%
				}
				else
				{
				%>
				formString += "<tr><td class = 'fieldname'>Organization:</td><td>"+data[i].organization+"</td><td></td></tr>";
				<%
				}
				%>				 
				
				
				formString += "<tr><td class = 'fieldname required'>Description:</td><td><textarea id = 'description' name = 'description' required = 'required'  title = 'Enter a brief description, no HTML tags'>"+data[i].description+"</textarea></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname'>Homepage:</td><td><input  title = 'Enter the full address of the program website starting with http://...' type = 'url' id = 'website' name = 'website' maxlength = '1000' value = '"+data[i].website+"'></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Status:</td><td><span id = 'status_well'>"+data[i].status+"<input type = 'hidden' id = 'status_id' name = 'status_id' value = '"+data[i].status_id+"'></span></td><td><a class = 'edit-control' href = '#' onClick = 'fetchStatusList();' title = 'Click edit program status'></a></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Program Manager:</td><td>"+data[i].pm_first_name+" "+data[i].pm_last_name+"</td><td><a class = 'edit-control' href = '#' onClick = '$(\"#dialog-form\").dialog(\"open\");' title = 'Click to edit program manager details'></a></td></tr>";
				formString += "<tr><td class = 'fieldname'>Date Created:</td><td>"+data[i].date_created+"</td><td></td></tr>";
				formString += "<tr><td class = 'fieldname'>Approval Status:</td><td>"+data[i].approval+"</td><td></td></tr>";
				if(data[i].date_updated != null)
					formString += "<tr><td class = 'fieldname'>Date Updated:</td><td>"+data[i].date_updated+"</td><td></td></tr>";
				formString += "</table>";					
				formString += "<input type = 'hidden' id = 'user_id' name = 'program_id' value = '"+data[i].program_id+"'>";
				$("#personForm #first_name").val(data[i].pm_first_name);
				$("#personForm #last_name").val(data[i].pm_last_name);
				$("#personForm #job_title").val(data[i].pm_job_title);
				$("#personForm #email").val(data[i].pm_email);
				$("#personForm #address").val(data[i].pm_address);
				$("#personForm #phone").val(data[i].pm_phone);
				$("#personForm #fax").val(data[i].pm_fax);
				$("#personForm #website").val(data[i].pm_homepage);
				$("#personForm #person_id").val(data[i].pm_id);
				$("#personForm #pmMode").val("update");
			}
			
			formString += "<br><span class = 'operation'><a href = '#' onClick = 'update();' class = 'op-save'>Save Changes</a></span></div>";

			$("#dynamic").html(formString);
			$("#mode").val("update");
			registerButtons();
			registerTooltips('basicOpts');
		}
		
		
		function create()
		{
			createProgram();
		}
		
		/**
		 * Function to display the form for new program creation
		 *
		 */
		function createProgram(pm_id)
		{
			$("#currentOp").text(" / Adding New");
			$("#dynamic").html("");
			$("#basicOpts").show();
			
			$("#programMode").val("create");
			registerTooltips('basicOpts');
			registerButtons();
			setTimeout("fetchStatusList();", 500);
			
		}
		 
		 	
		/**
		 * Function to show the program manager selection options
		 *		 
		 */
		function showProgramManagerOptions()
		{
			var inputs = $("#programForm").validator();
			if(!inputs.data("validator").checkValidity())
				return false;	
			$("#basicOpts").hide();
			$("#pmOpts").show();

			registerTooltips('pmOpts');
			registerButtons();
		}
		
		/**
		 * Function to save the data entered in the program manager form
		 *
		 * 
		 */
		function saveProgramManager()
		{
			var inputs = $("#pmForm").validator();
			//alert(inputs.data("validator"));
			if(!inputs.data("validator").checkValidity())
				return false;	
			$.getJSON("/goma/admin/person-ops", $("#pmForm").serialize(),  function(response){
				if(response.code == "success")
					{
						$("#pm_id").val(response.data[0].person_id);
						
						//update();
						saveProgram();
					}
				else
					{
						setMessage(response.message, response.code);
					}
			});
			
		}
			
		/**
		 * Function to request the list of status values from the servlet
		 *
		 */
		function fetchStatusList()
		{
			$.get("/goma/utilities", {mode: "listStatusValues", status_type_id: Statics.PROGRAM_STATUS}, loadStatusList);
		}
		
		
		/**
		 * Function to receive the HTTP request response from status list request and render it as a select
		 *
		 * @param (HTTPResponse) The object containing the list of status values			 
		 */
		function loadStatusList(response)
		{
			var statusStr = "<select class = 'picker' name = 'status_id' id ='status_id'>";
			var statuses = response.data;
			for(var i = 0; i < statuses.length; i++)
			{
				if(statuses[i].status_id == $("#status_id").val())
					statusStr += "<option selected = 'selected' value = '"+statuses[i].status_id+"'>"+statuses[i].value+"</option>";
				else
					statusStr += "<option value = '"+statuses[i].status_id+"'>"+statuses[i].value+"</option>";
			}
			statusStr += "</select>";
			$("#status_well").html(statusStr);
			
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
		 * Function to show the program manager selection options
		 *		 
		 */
		function editProgramManager()
		{
			$( "#dialog-form" ).dialog( "open" );
		}
		
    </script>
  </head>
<body>
		<%@ include file = "header.jsp" %>
		<%@ include file = "menu.jsp" %>
	<div class = "console">
		<div class = "page-title">Manage Programs<span id = "currentOp"></span></div>
		<div class = "operations">
			<ul>
				<li class = "operation"><a href = "#" class = "op-list button" onClick = "processQuery('listPrograms');">List Programs</a></li>
				<li class = "operation"><a href = "#" class = "op-add button" onClick = "create();">Add New</a></li>
			<%
			if(((HttpServletRequest) request).isUserInRole("CAU"))
			{
			%>
				<li class = "operation"><a href = "#" class = "op-delete button" onClick = "update();">Delete</a></li>
			<%
			}
			%>	
			</ul>
		</div>
		<form id = 'dynamicForm'>
			<input type = 'hidden' id = 'mode' name = 'mode' value = 'delete'>
			<div id = "dynamic"></div>
		</form>
		 
		<div id = 'basicOpts' style = "display: none;">
			<span class = 'wizardStage'>Basic Program Information</span>
			<form name = 'programForm' id = 'programForm'>
				<table class = 'formTable'>
					<tr><td class = 'fieldname required'>Program Name:</td><td><input title = 'Enter name of the program here' type = 'text' id = 'name' name = 'name' required = 'required' maxlength = '225'></td><td></td></tr>
					<tr><td class = 'fieldname required'>Organization:</td><td><span id = 'organizationWell'></span></td><td></td></tr>
					<tr><td class = 'fieldname required'>Description:</td><td><textarea  title = 'Enter a brief description, no HTML tags' id = 'description' name = 'description' required = 'required'></textarea></td><td></td></tr>
					<tr><td class = 'fieldname'>Website:</td><td><input  title = 'Enter the full address of the program website starting with http://...' type = 'url' id = 'website' name = 'website' maxlength = '1000'></td><td></td></tr>
					<tr><td class = 'fieldname required'>Status:</td><td><span id = 'status_well'></span></td><td></td></tr>
				</table>
				<input type = 'hidden' name = 'mode' id = 'programMode' value = 'create'>
				<input type = 'hidden' name = 'pm_id' id = 'pm_id'/>		
				<br><span class = 'operation'><a href = '#' onClick = 'showProgramManagerOptions()' class = 'op-next'>Next</a></span>
			</form>
		</div>	
		<div id = 'pmOpts' style = "display: none;">
			<span class = 'wizardStage'>Program Manager Details</span>
			<form name = 'pmForm' id = 'pmForm'>
				<table class = 'formTable'>
					<tr><td class='fieldname required'>First Name:</td><td><input type='text' name='first_name' id='first_name'  title = 'Enter the first name' required = 'required' maxlength = '40'/></tr>
					<tr><td class='fieldname required'>Last Name:</td><td><input type='text' name='last_name' id='last_name'  title = 'Enter the last name' required = 'required' maxlength = '50'/></tr>
					<tr><td class='fieldname required'>Job Title:</td><td><input type='text' name='job_title' id='job_title'  title = 'Enter the job title of this person in their parent organization' required = 'required' maxlength = '40'/></tr>
					<tr><td class='fieldname required'>Email:</td><td><input type='email' name='email' id='email'  title = 'Enter the email address' required = 'required' maxlength = '100'/></tr>
					<tr><td class='fieldname'>Address:</td><td><input type='text' name='address' id='address'  title = 'Enter the street address' maxlength = '200'/></tr>
					<tr><td class='fieldname required'>Phone:</td><td><input type='text' name='phone' id='phone' pattern="[0-9]{5,}" title = 'Enter the phone number without hyphens or spaces' required = 'required' maxlength = '15'/></tr>
					<tr><td class='fieldname'>Fax:</td><td><input type='text' name='fax' id='fax' title = 'Enter the fax number without hyphens or spaces' maxlength = '15'/></tr>
					<tr><td class='fieldname'>Website:</td><td><input type='url' name='website' id='website'  title = 'Enter the full address of the website starting with http://...' maxlength = '255'/></td></tr>
				</table>
				<input type = 'hidden' name = 'mode' id = 'progManMode' value = 'create'>
				<br><span class = 'operation'><a href = '#' onClick = 'saveProgramManager();' class = 'op-save'>Save</a></span>
			</form>		
		</div>		
	</div>
	<div id = "dialog-form">
		<form name = "personForm" id = "personForm" onSubmit = "savePerson();">
			<table class = "formTable">
					<tr><td class='fieldname required'>First Name:</td><td><input type='text' name='first_name' id='first_name'  title = 'Enter the first name' required = 'required' maxlength = '40'/></tr>
					<tr><td class='fieldname required'>Last Name:</td><td><input type='text' name='last_name' id='last_name'  title = 'Enter the last name' required = 'required' maxlength = '50'/></tr>
					<tr><td class='fieldname required'>Job Title:</td><td><input type='text' name='job_title' id='job_title'  title = 'Enter the job title of this person in their parent organization' required = 'required' maxlength = '40'/></tr>
					<tr><td class='fieldname required'>Email:</td><td><input type='email' name='email' id='email'  title = 'Enter the email address' required = 'required' maxlength = '100'/></tr>
					<tr><td class='fieldname'>Address:</td><td><input type='text' name='address' id='address'  title = 'Enter the street address' maxlength = '200'/></tr>
					<tr><td class='fieldname required'>Phone:</td><td><input type='text' name='phone' id='phone' pattern="[0-9]{5,}" title = 'Enter the phone number without hyphens or spaces' required = 'required' maxlength = '15'/></tr>
					<tr><td class='fieldname'>Fax:</td><td><input type='text' name='fax' id='fax' title = 'Enter the fax number without hyphens or spaces' maxlength = '15'/></tr>
					<tr><td class='fieldname'>Website:</td><td><input type='url' name='website' id='website'  title = 'Enter the full address of the website starting with http://...' maxlength = '255'/></td></tr>
			</table>
			<input type = "hidden" name = "mode" id = "pmMode" value = "create"/>
			<input type = "hidden" name = "person_id" id = "person_id" value = ""/>
		</form>
	</div>
  </body>
</html>