package edu.miami.ccs.goma.pojos;
// Generated May 4, 2012 4:38:51 PM by Hibernate Tools 3.4.0.CR1


import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Person generated by hbm2java
 */
public class Person  implements java.io.Serializable {


     private long personId;
     private String phone;
     private String firstName;
     private String lastName;
     private String jobTitle;
     private String address;
     private String fax;
     private String website;
     private String bio;
     private Date dateCreated;
     private long createdBy;
     private Date dateUpdated;
     private Long updatedBy;
     private String email;
     private Set programManagers = new HashSet(0);
     private Set projectManagers = new HashSet(0);
     private Set dataDistributors = new HashSet(0);
     private Set users = new HashSet(0);

    public Person() {
    }

	
    public Person(String phone, String firstName, String lastName, String jobTitle, Date dateCreated, long createdBy, String email) {

        this.phone = phone;
        this.firstName = firstName;
        this.lastName = lastName;
        this.jobTitle = jobTitle;
        this.dateCreated = dateCreated;
        this.createdBy = createdBy;
        this.email = email;
    }
    public Person(String phone, String firstName, String lastName, String jobTitle, String address, String fax, String website, String bio, Date dateCreated, long createdBy, Date dateUpdated, Long updatedBy, String email, Set programManagers, Set projectManagers, Set dataDistributors, Set users) {
       this.phone = phone;
       this.firstName = firstName;
       this.lastName = lastName;
       this.jobTitle = jobTitle;
       this.address = address;
       this.fax = fax;
       this.website = website;
       this.bio = bio;
       this.dateCreated = dateCreated;
       this.createdBy = createdBy;
       this.dateUpdated = dateUpdated;
       this.updatedBy = updatedBy;
       this.email = email;
       this.programManagers = programManagers;
       this.projectManagers = projectManagers;
       this.dataDistributors = dataDistributors;
       this.users = users;
    }
   
    public long getPersonId() {
        return this.personId;
    }
    
    public void setPersonId(long personId) {
        this.personId = personId;
    }
    public String getPhone() {
        return this.phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getFirstName() {
        return this.firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return this.lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getJobTitle() {
        return this.jobTitle;
    }
    
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }
    public String getAddress() {
        return this.address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    public String getFax() {
        return this.fax;
    }
    
    public void setFax(String fax) {
        this.fax = fax;
    }
    public String getWebsite() {
        return this.website;
    }
    
    public void setWebsite(String website) {
        this.website = website;
    }
    public String getBio() {
        return this.bio;
    }
    
    public void setBio(String bio) {
        this.bio = bio;
    }
    public Date getDateCreated() {
        return this.dateCreated;
    }
    
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
    public long getCreatedBy() {
        return this.createdBy;
    }
    
    public void setCreatedBy(long createdBy) {
        this.createdBy = createdBy;
    }
    public Date getDateUpdated() {
        return this.dateUpdated;
    }
    
    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }
    public Long getUpdatedBy() {
        return this.updatedBy;
    }
    
    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }
    public String getEmail() {
        return this.email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    public Set getProgramManagers() {
        return this.programManagers;
    }
    
    public void setProgramManagers(Set programManagers) {
        this.programManagers = programManagers;
    }
    public Set getProjectManagers() {
        return this.projectManagers;
    }
    
    public void setProjectManagers(Set projectManagers) {
        this.projectManagers = projectManagers;
    }
    public Set getDataDistributors() {
        return this.dataDistributors;
    }
    
    public void setDataDistributors(Set dataDistributors) {
        this.dataDistributors = dataDistributors;
    }
    public Set getUsers() {
        return this.users;
    }
    
    public void setUsers(Set users) {
        this.users = users;
    }




}


