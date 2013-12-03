package edu.miami.ccs.goma;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
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
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.jboss.logging.Logger;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import edu.miami.ccs.goma.pojos.Dictionary;
import edu.miami.ccs.goma.pojos.DictionaryTerm;
import edu.miami.ccs.goma.pojos.GeographicZone;
import edu.miami.ccs.goma.pojos.ObservationParameter;
import edu.miami.ccs.goma.pojos.ObservationTuple;
import edu.miami.ccs.goma.pojos.Organization;
import edu.miami.ccs.goma.pojos.OrganizationType;
import edu.miami.ccs.goma.pojos.Program;
import edu.miami.ccs.goma.pojos.Project;
import edu.miami.ccs.goma.pojos.Station;
import edu.miami.ccs.goma.pojos.StationProject;
import edu.miami.ccs.goma.pojos.StatusType;
import edu.miami.ccs.goma.pojos.StatusValue;

public class Utilities extends HttpServlet {

	private static ServiceRegistry serviceRegistry;
	private static SessionFactory sf; 
	private static PrintWriter out;
	private static HttpServletResponse response;
	private static HttpServletRequest request;
	private static Logger logger;
	private static SimpleDateFormat sdf;
	private static DecimalFormat df;
	
	public void init(ServletConfig config) throws ServletException
	{
		sf = connect();
		sdf = new SimpleDateFormat("MM/dd/yyyy");
		df = new DecimalFormat("#.######");
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
	    if(request.getParameter("mode").equals("listOrganizations"))		  
	    	listOrganizations();
	    else if(request.getParameter("mode").equals("listOrganizationTypes"))	
	    	listOrganizationTypes();
	    else if(request.getParameter("mode").equals("listPrograms"))	
	    	listPrograms();
	    else if(request.getParameter("mode").equals("listProjects"))	
	    	listProjects();
	    else if(request.getParameter("mode").equals("listStations"))	
	    	listStations();
	    else if(request.getParameter("mode").equals("listStationsByGZ"))	
	    	listStationsByGZ();
	    else if(request.getParameter("mode").equals("listStationsByAdHocGZ"))	
	    	listStationsByAdHocGZ();
	    else if(request.getParameter("mode").equals("listLinkedStations"))
	    	listLinkedStations();
	    else if(request.getParameter("mode").equals("listGZ"))	
	    	listGZ();
	    else if(request.getParameter("mode").equals("listTuples"))	
	    	listTuples();
	    else if(request.getParameter("mode").equals("listUserRoles"))	
	    	listUserRoles();
	    else if(request.getParameter("mode").equals("listApprovals"))	
	    	listApprovals();
	    else if(request.getParameter("mode").equals("listStatusValues"))	
	    	listStatusValues();
	    else if(request.getParameter("mode").equals("listDictionaryTerms"))	
	    	listDictionaryTerms();
	    else if(request.getParameter("mode").equals("fetchStation"))	
	    	fetchStation();
	    else if(request.getParameter("mode").equals("fetchOrganization"))	
	    	fetchOrganization();
	    else if(request.getParameter("mode").equals("fetchProgram"))	
	    	fetchProgram();
	    else if(request.getParameter("mode").equals("fetchProject"))	
	    	fetchProject();
	    else if(request.getParameter("mode").equals("listStnObservations"))	
	    	listStnObservations();
	    else if(request.getParameter("mode").equals("search"))	
	    	searchStations();

	}

	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		this.doGet(req, resp);
	}
	
	private void listOrganizations()
	{
		Session session = sf.openSession();
	    session.clear();
	    
    	List orgList = session.createQuery("from Organization o order by o.name").list();
    	
    	//Set up the data type for the JSON
    	response.setContentType("application/json");
    	//Define an output stream for the data to go
        
    	out.print("{ \"type\": \"organization\", \"hits\":"+orgList.size()+", \"data\": [");
    	for (Iterator iter = orgList.iterator(); iter.hasNext();) 
    	{
    		
    		Organization o = (Organization) iter.next();
    		OrganizationType ot = o.getOrganizationType();
    		
    		out.print("{ \"organization_id\": \""+o.getOrganizationId()+"\", \"name\": \""+o.getName()+"\", \"organization_type\": \""+ot.getType()+"\", \"description\": \""+o.getDescription()+"\", \"website\": \""+o.getWebsite()+"\"}");
    		if(iter.hasNext())
    			out.print(",");
    		//logger.debug(“{}”, element);
    	}
    	out.print("] }");
    	session.close();
	}
	
	private void listOrganizationTypes()
	{
		Session session = sf.openSession();
    	List orgTypesList = session.createQuery("from OrganizationType ot order by ot.type").list();
    	
    	//Set up the data type for the JSON
    	response.setContentType("application/json");
    	//Define an output stream for the data to go
        
    	out.print("{ \"type\": \"organization_type\", \"hits\":"+orgTypesList.size()+", \"data\": [");
    	for (Iterator iter = orgTypesList.iterator(); iter.hasNext();) 
    	{
    		
    		OrganizationType ot = (OrganizationType) iter.next();
    		
    		out.print("{ \"type_id\": \""+ot.getTypeId()+"\", \"type\": \""+ot.getType()+"\"}");
    		if(iter.hasNext())
    			out.print(",");
    		//logger.debug(“{}”, element);
    	}
    	out.print("] }");
    	session.close();
	}

	private void listPrograms()
	{
		Session session = sf.openSession();
	    HttpSession hs = request.getSession(true);    
	      
	    String query = "from Program p  where p.statusValueByApprovalStatusId.statusId = "+Statics.APPROVED;
    	
	    if(request.getParameter("organization_id") != null)
	    	query += " and p.organization.organizationId = " + request.getParameter("organization_id");
    	query += " order by p.name";
	    List prgList = session.createQuery(query).list();
	    
    	//Set up the data type for the JSON
    	response.setContentType("application/json");
    	//Define an output stream for the data to go
        
    	out.print("{ \"type\": \"program\", \"hits\":"+prgList.size()+", \"data\": [");
    	for (Iterator iter = prgList.iterator(); iter.hasNext();) 
    	{
    		
    		Program p = (Program) iter.next();
    		
    		out.print("{ \"program_id\": \""+p.getProgramId()+"\", \"name\": \""+p.getName()+"\", \"status\": \""+p.getStatusValueByStatusId().getStatusValue()+"\", \"program_manager\": \""+p.getProgramManager().getPerson().getFirstName()+" "+ p.getProgramManager().getPerson().getLastName() +"\", \"website\": \""+p.getWebsite()+"\"}");
    		if(iter.hasNext())
    			out.print(",");
    		//logger.debug(“{}”, element);
    	}
    	out.print("] }");	
    	session.close();
	}
	
	
	private void listProjects()
	{
		Session session = sf.openSession();
	    HttpSession hs = request.getSession(true);
	    String query = "from Project p where p.statusValueByApprovalStatusId.statusId = "+Statics.APPROVED;
	    
	    if(request.getParameter("program_id") != null)
	    	query += " and p.program.programId = " + request.getParameter("program_id");
    	query += " order by p.name";
    	List projList = session.createQuery(query).list();
    	
    	//Set up the data type for the JSON
    	response.setContentType("application/json");
    	//Define an output stream for the data to go
        
    	out.print("{ \"type\": \"project\", \"hits\":"+projList.size()+", \"data\": [");
    	for (Iterator iter = projList.iterator(); iter.hasNext();) 
    	{
    		
    		Project p = (Project) iter.next();
    		
    		out.print("{ \"project_id\": \""+p.getProjectId()+"\", \"name\": \""+p.getName()+"\", \"status\": \""+p.getStatusValueByStatusId().getStatusValue()+"\", \"project_manager\": \""+p.getProjectManager().getPerson().getFirstName()+" "+ p.getProjectManager().getPerson().getLastName() +"\", \"website\": \""+p.getWebsite()+"\"}");
    		if(iter.hasNext())
    			out.print(",");
    		//logger.debug(“{}”, element);
    	}
    	out.print("] }");
    	session.close();
	}
	
	
	private void searchStations()
	{
		Session session = sf.openSession();
	    HttpSession hs = request.getSession(true);
        WKTReader fromText = new WKTReader();
        Geometry location = null;
        boolean tupleQuery = false;
	    String query = "select distinct s from Station s ";
	    if(request.getParameter("source_medium_id") != null || request.getParameter("observation_type_id") != null || request.getParameter("observation_category_id") != null || request.getParameter("method_id") != null)
	    {
	    	query += "left join s.observationParameters op ";
	    	tupleQuery = true;
	    }
	    query += " where s.statusValueByApprovalStatusId.statusId = "+Statics.APPROVED;
	    if(tupleQuery)
	    {
	    	String tuples = "";
	    	if(request.getParameter("source_medium_id") != null)
	    		query += " and op.observationTuple.dictionaryTermByMediumId in ("+Arrays.toString(request.getParameterValues("source_medium_id")).substring(1, Arrays.toString(request.getParameterValues("source_medium_id")).length()-1)+")";
	    	if(request.getParameter("observation_type_id") != null)
	    		query += " and op.observationTuple.dictionaryTermByParamTypeId in ("+Arrays.toString(request.getParameterValues("observation_type_id")).substring(1,Arrays.toString(request.getParameterValues("observation_type_id")).length()-1)+")";
	    	if(request.getParameter("observation_category_id") != null)
	    		query += " and op.observationTuple.dictionaryTermByParamCatId in ("+Arrays.toString(request.getParameterValues("observation_category_id")).substring(1,Arrays.toString(request.getParameterValues("observation_category_id")).length()-1)+")";
	    	if(request.getParameter("method_id") != null)
	    		query += " and op.observationTuple.dictionaryTermByAnalysisMethodId in ("+Arrays.toString(request.getParameterValues("method_id")).substring(1,Arrays.toString(request.getParameterValues("method_id")).length()-1)+")";

	    }
	    
	    if(request.getParameter("q") != null)
	    	query += " and s.name like '%" + request.getParameter("q") + "%'";
	    if(request.getParameter("organization_type_id") != null)
	    	query += " and s.organization.organizationType.typeId in ("+Arrays.toString(request.getParameterValues("organization_type_id")).substring(1,Arrays.toString(request.getParameterValues("organization_type_id")).length()-1)+")";
	    if(request.getParameter("organization_id") != null)
	    	query += " and s.organization.organizationId in ("+Arrays.toString(request.getParameterValues("organization_id")).substring(1,Arrays.toString(request.getParameterValues("organization_id")).length()-1)+")";
	    if(request.getParameter("program_status_id") != null)
	    	query += " and s.program.statusValueByStatusId.statusId in ("+Arrays.toString(request.getParameterValues("program_status_id")).substring(1,Arrays.toString(request.getParameterValues("program_status_id")).length()-1)+")";
	    if(request.getParameter("program_id") != null)
	    	query += " and s.program.programId in ("+Arrays.toString(request.getParameterValues("program_id")).substring(1,Arrays.toString(request.getParameterValues("program_id")).length()-1)+")";
	    if(request.getParameter("project_status_id") != null)
	    	query += " and s.project.statusValueByStatusId.statusId in ("+Arrays.toString(request.getParameterValues("project_status_id")).substring(1,Arrays.toString(request.getParameterValues("project_status_id")).length()-1)+")";
	    if(request.getParameter("project_id") != null)
	    	query += " and s.project.projectId in (" + Arrays.toString(request.getParameterValues("project_id")).substring(1,Arrays.toString(request.getParameterValues("project_id")).length()-1)+")";
	    if(request.getParameter("station_status_id") != null)
	    	query += " and s.statusValueByStatusId.statusId in ("+Arrays.toString(request.getParameterValues("station_status_id")).substring(1,Arrays.toString(request.getParameterValues("station_status_id")).length()-1)+")";
	    if(request.getParameter("availability_id") != null)
	    	query += " and s.project.dictionaryTermByAvailabilityId in ("+Arrays.toString(request.getParameterValues("availability_id")).substring(1,Arrays.toString(request.getParameterValues("availability_id")).length()-1)+")";
	    if(request.getParameter("purpose_category_id") != null)
	    	query += " and s.project.dictionaryTermByPurposeCategoryId in ("+Arrays.toString(request.getParameterValues("purpose_category_id")).substring(1,Arrays.toString(request.getParameterValues("purpose_category_id")).length()-1)+")";
	    if(request.getParameter("project_methodology_id") != null)
	    	query += " and s.project.dictionaryTermByProjectMethodologyId in ("+Arrays.toString(request.getParameterValues("project_methodology_id")).substring(1,Arrays.toString(request.getParameterValues("project_methodology_id")).length()-1)+")";
	    if(request.getParameter("proprietary_restriction_id") != null)
	    	query += " and s.project.dictionaryTermByProprietaryRestrictionId in ("+Arrays.toString(request.getParameterValues("proprietary_restriction_id")).substring(1,Arrays.toString(request.getParameterValues("proprietary_restriction_id")).length()-1)+")";
	    if(request.getParameter("start_date").length() == 10)
	    	query += " and case when s.startDate is null then s.project.startDate else s.startDate end >= '"+request.getParameter("start_date")+"'";
	    if(request.getParameter("end_date").length() == 10)
	    	query += " and case when s.endDate is null then s.project.endDate else s.endDate end <= '"+request.getParameter("end_date")+"'";

	    List stationList = session.createQuery(query).list();
	    String stationIds = "(";
	    
    	for (Iterator iter = stationList.iterator(); iter.hasNext();) 
    	{   		
    		Station s = (Station) iter.next();
    		stationIds += s.getStationId();
    		if(iter.hasNext())
    			stationIds += ",";
    	}
	    stationIds += ")";
	    
	    if(request.getParameter("gzSource") != null && stationIds.length() > 2)
	    {
	    	String sqlQuery = "select * from aquamonitor.station s where ST_Within(s.location, ";
	    	if(request.getParameter("gzSource").equals("preDef"))
	    		sqlQuery += " (select gz.location from aquamonitor.geographic_zone gz where gz.gz_id = "+request.getParameter("gz")+")) ";
	    	else
	    		sqlQuery += " ST_GeomFromText('"+request.getParameter("gz_str")+"', 4326))";
	    	sqlQuery += " and s.station_id in" + stationIds;
	    	sqlQuery += " and s.approval_status_id="+Statics.APPROVED;	

	    	stationList = session.createSQLQuery(sqlQuery).addEntity(Station.class).list();
	    }
    	
    	//Set up the data type for the JSON
    	response.setContentType("application/json");
    	//Define an output stream for the data to go
        
    	out.print("{ \"type\": \"station\", \"hits\":"+stationList.size()+", ");
    	if(request.getParameter("gzSource") != null)
    	{
    		out.print("\"polygon\": [");
            String coordStr = "";

    		if(request.getParameter("gzSource").equals("preDef"))
    		{
    			GeographicZone gz = (GeographicZone) session.load(GeographicZone.class, new Long(request.getParameter("gz")));
    			coordStr = gz.getLocationStr();
    		}
    		else
    			coordStr = request.getParameter("gz_str");
            try {
    			location = fromText.read(coordStr);
    		} catch (com.vividsolutions.jts.io.ParseException e2) {
    			e2.printStackTrace();
                throw new RuntimeException("Not a WKT string:" + coordStr);
            }
            int numOfGeoms = location.getNumGeometries();
            for(int j=0; j<numOfGeoms; j++)
            {
            	out.println("{\"num\":"+j+",\"coords\":[");
            	Coordinate[] coordinates = location.getGeometryN(j).getCoordinates();
            	for(int i = 0; i < coordinates.length; i++)
            	{
            		out.print("{\"latitude\": \""+coordinates[i].y+"\", \"longitude\": \""+coordinates[i].x+"\"}");
            		if(i != coordinates.length-1)
            			out.print(",");
            	}	
            	out.print("]}");
            	if(j != numOfGeoms-1)
            		out.print(",");
            }
        	out.print("],");
    	}
    	out.print("\"data\": [");
    	for (Iterator iter = stationList.iterator(); iter.hasNext();) 
    	{   		
    		Station s = (Station) iter.next();
    		
    		try {
    			location = fromText.read(s.getLocationStr());
    			
    			//This is a station with a point location 
                if(location.getGeometryType().equals("Point"))
                	out.print("{  \"location\": { \"type\": \"point\",  \"coordinates\": {\"latitude\": \""+df.format(location.getCoordinate().y)+"\", \"longitude\": \""+df.format(location.getCoordinate().x)+"\"}}, ");
                else
                {
                	//This is a station bounded by a polygon, so we take the centroid to be able to display it on the map	
                	out.print("{  \"location\": { \"type\": \""+df.format(location.getGeometryType().toLowerCase())+"\",  \"coordinates\": {\"latitude\": \""+df.format(location.getCentroid().getCoordinate().y)+"\", \"longitude\": \""+location.getCentroid().getCoordinate().x+"\"}}, ");
                }
    		} catch (com.vividsolutions.jts.io.ParseException e2) {
    			out.print("{  \"code\": \"failure\", \"message\": \"Cannot parse - Location is not a WKT string\", ");
                return;
            }
    		out.print(" \"station_id\": \""+s.getStationId()+"\", \"name\": \""+s.getName()+"\", \"status\": \""+s.getStatusValueByStatusId().getStatusValue()+"\", \"program\": \""+s.getProgram().getName()+"\", \"project\": \""+s.getProject().getName()+"\", \"organization\": \""+s.getOrganization().getName()+"\", \"start_date\": \""+s.getStartDate()+"\", \"end_date\": \""+s.getEndDate()+"\"}");
   
    		if(iter.hasNext())
    			out.print(",");
    		//logger.debug(“{}”, element);
    	}
    	out.print("] }");
    	session.close();
	}

	
	private void listStations()
	{
		Session session = sf.openSession();
	    HttpSession hs = request.getSession(true);
        WKTReader fromText = new WKTReader();
        Geometry location = null;
        
	    String query = "from Station s where s.statusValueByApprovalStatusId.statusId = "+Statics.APPROVED;
	    
	    if(request.getParameter("project_id") != null)
	    	query += " and s.project.projectId = " + request.getParameter("project_id");
    	query += " order by s.name";
    	List stationList = session.createQuery(query).list();
    	
    	//Set up the data type for the JSON
    	response.setContentType("application/json");
    	//Define an output stream for the data to go
        
    	out.print("{ \"type\": \"station\", \"hits\":"+stationList.size()+", \"data\": [");
    	for (Iterator iter = stationList.iterator(); iter.hasNext();) 
    	{
    		
    		Station s = (Station) iter.next();

    		try {
    			location = fromText.read(s.getLocationStr());
    			
    			//This is a station with a point location 
                if(location.getGeometryType().equals("Point"))
                	out.print("{  \"location\": { \"type\": \"point\",  \"coordinates\": {\"latitude\": \""+df.format(location.getCoordinate().y)+"\", \"longitude\": \""+df.format(location.getCoordinate().x)+"\"}}, ");
                else
                {
                	//This is station bounded by a polygon, so we take the centroid to be able to display it on the map	
                	out.print("{  \"location\": { \"type\": \""+location.getGeometryType().toLowerCase()+"\",  \"coordinates\": {\"latitude\": \""+df.format(location.getCentroid().getCoordinate().y)+"\", \"longitude\": \""+df.format(location.getCentroid().getCoordinate().x)+"\"}}, ");
                }
    		} catch (com.vividsolutions.jts.io.ParseException e2) {
    			out.print("{  \"code\": \"failure\", \"message\": \"Cannot parse - Location is not a WKT string\", ");
                return;
            }
    		out.print(" \"station_id\": \""+s.getStationId()+"\", \"name\": \""+s.getName()+"\", \"status\": \""+s.getStatusValueByStatusId().getStatusValue()+"\", \"program\": \""+s.getProgram().getName()+"\", \"project\": \""+s.getProject().getName()+"\", \"organization\": \""+s.getOrganization().getName()+"\", \"start_date\": \""+s.getStartDate()+"\", \"end_date\": \""+s.getEndDate()+"\", \"website\": \""+s.getWebsite()+"\"}");
   
    		if(iter.hasNext())
    			out.print(",");
    		//logger.debug(“{}”, element);
    	}
    	out.print("] }");
    	session.close();
	}
	
	private void listStationsByGZ()
	{
		Session session = sf.openSession();
	    HttpSession hs = request.getSession(true);
        WKTReader fromText = new WKTReader();
        Geometry location = null;
        
	    String query = "select * from aquamonitor.station s where ST_Within(s.location, (select gz.location from aquamonitor.geographic_zone gz where gz.gz_id = "+request.getParameter("gz_id")+")) and s.approval_status_id="+Statics.APPROVED+" order by gz.name";

    	List stationList = session.createSQLQuery(query).addEntity(Station.class).list();
    	
    	//Set up the data type for the JSON
    	response.setContentType("application/json");
    	//Define an output stream for the data to go
        
    	out.print("{ \"type\": \"station\", \"hits\":"+stationList.size()+", \"data\": [");
    	for (Iterator iter = stationList.iterator(); iter.hasNext();) 
    	{
    		
    		Station s = (Station) iter.next();

    		try {
    			location = fromText.read(s.getLocationStr());
    			
    			//This is a station with a point location 
                if(location.getGeometryType().equals("Point"))
                	out.print("{  \"location\": { \"type\": \"point\",  \"coordinates\": {\"latitude\": \""+df.format(location.getCoordinate().y)+"\", \"longitude\": \""+df.format(location.getCoordinate().x)+"\"}}, ");
                else
                {
                	//This is station bounded by a polygon, so we take the centroid to be able to display it on the map	
                	out.print("{  \"location\": { \"type\": \""+location.getGeometryType().toLowerCase()+"\",  \"coordinates\": {\"latitude\": \""+df.format(location.getCentroid().getCoordinate().y)+"\", \"longitude\": \""+df.format(location.getCentroid().getCoordinate().x)+"\"}}, ");
                }
    		} catch (com.vividsolutions.jts.io.ParseException e2) {
    			out.print("{  \"code\": \"failure\", \"message\": \"Cannot parse - Location is not a WKT string\", ");
                return;
            }
    		out.print(" \"station_id\": \""+s.getStationId()+"\", \"name\": \""+s.getName()+"\", \"status\": \""+s.getStatusValueByStatusId().getStatusValue()+"\", \"program\": \""+s.getProgram().getName()+"\", \"project\": \""+s.getProject().getName()+"\", \"organization\": \""+s.getOrganization().getName()+"\", \"start_date\": \""+s.getStartDate()+"\", \"end_date\": \""+s.getEndDate()+"\"}");
   
    		if(iter.hasNext())
    			out.print(",");
    		//logger.debug(“{}”, element);
    	}
    	out.print("] }");
    	session.close();
	}
	
	private void listStationsByAdHocGZ()
	{
		Session session = sf.openSession();
	    HttpSession hs = request.getSession(true);
        WKTReader fromText = new WKTReader();
        Geometry location = null;
        
	    String query = "select * from aquamonitor.station s where ST_Within(s.location, ST_GeomFromText('"+request.getParameter("gz_str")+"', 4326)) and s.approval_status_id="+Statics.APPROVED+" order by gz.name";

    	List stationList = session.createSQLQuery(query).addEntity(Station.class).list();
    	
    	//Set up the data type for the JSON
    	response.setContentType("application/json");
    	//Define an output stream for the data to go
        
    	out.print("{ \"type\": \"station\", \"hits\":"+stationList.size()+", \"data\": [");
    	for (Iterator iter = stationList.iterator(); iter.hasNext();) 
    	{
    		
    		Station s = (Station) iter.next();

    		try {
    			location = fromText.read(s.getLocationStr());
    			
    			//This is a station with a point location 
                if(location.getGeometryType().equals("Point"))
                	out.print("{  \"location\": { \"type\": \"point\",  \"coordinates\": {\"latitude\": \""+df.format(location.getCoordinate().y)+"\", \"longitude\": \""+df.format(location.getCoordinate().x)+"\"}}, ");
                else
                {
                	//This is station bounded by a polygon, so we take the centroid to be able to display it on the map	
                	out.print("{  \"location\": { \"type\": \""+location.getGeometryType().toLowerCase()+"\",  \"coordinates\": {\"latitude\": \""+df.format(location.getCentroid().getCoordinate().y)+"\", \"longitude\": \""+df.format(location.getCentroid().getCoordinate().x)+"\"}}, ");
                }
    		} catch (com.vividsolutions.jts.io.ParseException e2) {
    			out.print("{  \"code\": \"failure\", \"message\": \"Cannot parse - Location is not a WKT string\", ");
                return;
            }
    		out.print(" \"station_id\": \""+s.getStationId()+"\", \"name\": \""+s.getName()+"\", \"status\": \""+s.getStatusValueByStatusId().getStatusValue()+"\", \"program\": \""+s.getProgram().getName()+"\", \"project\": \""+s.getProject().getName()+"\", \"organization\": \""+s.getOrganization().getName()+"\", \"start_date\": \""+s.getStartDate()+"\", \"end_date\": \""+s.getEndDate()+"\"}");
   
    		if(iter.hasNext())
    			out.print(",");
    		//logger.debug(“{}”, element);
    	}
    	out.print("] }");
    	session.close();
	}
	
	private static void listLinkedStations()
	{
		Session session = sf.openSession();
		session.clear();

	    HttpSession hs = request.getSession(true);
		
	    String query = "from StationProject sp where sp.station.statusValueByApprovalStatusId.statusId = "+Statics.APPROVED;
	    
	    if(request.getParameter("project_id") != null)
	    	query += " and sp.project.projectId = " + request.getParameter("project_id");
    	query += " order by sp.station.name";
    	List stationProjectList = session.createQuery(query).list();
    	
    	//Set up the data type for the JSON
    	response.setContentType("application/json");
    	//Define an output stream for the data to go
        
    	out.print("{ \"type\": \"station\", \"hits\":"+stationProjectList.size()+", \"data\": [");
    	for (Iterator iter = stationProjectList.iterator(); iter.hasNext();) 
    	{   		
    		StationProject sp = (StationProject) iter.next();
    		Station s = sp.getStation();
    		
    		out.print("{ \"station_id\": \""+s.getStationId()+"\", \"name\": \""+s.getName()+"\", \"status\": \""+s.getStatusValueByStatusId().getStatusValue()+"\", \"website\": \""+s.getWebsite()+"\"}");
    		if(iter.hasNext())
    			out.print(",");
    	}
    	out.print("] }");
    	session.close();
	}
	
	private void listGZ()
	{

		Session session = sf.openSession();
	    HttpSession hs = request.getSession(true); 
	    String mode = request.getParameter("type");
	      
    	List gzList = session.createQuery("from GeographicZone gz where gz.statusValue = "+Statics.APPROVED+" order by gz.name").list();
    	
    	//Set up the data type for the JSON
    	response.setContentType("application/json");
    	//Define an output stream for the data to go
        
    	out.print("{ \"type\": \"gz\", \"hits\":"+gzList.size()+", \"data\": [");
    	for (Iterator iter = gzList.iterator(); iter.hasNext();) 
    	{
    		
    		GeographicZone gz = (GeographicZone) iter.next();
            WKTReader fromText = new WKTReader();
            Geometry location = null;

            try {
    			location = fromText.read(gz.getLocationStr());
    		} catch (com.vividsolutions.jts.io.ParseException e2) {
    			e2.printStackTrace();
                throw new RuntimeException("Not a WKT string:" + request.getParameter("location"));
            }
          
            if(mode.equalsIgnoreCase("name"))
            	out.print("{ \"gz_id\": \""+gz.getGzId()+"\", \"name\": \""+gz.getName()+"\", \"approval\": \""+gz.getStatusValue().getStatusValue()+"\"}");
            else
            {
            	out.print("{ \"gz_id\": \""+gz.getGzId()+"\", \"name\": \""+gz.getName()+"\", \"location\": {\"type\": \""+location.getGeometryType().toLowerCase()+"\", \"coordinates\": [");
            	Coordinate[] coordinates = location.getCoordinates();
            	for(int i = 0; i < coordinates.length; i++)
            	{
            		out.print("{\"latitude\": \""+df.format(coordinates[i].y)+"\", \"longitude\": \""+df.format(coordinates[i].x)+"\"}");
            		if(i != coordinates.length-1)
            			out.print(",");
            	}	
            	out.print("]}, \"approval\": \""+gz.getStatusValue().getStatusValue()+"\"}");
                
            }
        	
    		if(iter.hasNext())
    			out.print(",");
    		//logger.debug(“{}”, element);
    	}
    	out.print("] }");	
    	session.close();
	}
	private void listApprovals()
	{
		
	}
	

	private static void listTuples()
	{
		Session session = sf.openSession();
    	List obsTupleList = session.createQuery("from ObservationTuple ot where ot.statusValue = "+Statics.APPROVED).list();
    	
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
	
	private void listStatusValues()
	{
		Session session = sf.openSession();
		session.clear();
	    Transaction tx = null;
    	StatusType st = (StatusType) session.load(StatusType.class, new Long(request.getParameter("status_type_id")));

    	Set entitySet = st.getStatusValues();
    	
    	//Set up the data type for the JSON
    	response.setContentType("application/json");
        
    	out.print("{ \"type\": \"statusValues\", \"hits\":"+entitySet.size()+", \"data\": [ ");
    	for (Iterator iter = entitySet.iterator(); iter.hasNext();) 
    	{
    		StatusValue sv = (StatusValue) iter.next();
    		out.print("{\"value\": \""+sv.getStatusValue()+"\", \"status_id\": \""+sv.getStatusId()+"\"}");
    		if(iter.hasNext())
    			out.print(",");
    		//logger.debug(“{}”, element);
    	}
    	out.print("]  }");
    	session.close();
	  
	}
	

	private void listUserRoles()
	{
		Session session = sf.openSession();
		Transaction tx = null;

	    try 
	    {
	    	tx = session.beginTransaction();
	    	List orgList = session.createQuery("select distinct role from UserRole ur").list();
	    	
	    	//Set up the data type for the JSON
	    	response.setContentType("application/json");
	        
	    	out.print("{ \"type\": \"userRole\", \"hits\":"+orgList.size()+", \"data\": [ ");
	    	for (Iterator iter = orgList.iterator(); iter.hasNext();) 
	    	{
	    		out.print("\""+iter.next()+"\"");
	    		if(iter.hasNext())
	    			out.print(",");
	    		//logger.debug(“{}”, element);
	    	}
	    	out.print("]  }");
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
	    session.close();
	}
	
	private static void listDictionaryTerms()
	{
		Session session = sf.openSession();
    	List termsList = session.createQuery("from DictionaryTerm dt where dt.dictionary.dictionaryId = "+ request.getParameter("dictionary_id")+" and dt.statusValue.statusId = "+Statics.APPROVED+" order by dt.term").list();
    	Dictionary d = (Dictionary) session.load(Dictionary.class, new Long(request.getParameter("dictionary_id")));
    	//Set up the data type for the JSON
    	response.setContentType("application/json");
        
    	out.print("{ \"hits\":"+termsList.size()+", \"dictionary\": \""+ d.getName() +"\", \"dictionary_description\": \""+ d.getDescription() +"\", \"code\": \""+d.getDictionaryCode()+"\", \"data\": [");
    	for (Iterator iter = termsList.iterator(); iter.hasNext();) 
    	{
    		 
    		DictionaryTerm dt = (DictionaryTerm) iter.next();

    		out.print("{  \"term_id\": \""+dt.getTermId()+"\", \"description\": \""+dt.getDescription()+"\", \"name\": \""+dt.getTerm()+"\"}");
    		if(iter.hasNext())
    			out.print(",");
    	}
    	out.print("] }");
    	session.close();
	}
	
	private static void fetchStation()
	{
		Session session = sf.openSession();
		session.clear();
		Station s = (Station) session.load(Station.class, new Long(request.getParameter("station_id")));
        WKTReader fromText = new WKTReader();
        Geometry location = null;
        
		try {
			location = fromText.read(s.getLocationStr());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	response.setContentType("application/json");      
    	out.print("{ \"data\": [ {");
    	out.print("\"station_id\": \""+s.getStationId()+"\", \"name\": \""+s.getName()+"\", \"description\": \""+s.getDescription()+"\", " +
    			"\"website\": \""+s.getWebsite()+"\", \"date_created\": \""+sdf.format(s.getDateCreated())+"\", \"created_by\": \""+s.getUserByCreatedBy().getUsername() + "\"," +
    			"\"organization\": \""+s.getOrganization().getName()+"\", \"organization_id\": \""+s.getOrganization().getOrganizationId()+"\", "+
    			"\"project\": \""+s.getProject().getName()+"\", \"project_id\": \""+s.getProject().getProjectId()+"\", \"program\": \""+s.getProgram().getName()+"\", "+
    			"\"location\": \"(longitude, latitude):("+location.getCoordinate().x+","+location.getCoordinate().y+")\", \"program_id\": \""+s.getProgram().getProgramId()+"\"");
    	if(s.getKeywords() != null)	
    		out.print(", \"keywords\": \""+s.getKeywords()+"\"");
    	if(s.getStartDate() != null)
    		out.print(", \"start_date\": \""+sdf.format(s.getStartDate())+"\", \"end_date\": \""+sdf.format(s.getEndDate())+"\"");
    	if(s.getUserByUpdatedBy() != null)
    		out.print(", \"date_updated\": \""+sdf.format(s.getDateUpdated())+"\", \"updated_by\": \""+s.getUserByUpdatedBy().getUsername()+"\"");
    	out.print(",\"status\": \""+s.getStatusValueByStatusId().getStatusValue()+"\",\"status_id\": \""+s.getStatusValueByStatusId().getStatusId()+"\", \"approval\": \""+s.getStatusValueByApprovalStatusId().getStatusValue()+"\" } ] }");
    	session.close();
	}

	private static void fetchOrganization()
	{
		Session session = sf.openSession();
		session.clear();
		Organization o = (Organization) session.load(Organization.class, new Long(request.getParameter("organization_id")));
		
    	response.setContentType("application/json");      
    	out.print("{ \"data\": [ {");
    	out.print("\"organization\": \""+o.getName()+"\", \"organization_id\": \""+o.getOrganizationId()+"\", "+ 
    			"\"description\": \""+o.getDescription()+"\", \"type\": \""+o.getOrganizationType().getType()+"\", "+ 
    			"\"website\": \""+o.getWebsite()+"\"");
    	out.print(" } ] }");
    	session.close();
	}
	
	private static void fetchProgram()
	{
		Session session = sf.openSession();
		session.clear();
		Program p = (Program) session.load(Program.class, new Long(request.getParameter("program_id")));
		
    	response.setContentType("application/json");      
    	out.print("{ \"data\": [ {");
    	out.print("\"program_id\": \""+p.getProgramId()+"\", \"name\": \""+p.getName()+"\", \"description\": \""+p.getDescription()+"\", \"pm_first_name\": \""+p.getProgramManager().getPerson().getFirstName()+"\", \"pm_last_name\": \""+p.getProgramManager().getPerson().getLastName() +
    			"\", \"organization\": \""+p.getOrganization().getName()+"\", \"organization_id\": \""+p.getOrganization().getOrganizationId()+"\", \"website\": \""+p.getWebsite()+"\", \"date_created\": \""+sdf.format(p.getDateCreated())+"\", \"created_by\": \""+p.getUserByCreatedBy().getUsername() + "\", \"pm_id\": \""+p.getProgramManager().getPerson().getPersonId()+"\"" +
 				", \"pm_email\": \""+p.getProgramManager().getPerson().getEmail()+"\", \"pm_address\": \""+p.getProgramManager().getPerson().getAddress()+"\", \"pm_job_title\": \""+p.getProgramManager().getPerson().getJobTitle()+"\", \"pm_phone\": \""+p.getProgramManager().getPerson().getPhone()+"\", \"pm_fax\": \""+p.getProgramManager().getPerson().getFax()+"\"" +
 				", \"pm_homepage\": \""+p.getProgramManager().getPerson().getWebsite()+"\"");
    	out.print(" } ] }");
    	session.close();
	}
	
	
	private static void fetchProject()
	{
		Session session = sf.openSession();
		session.clear();
		Project p = (Project) session.load(Project.class, new Long(request.getParameter("project_id")));
		
    	response.setContentType("application/json");      
    	out.print("{ \"data\": [ {");
    	out.print("\"project_id\": \""+p.getProjectId()+
    			"\", \"name\": \""+p.getName()+
    			"\", \"description\": \""+p.getDescription()+
    			"\", \"pm_first_name\": \""+p.getProjectManager().getPerson().getFirstName()+
    			"\", \"pm_last_name\": \""+p.getProjectManager().getPerson().getLastName() + 
    			"\", \"organization_id\": \""+p.getProgram().getOrganization().getOrganizationId()+
    			"\", \"organization\": \""+p.getProgram().getOrganization().getName()+
    			"\", \"program\": \""+p.getProgram().getName()+
    			"\", \"website\": \""+p.getWebsite()+
    			"\", \"date_created\": \""+sdf.format(p.getDateCreated())+
    			"\", \"created_by\": \""+p.getUserByCreatedBy().getUsername() + 
    			"\", \"pm_id\": \""+p.getProjectManager().getPerson().getPersonId()+
    			"\", \"data_quality_obj\": \""+p.getDataQualityObj()+
    			"\", \"start_date\": \""+sdf.format(p.getStartDate())+
    			"\", \"end_date\": \""+sdf.format(p.getEndDate())+
    			"\", \"usage_limitations\": \""+p.getUsageLimitations()+
    			"\", \"data_link_website\": \""+p.getDataLinkWebsite()+
    			"\", \"proprietary_restriction_text\": \""+p.getProprietaryRestrictionText()+
    			"\", \"project_methodology_id\": \""+p.getDictionaryTermByProjectMethodologyId().getTermId() +
    			"\", \"project_methodology\": \""+p.getDictionaryTermByProjectMethodologyId().getTerm() +
    			"\", \"availability\": \""+p.getDictionaryTermByAvailabilityId().getTerm()+
    			"\", \"proprietary_restriction\": \""+p.getDictionaryTermByProprietaryRestrictionId().getTerm()+
    			"\", \"purpose_category\": \""+p.getDictionaryTermByPurposeCategoryId().getTerm()+
    			"\", \"purpose_text\": \""+p.getPurposeText()+
    			"\", \"geo_boundary\": \""+p.getGeoBoundary() +
    			"\", \"availability_id\": \""+p.getDictionaryTermByAvailabilityId().getTermId()+
    			"\", \"proprietary_restriction_id\": \""+p.getDictionaryTermByProprietaryRestrictionId().getTermId()+
    			"\", \"purpose_category_id\": \""+p.getDictionaryTermByPurposeCategoryId().getTermId() +
 				"\", \"pm_email\": \""+p.getProjectManager().getPerson().getEmail()+
 				"\", \"pm_job_title\": \""+p.getProjectManager().getPerson().getJobTitle()+
 				"\", \"pm_address\": \""+p.getProjectManager().getPerson().getAddress()+
 				"\", \"pm_phone\": \""+p.getProjectManager().getPerson().getPhone()+
 				"\", \"pm_fax\": \""+p.getProjectManager().getPerson().getFax() +
 				"\", \"pm_homepage\": \""+p.getProjectManager().getPerson().getWebsite()+
 				"\", \"dd_email\": \""+p.getDataDistributor().getPerson().getEmail()+
 				"\", \"dd_address\": \""+p.getProjectManager().getPerson().getAddress()+
 				"\", \"dd_job_title\": \""+p.getDataDistributor().getPerson().getJobTitle()+
 				"\", \"dd_phone\": \""+p.getDataDistributor().getPerson().getPhone()+
 				"\", \"dd_fax\": \""+p.getDataDistributor().getPerson().getFax()+
 				"\", \"dd_homepage\": \""+p.getDataDistributor().getPerson().getWebsite()+
 				"\", \"dd_first_name\": \""+p.getDataDistributor().getPerson().getFirstName()+
 				"\", \"dd_last_name\": \""+p.getDataDistributor().getPerson().getLastName()+
 				"\", \"dd_id\": \""+p.getDataDistributor().getPerson().getPersonId()+"\"");
    	out.print(" } ] }");
    	session.close();
	}
	
	private static void listStnObservations()
	{
		Session session = sf.openSession();
	    session.flush();
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
    				"\", \"sampling_freq\": \""+op.getDictionaryTermBySamplingFreqId().getTerm());
    		if(op.getDictionaryTermBySamplingDepthId() != null)
    			out.print("\", \"sampling_depth\": \""+op.getDictionaryTermBySamplingDepthId().getTerm());
    		else
    			out.print("\", \"sampling_depth\": \"-");
    		if(op.getStartDate() != null && op.getStartDate().toString().length() > 8)
    			out.print("\", \"start_date\": \""+op.getStartDate());
    		else
    			out.print("\", \"start_date\": \"-");
    		if(op.getEndDate() != null && op.getEndDate().toString().length() > 8)
    			out.print("\", \"end_date\": \""+op.getEndDate());
    		else
    			out.print("\", \"end_date\": \"-");
    		out.print("\", \"approval_status\": \""+op.getStatusValue().getStatusValue()+"\"}");
    		if(iter.hasNext())
    			out.print(",");
    	}
    	out.print("] }");
    	session.close();
	}
}
