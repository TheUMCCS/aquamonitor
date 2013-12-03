package edu.miami.ccs.goma;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import edu.miami.ccs.goma.pojos.User;


public class ApprovalReminder {

  
	private static ServiceRegistry serviceRegistry;
	private static SessionFactory sf;
	private static Session session;
	
    public ApprovalReminder() {
    }

    
    public static void main(String[] args) {
    	
		Configuration cfg = new Configuration();
		cfg.configure();
		serviceRegistry = new ServiceRegistryBuilder().applySettings(cfg.getProperties()).buildServiceRegistry();        
	    sf = cfg.buildSessionFactory(serviceRegistry);
		session = sf.openSession();
	    
	    Query q = session.createQuery("from ApprovalRequest ar where ar.statusValue.statusId = :approvalStat");
    	q.setParameter("approvalStat", new Long(Statics.PENDING));
    	List elemList = q.list();
    	
    	if(elemList.size() == 0)
    		return;
    	
    	List uList = session.createQuery("from User u where u.userRole.role = 'CAU'").list();

    	
	    try 
	    {   	
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

    	    for (Iterator iter = uList.iterator(); iter.hasNext();) 
	    	{
	    		
    	    	User u = (User) iter.next();
	    	    InternetAddress addressTo = new InternetAddress(u.getUsername());
	
	    	    msg.setRecipient(Message.RecipientType.TO, addressTo);
	
	    	   	String msgBody =  "There are "+elemList.size()+" approval requests pending. Please resolve them at your earliest convenience. <a href = \"http://goma.ccs.miami.edu/admin/\">Click here to go to the GOMA Catalog website</a>.";
	    	    		
	    	    // Setting the Subject and Content Type
	    	    msg.setSubject("GoMonitor Pending Approval Reminder");
	
	    	    msg.setContent( msgBody, "text/plain");
	    	    Transport.send(msg);	    	
    	    }
	    }		    
	    catch (RuntimeException e) 
	    {
	       	e.printStackTrace();
	    } catch (AddressException e) {
	    	e.printStackTrace(); 	
		} catch (MessagingException e) {
			e.printStackTrace();
		}
    }

}
