package edu.miami.ccs.goma;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

import edu.miami.ccs.goma.pojos.ApprovalRequest;
import edu.miami.ccs.goma.pojos.ApprovalRequestType;
import edu.miami.ccs.goma.pojos.Organization;
import edu.miami.ccs.goma.pojos.Person;
import edu.miami.ccs.goma.pojos.Program;
import edu.miami.ccs.goma.pojos.ProgramManager;
import edu.miami.ccs.goma.pojos.Station;
import edu.miami.ccs.goma.pojos.StatusValue;
import edu.miami.ccs.goma.pojos.User;

public class ProgramOperations extends HttpServlet {
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
		User curr_user = (User) hs.getAttribute("curr_user"); 

    	User u = (User) session.load(User.class, curr_user.getUserId());
    	Set<Program> prgList = u.getOrganization().getPrograms();
    	
    	//Set up the data type for the JSON
    	response.setContentType("application/json");
    	//Define an output stream for the data to go
        
    	out.print("{ \"type\": \"program\", \"organization\": \""+u.getOrganization().getName()+"\", \"hits\":"+prgList.size()+", \"data\": [");
    	for (Iterator iter = prgList.iterator(); iter.hasNext();) 
    	{
    		
    		Program p = (Program) iter.next();
    		
    		out.print("{ \"program_id\": \""+p.getProgramId()+"\", \"name\": \""+p.getName()+"\", \"status\": \""+p.getStatusValueByStatusId().getStatusValue()+"\", \"program_manager\": \""+p.getProgramManager().getPerson().getFirstName()+" "+ p.getProgramManager().getPerson().getLastName() +"\", \"website\": \""+p.getWebsite()+"\"}");
    		if(iter.hasNext())
    			out.print(",");
    	}
    	out.print("] }");
	    session.close();
	}
	
	
	private static void fetch()
	{

		Session session = sf.openSession();
		Program p = (Program) session.load(Program.class, new Long(request.getParameter("program_id")));
		
    	response.setContentType("application/json");      
    	out.print("{ \"data\": [ {");
    	out.print("\"program_id\": \""+p.getProgramId()+"\", \"name\": \""+p.getName()+"\", \"description\": \""+p.getDescription()+"\", \"pm_first_name\": \""+p.getProgramManager().getPerson().getFirstName()+"\", \"pm_last_name\": \""+p.getProgramManager().getPerson().getLastName() +
    			"\", \"organization\": \""+p.getOrganization().getName()+"\", \"website\": \""+p.getWebsite()+"\", \"date_created\": \""+sdf.format(p.getDateCreated())+"\", \"created_by\": \""+p.getUserByCreatedBy().getUsername() + "\", \"pm_id\": \""+p.getProgramManager().getPerson().getPersonId()+"\"" +
 				", \"pm_email\": \""+p.getProgramManager().getPerson().getEmail()+"\", \"pm_address\": \""+p.getProgramManager().getPerson().getAddress()+"\", \"pm_job_title\": \""+p.getProgramManager().getPerson().getJobTitle()+"\", \"pm_phone\": \""+p.getProgramManager().getPerson().getPhone()+"\", \"pm_fax\": \""+p.getProgramManager().getPerson().getFax()+"\"" +
 				", \"pm_homepage\": \""+p.getProgramManager().getPerson().getWebsite()+"\"");
    	if(p.getUserByUpdatedBy() != null)
    		out.print(", \"date_updated\": \""+p.getDateUpdated()+"\", \"updated_by\": \""+p.getUserByUpdatedBy().getUsername()+"\"");
    	out.print(",\"status\": \""+p.getStatusValueByStatusId().getStatusValue()+"\",\"status_id\": \""+p.getStatusValueByStatusId().getStatusId()+"\", \"approval\": \""+p.getStatusValueByApprovalStatusId().getStatusValue()+"\" } ] }");
	    session.close();
	}


	private static void create()
	{
		Session session = sf.openSession();
		java.util.Date date= new java.util.Date();
		Transaction tx = null;
		HttpSession hs = request.getSession(true);
		User curr_user = (User) hs.getAttribute("curr_user"); 
		long pm_id = -1;
		
		User u_creator = (User) session.load(User.class, curr_user.getUserId());
		Person p = (Person) session.load(Person.class, new Long(request.getParameter("pm_id")));
		Organization o = null;
		if(request.getParameter("organization_id") != null)
			o = (Organization) session.load(Organization.class, new Long(request.getParameter("organization_id")));
		else
			o = u_creator.getOrganization();
		StatusValue s_prog_stat = (StatusValue) session.load(StatusValue.class, new Long(request.getParameter("status_id")));
		StatusValue s_approval_stat = (StatusValue) session.load(StatusValue.class, new Long(Statics.PENDING));
		ProgramManager pm = new ProgramManager(p);
	    try 
	    {
	    	tx = session.beginTransaction();
	    	session.save(pm);	
	    	pm_id = pm.getProgramManagerId();
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
	    //When the program is created for the first time, the creator is the owner
		Program prg = new Program(u_creator, u_creator, o, s_approval_stat, s_prog_stat, pm, request.getParameter("name"), request.getParameter("description"), date);
		if(request.getParameter("website") != null)
			prg.setWebsite(request.getParameter("website"));
			
	    try 
	    {
	    	tx = session.beginTransaction();
	    	session.save(prg);	
	    	session.flush();
			ApprovalRequestType art = (ApprovalRequestType) session.load(ApprovalRequestType.class, new Long(Statics.PROGRAM));
			ApprovalRequest ar = new ApprovalRequest(curr_user, art, curr_user, s_approval_stat, date, prg.getProgramId());
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
	    out.print("{  \"code\": \"success\", \"message\": \"Save Successful\", \"program_id\": \""+prg.getProgramId()+"\", \"data\": [] }");
	    session.close();
	}
	
	private static void update()
	{

		Session session = sf.openSession();
		java.util.Date date= new java.util.Date();
		Transaction tx = null;
		HttpSession hs = request.getSession(true);
		User curr_user = (User) hs.getAttribute("curr_user"); 


		Program prg = (Program) session.load(Program.class, new Long(request.getParameter("program_id")));
		if(!request.isUserInRole("CAU") && (curr_user.getOrganization().getOrganizationId() != prg.getOrganization().getOrganizationId()))
		{
			out.print("{  \"code\": \"failure\", \"message\": \"Access Violation: Permission Denied.\", \"data\": [] }");
			return;
		}
		
		
		prg.setDescription(request.getParameter("description"));
		prg.setName(request.getParameter("name"));
		
		StatusValue s_approval_stat = (StatusValue) session.load(StatusValue.class, new Long(Statics.PENDING));
		
		User u_update = prg.getUserByUpdatedBy();
		User u_owner = prg.getUserByOwnerId();
		
		Organization o = (Organization) session.load(Organization.class, new Long(request.getParameter("organization_id")));
		StatusValue s_prog_stat = (StatusValue) session.load(StatusValue.class, new Long(request.getParameter("status_id")));
		
		ProgramManager pm;
		if(request.getParameter("person_id") == null)
		{
			pm = prg.getProgramManager();
		}
		else
		{
			pm = (ProgramManager) session.load(ProgramManager.class, new Long(request.getParameter("pm_id")));
		}
		if(request.getParameter("website") != null)
			prg.setWebsite(request.getParameter("website"));
			
		prg.setStatusValueByStatusId(s_prog_stat);
		prg.setDateUpdated(date);
		prg.setUserByUpdatedBy(curr_user);
		prg.setStatusValueByApprovalStatusId(s_approval_stat);
		prg.setOrganization(o);
		
	    try 
	    {
	    	tx = session.beginTransaction();
	    	session.update(prg);	
			ApprovalRequestType art = (ApprovalRequestType) session.load(ApprovalRequestType.class, new Long(Statics.PROGRAM));
			ApprovalRequest ar = new ApprovalRequest(curr_user, art, curr_user, s_approval_stat, date, prg.getProgramId());
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
	    out.print("{  \"code\": \"success\", \"message\": \"Save Successful\", \"program_id\": \""+prg.getProgramId()+"\", \"data\": [] }");
	    session.close();
	}
	
	private static void delete()
	{
		Session session = sf.openSession();
	    Transaction tx = null;
	    String[] uList = request.getParameterValues("program_id");

	    for(String item : uList)
	    {
	    	Program o = (Program) session.load(Program.class, new Long(item));
		    try 
		    {
		    	tx = session.beginTransaction();
		    	session.delete(o);
		    	tx.commit();
		    } 
		    catch (ConstraintViolationException cve)
		    {
		    	out.print("{ \"code\": \"failure\", \"message\": \"Delete Failed: Cannot delete "+ o.getName() +" while it has users assigned to it.\", \"data\": [] }");
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
    		query = "select count(p) as count from Program p where p.statusValueByApprovalStatusId.statusId = "+Statics.APPROVED;
	    else
	    	query = "select count(p) as count from Program p where p.userByCreatedBy = "+curr_user.getUserId()+" and p.statusValueByApprovalStatusId.statusId = "+Statics.APPROVED;
		List entityList = session.createQuery(query).list();
		
		//Set up the data type for the JSON
		response.setContentType("application/json");
		//Define an output stream for the data to go
	    
		out.print("{ \"type\": \"program\", \"count\":\""+entityList.get(0).toString()+"\", \"data\": [] }");	
	    session.close();
	}
}
