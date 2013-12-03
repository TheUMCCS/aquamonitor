package edu.miami.ccs.goma;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;


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
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.jboss.logging.Logger;

import edu.miami.ccs.goma.pojos.User;

public class LoginInterceptor extends HttpServlet {
	private static ServiceRegistry serviceRegistry;
	private static SessionFactory sf;
	private static Session session;
	private static PrintWriter out;
	private static HttpServletResponse response;
	private static HttpServletRequest request;
	private static Logger logger;
	private static SimpleDateFormat sdf;
	
	public void init(ServletConfig config) throws ServletException
	{
		sf = connect();
		session = sf.openSession();
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
	    
	    HttpSession hs = request.getSession(true);
	    if(hs.getAttribute("curr_user") == null)
	    {
		    Transaction tx = null;
		    try 
		    {
		    	tx = session.beginTransaction();
		    	User u = (User) session.createQuery("from User u where u.username ='"+request.getRemoteUser()+"'").uniqueResult();
		    	hs.setAttribute("curr_user", u);
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
		       	out.print("{  \"code\": \"failure\", \"message\": \"Failure while setting session variables. Please login again\", \"stacktrace\": \""+e.getMessage()+"\" }");
		       	return;
		    }
		    out.print("{  \"code\": \"success\", \"message\": \"Session augmented\", \"data\": [] }");
	    }
	    else
	    	out.print("{  \"code\": \"success\", \"message\": \"Session already updated\", \"data\": [] }");
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
	

}
