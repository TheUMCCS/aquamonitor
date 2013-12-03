package edu.miami.ccs.goma.pojos;
// Generated May 4, 2012 4:38:51 PM by Hibernate Tools 3.4.0.CR1


import java.util.HashSet;
import java.util.Set;

/**
 * DataDistributor generated by hbm2java
 */
public class DataDistributor  implements java.io.Serializable {


     private long dataDistributorId;
     private Person person;
     private Set projects = new HashSet(0);

    public DataDistributor() {
    }

	
    public DataDistributor(Person person) {

        this.person = person;
    }
    public DataDistributor(long dataDistributorId, Person person, Set projects) {
       this.dataDistributorId = dataDistributorId;
       this.person = person;
       this.projects = projects;
    }
   
    public long getDataDistributorId() {
        return this.dataDistributorId;
    }
    
    public void setDataDistributorId(long dataDistributorId) {
        this.dataDistributorId = dataDistributorId;
    }
    public Person getPerson() {
        return this.person;
    }
    
    public void setPerson(Person person) {
        this.person = person;
    }
    public Set getProjects() {
        return this.projects;
    }
    
    public void setProjects(Set projects) {
        this.projects = projects;
    }




}

