package edu.miami.ccs.goma;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
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
import org.hibernate.SQLQuery;
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

import edu.miami.ccs.goma.pojos.ApprovalRequest;
import edu.miami.ccs.goma.pojos.ApprovalRequestType;
import edu.miami.ccs.goma.pojos.GeographicZone;
import edu.miami.ccs.goma.pojos.Program;
import edu.miami.ccs.goma.pojos.Project;
import edu.miami.ccs.goma.pojos.Station;
import edu.miami.ccs.goma.pojos.StatusValue;
import edu.miami.ccs.goma.pojos.User;

public class GZOperations extends HttpServlet {
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
		sdf = new SimpleDateFormat("MM/dd/yyyy");
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
	    else if(request.getParameter("mode").equals("delete"))
	    	delete();
	    else if(request.getParameter("mode").equals("fetch"))
	    	fetch();
	    else if(request.getParameter("mode").equals("update"))
	    	update();
	    else if(request.getParameter("mode").equals("list"))
	    	list();
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
	    HttpSession hs = request.getSession(true);    
	      
    	List gzList = session.createQuery("from GeographicZone gz").list();
    	
    	//Set up the data type for the JSON
    	response.setContentType("application/json");
    	//Define an output stream for the data to go
        
    	out.print("{ \"type\": \"gz\", \"hits\":"+gzList.size()+", \"data\": [");
    	for (Iterator iter = gzList.iterator(); iter.hasNext();) 
    	{
    		
    		GeographicZone gz = (GeographicZone) iter.next();
    		
    		out.print("{ \"gz_id\": \""+gz.getGzId()+"\", \"name\": \""+gz.getName()+"\", \"approval\": \""+gz.getStatusValue().getStatusValue()+"\"}");
    		if(iter.hasNext())
    			out.print(",");
    		//logger.debug(“{}”, element);
    	}
    	out.print("] }");	
	    session.close();
	}
	
	
	private static void fetch()
	{
		Session session = sf.openSession();
		session.clear();
		GeographicZone gz = (GeographicZone) session.load(GeographicZone.class, new Long(request.getParameter("gz_id")));
		
    	response.setContentType("application/json");      
    	out.print("{ \"data\": [ {");
    	out.print("\"gz_id\": \""+gz.getGzId()+"\", \"name\": \""+gz.getName()+"\", \"description\": \""+gz.getDescription()+"\", " +
    			"\"date_created\": \""+sdf.format(gz.getDateCreated())+"\", \"created_by\": \""+gz.getUserByCreatedBy().getUsername() + "\"," +
    			"\"location\": \""+gz.getLocationStr()+"\"");
    	if(gz.getUserByUpdatedBy() != null)
    		out.print(", \"date_updated\": \""+sdf.format(gz.getDateUpdated())+"\", \"updated_by\": \""+gz.getUserByUpdatedBy().getUsername()+"\"");
    	if(gz.getKeywords() != null)	
    		out.print(", \"keywords\": \""+gz.getKeywords()+"\"");
    	out.print(", \"approval\": \""+gz.getStatusValue().getStatusValue()+"\" } ] }");
	}


	private static void create()
	{
		Session session = sf.openSession();
		Date date= new Date();
		Transaction tx = null;
		HttpSession hs = request.getSession(true);
		User curr_user = (User) hs.getAttribute("curr_user"); 

		User u_creator = (User) session.load(User.class, curr_user.getUserId());

		StatusValue approval_stat = (StatusValue) session.load(StatusValue.class, new Long(Statics.PENDING));

        WKTReader fromText = new WKTReader();
        Geometry location = null;

        try {
			location = fromText.read(request.getParameter("location"));
		} catch (com.vividsolutions.jts.io.ParseException e2) {
			out.print("{  \"code\": \"failure\", \"message\": \"Save Failed - Location is not a WKT string\", \"data\": [] }");
            throw new RuntimeException("Not a WKT string:" + request.getParameter("location"));
        }

//        GeometryUserType gut = new GeometryUserType();
//        gut.assemble(location, (Object)value);
//        org.hibernate.type.Type geometryType = new CustomType(gut);
		
	    //When the project is created for the first time, the creator is the owner

        //User userByCreatedBy, StatusValue statusValue, String name, String description, String locationStr,  Date dateCreated
		GeographicZone gz = new GeographicZone(u_creator, approval_stat, request.getParameter("name"), request.getParameter("description"), request.getParameter("location"), date);

		if(request.getParameter("keywords") != null)
			gz.setKeywords(request.getParameter("keywords"));

		int update = -1;
	    try 
	    {
	    	tx = session.beginTransaction();
	    	session.save(gz);	
	    	session.flush();
			SQLQuery q = session.createSQLQuery("UPDATE aquamonitor.geographic_zone SET location = ST_GeomFromText('"+request.getParameter("location")+"',4326)  WHERE gz_id = "+gz.getGzId());
			update = q.executeUpdate();
	    	session.flush();
			ApprovalRequestType art = (ApprovalRequestType) session.load(ApprovalRequestType.class, new Long(Statics.GEOGRAPHIC_ZONE));
			ApprovalRequest ar = new ApprovalRequest(curr_user, art, curr_user, approval_stat, date, gz.getGzId());
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
	    out.print("{  \"code\": \"success\", \"message\": \"Save Successful\", \"gz_id\": \""+gz.getGzId()+"\", \"data\": [{\"update\": \""+update+"\"}] }");
	    session.close();
	}
	
	private static void update()
	{	
		Session session = sf.openSession();
		java.util.Date date= new java.util.Date();
		Transaction tx = null;
		HttpSession hs = request.getSession(true);
		User curr_user = (User) hs.getAttribute("curr_user"); 
		int update = -1;
		
		StatusValue approval_stat = (StatusValue) session.load(StatusValue.class, new Long(Statics.PENDING));
		GeographicZone gz = (GeographicZone) session.load(GeographicZone.class, new Long(request.getParameter("gz_id")));

		gz.setDescription(request.getParameter("description"));
		gz.setName(request.getParameter("name"));
		gz.setLocationStr(request.getParameter("location"));
		if(request.getParameter("keywords") != null)
			gz.setKeywords(request.getParameter("keywords"));
		
        WKTReader fromText = new WKTReader();
        Geometry location = null;

        try {
			location = fromText.read(request.getParameter("location"));
		} catch (com.vividsolutions.jts.io.ParseException e2) {
			out.print("{  \"code\": \"failure\", \"message\": \"Save Failed - Location is not valid a WKT string\", \"data\": [] }");
            return;
        } catch (IllegalArgumentException iae)
        {
        	out.print("{  \"code\": \"failure\", \"message\": \"Save Failed - Please provide matching vertices for endpoints\", \"data\": [] }");
            return;
        }
	
        
		gz.setStatusValue(approval_stat);
		gz.setDateUpdated(date);
		gz.setUserByUpdatedBy(curr_user);
	    try 
	    {
	    	tx = session.beginTransaction();
	    	session.update(gz);	
	    	SQLQuery q = session.createSQLQuery("UPDATE aquamonitor.geographic_zone SET location = ST_GeomFromText('"+request.getParameter("location")+"',4326)  WHERE gz_id = "+gz.getGzId());
			update = q.executeUpdate();
	    	session.flush();
			ApprovalRequestType art = (ApprovalRequestType) session.load(ApprovalRequestType.class, new Long(Statics.GEOGRAPHIC_ZONE));
			ApprovalRequest ar = new ApprovalRequest(curr_user, art, curr_user, approval_stat, date, gz.getGzId());
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
	    
	    out.print("{  \"code\": \"success\", \"message\": \"Save Successful\", \"gz_id\": \""+gz.getGzId()+"\", \"data\": [{\"update\": \""+update+"\"}] }");
	    session.close();
	}
	
	private static void delete()
	{
		Session session = sf.openSession();
	    Transaction tx = null;
	    String[] uList = request.getParameterValues("gz_id");

	    for(String item : uList)
	    {
	    	GeographicZone gz = (GeographicZone) session.load(GeographicZone.class, new Long(item));
		    try 
		    {
		    	tx = session.beginTransaction();
		    	session.delete(gz);
		    	tx.commit();
		    } 
		    catch (ConstraintViolationException cve)
		    {
		    	out.print("{ \"code\": \"failure\", \"message\": \"Delete Failed: Cannot delete "+ gz.getName() +" while it has dependencies assigned to it.\", \"data\": [] }");
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
    		query = "select count(gz) as count from GeographicZone gz where gz.statusValue.statusId = "+Statics.APPROVED;
	    else
	    	query = "select count(gz) as count from GeographicZone gz where gz.userByCreatedBy = "+curr_user.getUserId()+" and gz.statusValue.statusId = "+Statics.APPROVED;
		List entityList = session.createQuery(query).list();
		
		//Set up the data type for the JSON
		response.setContentType("application/json");
		//Define an output stream for the data to go
	    
		out.print("{ \"type\": \"Geographic Zone\", \"count\":\""+entityList.get(0).toString()+"\", \"data\": [] }");
	    session.close();
	}
}
