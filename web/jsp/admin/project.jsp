<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
  <head>
    <title>Project Administration Console  GoMonitor</title>
    <%@ include file = "imports.jsp" %>
	<%@ page import = "edu.miami.ccs.goma.pojos.User" %>

    <script type="text/javascript">
    
	    var requestCount = 0;
    	//The current program that we are working on
    	var program_id = <%=request.getParameter("program_id")%>;
    	var parent_name = "<%=request.getParameter("program_name")%>";
		$(function() {
			
			$( "#pm-form" ).dialog({
				title: 'Editing Details',
				autoOpen: false,
				height: 400,
				width: 600,
				modal: true,
                open: function (event, ui) {
                    $('#pm-form').css('overflow', 'hidden');
                },
				buttons: {
					"Save": function() {
							savePerson('pm');
							$( this ).dialog( "close" );
					},
					Cancel: function() {
						$( this ).dialog( "close" );
					}
				},
				close: function() {
					$("#pmForm input").val("");
					//allFields.val( "" ).removeClass( "ui-state-error" );
				}
			});
			$( "#dd-form" ).dialog({
				title: 'Editing Details',
				autoOpen: false,
				height: 400,
				width: 600,
				modal: true,
                open: function (event, ui) {
                    $('#dd-form').css('overflow', 'hidden');
                },
				buttons: {
					"Save": function() {
							savePerson('pm');
							$( this ).dialog( "close" );
					},
					Cancel: function() {
						$( this ).dialog( "close" );
					}
				},
				close: function() {
					$("#ddForm input").val("");
					//allFields.val( "" ).removeClass( "ui-state-error" );
				}
			});
			registerButtons();
			registerTooltips();
		});
		
		
		/**
		 * Initialize the display after the user session is augmented
		 *
		 */
		function init()
		{
		<%
	   		if(request.getParameter("project_id") != null)
	   		{
	   	%>
				$("#menuPane").hide();
				$userPane.hide();
	   			fetch(<%=request.getParameter("project_id")%>);
		<%
	   		}
	   		else if(request.getParameter("program_id") == null)
   		{
	   	%>
	    		getProgram();
	   	<%
	   		}
	   		else
	   		{
	   	%>
		    	processQuery("listProjects");
	   	<%
	   		}
	   	%>
		}
		


		
		/**
		 * If there is no program ID specified, show a form to get a program
		 *
		 */
		function getProgram()
		{
			$(".operations").hide();
			<%
			if(((HttpServletRequest) request).isUserInRole("CAU"))
			{
			%>
			$.get("/goma/utilities", {mode: "listPrograms"},  loadPrograms);
			<%
			}
			else
			{
				User curr_user =  (User) session.getAttribute("curr_user"); 
			%>
			$.get("/goma/utilities", {mode: "listPrograms", organization_id: <%=curr_user.getOrganization().getOrganizationId()%>},  loadPrograms);
			<%
			}
			%>	
		}
		
		/**
		 * Function to receive the HTTP request response from the program list request and render it as a select
		 *
		 * @param (HTTPResponse) The object containing the search results
		 */
		function loadPrograms(response)
		{
			var data = response.data;
			var selStr ="<br><br>Please select a program first:<br>" +
						"<select name = 'curr_prog' id = 'curr_prog' onChange = 'setProgram();'><option value = '0' selected>Select Program</option>";
			for(var i = 0; i < data.length; i++)
			{
				selStr += "<option value = '"+data[i].program_id+"'>"+data[i].name+"</option>";
			}
			selStr += "</select>";
			$('#dynamic').html(selStr);
		}
		
		/**
		 * Set the program_id as selected by the user and call to display project list
		 *
		 */
		 function setProgram()
		 {
			program_id = $("#curr_prog").val();
			$("#program_id").val(($("#curr_prog").val()));
			parent_name = $("#curr_prog option:selected").text();
			processQuery("listProjects");
		 }
		 
		/**
		 * Depending on the operation specified hit the appropriate servlet for data
		 *
		 * @param (String) The operation to be performed on the paging or noop if the first page is to be loaded
		 */
		function processQuery(operation)
		{
			 $.get("/goma/utilities", {mode: operation, program_id: program_id},  processResponse);
		}
		
		
		/**
		 * Function to receive the HTTP request response from the list request and render it as a table
		 *
		 * @param (HTTPResponse) The object containing the search results
		 */
		function processResponse(response)
		{
			$(".linkOp").html("");
			var data = response.data;
			$("#parentName").text(parent_name + ": ");
			$("#currentOp").text(" / Project Listing");
			var aDataSet = "[ ";
			for(var i = 0; i < data.length; i++)
			{
				aDataSet += '["<input type = \'checkbox\' name = \'project_id\' onClick = \'highlightRow(this);\' value = \''+data[i].project_id+'\'>'+
							'","<a href = \'stations?project_name='+data[i].name+'&project_id='+data[i].project_id+'\' class = \'list-control\' title = \'Click to view all stations for this project\'> </a>' +
							'","<a href = \'#\' onClick = \'fetch('+data[i].project_id+');\' class = \'details\'>'+ data[i].name + '</a>' +
							'","'+data[i].project_manager +
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

			colSet = "[ { \"sTitle\": \"<input type='checkbox' value = '0' name='selectAllB' id='selectAllB' onClick='jqCheckAll(this.id)'>\"}, { \"sTitle\": \"View Stations\" }, { \"sTitle\": \"Name (Click record to edit)\" }, { \"sTitle\": \"Project Manager\" }, { \"sTitle\": \"Status\" }, { \"sTitle\": \"Website\" } ]";

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
			$(".operations").show();
			if(data.length == 0)
				$('#consoleTable').after("NOTE: There may be additional records that have not been approved yet");
		}
		
		/**
		 * Function to save the data entered in the project manager form
		 *
		 * 
		 */
		function saveProjectManager()
		{
			var inputs = $("#createPMForm").validator();
			if(!inputs.data("validator").checkValidity())
				return false;	
			$.getJSON("/goma/admin/person-ops", $("#createPMForm").serialize(),  function(response){
				if(response.code == "success")
					{
						$("#pm_id").val(response.data[0].person_id);
						showDataDistributorOptions();
					}
				//setMessage(response.message, response.code);
			});
			
		}
		
		/**
		 * Function to save the data entered in the data distributor form
		 *
		 * 
		 */
		function saveDataDistributor()
		{
			var inputs = $("#createDDForm").validator();
			if(!inputs.data("validator").checkValidity())
				return false;
			$.getJSON("/goma/admin/person-ops", $("#createDDForm").serialize(),  function(response){
				if(response.code == "success")
					{
						$("#dd_id").val(response.data[0].person_id);
						$("#ddOpts").hide();
						$("#createPMForm input").val("");
						$("#createDDForm input").val("");
						$("#progManMode").val("create");
						$("#dataDistMode").val("create");
						update();
					}
				//setMessage(response.message, response.code);
			});
			
		}
		/**
		 * Function to save the data entered in the person form
		 *
		 * 
		 */
		function savePerson(type)
		{
			$.getJSON("/goma/admin/person-ops", $("#"+type+"Form").serialize(),  function(response){
			setMessage(response.message, response.code);
		});
			
		}
		/**
		 * Function to save the data entered in the project form
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

			if(!inputs.data("validator").checkValidity())
				return false;	
			$.getJSON("/goma/admin/project-ops", $("#dynamicForm").serialize(), processSave);
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
					fetch(response.project_id);
					$("#currentOp").text(" / Review and Save");
				}
				else
					processQuery("listProjects");
			}
			setMessage(response.message, response.code);
		 }
		
		
		/**
		 * Function to request the project data from the servlet
		 *
		 * @param (Number) The id of the project to be fetched for editing
		 */
		function fetch(project_id)
		{
			$.get("/goma/admin/project-ops", {mode: "fetch", project_id: project_id}, load);
		}
		
		
		/**
		 * Function to receive the HTTP request response from project fetch request and render it as a form
		 *
		 * @param (HTTPResponse) The object containing the project
		 */		 
		function load(response)
		{
			$("#currentOp").text(" / Editing Project");
			var formString = "";
			var data = response.data;
			
			for(var i = 0; i < data.length; i++)
			{
				formString += "<div id = 'basicOpts'><table class = 'formTable'>";
				formString += "<tr><td class = 'fieldname required'>Name:</td><td><input type = 'text' title = 'Enter the project name' maxlength = '255' id = 'name' name = 'name' required = 'required' value = '"+data[i].name+"'></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Description:</td><td><textarea id = 'description' name = 'description' title = 'Enter a brief description, no HTML tags' required = 'required'>"+data[i].description+"</textarea></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname'>Homepage:</td><td><input  title = 'Enter the full address of the project website starting with http://...' type = 'url' id = 'website' name = 'website' maxlength = '1000' value = '"+data[i].website+"'></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Status:</td><td><span id = 'status_well'>"+data[i].status+"<input type = 'hidden' id = 'status_id' name = 'status_id' value = '"+data[i].status_id+"'></span></td><td><a class = 'edit-control' href = '#' onClick = 'fetchStatusList();' title = 'Click to edit project status'></a></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Availability:</td><td><span id = 'availability_well'>"+data[i].availability+"<input type = 'hidden' id = 'availability_id' name = 'availability_id' value = '"+data[i].availability_id+"'></span></td><td><a class = 'edit-control' href = '#' onClick = 'editTerm(13);' title = 'Click to edit project data availability'></a></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Proprietary Restrictions:</td><td><span id = 'proprietary_restriction_well'>"+data[i].proprietary_restriction+"<input type = 'hidden' id = 'proprietary_restriction_id' name = 'proprietary_restriction_id' value = '"+data[i].proprietary_restriction_id+"'></span></td><td><a class = 'edit-control' href = '#' onClick = 'editTerm(12);' title = 'Click to edit proprietary restrictions'></a></td></tr>";
				formString += "<tr><td class = 'fieldname'>Proprietary Restriction Type:</td><td><input  title = 'If the data for this project has proprietary restrictions, please specify them here' type = 'text' id = 'proprietary_restriction_text' name = 'proprietary_restriction_text' value = '"+data[i].proprietary_restriction_text+"'></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Purpose Category:</td><td><span id = 'purpose_category_well'>"+data[i].purpose_category+"<input type = 'hidden' id = 'purpose_category_id' name = 'purpose_category_id' value = '"+data[i].purpose_category_id+"'></span></td><td><a class = 'edit-control' href = '#' onClick = 'editTerm(14);' title = 'Click to edit project purpose category'></a></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Purpose Description:</td><td><textarea  title = 'Enter project purpose statement' id = 'purpose_text' name = 'purpose_text' required = 'required'>"+data[i].purpose_text+"</textarea></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Project Methodology:</td><td><span id = 'project_methodology_well'>"+data[i].project_methodology+"<input type = 'hidden' id = 'project_methodology_id' name = 'project_methodology_id' value = '"+data[i].project_methodology_id+"'></span></td><td><a class = 'edit-control' href = '#' onClick = 'editTerm(33);' title = 'Click to edit project methodology'></a></td></tr>";
				formString += "<tr><td class = 'fieldname'>Geographic Boundary:<br><a href = 'http://en.wikipedia.org/wiki/Well-known_text' target = '_blank'>More Information about WKT</a></td><td><textarea  title = 'Well Known Text (WKT) format coordinates for the feature of interest(e.g. Project).  WKT coordinates are accepted for polygons.  Coordinates should be entered in decimal degrees with respect to the WGS 84 datum.  For example, POLYGON((30.4496 -84.3062, 26.88135 -80.05656, 25.76994 -80.13451, 27.0733 -82.4493, 30.4496 -84.3062))' id = 'geo_boundary' name = 'geo_boundary'>"+data[i].geo_boundary+"</textarea></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Data Quality Objective:</td><td><textarea  title = 'Enter the data quality objective for this project' id = 'data_quality_obj' name = 'data_quality_obj' required = 'required'>"+data[i].data_quality_obj+"</textarea></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Data Usage Limitations:</td><td><textarea  title = 'Enter the data usage limitations for this project' id = 'usage_limitations' name = 'usage_limitations' required = 'required'>"+data[i].usage_limitations+"</textarea></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname'>Data Website:</td><td><input  title = 'Enter the full address of the project data website starting with http://...' type = 'url' id = 'data_link_website' name = 'data_link_website' value = '"+data[i].data_link_website+"'></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Start Date:</td><td><input title = 'Enter the start date of the project here' type = 'text' id = 'start_date' name = 'start_date' required = 'required' value = '"+data[i].start_date+"'></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname required'>End Date:</td><td><input title = 'Enter the end date of the project here' type = 'text' id = 'end_date' name = 'end_date' required = 'required' value = '"+data[i].end_date+"'></td><td></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Project Manager:</td><td><span id = 'pm_options'>"+data[i].pm_first_name+" "+data[i].pm_last_name+"</span><span id = 'pm_well'></span></td><td><a class = 'edit-control' href = '#' onClick = '$(\"#pm-form\").dialog(\"open\");' title = 'Click to edit project manager details'></a></td></tr>";
				formString += "<tr><td class = 'fieldname required'>Data Distributor:</td><td><span id = 'dd_options'>"+data[i].dd_first_name+" "+data[i].dd_last_name+"</span><span id = 'dd_well'></span></td><td><a class = 'edit-control' href = '#' onClick = '$(\"#dd-form\").dialog(\"open\");' title = 'Click to edit data distributor details'></a></td></tr>";
				formString += "<tr><td class = 'fieldname'>Date Created:</td><td>"+data[i].date_created+"</td><td></td></tr>";
				formString += "<tr><td class = 'fieldname'>Approval Status:</td><td>"+data[i].approval+"</td><td></td></tr>";
				if(data[i].date_updated != null)
					formString += "<tr><td class = 'fieldname'>Date Updated:</td><td>"+data[i].date_updated+"</td><td></td></tr>";
				formString += "</table>";					
				formString += "<input type = 'hidden' id = 'user_id' name = 'project_id' value = '"+data[i].project_id+"'>";

				$("#pmForm #first_name").val(data[i].pm_first_name);
				$("#pmForm #last_name").val(data[i].pm_last_name);
				$("#pmForm #job_title").val(data[i].pm_job_title);
				$("#pmForm #email").val(data[i].pm_email);
				$("#pmForm #address").val(data[i].pm_address);
				$("#pmForm #phone").val(data[i].pm_phone);
				$("#pmForm #fax").val(data[i].pm_fax);
				$("#pmForm #website").val(data[i].pm_homepage);
				$("#pmForm #pmMode").val("update");
				$("#pmForm #pm_id").val(data[i].pm_id);
				
				$("#ddForm #first_name").val(data[i].dd_first_name);
				$("#ddForm #last_name").val(data[i].dd_last_name);
				$("#ddForm #job_title").val(data[i].dd_job_title);
				$("#ddForm #email").val(data[i].dd_email);
				$("#ddForm #address").val(data[i].dd_address);
				$("#ddForm #phone").val(data[i].dd_phone);
				$("#ddForm #fax").val(data[i].dd_fax);
				$("#ddForm #website").val(data[i].dd_homepage);
				$("#pmForm #dd_id").val(data[i].dd_id);
				
				formString += '<br><span class = "operation"><a href = "link-stations?project_name='+data[i].name+'&project_id='+data[i].project_id+'" class = "op-station button">Link Stations</a></span>';
			}
			
			
			formString += " <span class = 'operation'><a href = '#' onClick = 'update();' class = 'op-save'>Save Changes</a></span></div>";
	
			$("#dynamic").html(formString);
			$("#mode").val("update");
			registerTooltips('basicOpts');
			registerButtons();
			$( "#start_date" ).datepicker();
			$( "#end_date" ).datepicker();
		}
		
		/**
		 * Function to display the form for new project creation
		 *
		 */
		 function create()
		{
			$("#currentOp").text(" / Adding New");
			var formString = "";
			
			formString += "<div id = 'basicOpts'><span class = 'wizardStage'>Basic Project Information</span><table class = 'formTable'>";
			formString += "<tr><td class = 'fieldname required'>Name:</td><td><input title = 'Enter the name of the project here' type = 'text' id = 'name' name = 'name' required = 'required' maxlength = '255'></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname required'>Description:</td><td><textarea  title = 'Enter a brief description, no HTML tags' id = 'description' name = 'description' required = 'required'></textarea></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname'>Website:</td><td><input  title = 'Enter the full address of the project website starting with http://...' type = 'url' id = 'website' name = 'website' maxlength = '1000'></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname required'>Status:</td><td><span id = 'status_well'></span></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname required'>Availability:</td><td><span id = 'availability_well'></span></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname required'>Proprietary Restrictions:</td><td><span id = 'proprietary_restriction_well'></span></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname'>Proprietary Restriction Type:</td><td><input  title = 'If the data for this project has proprietary restrictions, please specify them here' type = 'text' id = 'proprietary_restriction_text' name = 'proprietary_restriction_text'></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname required'>Purpose Category:</td><td><span id = 'purpose_category_well'></span></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname required'>Purpose Description:</td><td><textarea  title = 'Enter project purpose statement' id = 'purpose_text' name = 'purpose_text' required = 'required'></textarea></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname required'>Project Methodology:</td><td><span id = 'project_methodology_well'></span></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname'>Geographic Boundary:<br><a href = 'http://en.wikipedia.org/wiki/Well-known_text' target = '_blank'>More Information about WKT</a></td><td><textarea  title = 'Well Known Text (WKT) format coordinates for the feature of interest(i.e. Project).  WKT coordinates are accepted for polygons.  Coordinates should be entered in decimal degrees with respect to the WGS 84 datum.  For example, POLYGON((30.4496 -84.3062, 26.88135 -80.05656, 25.76994 -80.13451, 27.0733 -82.4493, 30.4496 -84.3062))' id = 'geo_boundary' name = 'geo_boundary'></textarea></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname required'>Data Quality Objective:</td><td><textarea  title = 'Enter the data quality objective for this project' id = 'data_quality_obj' name = 'data_quality_obj' required = 'required'></textarea></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname required'>Data Usage Limitations:</td><td><textarea  title = 'Enter the data usage limitations for this project' id = 'usage_limitations' name = 'usage_limitations' required = 'required'></textarea></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname'>Data Website:</td><td><input  title = 'Enter the full address of the project data website starting with http://...' type = 'url' id = 'data_link_website' name = 'data_link_website'></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname required'>Start Date:</td><td><input title = 'Enter the start date of the project here' type = 'text' id = 'start_date' name = 'start_date' required = 'required'></td><td></td></tr>";
			formString += "<tr><td class = 'fieldname'>End Date Type:</td><td><input type = 'checkbox' id = 'end_date_type' name = 'end_date_type' value = 'unknown' onChange = 'insertEndDate();'/>Unknown/Ongoing</td><td></td></tr>";
			formString += "<tr><td class = 'fieldname required'>End Date:</td><td><input title = 'Enter the end date of the project here' type = 'text' id = 'end_date' name = 'end_date' required = 'required'></td><td></td></tr>";
			formString += "</table>";
			formString += "<br><span class = 'operation'><a href = '#' onClick = 'showProjectManagerOptions();' class = 'op-next'>Next</a></span></div>";

			formString += "<input type = 'hidden' name = 'pm_id' id = 'pm_id'/>";		
			formString += "<input type = 'hidden' name = 'dd_id' id = 'dd_id'/>";
			//formString += "</form>";
			
			$("#dynamic").html(formString);
			$("#mode").val("create");
			registerTooltips('basicOpts');
			registerButtons();

			setTimeout("fetchStatusList();", 500);
			populateSearchOptions()
			$( "#start_date" ).datepicker();
			$( "#end_date" ).datepicker();
		}
		 
			
		function populateSearchOptions()
		{

			termIds = new Array(Statics.PROPRIETARY_RESTRICTIONS, Statics.AVAILABILITY, Statics.PURPOSE_CATEGORY, Statics.PROJECT_METHODOLOGY);
			if(requestCount < termIds.length)
			{
				fetchTermsList(termIds[requestCount]);
				requestCount++;
			}
			else
			{
				$.get("/goma/utilities", {mode: "listStatusValues", status_type_id: Statics.PROGRAM_STATUS}, loadStatusList);
			}
		}

		/**
		 * Function to request the list of dictionary terms from the servlet
		 *
		 */
		function editTerm(dictionary_id)
		{
			$.get("/goma/utilities", {mode: "listDictionaryTerms", dictionary_id: dictionary_id}, loadDictionaryTerms);
		}		
		
		/**
		 * Function to receive the HTTP request response from dictionary terms request and render it as a select
		 *
		 * @param (HTTPResponse) The object containing the list of terms			 
		 */
		function loadDictionaryTerms(response)
		{
			var termsStr = "<select class = 'picker' name = '"+response.code.toLowerCase()+"_id' id ='"+response.code.toLowerCase()+"_id'><option value = '-1'>Pick from List</option>";
			var terms = response.data;
			for(var i = 0; i < terms.length; i++)
			{
				termsStr += "<option value = '"+terms[i].term_id+"'>"+terms[i].name+"</option>";
			}
			termsStr += "</select>";
			$("#"+response.code.toLowerCase()+"_well").html(termsStr);
		}
		
		/**
		 * Function to request the list of dictionary terms from the servlet
		 *
		 */
		function fetchTermsList(dictionary_id)
		{
			$.get("/goma/utilities", {mode: "listDictionaryTerms", dictionary_id: dictionary_id}, loadTermsList);
		}
		
		
		/**
		 * Function to receive the HTTP request response from dictionary terms request and render it as a select
		 *
		 * @param (HTTPResponse) The object containing the list of terms			 
		 */
		function loadTermsList(response)
		{
			var termsStr = "<select class = 'picker' name = '"+response.code.toLowerCase()+"_id' id ='"+response.code.toLowerCase()+"_id'><option value = '-1'>Pick from List</option>";
			var terms = response.data;
			for(var i = 0; i < terms.length; i++)
			{
				termsStr += "<option value = '"+terms[i].term_id+"'>"+terms[i].name+"</option>";
			}
			termsStr += "</select>";
			$("#"+response.code.toLowerCase()+"_well").html(termsStr);
			populateSearchOptions();
			
		}
			
		/**
		 * Function to set an arbitrary far away date as the end date for ongoing projects
		 * This is needed since end_date is a required field 
		 */
		function insertEndDate()
		{
			if($("#end_date_type").is(':checked'))
			{
				//Launch date of the Starship Enterprise-D
				$("#end_date").val("05/10/2363");
				//$("#end_date").prop("disabled", true);
				$("#end_date").toggle();
			}
			else
			{
				$("#end_date").val("");
				//$("#end_date").prop("disabled", false);
				$("#end_date").toggle();
			}
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
				statusStr += "<option value = '"+statuses[i].status_id+"'>"+statuses[i].value+"</option>";
			}
			statusStr += "</select>";
			$("#status_well").html(statusStr);
		}
		
		
		/**
		 * Function to show the project manager selection options
		 *		 
		 */
		function showProjectManagerOptions()
		{
			var inputs = $("#dynamicForm").validator();
			var chkSel = true;
				
			$('select').each(function(){
				if($(this).val() == "-1")
				{
					setMessage("Values must be selected for all drop downs", "warning");
					chkSel = false;
				}
			});
			if(!inputs.data("validator").checkValidity() || !chkSel)
				return false;	
			
			$("#basicOpts").hide();
			$("#pmOpts").show();
			
			registerTooltips('createPMForm');
			registerButtons();
		}
		
		/**
		 * Function to show the data distributor selection options
		 *		 
		 */
		function showDataDistributorOptions()
		{
			var inputs = $("#createPMForm").validator();
			if(!inputs.data("validator").checkValidity())
				return false;	
			
			$("#pmOpts").hide();
			$("#ddOpts").show();
			
			registerTooltips('createDDForm');
			registerButtons();
		}
		
    </script>
  </head>
<body>
		<%@ include file = "header.jsp" %>
		<%@ include file = "menu.jsp" %>
	<div class = "console">
		<div class = "page-title"><span id = "parentName"></span>Manage Projects<span id = "currentOp"></span></div>
		<div class = "operations">
			<ul class = "opList">
				<li class = "operation"><a href = "#" class = "op-list button" onClick = "processQuery('listProjects');">List</a></li>
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
			<input type = 'hidden' id = 'program_id' name = 'program_id' value = '<%=request.getParameter("program_id")%>'>
			<div id = "dynamic"></div>
		</form>
		<div id = 'pmOpts' style = "display: none;">
			<span class = 'wizardStage'>Project Manager Details</span>
			<form name = 'createPMForm' id = 'createPMForm'>
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
				<br><span class = 'operation'><a href = '#' onClick = 'saveProjectManager();' class = 'op-next'>Next</a></span>
			</form>		
		</div>
		<div id = 'ddOpts' style = "display: none;">
			<span class = 'wizardStage'>Data Distributor Details</span>
			<form name = 'createDDForm' id = 'createDDForm'>
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
				<input type = 'hidden' name = 'mode' id = 'dataDistMode' value = 'create'>
				<br><span class = 'operation'><a href = '#' onClick = 'saveDataDistributor();' class = 'op-next'>Next</a></span>
			</form>		
		</div>
		<div id = "pm-form">
			<form name = "pmForm" id = "pmForm" onSubmit = "savePerson('pm');">
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
				<input type = "hidden" name = "mode" id = "pmMode" value = "update"/>
				<input type = "hidden" name = "person_id" id = "pm_id" value = "update"/>
			</form>
		</div>
		<div id = "dd-form">
			<form name = "ddForm" id = "ddForm" onSubmit = "savePerson('dd');">
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
				<input type = "hidden" name = "mode" id = "ddMode" value = "update"/>
				<input type = "hidden" name = "person_id" id = "dd_id" value = ""/>
			</form>
		</div>
	</div>
	
  </body>
</html>