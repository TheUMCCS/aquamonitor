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
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.jboss.logging.Logger;

import edu.miami.ccs.goma.pojos.Announcement;
import edu.miami.ccs.goma.pojos.Organization;
import edu.miami.ccs.goma.pojos.User;

public class AnnouncementOperations extends HttpServlet {

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
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
	    response = resp;	    
	    request = req;
	    HttpSession hs = request.getSession(true);	    
	    
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
	    if(request.getParameter("mode").equals("list"))	
	    	list();
	    else if(request.getParameter("mode").equals("create"))	
	    	create();
	    else if(request.getParameter("mode").equals("delete"))	
	    	delete();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		this.doGet(req, resp);
	}
	
	
	private void list()
	{
		Session session = sf.openSession();
	    HttpSession hs = request.getSession(true);    
		User curr_user = (User) hs.getAttribute("curr_user"); 
	    Query q = session.createQuery("from Announcement a order by a.dateCreated desc");
	    q.setMaxResults(3);

    	List aList = q.list();
    	
    	//Set up the data type for the JSON
    	response.setContentType("application/json");
    	//Define an output stream for the data to go
        
    	out.print("{ \"type\": \"announcement\", \"hits\":"+aList.size()+", \"data\": [");
    	for (Iterator iter = aList.iterator(); iter.hasNext();) 
    	{ 		
    		Announcement a = (Announcement) iter.next();
    		
    		out.print("{ \"announcement_id\": \""+a.getAnnouncementId()+"\", " +
    				"\"date_created\": \""+sdf.format(a.getDateCreated())+"\", \"created_by\": \""+a.getUserByCreatedBy().getUsername()+"\"," +
    				"\"name\": \""+a.getName()+"\", \"description\": \""+a.getDescription()+"\"");
    		out.print("}");
    		if(iter.hasNext())
    			out.print(",");
    		//logger.debug(“{}”, element);
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
		
		User u = (User) session.load(User.class, curr_user.getUserId());
		
		Announcement a = new Announcement(u, request.getParameter("name"), request.getParameter("description"), date);
		
	    try 
	    {
	    	tx = session.beginTransaction();
	    	session.save(a);	
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
	    out.print("{  \"code\": \"success\", \"message\": \"Save Successful\", \"data\": [] }");
	    session.close();
	}
	
	private static void delete()
	{
		Session session = sf.openSession();
	    Transaction tx = null;
	    String[] uList = request.getParameterValues("announcement_id");

	    for(String item : uList)
	    {
	    	Announcement a = (Announcement) session.load(Announcement.class, new Long(item));
		    try 
		    {
		    	tx = session.beginTransaction();
		    	session.delete(a);
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
	    }
	    out.print("{ \"code\": \"success\", \"message\": \"Delete Successful\", \"data\": [] }");
	    session.close();
	}
	
}
