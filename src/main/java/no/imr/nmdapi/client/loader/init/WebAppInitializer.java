package no.imr.nmdapi.client.loader.init;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import no.imr.framework.logging.logback.initalize.InitalizeLogbackHandler;
import no.imr.framework.logging.slf4j.exceptions.LoggerInitalizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.support.AbstractDispatcherServletInitializer;

/**
 *
 * @author sjurl
 */
public class WebAppInitializer extends AbstractDispatcherServletInitializer {

    /**
     * Class logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(WebAppInitializer.class);

    @Override
    protected WebApplicationContext createServletApplicationContext() {
        return new AnnotationConfigWebApplicationContext();
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/*"};
    }

    @Override
    protected WebApplicationContext createRootApplicationContext() {
        AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext();
        ctx.scan("no.imr.nmdapi.client.loader.config", "no.imr.nmdapi.client.loader.service");
        return ctx;
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        super.onStartup(servletContext);

        try {
            InitalizeLogbackHandler.getInstance().initalize(System.getProperty("catalina.base") + "/conf/mission_data_generator_logback_v1.xml", true);
        } catch (LoggerInitalizationException ex) {
            LOGGER.error("Logging initializaton failed.", ex);
        }
        LOGGER.info("Entering application.");
    }
}
