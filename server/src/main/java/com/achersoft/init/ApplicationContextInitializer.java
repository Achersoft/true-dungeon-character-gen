package com.achersoft.init;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApplicationContextInitializer implements WebApplicationInitializer  {

    @Override
    public void onStartup(ServletContext ctx) {
        // Create the 'root' Spring application context
        ctx.addListener(ContextLoaderListener.class);
        ctx.addListener(RequestContextListener.class);
        ctx.setInitParameter(ContextLoader.CONTEXT_CLASS_PARAM, AnnotationConfigWebApplicationContext.class.getName());
        ctx.setInitParameter(ContextLoader.CONFIG_LOCATION_PARAM, SpringConfig.class.getName());
        
        // Register and map the dispatcher servlet
        final ServletRegistration.Dynamic dispatcher = ctx.addServlet("td-character-gen", ServletContainer.class.getName());
        dispatcher.setInitParameter("javax.ws.rs.Application", JerseyConfig.class.getName());
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/*");
        
    }
}
