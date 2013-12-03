package edu.miami.ccs.goma;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.jboss.logging.Logger;

import edu.miami.ccs.goma.pojos.Organization;
import edu.miami.ccs.goma.pojos.Person;
import edu.miami.ccs.goma.pojos.User;

public class PersonOperations extends HttpServlet {

	private static ServiceRegistry serviceRegistry;
	private static SessionFactory sf;
	private static PrintWriter out;
	private static HttpServletResponse response;
	private static HttpServletRequest request;
	private static Logger logger;
	
	public void init(ServletConfig config) throws ServletException
	{
		sf = connect();

	} 

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
				
	    response = resp;	    
	    request = req;
	    HttpSession hs = request.getSession(true);	    
	    
	    try 
	    {
	    	out = response.getWriter();
	    }
	    catch(IOException e)
	    {
	    	logger.debug("Error opening output stream using PrintWriter");
	    }
	    
	    if(request.getParameter("mode").equals("fetch"))
	    	fetch();
	    else if(request.getParameter("mode").equals("update"))
	    	update();
	    else if(request.getParameter("mode").equals("create"))
	    	create();
	    else if(request.getParameter("mode").equals("list"))
	    	list();

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
	    Transaction tx = null;

	    try 
	    {
	    	tx = session.beginTransaction();
	    	List personList = session.createQuery("from Person p").list();
	    	
	    	//Set up the data type for the JSON
	    	response.setContentType("application/json");
	        
	    	out.print("{ \"hits\":"+personList.size()+", \"data\": [");
	    	for (Iterator iter = personList.iterator(); iter.hasNext();) 
	    	{
	    		 
	    		Person p = (Person) iter.next();

	    		out.print("{  \"person_id\": \""+p.getPersonId()+"\", \"email\": \""+p.getEmail()+"\", \"first_name\": \""+p.getFirstName()+"\", \"last_name\": \""+p.getLastName()+"\"}");
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
	
	
	private static void fetch()
	{
		Session session = sf.openSession();
	    session.clear();
    	HttpSession hs = request.getSession(true);	
		User curr_user = (User) hs.getAttribute("curr_user"); 
		
		User u = (User) session.load(User.class, curr_user.getUserId());
    	//Set up the data type for the JSON
    	response.setContentType("application/json");
        
    	out.print("{ \"data\": [");

		Person p = u.getPerson();
		out.print("{ \"first_name\": \""+p.getFirstName()+"\", \"last_name\": \""+p.getLastName()+"\", \"email\": \""+u.getUsername()+"\"" +
				", \"address\": \""+p.getAddress()+"\", \"phone\": \""+p.getPhone()+"\", \"fax\": \""+p.getFax()+"\", \"website\": \""+p.getWebsite()+"\"" +
				", \"organization\": \""+u.getOrganization().getName()+"\", \"job_title\": \""+p.getJobTitle()+"\", \"bio\": \""+p.getBio()+"\"}");
    	
    	out.print("] }");

	    session.close();
	}
	
	
	private static void create()
	{
		Session session = sf.openSession();
		java.util.Date date = new java.util.Date();
		
		Transaction tx = null;
		long personId = -1;
		
		HttpSession hs = request.getSession(true);		
		User curr_user = (User) hs.getAttribute("curr_user"); 
	 
	    Person p = new Person(request.getParameter("phone"), request.getParameter("first_name"), request.getParameter("last_name"), 
				request.getParameter("job_title"),  date, curr_user.getUserId(), 
				request.getParameter("email"));
		if(request.getParameter("address") != null)
			p.setAddress(request.getParameter("address"));
		if(request.getParameter("fax") != null)
			p.setFax(request.getParameter("fax"));
		if(request.getParameter("website") != null)
			p.setWebsite(request.getParameter("website"));
		if(request.getParameter("bio") != null)
			p.setBio(request.getParameter("bio"));
	    
	    try 
	    {
	    	tx = session.beginTransaction();
	    	session.save(p);	
	    	personId = p.getPersonId();
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
	    out.print("{  \"code\": \"success\", \"message\": \"Save Successful\", \"data\": [{ \"person_id\": \""+ personId +"\", \"name\": \""+ p.getFirstName()+" "+p.getLastName()+"\"}] }");
	    session.close();
	}
	
	
	private static void update()
	{
		Session session = sf.openSession();
		java.util.Date date = new java.util.Date();
		long userId = -1;
		Person p = null;
		
		Transaction tx = null;
		HttpSession hs = request.getSession(true);
		User curr_user = (User) hs.getAttribute("curr_user"); 
	    String password;
	    
	    //If we are not coming from profile, we fetch the person from the request
	    if(request.getParameter("person_id") != null)
	    	p = (Person) session.load(Person.class, new Long(request.getParameter("person_id")));
	    //We are coming from profile, so we need to get the person associated with the current user
	    else
	    {
	    	User u = (User) session.load(User.class, curr_user.getUserId());
	    	p = u.getPerson();
			if(request.getParameter("password")!= null)
			{
				password = DigestUtils.md5Hex(request.getParameter("password"));
				u.setPassword(password);
			}
	    }
								
		
		p.setPhone(request.getParameter("phone"));
		p.setFirstName(request.getParameter("first_name"));
		p.setLastName(request.getParameter("last_name"));
		p.setJobTitle(request.getParameter("job_title"));
		
		if(request.getParameter("address") != null)
			p.setAddress(request.getParameter("address"));
		if(request.getParameter("fax") != null)
			p.setFax(request.getParameter("fax"));
		if(request.getParameter("website") != null)
			p.setWebsite(request.getParameter("website"));
		if(request.getParameter("bio") != null)
			p.setBio(request.getParameter("bio"));
						
		p.setDateUpdated(date);
		p.setUpdatedBy(curr_user.getUserId());
	    
	    try 
	    {
	    	tx = session.beginTransaction();
	    	session.update(p);	
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
	    
	    out.print("{  \"code\": \"success\", \"message\": \"Save Successful\", \"data\": [{ \"person_id\": \""+ p.getPersonId() +"\", \"name\": \""+ p.getFirstName()+" "+p.getLastName()+"\"}] }");
	    session.close();
	}

}
