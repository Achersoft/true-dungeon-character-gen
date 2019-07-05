package com.achersoft.exception.handlers;

import com.achersoft.exception.AuthenticationException;
import com.achersoft.exception.SystemError;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class AuthenticationExceptionHandler implements ExceptionMapper<AuthenticationException> {
	
    @Override 
    public Response toResponse(AuthenticationException e) {
        return Response.status(Response.Status.UNAUTHORIZED.getStatusCode())
                       .entity(SystemError.builder()
                                           .status(Response.Status.UNAUTHORIZED.getStatusCode())
                                           .message(e.error)
                                           .build())
                       .type(MediaType.APPLICATION_JSON)
                       .build();	
    }
}
