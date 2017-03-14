package com.achersoft.exception.handlers;

import com.achersoft.exception.SystemError;
import java.nio.file.AccessDeniedException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class AccessDeniedExceptionHandler implements ExceptionMapper<AccessDeniedException> {
	
    @Override 
    public Response toResponse(AccessDeniedException e) { 
        return Response.status(Response.Status.FORBIDDEN.getStatusCode())
                       .entity(SystemError.builder()
                                           .status(Response.Status.FORBIDDEN.getStatusCode())
                                           .message(e.getMessage())
                                           .build())
                       .type(MediaType.APPLICATION_JSON)
                       .build();	
    }
}
