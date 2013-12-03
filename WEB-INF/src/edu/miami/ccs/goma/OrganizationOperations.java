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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

import edu.miami.ccs.goma.pojos.Organization;
import edu.miami.ccs.goma.pojos.OrganizationType;
import edu.miami.ccs.goma.pojos.Person;
import edu.miami.ccs.goma.pojos.Station;
import edu.miami.ccs.goma.pojos.User;

public class OrganizationOperations extends HttpServlet {
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
	    
        response.setHeader("Expires", "Tue, 03 Jul 2001 06:00:00 GMT");
        response.setHeader("Last-Modified", new Date().toString());
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        
	    try 
	    {
	    	out = response.getWriter();
	    }
	    catch(IOException e)
	    {
	    	logger.debug("Error opening output stream using PrintWriter");
	    }
	    
	    if(!request.isUserInRole("CAU"))
	    {
	    	out.print("Access to this page is forbidden with your current credentials. Please login with the appropriate credentials. If you feel you are getting this message in error, please contact the system administrator.");
	    	return;
	    }

	    if(request.getParameter("mode").equals("create"))	
	    	create();
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
		Organization o = (Organization) session.load(Organization.class, new Long(request.getParameter("organization_id")));
		
    	response.setContentType("application/json");      
    	out.print("{ \"data\": [ {");
    	out.print("\"organization_id\": \""+o.getOrganizationId()+"\", \"name\": \""+o.getName()+"\", \"description\": \""+o.getDescription()+
    			"\", \"website\": \""+o.getWebsite()+"\", \"date_created\": \""+sdf.format(o.getDateCreated())+"\", \"created_by\": \""+o.getUserByCreatedBy().getUsername() + "\"");
    	if(o.getUserByUpdatedBy() != null)
    		out.print(", \"date_updated\": \""+o.getDateUpdated()+"\", \"updated_by\": \""+o.getUserByUpdatedBy().getUsername()+"\"");
    	out.print(",\"type_id\": \""+o.getOrganizationType().getTypeId()+"\", \"type\": \""+o.getOrganizationType().getType()+"\" } ] }");
	    session.close();
	}

	private static void create()
	{
		Session session = sf.openSession();
		java.util.Date date= new java.util.Date();
		Transaction tx = null;
		HttpSession hs = request.getSession(true);
		User curr_user = (User) hs.getAttribute("curr_user"); 
		
		User u = (User) session.load(User.class, curr_user.getUserId());
		OrganizationType ot = (OrganizationType) session.load(OrganizationType.class, new Long(request.getParameter("type_id")));

		Organization o = new Organization(u, ot, request.getParameter("name"), request.getParameter("description"), date);
		if(request.getParameter("website") != null)
			o.setWebsite(request.getParameter("website"));
			
	    try 
	    {
	    	tx = session.beginTransaction();
	    	session.save(o);	
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
		OrganizationType ot = (OrganizationType) session.load(OrganizationType.class, new Long(request.getParameter("type_id")));
		Organization o = (Organization) session.load(Organization.class, new Long(request.getParameter("organization_id")));
		
		o.setDescription(request.getParameter("description"));
		o.setName(request.getParameter("name"));
		o.setOrganizationType(ot);
		if(request.getParameter("website") != null)
			o.setWebsite(request.getParameter("website"));

		o.setDateUpdated(date);
		o.setUserByUpdatedBy(u);

	    try 
	    {
	    	tx = session.beginTransaction();
	    	session.update(o);	
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
	
	private static void delete()
	{
		Session session = sf.openSession();
	    Transaction tx = null;
	    String[] uList = request.getParameterValues("organization_id");

	    for(String item : uList)
	    {
	    	Organization o = (Organization) session.load(Organization.class, new Long(item));
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
	    String query = "select count(o) as count from Organization";
		List entityList = session.createQuery(query).list();
		
		//Set up the data type for the JSON
		response.setContentType("application/json");
		//Define an output stream for the data to go
	    
		out.print("{ \"type\": \"organization\", \"count\":\""+entityList.get(0).toString()+"\", \"data\": [] }");	
	    session.close();
	}
}
