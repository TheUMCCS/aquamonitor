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

import org.hibernate.FlushMode;
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
import edu.miami.ccs.goma.pojos.Organization;
import edu.miami.ccs.goma.pojos.Project;
import edu.miami.ccs.goma.pojos.Station;
import edu.miami.ccs.goma.pojos.StationProject;
import edu.miami.ccs.goma.pojos.StationProjectId;
import edu.miami.ccs.goma.pojos.StatusValue;
import edu.miami.ccs.goma.pojos.User;

public class StationOperations extends HttpServlet {
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
	    else if(request.getParameter("mode").equals("link"))
	    	link();
	    else if(request.getParameter("mode").equals("deleteLinked"))
	    	deleteLinked();
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
		session.clear();

	    HttpSession hs = request.getSession(true);
		Project p = (Project) session.load(Project.class, new Long(request.getParameter("project_id")));

    	Set<Station> stationList = p.getStations();
    	
    	//Set up the data type for the JSON
    	response.setContentType("application/json");
    	//Define an output stream for the data to go
        
    	out.print("{ \"type\": \"station\", \"hits\":"+stationList.size()+", \"data\": [");
    	for (Iterator iter = stationList.iterator(); iter.hasNext();) 
    	{   		
    		Station s = (Station) iter.next();
    		
    		out.print("{ \"station_id\": \""+s.getStationId()+"\", \"name\": \""+s.getName()+"\", \"status\": \""+s.getStatusValueByStatusId().getStatusValue()+"\", \"website\": \""+p.getWebsite()+"\"}");
    		if(iter.hasNext())
    			out.print(",");
    	}
    	out.print("] }");
    	 session.close();
	}
	
	
	private static void fetch()
	{
		Session session = sf.openSession();
		session.clear();
		Station s = (Station) session.load(Station.class, new Long(request.getParameter("station_id")));
		
    	response.setContentType("application/json");      
    	out.print("{ \"data\": [ {");
    	out.print("\"station_id\": \""+s.getStationId()+"\", \"name\": \""+s.getName()+"\", \"description\": \""+s.getDescription()+"\", " +
    			"\"website\": \""+s.getWebsite()+"\", \"date_created\": \""+sdf.format(s.getDateCreated())+"\", \"created_by\": \""+s.getUserByCreatedBy().getUsername() + "\"," +
    			"\"organization\": \""+s.getOrganization().getName()+"\", \"project\": \""+s.getProject().getName()+"\", \"program\": \""+s.getProgram().getName()+"\", \"location\": \""+s.getLocationStr()+"\"");
    	if(s.getKeywords() != null)	
    		out.print(", \"keywords\": \""+s.getKeywords()+"\"");
    	if(s.getStartDate() != null)
    		out.print(", \"start_date\": \""+sdf.format(s.getStartDate())+"\", \"end_date\": \""+sdf.format(s.getEndDate())+"\"");
    	if(s.getUserByUpdatedBy() != null)
    		out.print(", \"date_updated\": \""+sdf.format(s.getDateUpdated())+"\", \"updated_by\": \""+s.getUserByUpdatedBy().getUsername()+"\"");
    	out.print(",\"status\": \""+s.getStatusValueByStatusId().getStatusValue()+"\",\"status_id\": \""+s.getStatusValueByStatusId().getStatusId()+"\", \"approval\": \""+s.getStatusValueByApprovalStatusId().getStatusValue()+"\" } ] }");
    	 session.close();
	}


	private static void create()
	{
		Session session = sf.openSession();
		session.setFlushMode(FlushMode.MANUAL);
		session.flush();
		Date date= new Date();
		Transaction tx = null;
		HttpSession hs = request.getSession(true);
		User curr_user = (User) hs.getAttribute("curr_user"); 
		Project prj = (Project) session.load(Project.class, new Long(request.getParameter("project_id")));
		

		Date startDate = null;
		Date endDate = null;
		

		try {
			if(request.getParameter("start_date") != null)
				startDate = sdf.parse(request.getParameter("start_date"));
			if(request.getParameter("end_date") != null)
				endDate = sdf.parse(request.getParameter("end_date"));
		} catch (ParseException pe) {
			
			pe.printStackTrace();
		}
		
		User u_creator = (User) session.load(User.class, curr_user.getUserId());
		Organization o = u_creator.getOrganization();
		StatusValue station_stat = (StatusValue) session.load(StatusValue.class, new Long(request.getParameter("status_id")));
		StatusValue approval_stat = (StatusValue) session.load(StatusValue.class, new Long(Statics.PENDING));

        WKTReader fromText = new WKTReader();
        Geometry location = null;

        try {
			location = fromText.read(request.getParameter("location"));
		} catch (com.vividsolutions.jts.io.ParseException e2) {
			out.print("{  \"code\": \"failure\", \"message\": \"Save Failed - Location is not a WKT string\", \"data\": [] }");
            return;
        }

		
	    //When the project is created for the first time, the creator is the owner

		Station s = new Station(u_creator, prj, prj.getProgram(), prj.getProgram().getOrganization(), approval_stat, station_stat, 
				request.getParameter("name"), request.getParameter("description"), date, request.getParameter("location"));

		if(request.getParameter("website") != null)
			s.setWebsite(request.getParameter("website"));
		if(request.getParameter("start_date") != null)
			s.setStartDate(startDate);
		if(request.getParameter("end_date") != null)
			s.setEndDate(endDate);
		if(request.getParameter("keywords") != null)
			s.setKeywords(request.getParameter("keywords"));

		int update = -1;
	    try 
	    {
	    	tx = session.beginTransaction();
	    	session.flush();
	    	session.save(s);	

			SQLQuery q = session.createSQLQuery("UPDATE aquamonitor.station SET location = ST_GeomFromText('"+request.getParameter("location")+"',4326)  WHERE station_id = "+s.getStationId());
			update = q.executeUpdate();
			ApprovalRequestType art = (ApprovalRequestType) session.load(ApprovalRequestType.class, new Long(Statics.STATION));
			ApprovalRequest ar = new ApprovalRequest(curr_user, art, curr_user, approval_stat, date, s.getStationId());
			ar.setComment("New Record Added");
	    	session.save(ar);
	    	
	    	StationProjectId spid = new StationProjectId(prj.getProjectId(), s.getStationId());
	    	StationProject sp = new StationProject(spid, s, prj);
	    	session.save(sp);
	    	session.flush();
		    session.clear();
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
	    out.print("{  \"code\": \"success\", \"message\": \"Save Successful\", \"station_id\": \""+s.getStationId()+"\", \"data\": [{\"update\": \""+update+"\"}] }");

	    session.close();
	}
	
	private static void update()
	{	
		Session session = sf.openSession();
		java.util.Date date= new java.util.Date();
		Transaction tx = null;
		HttpSession hs = request.getSession(true);
		User curr_user = (User) hs.getAttribute("curr_user"); 
		Date startDate = null;
		Date endDate = null;
		int update = -1;
		StatusValue approval_stat = (StatusValue) session.load(StatusValue.class, new Long(Statics.PENDING));
		
		Station s = (Station) session.load(Station.class, new Long(request.getParameter("station_id")));
		if(!request.isUserInRole("CAU") && (curr_user.getOrganization().getOrganizationId() != s.getOrganization().getOrganizationId()))
		{
			out.print("{  \"code\": \"failure\", \"message\": \"Access Violation: Permission Denied.\", \"data\": [] }");
			return;
		}
		
		try {
			startDate = sdf.parse(request.getParameter("start_date"));
			endDate = sdf.parse(request.getParameter("end_date"));
		} catch (ParseException pe) {			
			pe.printStackTrace();
		}
		
        WKTReader fromText = new WKTReader();
        Geometry location = null;

        try {
			location = fromText.read(request.getParameter("location"));
		} catch (com.vividsolutions.jts.io.ParseException e2) {
			out.print("{  \"code\": \"failure\", \"message\": \"Save Failed - Location is not a WKT string\", \"data\": [] }");
            return;
        }

        
		if(request.getParameter("website") != null)
			s.setWebsite(request.getParameter("website"));
		if(request.getParameter("start_date") != null)
			s.setStartDate(startDate);		
		if(request.getParameter("end_date") != null)
			s.setEndDate(endDate);
		if(request.getParameter("status_id") != null)
			s.setStatusValueByStatusId((StatusValue) session.load(StatusValue.class, new Long(request.getParameter("status_id"))));
		if(request.getParameter("keywords") != null)
			s.setKeywords(request.getParameter("keywords"));
		
		s.setDescription(request.getParameter("description"));
		s.setName(request.getParameter("name"));
		s.setLocationStr(request.getParameter("location"));
		
	
		s.setDateUpdated(date);
		s.setUserByUpdatedBy(curr_user);
		s.setStatusValueByApprovalStatusId(approval_stat);
		
	    try 
	    {
	    	tx = session.beginTransaction();
	    	session.update(s);	
	    	SQLQuery q = session.createSQLQuery("UPDATE aquamonitor.station SET location = ST_GeomFromText('"+request.getParameter("location")+"',4326)  WHERE station_id = "+s.getStationId());
			update = q.executeUpdate();
	    	session.flush();
			ApprovalRequestType art = (ApprovalRequestType) session.load(ApprovalRequestType.class, new Long(Statics.STATION));
			ApprovalRequest ar = new ApprovalRequest(curr_user, art, curr_user, approval_stat, date, s.getStationId());
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
	    out.print("{  \"code\": \"success\", \"message\": \"Save Successful\", \"station_id\": \""+s.getStationId()+"\", \"data\": [{\"update\": \""+update+"\"}] }");
	    session.close();
	}
	
	private static void delete()
	{
		Session session = sf.openSession();
	    Transaction tx = null;
	    String[] uList = request.getParameterValues("station_id");

	    for(String item : uList)
	    {
	    	Station s = (Station) session.load(Station.class, new Long(item));
		    try 
		    {
		    	tx = session.beginTransaction();
		    	session.delete(s);
		    	tx.commit();
		    } 
		    catch (ConstraintViolationException cve)
		    {
		    	out.print("{ \"code\": \"failure\", \"message\": \"Delete Failed: Cannot delete "+ s.getName() +" while it has observations assigned to it.\", \"data\": [] }");
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
	
	private static void link()
	{
		Session session = sf.openSession();
		Transaction tx = null;
		HttpSession hs = request.getSession(true);
		User curr_user = (User) hs.getAttribute("curr_user"); 
		Project prj = (Project) session.load(Project.class, new Long(request.getParameter("project_id")));

		User u_creator = (User) session.load(User.class, curr_user.getUserId());
		String[] station_ids = request.getParameterValues("station_id");
	    //When the project is created for the first time, the creator is the owner
	    try 
	    {
	    	tx = session.beginTransaction();
	    	
	    	for(int i = 0; i < station_ids.length; i++)
	    	{
	    		Station s = (Station) session.load(Station.class, new Long(station_ids[i]));
		    	StationProjectId spid = new StationProjectId(prj.getProjectId(), s.getStationId());
		    	StationProject sp = new StationProject(spid, s, prj);
		    	session.save(sp);
	    	}
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
	
	private static void deleteLinked()
	{
		Session session = sf.openSession();
	    Transaction tx = null;

    	Station s = (Station) session.load(Station.class, new Long(request.getParameter("station_id")));
		Project prj = (Project) session.load(Project.class, new Long(request.getParameter("project_id")));
    	StationProjectId spid = new StationProjectId(s.getStationId(), prj.getProjectId());
    	StationProject sp = (StationProject) session.load(StationProject.class, spid);
	    String query = "delete from aquamonitor.station_project sp where sp.station_id = "+request.getParameter("station_id")+"and sp.program_id = "+request.getParameter("project_id");
	    int dIndex = -1;
	     	
    	try 
	    {
	    	tx = session.beginTransaction();
	    	dIndex = session.createSQLQuery(query).executeUpdate();
	    	tx.commit();
	    } 
	    catch (ConstraintViolationException cve)
	    {
	    	out.print("{ \"code\": \"failure\", \"message\": \"Delete Failed: Cannot delete "+ s.getName() +" while it has dependencies assigned to it.\", \"data\": [] }");
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
    		query = "select count(s) as count from Station s where s.statusValueByApprovalStatusId.statusId = "+Statics.APPROVED;
	    else
	    	query = "select count(s) as count from Station s where s.userByCreatedBy = "+curr_user.getUserId()+" and s.statusValueByApprovalStatusId.statusId = "+Statics.APPROVED;
		List entityList = session.createQuery(query).list();
		
		//Set up the data type for the JSON
		response.setContentType("application/json");
		//Define an output stream for the data to go
	    
		out.print("{ \"type\": \"station\", \"count\":\""+entityList.get(0).toString()+"\", \"data\": [] }");	
		 session.close();
	}
	
}
