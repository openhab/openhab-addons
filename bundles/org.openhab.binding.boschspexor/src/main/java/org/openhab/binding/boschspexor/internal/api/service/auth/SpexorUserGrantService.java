package org.openhab.binding.boschspexor.internal.api.service.auth;

import static org.openhab.binding.boschspexor.internal.BoschSpexorBindingConstants.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.oauth2.sdk.http.HTTPRequest;

@Component(service = SpexorUserGrantService.class, configurationPid = "binding.spexor.userGrantService")
public class SpexorUserGrantService {
    private static final String HTML_FOLDER = "html/";
    private static final String HTML_DEVICE_CODE_AUTH = HTML_FOLDER + "deviceCode.html";

    private final Logger logger = LoggerFactory.getLogger(SpexorUserGrantService.class);

    private HttpService httpService;
    private BundleContext bundleContext;

    private HTTPRequest httpRequest;
    private SpexorAuthorizationService authService;

    @Activate
    protected void activate(ComponentContext componentContext, Map<String, Object> properties) {
        try {
            bundleContext = componentContext.getBundleContext();
            httpService.registerServlet(SPEXOR_OPENHAB_URL, createServlet(), new Hashtable<>(),
                    httpService.createDefaultHttpContext());
            httpService.registerResources(SPEXOR_OPENHAB_RESOURCES_URL, "html/resources", null);
        } catch (NamespaceException | ServletException | IOException e) {
            logger.error("unknown error in openHAB spexor grant service", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        httpService.unregister(SPEXOR_OPENHAB_URL);
        httpService.unregister(SPEXOR_OPENHAB_RESOURCES_URL);
    }

    public void initialize(SpexorAuthorizationService authService) {
        this.authService = authService;
    }

    /**
     * Creates a new {@link SpexorAuthServlet}.
     *
     * @return the newly created servlet
     * @throws IOException thrown when an invalid state in the processing occurs
     */
    private HttpServlet createServlet() throws IOException {
        return new SpexorAuthServlet(this, loadHtmlResource(HTML_DEVICE_CODE_AUTH));
    }

    /**
     * Reads a template from file and returns the content as String.
     *
     * @param htmlFilePath name of the html file to read
     * @return The content of the html file
     * @throws IOException thrown when the requested resource couldn't be loaded
     */
    private String loadHtmlResource(String htmlFilePath) throws IOException {
        final URL index = bundleContext.getBundle().getEntry(htmlFilePath);
        String result = null;
        if (index == null) {
            throw new FileNotFoundException(
                    String.format("'{}' was not found. Failed to initialize spexor auth servlet", htmlFilePath));
        } else {
            try (InputStream inputStream = index.openStream()) {
                result = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
        return result;
    }

    @Reference
    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

    public SpexorAuthorizationService getAuthService() {
        return authService;
    }
}
