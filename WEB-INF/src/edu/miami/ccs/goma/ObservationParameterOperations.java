package edu.miami.ccs.goma;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

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
import edu.miami.ccs.goma.pojos.DictionaryTerm;
import edu.miami.ccs.goma.pojos.ObservationParameter;
import edu.miami.ccs.goma.pojos.ObservationTuple;
import edu.miami.ccs.goma.pojos.Station;
import edu.miami.ccs.goma.pojos.StatusValue;
import edu.miami.ccs.goma.pojos.User;

public class ObservationParameterOperations extends HttpServlet {
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
		ObservationParameter op = (ObservationParameter) session.load(ObservationParameter.class, new Long(request.getParameter("obs_param_id")));

    	response.setContentType("application/json");      
    	out.print("{ \"data\": [ {");
    	out.print("\"obs_param_id\": \""+op.getObservationParamId()+
    			"\", \"sampling_freq\": \""+op.getDictionaryTermBySamplingFreqId().getTerm());
    	if(op.getDictionaryTermBySamplingDepthId() != null)
    		out.print("\", \"sampling_depth\": \""+op.getDictionaryTermBySamplingDepthId().getTerm());
    	out.print("\", \"medium\": \""+op.getObservationTuple().getDictionaryTermByMediumId().getTerm()+
    			"\", \"category\": \""+op.getObservationTuple().getDictionaryTermByParamCatId().getTerm()+
    			"\", \"type\": \""+op.getObservationTuple().getDictionaryTermByParamTypeId().getTerm()+
    			"\", \"method\": \""+op.getObservationTuple().getDictionaryTermByAnalysisMethodId().getTerm()+
    			"\", \"organization\": \""+op.getStation().getProgram().getOrganization().getName()+
    			"\", \"program\": \""+op.getStation().getProgram().getName()+
    			"\", \"project\": \""+op.getStation().getProject().getName()+
    			"\", \"station\": \""+op.getStation().getName()+
    			"\", \"approval_status\": \""+op.getStatusValue().getStatusValue()+"\"");
    	if(op.getStartDate() != null)
    		out.print(",\"start_date\": \""+sdf.format(op.getStartDate())+"\", \"end_date\": \""+sdf.format(op.getEndDate())+"\"");
    	out.print("} ] }");
	    session.close();
	}

	private static void list()
	{
		Session session = sf.openSession();
	    session.clear();
    	Station s = (Station) session.load(Station.class, new Long(request.getParameter("station_id")));
    	Set<ObservationParameter> obsParamList = s.getObservationParameters();
    	//Set up the data type for the JSON
    	response.setContentType("application/json");
        
    	out.print("{ \"hits\":"+obsParamList.size()+", \"data\": [");
    	for (Iterator iter = obsParamList.iterator(); iter.hasNext();) 
    	{
    		 
    		ObservationParameter op = (ObservationParameter) iter.next();

    		out.print("{  \"obs_param_id\": \""+op.getObservationParamId()+
    				"\", \"medium\": \""+op.getObservationTuple().getDictionaryTermByMediumId().getTerm()+
    				"\", \"category\": \""+op.getObservationTuple().getDictionaryTermByParamCatId().getTerm()+
    				"\", \"type\": \""+op.getObservationTuple().getDictionaryTermByParamTypeId().getTerm()+
    				"\", \"method\": \""+op.getObservationTuple().getDictionaryTermByAnalysisMethodId().getTerm()+
    				"\", \"sampling_freq\": \""+op.getDictionaryTermBySamplingFreqId().getTerm()+
    				"\", \"approval_status\": \""+op.getStatusValue().getStatusValue()+"\"}");
    		if(iter.hasNext())
    			out.print(",");
    	}
    	out.print("] }");
	    session.close();
	}
	
	
	private static void create()
	{
		Session session = sf.openSession();
		Date date= new Date();
		Date startDate = null;
		Date endDate = null;
		Transaction tx = null;
		HttpSession hs = request.getSession(true);
		User curr_user = (User) hs.getAttribute("curr_user"); 
		StatusValue approval_stat = (StatusValue) session.load(StatusValue.class, new Long(Statics.PENDING));
		
		User u = (User) session.load(User.class, curr_user.getUserId());
		DictionaryTerm sampling_freq = (DictionaryTerm) session.load(DictionaryTerm.class, new Long(request.getParameter("sampling_frequency_id")));
		Station s = (Station) session.load(Station.class, new Long(request.getParameter("station_id")));
		ObservationTuple ot = (ObservationTuple) session.load(ObservationTuple.class, new Long(request.getParameter("tuple_id")));
		
		ObservationParameter op = new ObservationParameter(u, sampling_freq, ot, s, approval_stat, date);

		if(request.getParameter("sampling_depth_id") != null)
			op.setDictionaryTermBySamplingDepthId((DictionaryTerm) session.load(DictionaryTerm.class, new Long(request.getParameter("sampling_depth_id"))));
		try 
		{
			if(request.getParameter("start_date") != null)
			{
				startDate = sdf.parse(request.getParameter("start_date"));
				op.setStartDate(startDate);
			}
			if(request.getParameter("end_date") != null)
			{
				endDate = sdf.parse(request.getParameter("end_date"));
				op.setEndDate(endDate);	
			}
		} 
		catch (ParseException pe) {
			pe.printStackTrace();
		}
		
		
	    try 
	    {
	    	tx = session.beginTransaction();
	    	session.save(op);	
	    	session.flush();
			ApprovalRequestType art = (ApprovalRequestType) session.load(ApprovalRequestType.class, new Long(Statics.OBSERVATION_PARAMETER));
			ApprovalRequest ar = new ApprovalRequest(curr_user, art, curr_user, approval_stat, date, op.getObservationParamId());
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
	    out.print("{  \"code\": \"success\", \"message\": \"Save Successful\", \"obs_param_id\": \""+op.getObservationParamId()+"\", \"data\": [] }");
	    session.close();
	}
	
	private static void update()
	{
		Session session = sf.openSession();
		java.util.Date date= new java.util.Date();
		Date startDate = null;
		Date endDate = null;
		Transaction tx = null;
		HttpSession hs = request.getSession(true);
		User curr_user = (User) hs.getAttribute("curr_user"); 
		
		User u = (User) session.load(User.class, curr_user.getUserId());
		StatusValue approval_stat = (StatusValue) session.load(StatusValue.class, new Long(Statics.PENDING));
		
		ObservationParameter op = (ObservationParameter) session.load(ObservationParameter.class, new Long(request.getParameter("obs_param_id")));
		op.setUserByUpdatedBy(u);
		op.setDateUpdated(date);
		op.setStatusValue(approval_stat);
		try 
		{
			if(request.getParameter("start_date") != null)
			{
				startDate = sdf.parse(request.getParameter("start_date"));
				op.setStartDate(startDate);
			}
			if(request.getParameter("end_date") != null)
			{
				endDate = sdf.parse(request.getParameter("end_date"));
				op.setEndDate(endDate);	
			}
		}
		catch (ParseException pe) {
			pe.printStackTrace();
		}
		if(request.getParameter("sampling_depth_id") != null)
			op.setDictionaryTermBySamplingDepthId((DictionaryTerm) session.load(DictionaryTerm.class, new Long(request.getParameter("sampling_depth_id"))));
		
	    try 
	    {
	    	tx = session.beginTransaction();
	    	session.update(op);	
	    	session.flush();
			ApprovalRequestType art = (ApprovalRequestType) session.load(ApprovalRequestType.class, new Long(Statics.OBSERVATION_PARAMETER));
			ApprovalRequest ar = new ApprovalRequest(curr_user, art, curr_user, approval_stat, date, op.getObservationParamId());
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
	    out.print("{  \"code\": \"success\", \"message\": \"Save Successful\", \"obs_param_id\": \""+op.getObservationParamId()+"\", \"data\": [] }");
	    session.close();
	}
	
	private static void delete()
	{
		Session session = sf.openSession();
	    Transaction tx = null;
	    String[] uList = request.getParameterValues("obs_param_id");

	    for(String item : uList)
	    {
	    	ObservationParameter op = (ObservationParameter) session.load(ObservationParameter.class, new Long(item));
		    try 
		    {
		    	tx = session.beginTransaction();
		    	session.delete(op);
		    	tx.commit();
		    } 
		    catch (ConstraintViolationException cve)
		    {
		    	out.print("{ \"code\": \"failure\", \"message\": \"Delete Failed: Cannot delete "+ op.getObservationParamId() +" while it has data assigned to it.\", \"data\": [] }");
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
}
