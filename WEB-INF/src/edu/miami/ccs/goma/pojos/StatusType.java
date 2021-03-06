package edu.miami.ccs.goma.pojos;
// Generated May 4, 2012 4:38:51 PM by Hibernate Tools 3.4.0.CR1


import java.util.HashSet;
import java.util.Set;

/**
 * StatusType generated by hbm2java
 */
public class StatusType  implements java.io.Serializable {


     private long statusTypeId;
     private String statusTypeCode;
     private String statusTypeName;
     private Set statusValues = new HashSet(0);

    public StatusType() {
    }

	
    public StatusType(String statusTypeCode, String statusTypeName) {

        this.statusTypeCode = statusTypeCode;
        this.statusTypeName = statusTypeName;
    }
    public StatusType(long statusTypeId, String statusTypeCode, String statusTypeName, Set statusValues) {
       this.statusTypeId = statusTypeId;
       this.statusTypeCode = statusTypeCode;
       this.statusTypeName = statusTypeName;
       this.statusValues = statusValues;
    }
   
    public long getStatusTypeId() {
        return this.statusTypeId;
    }
    
    public void setStatusTypeId(long statusTypeId) {
        this.statusTypeId = statusTypeId;
    }
    public String getStatusTypeCode() {
        return this.statusTypeCode;
    }
    
    public void setStatusTypeCode(String statusTypeCode) {
        this.statusTypeCode = statusTypeCode;
    }
    public String getStatusTypeName() {
        return this.statusTypeName;
    }
    
    public void setStatusTypeName(String statusTypeName) {
        this.statusTypeName = statusTypeName;
    }
    public Set getStatusValues() {
        return this.statusValues;
    }
    
    public void setStatusValues(Set statusValues) {
        this.statusValues = statusValues;
    }




}


