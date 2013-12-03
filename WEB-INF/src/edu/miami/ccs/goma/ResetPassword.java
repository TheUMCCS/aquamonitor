package edu.miami.ccs.goma;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
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

import edu.miami.ccs.goma.pojos.User;

public class ResetPassword extends HttpServlet {
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
	    Transaction tx = null;
	    try 
	    {
	    	
	    	User u = (User) session.createQuery("from User u where u.username ='"+request.getParameter("username")+"'").uniqueResult();
	    	
	    	if(u == null)
	    	{
	    		out.print("{  \"code\": \"error\", \"message\": \"This user does not exist.\", \"data\": [] }"); 
	    	}
	    	else
	    	{
	    		tx = session.beginTransaction();
	    	    final int PASSWORD_LENGTH = 8;  
	    	    StringBuffer sb = new StringBuffer();  
	    	    for (int x = 0; x < PASSWORD_LENGTH; x++)  
	    	    {  
	    	      sb.append((char)((int)(Math.random()*26)+97));  
	    	    }  
	    	    String pw = sb.toString();  
		
	    		
	    		u.setPassword(DigestUtils.md5Hex(pw));
	    		tx.commit();
	    		
	    	     boolean debug = false;

	    	     //Set the host smtp address
	    	     String MAIL_PROPS = "/mail_setup.properties";
	             InputStream is = Statics.class.getResourceAsStream(MAIL_PROPS);
	             Properties props = new Properties();
	             try
	             {
	                    props.load(is);
	             }
	             catch (IOException e)
	             {
	                    System.out.println("Error reading the properties file");
	             }


	    	    // create some properties and get the default Session
	    	    javax.mail.Session s = javax.mail.Session.getDefaultInstance(props, null);
	    	    s.setDebug(debug);

	    	    // create a message
	    	    Message msg = new MimeMessage(s);

	    	    // set the from and to address
	    	    InternetAddress addressFrom = new InternetAddress(props.getProperty("mail.from").toString());
	    	    msg.setFrom(addressFrom);

	    	    InternetAddress addressTo = new InternetAddress(request.getParameter("username"));

	    	    msg.setRecipient(Message.RecipientType.TO, addressTo);

	    	   	String msgBody =  "A password reset request was submitted for this email address. Your new password is "+pw+". Please use this password to login and change it as soon as possible. If you did not request a password reset, please contact the GOMA system administrator immediately.";
	    	    		
	    	    // Setting the Subject and Content Type
	    	    msg.setSubject("GoMonitor Password Reset");

	    	    msg.setContent( msgBody, "text/plain");
	    	    Transport.send(msg);

	    	    out.print("{  \"code\": \"success\", \"message\": \"Password Reset Successful. Please check your email for the new password\", \"data\": [] }");
	    	}
	    	
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
	       	out.print("{  \"code\": \"failure\", \"message\": \"Failure while generating password. Please try agian\", \"data\": [ {\"details\": \""+e.getMessage()+"\"} ] }");
	    } catch (AddressException e) {
	    	out.print("{  \"code\": \"failure\", \"message\": \"Failure while sending mail. Please try again\", \"data\": [ {\"details\": \""+e.getMessage()+"\"} ] }"); 	
		} catch (MessagingException e) {
			out.print("{  \"code\": \"failure\", \"message\": \"Failure while sending mail. Please try again\", \"data\": [ {\"details\": \""+e.getMessage()+"\"} ] }");
			//e.printStackTrace();
		}

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
