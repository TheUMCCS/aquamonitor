package edu.miami.ccs.goma;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
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
import edu.miami.ccs.goma.pojos.Dictionary;
import edu.miami.ccs.goma.pojos.DictionaryTerm;
import edu.miami.ccs.goma.pojos.StatusValue;
import edu.miami.ccs.goma.pojos.User;

public class DictionaryTermOperations extends HttpServlet {
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
		sdf = new SimpleDateFormat("MMM dd, yyyy");
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
	    else if(request.getParameter("mode").equals("list"))
	    	list();
	    else if(request.getParameter("mode").equals("delete"))
	    	delete();
	    else if(request.getParameter("mode").equals("fetch"))
	    	fetch();
	    else if(request.getParameter("mode").equals("update"))
	    	update();
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
	
	private static void fetch()
	{
		Session session = sf.openSession();
		DictionaryTerm dt = (DictionaryTerm) session.load(DictionaryTerm.class, new Long(request.getParameter("term_id")));
		
    	response.setContentType("application/json");      
    	out.print("{ \"data\": [ {");
    	out.print("\"dictionary\": \""+dt.getDictionary().getName()+"\", \"term_id\": \""+dt.getTermId()+"\", \"name\": \""+dt.getTerm()+"\", \"description\": \""+dt.getDescription()+
    			"\", \"approval_status\": \""+dt.getStatusValue()+"\", \"date_created\": \""+sdf.format(dt.getDateCreated())+"\", \"created_by\": \""+dt.getUserByCreatedBy().getUsername() + "\"");
    	if(dt.getUserByUpdatedBy() != null)
    		out.print(", \"date_updated\": \""+sdf.format(dt.getDateUpdated())+"\", \"updated_by\": \""+dt.getUserByUpdatedBy().getUsername()+"\"");
    	out.print("} ] }");
	    session.close();
	}

	private static void list()
	{
		Session session = sf.openSession();
    	List termsList = session.createQuery("from DictionaryTerm dt where dt.dictionary.dictionaryId = "+ request.getParameter("dictionary_id")+" and dt.statusValue.statusId = "+Statics.APPROVED).list();
    	Dictionary d = (Dictionary) session.load(Dictionary.class, new Long(request.getParameter("dictionary_id")));
    	//Set up the data type for the JSON
    	response.setContentType("application/json");
        
    	out.print("{ \"hits\":"+termsList.size()+", \"code\": \""+d.getDictionaryCode()+"\", \"data\": [");
    	for (Iterator iter = termsList.iterator(); iter.hasNext();) 
    	{
    		 
    		DictionaryTerm dt = (DictionaryTerm) iter.next();

    		out.print("{  \"term_id\": \""+dt.getTermId()+"\", \"description\": \""+dt.getDescription()+"\", \"name\": \""+dt.getTerm()+"\"}");
    		if(iter.hasNext())
    			out.print(",");
    	}
    	out.print("] }");
	    session.close();
	}
	
	private static void create()
	{
		Session session = sf.openSession();
		java.util.Date date= new java.util.Date();
		Transaction tx = null;
		HttpSession hs = request.getSession(true);
		User curr_user = (User) hs.getAttribute("curr_user"); 
		StatusValue approval_stat = (StatusValue) session.load(StatusValue.class, new Long(Statics.PENDING));	
		User u = (User) session.load(User.class, curr_user.getUserId());
		Dictionary d = (Dictionary) session.load(Dictionary.class, new Long(request.getParameter("dictionary_id")));
		
		DictionaryTerm dt = new DictionaryTerm(u, d, approval_stat, request.getParameter("name"), request.getParameter("description"), date);
		

	    try 
	    {
	    	tx = session.beginTransaction();
	    	session.save(dt);
	    	session.flush();
			ApprovalRequestType art = (ApprovalRequestType) session.load(ApprovalRequestType.class, new Long(Statics.DICTIONARY_TERM));
			ApprovalRequest ar = new ApprovalRequest(curr_user, art, curr_user, approval_stat, date, dt.getTermId());
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
	    out.print("{  \"code\": \"success\", \"message\": \"Save Successful\", \"data\": [] }");
	    session.close();
	}
	
	private static void update()
	{
		Session session = sf.openSession();
		java.util.Date date= new java.util.Date();
		Transaction tx = null;
		HttpSession hs = request.getSession(true);
		User curr_user = (User) hs.getAttribute("curr_user"); 
		StatusValue approval_stat = (StatusValue) session.load(StatusValue.class, new Long(Statics.PENDING));	
		User u = (User) session.load(User.class, curr_user.getUserId());
	
		DictionaryTerm dt = (DictionaryTerm) session.load(DictionaryTerm.class, new Long(request.getParameter("term_id")));
		
		dt.setDescription(request.getParameter("description"));
		dt.setTerm(request.getParameter("name"));
		dt.setStatusValue(approval_stat);
		dt.setDateUpdated(date);
		dt.setUserByUpdatedBy(u);
		
	    try 
	    {
	    	tx = session.beginTransaction();
	    	session.update(dt);	
	    	session.flush();
			ApprovalRequestType art = (ApprovalRequestType) session.load(ApprovalRequestType.class, new Long(Statics.DICTIONARY_TERM));
			ApprovalRequest ar = new ApprovalRequest(curr_user, art, curr_user, approval_stat, date, dt.getTermId());
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
	    out.print("{  \"code\": \"success\", \"message\": \"Update Successful\", \"data\": [] }");
	    session.close();
	}
	
	private static void delete()
	{
		Session session = sf.openSession();
	    Transaction tx = null;
	    String[] tList = request.getParameterValues("term_id");

	    for(String item : tList)
	    {
	    	DictionaryTerm dt = (DictionaryTerm) session.load(DictionaryTerm.class, new Long(item));
		    try 
		    {
		    	tx = session.beginTransaction();
		    	session.delete(dt);
		    	tx.commit();
		    } 
		    catch (ConstraintViolationException cve)
		    {
		    	out.print("{ \"code\": \"failure\", \"message\": \"Delete Failed: Cannot delete "+ dt.getTerm() +" - Constraint Violation, \"data\": [] }");
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
    		query = "select count(dt) as count from DictionaryTerm dt where dt.statusValue.statusId = "+Statics.APPROVED;
	    else
	    	query = "select count(dt) as count from DictionaryTerm dt where dt.userByCreatedBy = "+curr_user.getUserId()+" and dt.statusValue.statusId = "+Statics.APPROVED;
		List entityList = session.createQuery(query).list();
		
		//Set up the data type for the JSON
		response.setContentType("application/json");
		//Define an output stream for the data to go
	    
		out.print("{ \"type\": \"Dictionary Term\", \"count\":\""+entityList.get(0).toString()+"\", \"data\": [] }");	
	    session.close();
	}
}
