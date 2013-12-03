package edu.miami.ccs.goma;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

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

import edu.miami.ccs.goma.pojos.Dictionary;
import edu.miami.ccs.goma.pojos.Person;
import edu.miami.ccs.goma.pojos.StatusValue;
import edu.miami.ccs.goma.pojos.User;

public class DictionaryOperations extends HttpServlet {
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
		// TODO Auto-generated method stub
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
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
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
		Dictionary d = (Dictionary) session.load(Dictionary.class, new Long(request.getParameter("dictionary_id")));
		
    	response.setContentType("application/json");      
    	out.print("{ \"data\": [ {");
    	out.print("\"dictionary_id\": \""+d.getDictionaryId()+"\", \"name\": \""+d.getName()+"\", \"description\": \""+d.getDescription()+
    			"\", \"approval_status\": \""+d.getStatusValue()+"\", \"date_created\": \""+sdf.format(d.getDateCreated())+"\", \"created_by\": \""+d.getUserByCreatedBy().getUsername() + "\"");
    	if(d.getUserByUpdatedBy() != null)
    		out.print(", \"date_updated\": \""+sdf.format(d.getDateUpdated())+"\", \"updated_by\": \""+d.getUserByUpdatedBy().getUsername()+"\"");
    	out.print("} ] }");
	    session.close();
	}

	private static void list()
	{
	    Transaction tx = null;
	    Session session = sf.openSession();
	    try 
	    {
	    	tx = session.beginTransaction();
	    	List dictionaryList = session.createQuery("from Dictionary d order by d.name").list();
	    	
	    	//Set up the data type for the JSON
	    	response.setContentType("application/json");
	        
	    	out.print("{ \"hits\":"+dictionaryList.size()+", \"data\": [");
	    	for (Iterator iter = dictionaryList.iterator(); iter.hasNext();) 
	    	{
	    		 
	    		Dictionary d = (Dictionary) iter.next();

	    		out.print("{  \"dictionary_id\": \""+d.getDictionaryId()+"\", \"description\": \""+d.getDescription()+"\", \"name\": \""+d.getName()+"\", \"approval_status\": \""+d.getStatusValue().getStatusValue()+"\"}");
	    		if(iter.hasNext())
	    			out.print(",");
	    	}
	    	out.print("] }");
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
		
		Dictionary d = new Dictionary(u, approval_stat, request.getParameter("name"), request.getParameter("description"), date, request.getParameter("dictionary_code"));
		
	    try 
	    {
	    	tx = session.beginTransaction();
	    	session.save(d);	
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
		
		User u = (User) session.load(User.class, curr_user.getUserId());
	
		Dictionary d = (Dictionary) session.load(Dictionary.class, new Long(request.getParameter("dictionary_id")));
		
		d.setDescription(request.getParameter("description"));
		d.setName(request.getParameter("name"));
		
		if(request.getParameter("approval_status") != null)
			d.getStatusValue().setStatusValue(request.getParameter("approval_status"));

		d.setDateUpdated(date);
		d.setUserByUpdatedBy(u);
			
	    try 
	    {
	    	tx = session.beginTransaction();
	    	session.update(d);	
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
	    String[] uList = request.getParameterValues("dictionary_id");

	    for(String item : uList)
	    {
	    	Dictionary d = (Dictionary) session.load(Dictionary.class, new Long(item));
		    try 
		    {
		    	tx = session.beginTransaction();
		    	session.delete(d);
		    	tx.commit();
		    } 
		    catch (ConstraintViolationException cve)
		    {
		    	out.print("{ \"code\": \"failure\", \"message\": \"Delete Failed: Cannot delete "+ d.getName() +" - Constraint Violation, \"data\": [] }");
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
}
