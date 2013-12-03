package edu.miami.ccs.goma.pojos;
// Generated May 4, 2012 4:38:51 PM by Hibernate Tools 3.4.0.CR1


import java.util.HashSet;
import java.util.Set;

/**
 * UserRole generated by hbm2java
 */
public class UserRole  implements java.io.Serializable {


     private long userRoleId;
     private String username;
     private String role;
     private Set users = new HashSet(0);

    public UserRole() {
    }

	
    public UserRole(String username, String role) {

        this.username = username;
        this.role = role;
    }
    public UserRole(long userRoleId, String username, String role, Set users) {
       this.userRoleId = userRoleId;
       this.username = username;
       this.role = role;
       this.users = users;
    }
   
    public long getUserRoleId() {
        return this.userRoleId;
    }
    
    public void setUserRoleId(long userRoleId) {
        this.userRoleId = userRoleId;
    }
    public String getUsername() {
        return this.username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    public String getRole() {
        return this.role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    public Set getUsers() {
        return this.users;
    }
    
    public void setUsers(Set users) {
        this.users = users;
    }




}

