package com.achersoft.exception.handlers;

import com.achersoft.exception.InvalidDataException;
import com.achersoft.exception.SystemError;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class InvalidDataExceptionHandler implements ExceptionMapper<InvalidDataException> {

    @Override
    public Response toResponse(InvalidDataException e) {
        Logger.getLogger(InvalidDataException.class.getName()).log(Level.SEVERE, null, e);
        return Response.status(Response.Status.BAD_REQUEST.getStatusCode())
                       .entity(SystemError.builder()
                                           .status(Response.Status.BAD_REQUEST.getStatusCode())
                                           .code(e.error.code)
                                           .message(e.error.message)
                                           .build())
                       .type(MediaType.APPLICATION_JSON)
                       .build();
    }
}
