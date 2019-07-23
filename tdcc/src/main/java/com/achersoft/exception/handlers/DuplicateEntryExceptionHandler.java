package com.achersoft.exception.handlers;

import com.achersoft.exception.SystemError;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.springframework.dao.DuplicateKeyException;

@Provider
public class DuplicateEntryExceptionHandler implements ExceptionMapper<DuplicateKeyException> {
    
    @Override
    public Response toResponse(DuplicateKeyException e) {
        return Response.status(Response.Status.BAD_REQUEST.getStatusCode())
                       .entity(SystemError.builder()
                                           .status(Response.Status.BAD_REQUEST.getStatusCode())
                                           .message("Entry already exists.")
                                           .build())
                       .type(MediaType.APPLICATION_JSON)
                       .build();	
    }
}
