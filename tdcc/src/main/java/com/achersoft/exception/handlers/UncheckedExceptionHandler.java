package com.achersoft.exception.handlers;

import com.achersoft.exception.SystemError;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class UncheckedExceptionHandler implements ExceptionMapper<Throwable> {
	
    @Override 
    public Response toResponse(Throwable e) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                       .entity(SystemError.builder()
                                           .status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                                           .message("An unknown error occurred")
                                           .build())
                       .type(MediaType.APPLICATION_JSON)
                       .build();	
    }
}
