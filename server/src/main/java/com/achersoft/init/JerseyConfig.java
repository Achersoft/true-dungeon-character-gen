package com.achersoft.init;

import com.achersoft.exception.handlers.AccessDeniedExceptionHandler;
import com.achersoft.exception.handlers.AuthenticationExceptionHandler;
import com.achersoft.exception.handlers.InvalidDataExceptionHandler;
import com.achersoft.exception.handlers.NotFoundExceptionHandler;
import com.achersoft.exception.handlers.UncheckedExceptionHandler;
import com.achersoft.security.features.RequiresPrivilegeDynamicFeature;
import com.achersoft.security.filters.AuthenticationFilter;
import com.achersoft.security.filters.UserContextFilter;
import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.filter.RequestContextFilter;

@ApplicationPath("/*")
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        packages("com.achersoft.rest.services");
        
        // register filters
        register(RequestContextFilter.class);
        register(AuthenticationFilter.class);
        register(UserContextFilter.class);
        
        // register exception handlers
        register(UncheckedExceptionHandler.class);
        register(AuthenticationExceptionHandler.class);
        register(AccessDeniedExceptionHandler.class);
        register(InvalidDataExceptionHandler.class);
        register(NotFoundExceptionHandler.class);
        register(DuplicateKeyException.class);
        
        // register features
        register(JacksonFeature.class);
        register(RequiresPrivilegeDynamicFeature.class);
        register(MultiPartFeature.class);
    }
}