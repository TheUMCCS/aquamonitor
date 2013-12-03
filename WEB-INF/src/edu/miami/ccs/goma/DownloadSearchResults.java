/**
 * 
 */
package edu.miami.ccs.goma;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.jboss.logging.Logger;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

import edu.miami.ccs.goma.pojos.DictionaryTerm;
import edu.miami.ccs.goma.pojos.ObservationParameter;
import edu.miami.ccs.goma.pojos.Station;

/**
 * @author Sreeharsha Venkatapuram, UM Center for Computational Science
 */
public class DownloadSearchResults extends HttpServlet
{
	private static final int FILE_LENGTH = 20*1024*1024; //limit file length to 20MB 
	private static ServiceRegistry serviceRegistry;
	private static SessionFactory sf;
	private static Session session;
	private static PrintWriter out;
	private static HttpServletResponse response;
	private static HttpServletRequest request;
	private static Logger logger;
	private ServletConfig config;

	public void init(ServletConfig config) throws ServletException
	{
		this.config = config;
		sf = connect();
		session = sf.openSession();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		this.request = req;
		this.response = resp;
		downloadSearchResults();
	}

	private void downloadSearchResults() throws IOException
	{
		HttpSession hs = request.getSession(true);
		WKTReader fromText = new WKTReader();
		Geometry location = null;
		boolean tupleQuery = false;

		//Setting up file name to download results into
		String extension = ".csv";
		String delim = ",";
		String filename = "searchResults";
		Calendar cal = Calendar.getInstance();
		cal.getTimeInMillis();
		filename = filename + cal.getTimeInMillis() + extension;
		
		//Set up ServletOutputStream to write the file to
		ServletOutputStream sos = response.getOutputStream();
		ServletContext context = config.getServletContext();
		String mimetype = context.getMimeType(filename);

		//Set up response to write to a file
		response.setContentType((mimetype != null) ? mimetype : "application/octet-stream");
		response.setContentLength(FILE_LENGTH);
		response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

		String query = "select distinct s.organization.name as org_name,s.organization.organizationType.type as org_type," +
				"s.program.name as program_name,s.program.statusValueByStatusId.statusValue as program_status,s.program.programManager.person.firstName as program_mgr_first_name," +
				"s.program.programManager.person.lastName as program_mgr_last_name,s.program.programManager.person.email as program_mgr_email," +
				"s.project.name as project_name,s.project.statusValueByStatusId.statusValue as project_status,s.project.projectManager.person.firstName as project_mgr_first_name," +
				"s.project.projectManager.person.lastName as project_mgr_last_name,s.project.projectManager.person.email as project_mgr_email," +
				"s.project.dictionaryTermByProjectMethodologyId.term as project_methodology,s.project.dataDistributor.person.firstName as data_distributor_first_name," +
				"s.project.dataDistributor.person.lastName as data_distributor_last_name,s.project.dataDistributor.person.email as data_distributor_email," +
				"s.project.dictionaryTermByAvailabilityId.term as availability,s.project.dataLinkWebsite as data_website,s.project.dictionaryTermByPurposeCategoryId.term as purpose_category," +
				"s.project.dataQualityObj as data_quality_objective,s.project.startDate as project_start_date,s.project.endDate as project_end_date," +
				"s.name as station_name,s.locationStr as location_as_WKT,s.statusValueByStatusId.statusValue as station_status,s.website as station_website," +
				"s.startDate as station_start_date,s.endDate as station_end_date,s.stationId as station_id from Station s";

		query += " where s.statusValueByApprovalStatusId.statusId = " + Statics.APPROVED;

		if (request.getParameter("stationList") != null)
			query += " and s.stationId in (" + request.getParameter("stationList") + ")";
		
		//Print column aliases from query on to the header of CSV
		String[] aliases = session.createQuery(query).getReturnAliases();
		for(int j = 0; j < aliases.length; j++)
		{
			sos.print(aliases[j]);
			sos.print(",");
		}
		sos.print("medium,observation_category,observation_type,analysis_method,sampling_frequency,sampling_depth,observation_start_date,observation_end_date");
		sos.println();
		List results = new ArrayList();
		results.clear();
		results = session.createQuery(query).list();
		for (Iterator iter = results.iterator(); iter.hasNext();)
		{
			Object[] row = (Object[])iter.next();
			Long stationId = (Long) row[row.length-1];
			Station s = (Station) session.load(Station.class, stationId);
	    	Set<ObservationParameter> obsParamList = s.getObservationParameters();
	    	if(obsParamList.size() > 0)
		    	for (Iterator it = obsParamList.iterator(); it.hasNext();) 
		    	{
		    		for(int j=0; j < row.length; j++)
					{
		    			String col = "undefined";
		    			if(row[j] != null)
		    				col = row[j].toString();
	    				sos.print(col);
						sos.print(",");
					}
		    		ObservationParameter op = (ObservationParameter) it.next();
		    		DictionaryTerm dt = op.getDictionaryTermBySamplingDepthId();
		    		String samplingDepth = "undefined";
		    		if(dt != null)
		    		{
		    			samplingDepth = dt.getTerm();
		    		}
		    		String medium = op.getObservationTuple().getDictionaryTermByMediumId().getTerm();
		    		String category = op.getObservationTuple().getDictionaryTermByParamCatId().getTerm();
		    		String paramType = op.getObservationTuple().getDictionaryTermByParamTypeId().getTerm();
		    		String analysisMethod = op.getObservationTuple().getDictionaryTermByAnalysisMethodId().getTerm();
		    		String sampFreq = op.getDictionaryTermBySamplingFreqId().getTerm();
		    		String sDate = "undefined";
		    		String eDate = "undefined";
		    		Date obsStartDate = op.getStartDate();
		    		Date obsEndDate = op.getEndDate();
		    		if(obsStartDate != null)
		    			sDate = obsStartDate.toString();
		    		if(obsEndDate != null)
		    			eDate = obsEndDate.toString();
		    		
		    		sos.print(medium+","+category+","+paramType+","+analysisMethod+","+sampFreq+","+samplingDepth+","+sDate+","+eDate);
		    		if(it.hasNext())
		    			sos.println();
		    	}
	    	else //if a station doesn't have observation parameters, populate only station details
	    	{
	    		for(int j=0; j < row.length; j++)
				{
	    			String col = "undefined";
	    			if(row[j] != null)
	    				col = row[j].toString();
    				sos.print(col);
					if(j < row.length-1)
						sos.print(",");
				}
	    	}
			
			if (iter.hasNext())
				sos.println();
		}
		sos.close();
	}

	/**
	 * Called by the init(...) method to generate hibernate Session object
	 * 
	 * @return Hibernate SessionFactory object
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
