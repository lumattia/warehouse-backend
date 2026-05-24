package com.demo.warehouse.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.nio.file.AccessDeniedException;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.demo.warehouse.tenantFilter.UserContextHolder;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String username;

    @Column
    private Long activeUserContextId;

    @Column(unique = true)
    private String auth0Sub;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_allowed_tenants",
        joinColumns = @JoinColumn(name = "user_id", foreignKey = @jakarta.persistence.ForeignKey(foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE")),
        inverseJoinColumns = @JoinColumn(
            name = "tenant_id", 
            foreignKey = @jakarta.persistence.ForeignKey(foreignKeyDefinition = "FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE") 
        )
    )
    @JsonIgnore
    private Set<Tenant> allowedTenants = new HashSet<>();
    
    public void setRole(UserRole role) {
        validateRoleAssignment(role);
        this.role = role;
    }
    
    public void validateRoleAssignment(UserRole newRole) {
        UserRole currentUserRole = UserContextHolder.get().getUser().getRole();
        
        // no changes
        if (newRole == this.getRole()) return;

        // superadmin can do anything
        if(currentUserRole == UserRole.SUPERADMIN) return;

        // user can do nothing
        if (currentUserRole == UserRole.USER) throw new IllegalArgumentException("USER role is not allowed to change role");
        
        // If the new rol is Reseller, can only be assigned by a superadmin
        if (newRole == UserRole.RESELLER) throw new IllegalArgumentException("RESELLER role can only be assigned by a superadmin");
        
        // If the new rol is Superadmin, can only be assigned by a superadmin
        if (newRole == UserRole.SUPERADMIN) throw new IllegalArgumentException("SUPERADMIN role can only be assigned by a superadmin");
    }

    public static void validateCanModifyUser(User targetUser) {
        User actor = UserContextHolder.get().getUser();
        
        // superadmin can do anything
        if (actor.getRole() == UserRole.SUPERADMIN) return; 
        
        // allow editing their own data
        if (actor.getId().equals(targetUser.getId())) return; 

        // A normal USER cannot edit anyone else
        if (actor.getRole() == UserRole.USER) throw new IllegalArgumentException("A user can't edit anyone.");

        // Nobody (except another SuperAdmin) can touch a SUPERADMIN
        if (targetUser.getRole() == UserRole.SUPERADMIN || targetUser.getRole() == UserRole.RESELLER) {
            throw new IllegalArgumentException("Can't edit the target user.");
        }
    }
}
