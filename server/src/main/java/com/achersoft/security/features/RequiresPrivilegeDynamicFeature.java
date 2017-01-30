package com.achersoft.security.features;

import com.achersoft.security.annotations.RequiresPrivilege;
import com.achersoft.security.context.CustomSecurityContext;
import com.achersoft.security.type.Privilege;
import java.io.IOException;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

import javax.annotation.Priority;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;

import org.glassfish.jersey.server.model.AnnotatedMethod;

public class RequiresPrivilegeDynamicFeature implements DynamicFeature {

    @Override
    public void configure(final ResourceInfo resourceInfo, final FeatureContext configuration) {
        final AnnotatedMethod am = new AnnotatedMethod(resourceInfo.getResourceMethod());

        // DenyAll on the method take precedence over RolesAllowed and PermitAll
        if (am.isAnnotationPresent(DenyAll.class)) {
            configuration.register(new RequiresPrivliageRequestFilter());
            return;
        }

        // RequiresPrivliage on the method takes precedence over PermitAll
        RequiresPrivilege ra = am.getAnnotation(RequiresPrivilege.class);
        if (ra != null) {
            configuration.register(new RequiresPrivliageRequestFilter(ra.value()));
            return;
        }

        // PermitAll takes precedence over RolesAllowed on the class
        if (am.isAnnotationPresent(PermitAll.class)) {
            // Do nothing.
            return;
        }

        // DenyAll can't be attached to classes

        // RolesAllowed on the class takes precedence over PermitAll
        ra = resourceInfo.getResourceClass().getAnnotation(RequiresPrivilege.class);
        if (ra != null) {
            configuration.register(new RequiresPrivliageRequestFilter(ra.value()));
        }
    }

    @Priority(Priorities.AUTHORIZATION)
    private static class RequiresPrivliageRequestFilter implements ContainerRequestFilter {

        private final boolean denyAll;
        private final Privilege[] privilegesAllowed;

        RequiresPrivliageRequestFilter() {
            this.denyAll = true;
            this.privilegesAllowed = null;
        }

        RequiresPrivliageRequestFilter(final Privilege[] privilegesAllowed) {
            this.denyAll = false;
            this.privilegesAllowed = (privilegesAllowed != null) ? privilegesAllowed : new Privilege[] {};
        }

        @Override
        public void filter(final ContainerRequestContext requestContext) throws IOException {
            if (!denyAll) {
                if (privilegesAllowed.length > 0 && !isAuthenticated(requestContext)) {
                    throw new ForbiddenException();
                }

                for (final Privilege privilege : privilegesAllowed) {
                    if (((CustomSecurityContext)requestContext.getSecurityContext()).userHasPrivilege(privilege)) {
                        return;
                    }
                }
            }

            throw new ForbiddenException();
        }

        private static boolean isAuthenticated(final ContainerRequestContext requestContext) {
            return requestContext.getSecurityContext().getUserPrincipal() != null;
        }
    }
}