package edu.miami.ccs.goma;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.hibernate.type.StandardBasicTypes;
import org.jboss.logging.Logger;

import edu.miami.ccs.goma.pojos.ApprovalRequest;
import edu.miami.ccs.goma.pojos.DictionaryTerm;
import edu.miami.ccs.goma.pojos.GeographicZone;
import edu.miami.ccs.goma.pojos.ObservationParameter;
import edu.miami.ccs.goma.pojos.ObservationTuple;
import edu.miami.ccs.goma.pojos.Person;
import edu.miami.ccs.goma.pojos.Program;
import edu.miami.ccs.goma.pojos.Project;
import edu.miami.ccs.goma.pojos.Station;
import edu.miami.ccs.goma.pojos.StatusValue;
import edu.miami.ccs.goma.pojos.User;

public class ApprovalOperations extends HttpServlet {

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
	    else if(request.getParameter("mode").equals("update"))	
	    	update();
	    else if(request.getParameter("mode").equals("statistics"))	
	    	statistics();
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
	    Query q;
	    if(request.isUserInRole("CAU"))	
	    {
	    	q = session.createQuery("from ApprovalRequest ar where ar.approvalRequestType.typeId = :reqType and (ar.statusValue.statusId = :pendingStat or ar.statusValue.statusId = :incompleteStat)");
	    	q.setParameter("pendingStat", new Long(Statics.PENDING));
	    	q.setParameter("incompleteStat", new Long(Statics.INCOMPLETE));	
	    }
	    else
	    {
	    	q = session.createQuery("from ApprovalRequest ar where ar.approvalRequestType.typeId = :reqType and ar.userByCreatedBy = :currUser");
	    	q.setParameter("currUser", curr_user);
	    }
	    if(request.getParameter("type").equals("program"))	
	    	q.setParameter("reqType", new Long(Statics.PROGRAM));
	    else if(request.getParameter("type").equals("project"))	
	    	q.setParameter("reqType", new Long(Statics.PROJECT));
	    else if(request.getParameter("type").equals("station"))	
	    	q.setParameter("reqType", new Long(Statics.STATION));
	    else if(request.getParameter("type").equals("gz"))	
	    	q.setParameter("reqType", new Long(Statics.GEOGRAPHIC_ZONE));
	    else if(request.getParameter("type").equals("obsTuple"))	
	    	q.setParameter("reqType", new Long(Statics.OBSERVATION_TUPLE));
	    else if(request.getParameter("type").equals("obsParam"))	
	    	q.setParameter("reqType", new Long(Statics.OBSERVATION_PARAMETER));
	    else if(request.getParameter("type").equals("dictionaryTerm"))	
	    	q.setParameter("reqType", new Long(Statics.DICTIONARY_TERM));
    	List prgList = q.list();
    	
    	//Set up the data type for the JSON
    	response.setContentType("application/json");
    	//Define an output stream for the data to go
        
    	out.print("{ \"type\": \"approval_request\", \"hits\":"+prgList.size()+", \"data\": [");
    	for (Iterator iter = prgList.iterator(); iter.hasNext();) 
    	{ 		
    		ApprovalRequest ar = (ApprovalRequest) iter.next();
    		
    		out.print("{ \"request_id\": \""+ar.getRequestId()+"\", \"requestor\": \""+ar.getUserByRequestorId().getUsername()+"\", " +
    				"\"date_created\": \""+ar.getDateCreated()+"\", \"approval_status\": \""+ar.getStatusValue().getStatusValue() +"\", " +
    				"\"comment\": \""+ar.getComment()+"\", \"parent_id\": \""+ar.getParentKeyValue()+"\"");
    		if(ar.getUserByApproverId() != null)
    			out.print(", \"approver\": \""+ar.getUserByApproverId().getUsername()+"\"");
    		out.print("}");
    		if(iter.hasNext())
    			out.print(",");
    		//logger.debug(“{}”, element);
    	}
    	out.print("] }");	
    	session.close();
	}
	
	private void update()
	{
		Session session = sf.openSession();
	    HttpSession hs = request.getSession(true);    
	    Transaction tx = null;
		User curr_user = (User) hs.getAttribute("curr_user"); 
		StatusValue sv = (StatusValue) session.load(StatusValue.class, new Long(request.getParameter("status_id")));

		
		//If the update request came from a lower-privileged user, reject it
	    if(!request.isUserInRole("CAU"))	
	    	return;
	    
	    String[] arList = request.getParameterValues("request_id");
	    
	    for(int i = 0; i < arList.length; i++)
	    {	    
		    //First update the approval request with the new values
		    ApprovalRequest ar = (ApprovalRequest) session.load(ApprovalRequest.class, new Long(arList[i]));

		    ar.setStatusValue(sv);
		    ar.setUserByUpdatedBy(curr_user);
		    String comment = "";
		    if(request.getParameter("comment") != null && request.getParameter("comment").length() == 0)
		    	comment = "N/A";
		    else 
		    	comment = request.getParameter("comment");
		    ar.setComment(comment);
		    
		    if(Statics.APPROVED == Integer.parseInt(request.getParameter("status_id")))
		    	ar.setUserByApproverId(curr_user);

		    try 
		    {
		    	tx = session.beginTransaction();
		    	
			    if(Statics.REJECTED == Integer.parseInt(request.getParameter("status_id")))
			    	session.delete(ar);
			    else
			    	session.update(ar);	
		    	 
			    //Now we change the actual record in the parent table
			    if(request.getParameter("type").equals("program"))	
			    {
			    	Program p = (Program) session.load(Program.class, ar.getParentKeyValue());
			    	p.setStatusValueByApprovalStatusId(sv);
			    	session.update(p);
			    }
			    else if(request.getParameter("type").equals("project"))	
			    {
			    	Project p = (Project) session.load(Project.class, ar.getParentKeyValue());
			    	p.setStatusValueByApprovalStatusId(sv);
			    	session.update(p);
			    }
			    else if(request.getParameter("type").equals("station"))	
			    {
			    	Station p = (Station) session.load(Station.class, ar.getParentKeyValue());
			    	if(Statics.REJECTED == Integer.parseInt(request.getParameter("status_id")))
			    		session.delete(p);
			    	else
			    	{
			    		p.setStatusValueByApprovalStatusId(sv);
			    		session.update(p);
			    	}
			    }
			    else if(request.getParameter("type").equals("gz"))	
			    {
			    	GeographicZone p = (GeographicZone) session.load(GeographicZone.class, ar.getParentKeyValue());
			    	p.setStatusValue(sv);
			    	session.update(p);
			    }
			    else if(request.getParameter("type").equals("obsTuple"))	
			    {
			    	ObservationTuple p = (ObservationTuple) session.load(ObservationTuple.class, ar.getParentKeyValue());
			    	p.setStatusValue(sv);
			    	session.update(p);
			    }
			    else if(request.getParameter("type").equals("obsParam"))	
			    {
			    	ObservationParameter p = (ObservationParameter) session.load(ObservationParameter.class, ar.getParentKeyValue());
			    	p.setStatusValue(sv);
			    	session.update(p);
			    }
			    else if(request.getParameter("type").equals("dictionaryTerm"))	
			    {
			    	DictionaryTerm p = (DictionaryTerm) session.load(DictionaryTerm.class, ar.getParentKeyValue());
			    	p.setStatusValue(sv);
			    	session.update(p);
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
		       	out.print("{  \"code\": \"failure\", \"message\": \""+e.getMessage()+"\", \"data\": [] }");
		       	tx.rollback();
		       	return;
		    }
	    }
	    out.print("{  \"code\": \"success\", \"message\": \"Update Successful\", \"data\": [] }");
	    session.close();
	}
	
	private void statistics()
	{   
		Session session = sf.openSession();
		HttpSession hs = request.getSession(true);
		User curr_user = (User) hs.getAttribute("curr_user"); 
	    String query = "";

	    if(request.isUserInRole("CAU"))
    		query = "select count(a.request_id) as count, art.name from aquamonitor.approval_request a, aquamonitor.approval_request_type art where a.type_id=art.type_id and a.approval_status_id="+Statics.PENDING+" group by art.name";
	    else
	    	query = "select count(a.request_id) as count, art.name from aquamonitor.approval_request a, aquamonitor.approval_request_type art where a.type_id=art.type_id and a.approval_status_id="+Statics.PENDING+" and a.created_by = "+curr_user.getUserId()+" group by art.name";
	    List<String> entityList = new ArrayList<String>();
	    SQLQuery sqlQuery = session.createSQLQuery(query).addScalar("count", StandardBasicTypes.STRING).addScalar("name", StandardBasicTypes.STRING);
		entityList = sqlQuery.list();
		
		//Set up the data type for the JSON
		response.setContentType("application/json");
		//Define an output stream for the data to go
	    
		out.print("{ \"type\": \"approvals\", \"statistics\":[");
		Iterator it = entityList.iterator();
    	while(it.hasNext()) 
    	{
    		Object[] tmp = (Object[])it.next();
    		out.print("{\"count\": \""+(String)tmp[0]+"\", \"name\": \""+(String)tmp[1]+"\"}");
    		if(it.hasNext())
    			out.print(",");
    	}
		out.print("] }");	
		session.close();
	}
}
