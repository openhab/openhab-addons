package org.openhab.binding.vthing;

import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.http.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// http://docs.osgi.org/specification/osgi.cmpn/7.0.0/service.http.html#d0e6684

/**
 * @author Juergen Weber - Initial contribution
 */
public class VLampHttpContext implements HttpContext {

    private final Logger logger = LoggerFactory.getLogger(VLampHttpContext.class);

    public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return true;
    }

    public URL getResource(String name) {
        logger.debug("getResource: {}", name);
        return getClass().getResource(name);
    }

    public String getMimeType(String name) {
        return null;
    }
}
