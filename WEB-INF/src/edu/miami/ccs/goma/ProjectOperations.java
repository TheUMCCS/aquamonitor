package edu.miami.ccs.goma;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.jboss.logging.Logger;

import edu.miami.ccs.goma.pojos.ApprovalRequest;
import edu.miami.ccs.goma.pojos.ApprovalRequestType;
import edu.miami.ccs.goma.pojos.DataDistributor;
import edu.miami.ccs.goma.pojos.DictionaryTerm;
import edu.miami.ccs.goma.pojos.Organization;
import edu.miami.ccs.goma.pojos.Person;
import edu.miami.ccs.goma.pojos.Program;
import edu.miami.ccs.goma.pojos.ProgramManager;
import edu.miami.ccs.goma.pojos.Project;
import edu.miami.ccs.goma.pojos.ProjectManager;
import edu.miami.ccs.goma.pojos.StatusValue;
import edu.miami.ccs.goma.pojos.User;

public class ProjectOperations extends HttpServlet {
	private static ServiceRegistry serviceRegistry;
	private static SessionFactory sf;
	private static PrintWriter out;
	private static HttpServletResponse response;
	private static HttpServletRequest request;
	private static Logger logger;
	private static SimpleDateFormat sdf;
	
	public void init(ServletConfig config) throws ServletException
	{
		sf = connect();
		sdf = new SimpleDateFormat("MM/dd/yyyy");
	} 

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
	    response = resp;	    
	    request = req;
	    try 
	    {
	    	out = response.getWriter();
	    }
	    catch(IOException e)
	    {
	    	logger.debug("Error opening output stream using PrintWriter");
	    }


	    if(request.getParameter("mode").equals("create"))	
	    	create();
	    else if(request.getParameter("mode").equals("delete"))
	    	delete();
	    else if(request.getParameter("mode").equals("fetch"))
	    	fetch();
	    else if(request.getParameter("mode").equals("update"))
	    	update();
	    else if(request.getParameter("mode").equals("list"))
	    	list();
	    else if(request.getParameter("mode").equals("statistics"))
	    	statistics();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		this.doGet(req, resp);
	}
	
	
	/**
	 * Called by the init(...) method to generate hibernate Session object
	 * 
	 * @return Hibernate SessionFactory object
	 * 
	 */
	public static SessionFactory connect()
	{
		SessionFactory sf;
		Configuration cfg = new Configuration();
		cfg.configure();
		serviceRegistry = new ServiceRegistryBuilder().applySettings(cfg.getProperties()).buildServiceRegistry();        
	    sf = cfg.buildSessionFactory(serviceRegistry);
	    
	    return sf;
	}
	
	
	private static void list()
	{
		Session session = sf.openSession();
		session.clear();

	    HttpSession hs = request.getSession(true);
		Program prg = (Program) session.load(Program.class, new Long(request.getParameter("program_id")));

    	Set<Project> projList = prg.getProjects();
    	
    	//Set up the data type for the JSON
    	response.setContentType("application/json");
    	//Define an output stream for the data to go
        
    	out.print("{ \"type\": \"project\", \"hits\":"+projList.size()+", \"data\": [");
    	for (Iterator iter = projList.iterator(); iter.hasNext();) 
    	{   		
    		Project p = (Project) iter.next();
    		
    		out.print("{ \"project_id\": \""+p.getProjectId()+"\", \"name\": \""+p.getName()+"\", \"status\": \""+p.getStatusValueByStatusId().getStatusValue()+"\", \"project_manager\": \""+p.getProjectManager().getPerson().getFirstName()+" "+ p.getProjectManager().getPerson().getLastName() +"\", \"website\": \""+p.getWebsite()+"\"}");
    		if(iter.hasNext())
    			out.print(",");
    	}
    	out.print("] }");
    	session.close();
	}
	
	
	private static void fetch()
	{
		Session session = sf.openSession();
		session.clear();
		Project p = (Project) session.load(Project.class, new Long(request.getParameter("project_id")));
		
    	response.setContentType("application/json");      
    	out.print("{ \"data\": [ {");
    	out.print("\"project_id\": \""+p.getProjectId()+"\", \"name\": \""+p.getName()+"\", \"description\": \""+p.getDescription()+"\", \"pm_first_name\": \""+p.getProjectManager().getPerson().getFirstName()+"\", \"pm_last_name\": \""+p.getProjectManager().getPerson().getLastName() +
    			"\", \"organization\": \""+p.getProgram().getOrganization().getName()+"\", \"program\": \""+p.getProgram().getName()+"\", \"website\": \""+p.getWebsite()+"\", \"date_created\": \""+sdf.format(p.getDateCreated())+"\", \"created_by\": \""+p.getUserByCreatedBy().getUsername() + "\", \"pm_id\": \""+p.getProjectManager().getPerson().getPersonId()+"\", \"data_quality_obj\": \""+p.getDataQualityObj()+"\", \"start_date\": \""+sdf.format(p.getStartDate())+"\", \"end_date\": \""+sdf.format(p.getEndDate())+
    			"\", \"usage_limitations\": \""+p.getUsageLimitations()+
    			"\", \"data_link_website\": \""+p.getDataLinkWebsite()+
    			"\", \"proprietary_restriction_text\": \""+p.getProprietaryRestrictionText()+
    			"\", \"project_methodology_id\": \""+p.getDictionaryTermByProjectMethodologyId().getTermId()+
    			"\", \"project_methodology\": \""+p.getDictionaryTermByProjectMethodologyId().getTerm()+"\"" +
    			", \"availability\": \""+p.getDictionaryTermByAvailabilityId().getTerm()+"\", \"proprietary_restriction\": \""+p.getDictionaryTermByProprietaryRestrictionId().getTerm()+"\", \"purpose_category\": \""+p.getDictionaryTermByPurposeCategoryId().getTerm()+"\", \"purpose_text\": \""+p.getPurposeText()+"\", \"geo_boundary\": \""+p.getGeoBoundary()+"\"" +
    			", \"availability_id\": \""+p.getDictionaryTermByAvailabilityId().getTermId()+"\", \"proprietary_restriction_id\": \""+p.getDictionaryTermByProprietaryRestrictionId().getTermId()+"\", \"purpose_category_id\": \""+p.getDictionaryTermByPurposeCategoryId().getTermId()+"\"" +
 				", \"pm_email\": \""+p.getProjectManager().getPerson().getEmail()+"\", \"pm_job_title\": \""+p.getProjectManager().getPerson().getJobTitle()+"\", \"pm_address\": \""+p.getProjectManager().getPerson().getAddress()+"\", \"pm_phone\": \""+p.getProjectManager().getPerson().getPhone()+"\", \"pm_fax\": \""+p.getProjectManager().getPerson().getFax()+"\"" +
 				", \"pm_homepage\": \""+p.getProjectManager().getPerson().getWebsite()+"\" , \"dd_email\": \""+p.getDataDistributor().getPerson().getEmail()+"\", \"dd_address\": \""+p.getProjectManager().getPerson().getAddress()+"\", \"dd_job_title\": \""+p.getDataDistributor().getPerson().getJobTitle()+"\", \"dd_phone\": \""+p.getDataDistributor().getPerson().getPhone()+"\", \"dd_fax\": \""+p.getDataDistributor().getPerson().getFax()+"\"" +
 				", \"dd_homepage\": \""+p.getDataDistributor().getPerson().getWebsite()+"\", \"dd_first_name\": \""+p.getDataDistributor().getPerson().getFirstName()+"\", \"dd_last_name\": \""+p.getDataDistributor().getPerson().getLastName()+"\", \"dd_id\": \""+p.getDataDistributor().getPerson().getPersonId()+"\"");
    	if(p.getUserByUpdatedBy() != null)
    		out.print(", \"date_updated\": \""+sdf.format(p.getDateUpdated())+"\", \"updated_by\": \""+p.getUserByUpdatedBy().getUsername()+"\"");
    	out.print(",\"status\": \""+p.getStatusValueByStatusId().getStatusValue()+"\",\"status_id\": \""+p.getStatusValueByStatusId().getStatusId()+"\", \"approval\": \""+p.getStatusValueByApprovalStatusId().getStatusValue()+"\" } ] }");
    	session.close();
	}


	private static void create()
	{
		Session session = sf.openSession();
		Date date= new Date();
		Transaction tx = null;
		HttpSession hs = request.getSession(true);
		User curr_user = (User) hs.getAttribute("curr_user"); 
		Program prg = (Program) session.load(Program.class, new Long(request.getParameter("program_id")));
		
		if(!request.isUserInRole("CAU") && (curr_user.getOrganization().getOrganizationId() != prg.getOrganization().getOrganizationId()))
		{
			out.print("{  \"code\": \"failure\", \"message\": \"You are not a member of the owning organization for this program\", \"data\": [] }");
			return;
		}
		Date startDate = null;
		Date stopDate = null;
		
		
		try {
			startDate = sdf.parse(request.getParameter("start_date"));
			stopDate = sdf.parse(request.getParameter("end_date"));
		} catch (ParseException pe) {
			
			pe.printStackTrace();
		}
		
		DataDistributor dd = new DataDistributor((Person)session.load(Person.class, new Long(request.getParameter("dd_id"))));
		ProjectManager pm = new ProjectManager((Person)session.load(Person.class, new Long(request.getParameter("pm_id"))));
		
	    try 
	    {
	    	tx = session.beginTransaction();
	    	session.save(dd);
	    	session.save(pm);
	    	tx.commit();
	    } 
	    catch (RuntimeException e) 
	    {
	       	if (tx != null && tx.isActive()) 
	       	{
		        try 
		        {
		        	// Second try catch as the rollback could fail as well
		        	tx.rollback();
		        } 
		        catch (HibernateException e1) 
		        {
		        	logger.debug("Error rolling back transaction");
		        }
		        // throw the first exception again 
		        throw e;
	       	}
	    }
	    catch (Exception e)
	    {
	       	out.print("{  \"code\": \"failure\", \"message\": \""+e.getMessage()+"\", \"data\": [] }");
	       	tx.rollback();
	       	return;
	    }
		DictionaryTerm propRes = (DictionaryTerm) session.load(DictionaryTerm.class, new Long(request.getParameter("proprietary_restriction_id")));
		DictionaryTerm purposeCat = (DictionaryTerm) session.load(DictionaryTerm.class, new Long(request.getParameter("purpose_category_id")));
		DictionaryTerm availability = (DictionaryTerm) session.load(DictionaryTerm.class, new Long(request.getParameter("availability_id")));
		DictionaryTerm methodology =  (DictionaryTerm) session.load(DictionaryTerm.class, new Long(request.getParameter("project_methodology_id")));
		User u_creator = (User) session.load(User.class, curr_user.getUserId());
		Organization o = u_creator.getOrganization();
		StatusValue proj_stat = (StatusValue) session.load(StatusValue.class, new Long(request.getParameter("status_id")));
		StatusValue approval_stat = (StatusValue) session.load(StatusValue.class, new Long(Statics.PENDING));

		
	    //When the project is created for the first time, the creator is the owner
		Project proj = new Project(u_creator, propRes, dd, prg, pm, purposeCat, approval_stat, availability, proj_stat, 
				request.getParameter("name"), request.getParameter("description"), request.getParameter("purpose_text"), 
				request.getParameter("data_quality_obj"), startDate, stopDate, date, methodology);
		if(request.getParameter("website") != null)
			proj.setWebsite(request.getParameter("website"));
		if(request.getParameter("usage_limitations") != null)
			proj.setUsageLimitations(request.getParameter("usage_limitations"));
		if(request.getParameter("fax") != null)
			proj.setFax(request.getParameter("fax"));
		if(request.getParameter("data_link_website") != null)
			proj.setDataLinkWebsite(request.getParameter("data_link_website"));
		if(request.getParameter("proprietary_restriction_text") != null)
			proj.setFax(request.getParameter("proprietary_restriction_text"));
		if(request.getParameter("geo_boundary") != null)
			proj.setGeoBoundary(request.getParameter("geo_boundary"));
		
	    try 
	    {
	    	tx = session.beginTransaction();
	    	session.save(proj);	
	    	//session.flush();
			ApprovalRequestType art = (ApprovalRequestType) session.load(ApprovalRequestType.class, new Long(Statics.PROJECT));
			ApprovalRequest ar = new ApprovalRequest(curr_user, art, curr_user, approval_stat, date, proj.getProjectId());
			ar.setComment("New Record Added");
	    	session.save(ar);
	    	tx.commit();
	    } 
	    catch (RuntimeException e) 
	    {
	       	if (tx != null && tx.isActive()) 
	       	{
		        try 
		        {
		        	// Second try catch as the rollback could fail as well
		        	tx.rollback();
		        } 
		        catch (HibernateException e1) 
		        {
		        	logger.debug("Error rolling back transaction");
		        }
		        // throw the first exception again 
		        throw e;
	       	}
	    }
	    catch (Exception e)
	    {
	       	out.print("{  \"code\": \"failure\", \"message\": \""+e.getMessage()+"\", \"data\": [] }");
	       	tx.rollback();
	       	return;
	    }
	    out.print("{  \"code\": \"success\", \"message\": \"Save Successful\", \"project_id\": \""+proj.getProjectId()+"\", \"data\": [] }");

	    session.close();
	}
	
	private static void update()
	{	
		Session session = sf.openSession();
		java.util.Date date= new java.util.Date();
		Transaction tx = null;
		HttpSession hs = request.getSession(true);
		User curr_user = (User) hs.getAttribute("curr_user"); 
		Date startDate = null;
		Date endDate = null;

		Project proj = (Project) session.load(Project.class, new Long(request.getParameter("project_id")));

		if(!request.isUserInRole("CAU") && (curr_user.getOrganization().getOrganizationId() != proj.getProgram().getOrganization().getOrganizationId()))
		{
			out.print("{  \"code\": \"failure\", \"message\": \"Access Violation: Permission Denied.\", \"data\": [] }");
			return;
		}
		DictionaryTerm propRes = (DictionaryTerm) session.load(DictionaryTerm.class, new Long(request.getParameter("proprietary_restriction_id")));
		DictionaryTerm purposeCat = (DictionaryTerm) session.load(DictionaryTerm.class, new Long(request.getParameter("purpose_category_id")));
		DictionaryTerm availability = (DictionaryTerm) session.load(DictionaryTerm.class, new Long(request.getParameter("availability_id")));
		DictionaryTerm methodology =  (DictionaryTerm) session.load(DictionaryTerm.class, new Long(request.getParameter("project_methodology_id")));
		StatusValue proj_stat = (StatusValue) session.load(StatusValue.class, new Long(request.getParameter("status_id")));
		StatusValue approval_stat = (StatusValue) session.load(StatusValue.class, new Long(Statics.PENDING));
		
		if(request.getParameter("website") != null)
			proj.setWebsite(request.getParameter("website"));
		if(request.getParameter("usage_limitations") != null)
			proj.setUsageLimitations(request.getParameter("usage_limitations"));
		if(request.getParameter("fax") != null)
			proj.setFax(request.getParameter("fax"));
		if(request.getParameter("website") != null)
			proj.setWebsite(request.getParameter("website"));
		if(request.getParameter("usage_limitations") != null)
			proj.setUsageLimitations(request.getParameter("usage_limitations"));
		if(request.getParameter("data_link_website") != null)
			proj.setDataLinkWebsite(request.getParameter("data_link_website"));
		if(request.getParameter("proprietary_restriction_text") != null)
			proj.setProprietaryRestrictionText(request.getParameter("proprietary_restriction_text"));	
		
		try {
			startDate = sdf.parse(request.getParameter("start_date"));
			endDate = sdf.parse(request.getParameter("end_date"));
		} catch (ParseException pe) {
			
			pe.printStackTrace();
		}
		proj.setDescription(request.getParameter("description"));
		proj.setName(request.getParameter("name"));
		proj.setStartDate(startDate);
		proj.setEndDate(endDate);
		proj.setDictionaryTermByAvailabilityId(availability);
		proj.setDictionaryTermByProprietaryRestrictionId(propRes);
		proj.setDictionaryTermByPurposeCategoryId(purposeCat);
		proj.setGeoBoundary(request.getParameter("geo_boundary"));
		proj.setDataQualityObj(request.getParameter("data_quality_obj"));
		proj.setPurposeText(request.getParameter("purpose_text"));
		proj.setDictionaryTermByProjectMethodologyId(methodology);
		
		proj.setDateUpdated(date);
		proj.setUserByUpdatedBy(curr_user);
		proj.setStatusValueByApprovalStatusId(approval_stat);
		
	    try 
	    {
	    	tx = session.beginTransaction();
	    	session.update(proj);	
	    	session.flush();
			ApprovalRequestType art = (ApprovalRequestType) session.load(ApprovalRequestType.class, new Long(Statics.PROJECT));
			ApprovalRequest ar = new ApprovalRequest(curr_user, art, curr_user, approval_stat, date, proj.getProjectId());
			ar.setComment("Record Updated");
	    	session.save(ar);
	    	tx.commit();
	    } 
	    catch (RuntimeException e) 
	    {
	       	if (tx != null && tx.isActive()) 
	       	{
		        try 
		        {
		        	// Second try catch as the rollback could fail as well
		        	tx.rollback();
		        } 
		        catch (HibernateException e1) 
		        {
		        	logger.debug("Error rolling back transaction");
		        }
		        // throw the first exception again 
		        throw e;
	       	}
	    }
	    catch (Exception e)
	    {
	       	out.print("{  \"code\": \"failure\", \"message\": \""+e.getMessage()+"\", \"data\": [] }");
	       	tx.rollback();
	       	return;
	    }
	    out.print("{  \"code\": \"success\", \"message\": \"Save Successful\", \"project_id\": \""+proj.getProjectId()+"\", \"data\": [] }");
	    session.close();
	}
	
	private static void delete()
	{
		Session session = sf.openSession();
	    Transaction tx = null;
	    String[] uList = request.getParameterValues("project_id");

	    for(String item : uList)
	    {
	    	Project p = (Project) session.load(Project.class, new Long(item));
		    try 
		    {
		    	tx = session.beginTransaction();
		    	session.delete(p);
		    	tx.commit();
		    } 
		    catch (ConstraintViolationException cve)
		    {
		    	out.print("{ \"code\": \"failure\", \"message\": \"Delete Failed: Cannot delete "+ p.getName() +" while it has stations assigned to it.\", \"data\": [] }");
		    	tx.rollback();
		    	return;
		    }
		    catch (RuntimeException e) 
		    {
		       	if (tx != null && tx.isActive()) 
		       	{
			        try 
			        {
			        	// Second try catch as the rollback could fail as well
			        	tx.rollback();
			        } 
			        catch (HibernateException e1) 
			        {
			        	logger.debug("Error rolling back transaction");
			        }
			        // throw the first exception again
			        throw e;
		       	}
		    }
		    catch (Exception e)
		    {
		       	out.print("{  \"code\": \"failure\", \"message\": \""+e.getMessage()+"\", \"data\": [] }");
		       	tx.rollback();
		       	return;
		    }
	    }
	    out.print("{ \"code\": \"success\", \"message\": \"Delete Successful\", \"data\": [] }");
	    session.close();
	}
	
	private void statistics()
	{   
		Session session = sf.openSession();
		HttpSession hs = request.getSession(true);
		User curr_user = (User) hs.getAttribute("curr_user"); 
	    String query = "";
	    if(request.isUserInRole("CAU"))
    		query = "select count(p) as count from Project p where p.statusValueByApprovalStatusId.statusId = "+Statics.APPROVED;
	    else
	    	query = "select count(p) as count from Project p where p.userByCreatedBy = "+curr_user.getUserId()+" and p.statusValueByApprovalStatusId.statusId = "+Statics.APPROVED;
		List entityList = session.createQuery(query).list();
		
		//Set up the data type for the JSON
		response.setContentType("application/json");
		//Define an output stream for the data to go
	    
		out.print("{ \"type\": \"project\", \"count\":\""+entityList.get(0).toString()+"\", \"data\": [] }");	
		session.close();
	}
}
