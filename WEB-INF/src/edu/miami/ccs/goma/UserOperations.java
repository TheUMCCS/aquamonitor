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

import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.jboss.logging.Logger;

import edu.miami.ccs.goma.pojos.Organization;
import edu.miami.ccs.goma.pojos.Person;
import edu.miami.ccs.goma.pojos.User;
import edu.miami.ccs.goma.pojos.UserRole;

public class UserOperations extends HttpServlet {

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
	    HttpSession hs = request.getSession(true);	    
	    
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

	    if(request.getParameter("mode").equals("list"))		  
	    	list();
	    else if(request.getParameter("mode").equals("create"))	
	    	create();
	    else if(request.getParameter("mode").equals("update"))	
	    	update();
	    else if(request.getParameter("mode").equals("delete"))
	    	delete();
	    else if(request.getParameter("mode").equals("fetch"))
	    	fetch();
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
	    Transaction tx = null;
	    session.clear();
	    
	    try 
	    {
	    	tx = session.beginTransaction();
	    	List userList = session.createQuery("from User u").list();
	    	
	    	//Set up the data type for the JSON
	    	response.setContentType("application/json");
	    	
	    	out.print("{ \"hits\":"+userList.size()+", \"data\": [");
	    	for (Iterator iter = userList.iterator(); iter.hasNext();) 
	    	{
	    		User u = (User) iter.next();
	    		
	    		Person p = u.getPerson();
	    		UserRole ur = u.getUserRole();
	    		out.print("{ \"user_id\": \""+u.getUserId()+"\", \"job_title\": \""+p.getJobTitle()+"\", \"role\": \""+ur.getRole()+"\", \"email\": \""+u.getUsername()+"\", \"first_name\": \""+p.getFirstName()+"\", \"last_name\": \""+p.getLastName()+"\"}");
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
	    Transaction tx = null;

	    try 
	    {
	    	tx = session.beginTransaction();
	    	
	    	User u = (User) session.createQuery("from User u where u.userId ="+request.getParameter("user_id")).uniqueResult();
	    	//Set up the data type for the JSON
	    	response.setContentType("application/json");
	        
	    	out.print("{ \"data\": [");

    		UserRole ur = u.getUserRole();
    		Person p = u.getPerson(); 
    		Organization o = u.getOrganization();
    		out.print("{ \"user_id\": \""+u.getUserId()+"\", \"role\": \""+ur.getRole()+"\", \"email\": \""+u.getUsername()+"\",  " +
    				"\"first_name\": \""+p.getFirstName()+"\", \"last_name\": \""+p.getLastName()+"\", \"person_id\": \""+u.getPerson().getPersonId() + "\"" +
    				", \"date_created\": \""+sdf.format(u.getDateCreated())+"\", \"created_by\": \""+u.getCreatedBy()+"\", \"user_role_id\": \""+ur.getUserRoleId()+"\"" +
    				", \"address\": \""+p.getAddress()+"\", \"phone\": \""+p.getPhone()+"\", \"fax\": \""+p.getFax()+"\", \"website\": \""+p.getWebsite()+"\"" +
    				", \"job_title\": \""+p.getJobTitle()+"\", \"bio\": \""+p.getBio()+"\", \"organization\": \""+o.getName()+"\", \"organization_id\": \""+o.getOrganizationId()+"\"");
    		if(u.getUpdatedBy() != null)
    			out.print(", \"updated_by\": \""+u.getUpdatedBy()+"\", \"date_updated\": \""+sdf.format(u.getDateUpdated())+"\"");
	    	
	    	out.print("} ] }");
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
		java.util.Date date = new java.util.Date();
		
		Transaction tx = null;
		long personId = -1;
		long userId = -1;
		User u;
		
    	List tmp = (List) session.createQuery("from User u where u.username ='"+request.getParameter("username")+"'").list();
    	
    	if(!tmp.isEmpty())
    	{
    		out.print("{  \"code\": \"failure\", \"message\": \"This username is already in the system\", \"data\": [] }"); 
    		return;
    	}
		
		HttpSession hs = request.getSession(true);
		User curr_user = (User) hs.getAttribute("curr_user"); 

	    String password = DigestUtils.md5Hex(request.getParameter("password"));
	 
	    Person p = new Person(request.getParameter("phone"), request.getParameter("first_name"), request.getParameter("last_name"), 
				request.getParameter("job_title"),  date, curr_user.getUserId(), 
				request.getParameter("username"));
		if(request.getParameter("address") != null)
			p.setAddress(request.getParameter("address"));
		if(request.getParameter("fax") != null)
			p.setFax(request.getParameter("fax"));
		if(request.getParameter("website") != null)
			p.setWebsite(request.getParameter("website"));
		if(request.getParameter("bio") != null)
			p.setBio(request.getParameter("bio"));
	    
	    UserRole ur = new UserRole(request.getParameter("username"), request.getParameter("role"));
	    
	    Organization o = (Organization) session.load(Organization.class, new Long(request.getParameter("organization_id")));
		u = new User(o, ur, p, request.getParameter("username"), password, date, curr_user.getUserId());
		
	    try 
	    {
	    	tx = session.beginTransaction();
	    	session.saveOrUpdate(u);	
	    	userId = u.getUserId();
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
		java.util.Date date = new java.util.Date();
		long userId = -1;
		
		Transaction tx = null;
		HttpSession hs = request.getSession(true);
		User curr_user = (User) hs.getAttribute("curr_user"); 
	    String password;
	   
		
		User u = (User) session.load(User.class, new Long(request.getParameter("user_id")));
		Person p = (Person) session.load(Person.class, new Long(request.getParameter("person_id")));
		UserRole ur = (UserRole) session.load(UserRole.class, new Long(request.getParameter("user_role_id")));
		Organization o = (Organization) session.load(Organization.class, new Long(request.getParameter("organization_id")));
						
		
		if(request.getParameter("password")!= null)
		{
			password = DigestUtils.md5Hex(request.getParameter("password"));
			u.setPassword(password);
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
		
	    ur.setRole(request.getParameter("role"));

	    u.setDateUpdated(date);
		u.setUpdatedBy(curr_user.getUserId());
	    u.setPerson(p);
	    u.setUserRole(ur);
	    u.setOrganization(o);
	    
	    try 
	    {
	    	tx = session.beginTransaction();
	    	session.saveOrUpdate(u);	
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
	    String[] uList = request.getParameterValues("user_id");

	    for(String item : uList)
	    {
	    	User u = (User) session.load(User.class, new Long(item));
		    try 
		    {
		    	tx = session.beginTransaction();
		    	session.delete(u);
		    	tx.commit();
		    } 
		    catch (ConstraintViolationException cve)
		    {
		    	out.print("{ \"code\": \"failure\", \"message\": \"Delete Failed: Cannot delete "+ u.getUsername() +" due to other objects that may depend on it\", \"data\": [] }");
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
    		query = "select count(u) as count from User u";
	    else
	    {
	    	out.print("{ \"type\": \"user\", \"count\":\"User Not Permitted\", \"data\": [] }");
	    	return;
	    }
		List entityList = session.createQuery(query).list();
		
		//Set up the data type for the JSON
		response.setContentType("application/json");
		//Define an output stream for the data to go
	    
		out.print("{ \"type\": \"user\", \"count\":\""+entityList.get(0).toString()+"\", \"data\": [] }");	
		 session.close();
	}

}
