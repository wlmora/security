package com.business.security.user;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;

@DgsComponent
public class TestDatafetcher {

    @DgsQuery
    public String secureNone() {
        return "Hello Sprig Boot With Keycloak to everyone";
    }
    @DgsQuery
    @PreAuthorize("hasRole('user_client') or hasRole('admin_client')")
    public String secureUser() {
        return "Hello Sprig Boot With Keycloak with USER and ADMIN";
    }

    @DgsQuery
    @PreAuthorize("hasRole('admin_client')")
    public String secureAdmin() {
        return "Hello Sprig Boot With Keycloak with ADMIN";
    }
}
