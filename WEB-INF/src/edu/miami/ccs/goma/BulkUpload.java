package edu.miami.ccs.goma;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.jboss.logging.Logger;

import edu.miami.ccs.goma.pojos.ApprovalRequest;
import edu.miami.ccs.goma.pojos.ApprovalRequestType;
import edu.miami.ccs.goma.pojos.ObservationParameter;
import edu.miami.ccs.goma.pojos.Organization;
import edu.miami.ccs.goma.pojos.Station;
import edu.miami.ccs.goma.pojos.StationProject;
import edu.miami.ccs.goma.pojos.StationProjectId;
import edu.miami.ccs.goma.pojos.StatusValue;
import edu.miami.ccs.goma.pojos.User;

 public class BulkUpload extends HttpServlet {


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
	
   
   public void doPost(HttpServletRequest request, HttpServletResponse response)
       throws ServletException, IOException 
   {
		Session session = connect().openSession();
		session.setFlushMode(FlushMode.MANUAL);
    	clearSession(session);
		Date date= new Date();
		Transaction tx = null;
		response.setContentType("text/plain");
		Station template_stn = null;
		HttpSession hs = request.getSession(true);
		User curr_user = (User) hs.getAttribute("curr_user"); 
		User u_creator = (User) session.load(User.class, curr_user.getUserId());
		Organization o = u_creator.getOrganization();
		StatusValue approval_stat = (StatusValue) session.load(StatusValue.class, new Long(Statics.PENDING));
		List<String[]> colsList = new ArrayList<String[]>();
		
	    try 
	    {
	    	out = response.getWriter();
	    }
	    catch(IOException e)
	    {
	    	logger.debug("Error opening output stream using PrintWriter");
	    }
	   
	   try {

	    
	    if ( ServletFileUpload.isMultipartContent( request ))
	    {
	    	// Create a factory for disk-based file items
	    	FileItemFactory factory = new DiskFileItemFactory();

	    	// Create a new file upload handler
	    	ServletFileUpload upload = new ServletFileUpload(factory);
	    	
	        List<FileItem> items = upload.parseRequest( request );

	     // Process the uploaded items
	        Iterator iter = items.iterator();
	        while (iter.hasNext()) {
	            FileItem item = (FileItem) iter.next();

	            if (item.isFormField()) 
	            {
	            	if(item.getFieldName().equals("station_id"))
	            	{
	            		template_stn = (Station) session.load(Station.class, new Long(item.getString()));
	            	}
	            } else 
	            {
	            	InputStream stream = item.getInputStream();
					BufferedReader br = new BufferedReader(new InputStreamReader(stream));
					String line = null;
					line = br.readLine();
					String[] headers = line.split(",");
					
					while ((line = br.readLine()) != null)
					{
						String[] cols = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -2);

						colsList.add(cols);
						
						//out.println(colsList.get(0)[0]+", "+colsList.get(0)[1]+", "+colsList.get(0)[2]);
					}
					br.close();
	            }
	        }
	        String tmpltProjName = template_stn.getProject().getName();
	        long tmpltProjId = template_stn.getProject().getProjectId();
	        for(int i = 0; i < colsList.size(); i++)
	        {
	        	Station s = new Station();
	        	try
	        	{
	        		s = new Station(u_creator, template_stn.getProject(), template_stn.getProgram(), template_stn.getOrganization(), approval_stat, template_stn.getStatusValueByStatusId(), 
						colsList.get(i)[0], colsList.get(i)[1], date, colsList.get(i)[2], colsList.get(i)[3], sdf.parse(colsList.get(i)[4]), sdf.parse(colsList.get(i)[5]));
	        	}
	        	catch(ParseException pe)
	        	{
	        		s = new Station(u_creator, template_stn.getProject(), template_stn.getProgram(), template_stn.getOrganization(), approval_stat, template_stn.getStatusValueByStatusId(), 
							colsList.get(i)[0], colsList.get(i)[1], date, colsList.get(i)[2], colsList.get(i)[3], null, null);
	        	}
				int update = -1;
			    try 
			    {
			    	tx = session.beginTransaction();
			    	session.save(s);
			    	session.flush();
					SQLQuery q = session.createSQLQuery("UPDATE aquamonitor.station SET location = ST_GeomFromText('"+colsList.get(i)[2]+"',4326)  WHERE station_id = "+s.getStationId());
					update = q.executeUpdate();

					ApprovalRequestType art = (ApprovalRequestType) session.load(ApprovalRequestType.class, new Long(Statics.STATION));
					ApprovalRequest ar = new ApprovalRequest(curr_user, art, curr_user, approval_stat, date, s.getStationId());
					ar.setComment("New Record Added");
			    	session.save(ar);
			    	
			    	StationProjectId spid = new StationProjectId(template_stn.getProject().getProjectId(), s.getStationId());
			    	StationProject sp = new StationProject(spid, s, template_stn.getProject());
			    	session.save(sp);
			    	
			    	Set<ObservationParameter> opList = template_stn.getObservationParameters();
			    	for (Iterator<ObservationParameter> it = opList.iterator(); it.hasNext();) 
			    	{
			    		ObservationParameter op = (ObservationParameter) it.next();
			    		ObservationParameter temp = new ObservationParameter(u_creator, op.getDictionaryTermBySamplingFreqId(), op.getObservationTuple(), s, approval_stat, date);
			    		temp.setDictionaryTermBySamplingDepthId(op.getDictionaryTermBySamplingDepthId());
			    		session.save(temp);
			    		session = clearSession(session);
			    	}
			    	session = clearSession(session);
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
	        }
	        response.sendRedirect("/goma/admin/stations?project_id="+tmpltProjId+"&project_name="+tmpltProjName);
	    }
     } catch (Exception ex) {
       throw new ServletException(ex);
     }
	 session.close();  
   }



private Session clearSession(Session session)
{
	session.flush();
	session.clear();
	return session;
}
 }