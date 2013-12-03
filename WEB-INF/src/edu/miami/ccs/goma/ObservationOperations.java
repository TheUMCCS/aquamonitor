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
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.jboss.logging.Logger;

import edu.miami.ccs.goma.pojos.ApprovalRequest;
import edu.miami.ccs.goma.pojos.ApprovalRequestType;
import edu.miami.ccs.goma.pojos.Dictionary;
import edu.miami.ccs.goma.pojos.DictionaryTerm;
import edu.miami.ccs.goma.pojos.ObservationTuple;
import edu.miami.ccs.goma.pojos.StatusValue;
import edu.miami.ccs.goma.pojos.User;

public class ObservationOperations extends HttpServlet {
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
	    
	    if(request.getParameter("mode").equals("create"))	
	    	create();
	    else if(request.getParameter("mode").equals("list"))
	    	list();
	    else if(request.getParameter("mode").equals("delete"))
	    	delete();
	    else if(request.getParameter("mode").equals("fetch"))
	    	fetch();
	    else if(request.getParameter("mode").equals("update"))
	    	update();
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
	
	private static void fetch()
	{
		Session session = sf.openSession();
		ObservationTuple ot = (ObservationTuple) session.load(ObservationTuple.class, new Long(request.getParameter("obs_tuple_id")));
		
    	response.setContentType("application/json");      
    	out.print("{ \"data\": [ {");
    	out.print("\"tuple_id\": \""+ot.getObservationTupleId()+
    			"\", \"medium\": \""+ot.getDictionaryTermByMediumId().getTerm()+
    			"\", \"category\": \""+ot.getDictionaryTermByParamCatId().getTerm()+
    			"\", \"type\": \""+ot.getDictionaryTermByParamTypeId().getTerm()+
    			"\", \"method\": \""+ot.getDictionaryTermByAnalysisMethodId().getTerm()+
    			"\", \"approval_status\": \""+ot.getStatusValue().getStatusValue()+"\", \"date_created\": \""+sdf.format(ot.getDateCreated())+"\", \"created_by\": \""+ot.getUserByCreatedBy().getUsername() + "\"");
    	if(ot.getUserByUpdatedBy() != null)
    		out.print(", \"date_updated\": \""+sdf.format(ot.getDateUpdated())+"\", \"updated_by\": \""+ot.getUserByUpdatedBy().getUsername()+"\"");
    	out.print("} ] }");
	    session.close();
	}

	private static void list()
	{
		Session session = sf.openSession();
    	List obsTupleList = session.createQuery("from ObservationTuple ot").list();
    	
    	//Set up the data type for the JSON
    	response.setContentType("application/json");
        
    	out.print("{ \"hits\":"+obsTupleList.size()+", \"data\": [");
    	for (Iterator iter = obsTupleList.iterator(); iter.hasNext();) 
    	{
    		 
    		ObservationTuple ot = (ObservationTuple) iter.next();

    		out.print("{  \"tuple_id\": \""+ot.getObservationTupleId()+"\", \"medium\": \""+ot.getDictionaryTermByMediumId().getTerm()+"\", \"category\": \""+ot.getDictionaryTermByParamCatId().getTerm()+"\", \"type\": \""+ot.getDictionaryTermByParamTypeId().getTerm()+"\", \"method\": \""+ot.getDictionaryTermByAnalysisMethodId().getTerm()+"\", \"approval_status\": \""+ot.getStatusValue().getStatusValue()+"\"}");
    		if(iter.hasNext())
    			out.print(",");
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
		StatusValue approval_stat = (StatusValue) session.load(StatusValue.class, new Long(Statics.PENDING));
		
		User u = (User) session.load(User.class, curr_user.getUserId());
		DictionaryTerm medium = (DictionaryTerm) session.load(DictionaryTerm.class, new Long(request.getParameter("medium")));
		DictionaryTerm category = (DictionaryTerm) session.load(DictionaryTerm.class, new Long(request.getParameter("category")));
		DictionaryTerm type = (DictionaryTerm) session.load(DictionaryTerm.class, new Long(request.getParameter("type")));
		DictionaryTerm method = (DictionaryTerm) session.load(DictionaryTerm.class, new Long(request.getParameter("method")));
		
		
		ObservationTuple ot = new ObservationTuple(u, medium, type, category, approval_stat, method, date);
		
	    try 
	    {
	    	tx = session.beginTransaction();
	    	session.save(ot);	
	    	session.flush();
			ApprovalRequestType art = (ApprovalRequestType) session.load(ApprovalRequestType.class, new Long(Statics.OBSERVATION_TUPLE));
			ApprovalRequest ar = new ApprovalRequest(curr_user, art, curr_user, approval_stat, date, ot.getObservationTupleId());
			ar.setComment("New Record Added");
	    	session.save(ar);
	    	tx.commit();
	    } 
	    catch (ConstraintViolationException cve)
	    {
	    	out.print("{  \"code\": \"failure\", \"message\": \"Save Failed - This is a duplicate combination\", \"data\": [] }");
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
	       	out.print("{  \"code\": \"failure\", \"message\": \""+e.getMessage()+"\", \"data\": [] }");
	       	tx.rollback();
	       	return;
	    }
	    out.print("{  \"code\": \"success\", \"message\": \"Save Successful\", \"data\": [] }");
	    session.close();
	}
	
	private static void update()
	{
		//This is not needed as of now
	}
	
	private static void delete()
	{
		//This is not needed as of now
	}
}
