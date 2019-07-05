package com.achersoft.exception.handlers;

import com.achersoft.exception.SystemError;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class NotFoundExceptionHandler implements ExceptionMapper<NotFoundException> {
    @Override
    public Response toResponse(NotFoundException e) {
        return Response.status(Response.Status.BAD_REQUEST.getStatusCode())
                       .entity(SystemError.builder()
                                           .status(Response.Status.BAD_REQUEST.getStatusCode())
                                           .message(e.getMessage())
                                           .build())
                       .type(MediaType.APPLICATION_JSON)
                       .build();
    }
}
