package edu.miami.ccs.goma;


/**
 * Class to hold all the static variables that are referred to across the project
 */
public class Statics  implements java.io.Serializable {

	//Approval status codes for data
	public static int PENDING = 3;
	public static int APPROVED = 4;
	public static int INCOMPLETE = 5;
	public static int REJECTED = 6;
	
	//Status type codes
	public static int PROGRAM_STATUS = 1;
	public static int APPROVAL = 2;
	public static int PROJECT_STATUS = 3;
	public static int STATION_STATUS = 4;
   
	//Dictionary codes
	public static int SOURCE_MEDIUM = 1;
	public static int GEOMETRY_TYPE = 8;
	public static int COLLECTION_METHOD = 9; 
	public static int ANALYTE_GROUP = 10;
	public static int SAMPLING_FREQUENCY = 11;
	public static int PROPRIETARY_RESTRICTIONS = 12; 
	public static int AVAILABILITY = 13;
	public static int PURPOSE_CATEGORY = 14;
	public static int DATA_QUALITY_OBJ = 15;
	public static int ACCESS_RESTRICTIONS = 16;
	public static int OBSERVATION_CATEGORY = 17;
	public static int OBSERVATION_TYPE = 18;
	public static int METHOD = 19;
	public static int PROJECT_METHODOLOGY = 33;
	
	//Approval Type Codes
	public static int PROGRAM = 1;
	public static int PROJECT = 2;
	public static int STATION = 3;
	public static int DICTIONARY = 4;
	public static int DICTIONARY_TERM = 5;
	public static int GEOGRAPHIC_ZONE = 6;
	public static int OBSERVATION_PARAMETER = 7;
	public static int OBSERVATION_TUPLE = 8;

    public Statics() {
    }

}


