package edu.miami.ccs.goma.pojos;
// Generated Feb 20, 2012 3:22:28 PM by Hibernate Tools 3.4.0.CR1


import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Program generated by hbm2java
 */
public class Program  implements java.io.Serializable {


     private long programId;
     private User userByCreatedBy;
     private User userByOwnerId;
     private Organization organization;
     private User userByUpdatedBy;
     private StatusValue statusValueByApprovalStatusId;
     private StatusValue statusValueByStatusId;
     private ProgramManager programManager;
     private String name;
     private String description;
     private String website;
     private Date dateCreated;
     private Date dateUpdated;
     private Set projects = new HashSet(0);
     private Set programAdmins = new HashSet(0);
     private Set stations = new HashSet(0);

    public Program() {
    }

	
    public Program(User userByCreatedBy, User userByOwnerId, Organization organization, StatusValue statusValueByApprovalStatusId, StatusValue statusValueByStatusId, ProgramManager programManager, String name, String description, Date dateCreated) {

        this.userByCreatedBy = userByCreatedBy;
        this.userByOwnerId = userByOwnerId;
        this.organization = organization;
        this.statusValueByApprovalStatusId = statusValueByApprovalStatusId;
        this.statusValueByStatusId = statusValueByStatusId;
        this.programManager = programManager;
        this.name = name;
        this.description = description;
        this.dateCreated = dateCreated;
    }
    public Program(long programId, User userByCreatedBy, User userByOwnerId, Organization organization, User userByUpdatedBy, StatusValue statusValueByApprovalStatusId, StatusValue statusValueByStatusId, ProgramManager programManager, String name, String description, String website, Date dateCreated, Date dateUpdated, Set projects, Set programAdmins, Set stations) {
       this.programId = programId;
       this.userByCreatedBy = userByCreatedBy;
       this.userByOwnerId = userByOwnerId;
       this.organization = organization;
       this.userByUpdatedBy = userByUpdatedBy;
       this.statusValueByApprovalStatusId = statusValueByApprovalStatusId;
       this.statusValueByStatusId = statusValueByStatusId;
       this.programManager = programManager;
       this.name = name;
       this.description = description;
       this.website = website;
       this.dateCreated = dateCreated;
       this.dateUpdated = dateUpdated;
       this.projects = projects;
       this.programAdmins = programAdmins;
       this.stations = stations;
    }
   
    public long getProgramId() {
        return this.programId;
    }
    
    public void setProgramId(long programId) {
        this.programId = programId;
    }
    public User getUserByCreatedBy() {
        return this.userByCreatedBy;
    }
    
    public void setUserByCreatedBy(User userByCreatedBy) {
        this.userByCreatedBy = userByCreatedBy;
    }
    public User getUserByOwnerId() {
        return this.userByOwnerId;
    }
    
    public void setUserByOwnerId(User userByOwnerId) {
        this.userByOwnerId = userByOwnerId;
    }
    public Organization getOrganization() {
        return this.organization;
    }
    
    public void setOrganization(Organization organization) {
        this.organization = organization;
    }
    public User getUserByUpdatedBy() {
        return this.userByUpdatedBy;
    }
    
    public void setUserByUpdatedBy(User userByUpdatedBy) {
        this.userByUpdatedBy = userByUpdatedBy;
    }
    public StatusValue getStatusValueByApprovalStatusId() {
        return this.statusValueByApprovalStatusId;
    }
    
    public void setStatusValueByApprovalStatusId(StatusValue statusValueByApprovalStatusId) {
        this.statusValueByApprovalStatusId = statusValueByApprovalStatusId;
    }
    public StatusValue getStatusValueByStatusId() {
        return this.statusValueByStatusId;
    }
    
    public void setStatusValueByStatusId(StatusValue statusValueByStatusId) {
        this.statusValueByStatusId = statusValueByStatusId;
    }
    public ProgramManager getProgramManager() {
        return this.programManager;
    }
    
    public void setProgramManager(ProgramManager programManager) {
        this.programManager = programManager;
    }
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    public String getWebsite() {
        return this.website;
    }
    
    public void setWebsite(String website) {
        this.website = website;
    }
    public Date getDateCreated() {
        return this.dateCreated;
    }
    
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
    public Date getDateUpdated() {
        return this.dateUpdated;
    }
    
    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }
    public Set getProjects() {
        return this.projects;
    }
    
    public void setProjects(Set projects) {
        this.projects = projects;
    }
    public Set getProgramAdmins() {
        return this.programAdmins;
    }
    
    public void setProgramAdmins(Set programAdmins) {
        this.programAdmins = programAdmins;
    }
    public Set getStations() {
        return this.stations;
    }
    
    public void setStations(Set stations) {
        this.stations = stations;
    }




}


