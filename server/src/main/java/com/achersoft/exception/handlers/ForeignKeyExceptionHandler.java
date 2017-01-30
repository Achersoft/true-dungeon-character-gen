package com.achersoft.exception.handlers;

import com.achersoft.exception.SystemError;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.springframework.dao.DataIntegrityViolationException;

@Provider
public class ForeignKeyExceptionHandler implements ExceptionMapper<DataIntegrityViolationException> {

    @Override
    public Response toResponse(DataIntegrityViolationException e) {
        Logger.getLogger(DataIntegrityViolationException.class.getName()).log(Level.SEVERE, null, e);
        return Response.status(Response.Status.BAD_REQUEST.getStatusCode())
                       .entity(SystemError.builder()
                                           .status(Response.Status.BAD_REQUEST.getStatusCode())
                                           .code(SystemError.INVALID_REQUEST_DATA)
                                           .message("Entry has dependent foreign keys.")
                                           .build())
                       .type(MediaType.APPLICATION_JSON)
                       .build();
    }
}
