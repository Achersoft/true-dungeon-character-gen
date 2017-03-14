package com.achersoft.exception.handlers;

import com.achersoft.exception.SystemError;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.springframework.dao.DataAccessException;

@Provider
public class DataAccessExceptionHandler implements ExceptionMapper<DataAccessException> {
    
    @Override
    public Response toResponse(DataAccessException e) {
        Logger.getLogger(DataAccessExceptionHandler.class.getName()).log(Level.SEVERE, null, e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                       .entity(SystemError.builder()
                                           .status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                                           .message("Persistence layer exception.")
                                           .build())
                       .type(MediaType.APPLICATION_JSON)
                       .build();
    }
}
