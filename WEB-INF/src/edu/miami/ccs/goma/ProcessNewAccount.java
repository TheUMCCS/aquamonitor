package edu.miami.ccs.goma;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import edu.miami.ccs.goma.pojos.User;


/**
 * @author ndatar
 *
 */
public class ProcessNewAccount extends HttpServlet {

  
	private static ServiceRegistry serviceRegistry;
	private static SessionFactory sf;
	private static Session session;
	
    public ProcessNewAccount() {
    }

    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
    {
		Configuration cfg = new Configuration();
		cfg.configure();
		serviceRegistry = new ServiceRegistryBuilder().applySettings(cfg.getProperties()).buildServiceRegistry();        
	    sf = cfg.buildSessionFactory(serviceRegistry);
		session = sf.openSession();
	    PrintWriter out = response.getWriter();
	    Query q = session.createQuery("from ApprovalRequest ar where ar.statusValue.statusId = :approvalStat");
    	q.setParameter("approvalStat", new Long(Statics.PENDING));
    	List elemList = q.list();
    	
    	if(elemList.size() == 0)
    		return;
    	
    	List uList = session.createQuery("from User u where u.userRole.role = 'CAU'").list();

    	
	    try 
	    {   	
    	     boolean debug = false;

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

    	    for (Iterator iter = uList.iterator(); iter.hasNext();) 
	    	{
	    		
    	    	User u = (User) iter.next();
	    	    InternetAddress addressTo = new InternetAddress(u.getUsername());
	
	    	    msg.setRecipient(Message.RecipientType.TO, addressTo);
	    	    String msgBody =  "Submitted By: "+request.getParameter("first_name")+" "+request.getParameter("last_name")+"\nEmail: "+request.getParameter("email")+"\nAffiliation: "+request.getParameter("job_title")+", "+request.getParameter("organization")+"\nPhone: "+request.getParameter("phone");
	    	    		
	    	    // Setting the Subject and Content Type
	    	    msg.setSubject("GoMonitor Account Request");
	
	    	    msg.setContent( msgBody, "text/plain");
	    	    Transport.send(msg);	
    	    }
    	    out.print("{ \"code\": \"success\" }");
	    }		    
	    catch (RuntimeException e) 
	    {
	    	out.print("{ \"code\": \"error\" }");
	       	e.printStackTrace();
	    } catch (AddressException e) {
	    	out.print("{ \"code\": \"error\" }");
	    	e.printStackTrace(); 	
		} catch (MessagingException e) {
			out.print("{ \"code\": \"error\" }");
			e.printStackTrace();
		}
    }

}
