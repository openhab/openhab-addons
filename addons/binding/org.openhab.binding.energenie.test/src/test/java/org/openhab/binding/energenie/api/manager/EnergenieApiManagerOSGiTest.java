/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.energenie.api.manager;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openhab.binding.energenie.internal.api.JsonDevice;
import org.openhab.binding.energenie.internal.api.manager.EnergenieApiConfiguration;
import org.openhab.binding.energenie.internal.api.manager.EnergenieApiManager;
import org.openhab.binding.energenie.internal.api.manager.EnergenieApiManagerImpl;
import org.openhab.binding.energenie.internal.api.manager.FailingRequestHandler;
import org.openhab.binding.energenie.internal.rest.RestClient;
import org.openhab.binding.energenie.internal.rest.RestClientImpl;
import org.openhab.binding.energenie.internal.ssl.SSLContextBuilder;
import org.openhab.binding.energenie.test.FailingRequestHandlerMock;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

@RunWith(Parameterized.class)
public class EnergenieApiManagerOSGiTest extends JavaOSGiTest {

    private Logger logger = LoggerFactory.getLogger(EnergenieApiManagerOSGiTest.class);

    // Parameters for the parameterized test

    /** Path of the test servlet */
    private String path;

    /** Content of the request that will be send to the server */
    private String requestContent;

    /** A callback object that executes the request */
    private Callback callback;

    /** HTTP status of the server's response */
    private HttpStatus status;

    /** Determines if the status of the HTTP request is expected to be anything different from OK_200 */
    private boolean httpRequestFailed;

    /** Determines if the status of the JSON request is expected to be anything different from "status":"success" */
    private boolean jsonRequestFailed;

    /** Determines if an IOException is expected to be thrown */
    private boolean IOExceptionCaught;

    /** An instance of the {@link FailingRequestHandler} */
    private FailingRequestHandlerMock failingRequestHandler;

    private static Gson gson = new Gson();

    // Server information
    public static final String PROTOCOL = "https";
    public static final String HOST = "localhost";
    public static final int SECURE_PORT = 9443;
    public static final String TEST_URL = PROTOCOL + "://" + HOST + ":" + SECURE_PORT;

    // Authentication data
    public static final String TEST_USERNAME = "test@gmail.com";
    public static final String TEST_API_KEY = "61481151528df0db511ajg5efdf0e3f362b15a";

    // Key store info
    /** Path relative to the bundle root directory */
    public static final String KEYSTORE_PATH = "SSL";
    public static final String KEYSTORE_NAME = "keystore";
    public static final String KEYSTORE_PASSWORD = "mihome";

    // Devices info
    private static final int TEST_SUBDEVICE_ID = 4515;

    public static final int TEST_CONNECTION_TIMEOUT = 1000;

    HttpServlet servlet;
    static EnergenieApiManager apiManager;

    public EnergenieApiManagerOSGiTest(Logger logger, String path, HttpServlet servlet) {
        super();
        this.logger = logger;
        this.path = path;
        this.servlet = servlet;
    }

    interface Callback {
        JsonDevice[] execute();
    }

    @Parameterized.Parameters
    public static Object[][] getParams() {
        // Each case tests specific request to the Mi|Home REST API
        // The first parameter is the path where the server expects to receive the request
        // The second parameter is the content that the server expects to receive (parameters stored in JSON String)
        // The third parameter is a callback object that executes the request
        // The fourth parameter indicates whether the request should be successful
        // The fifth parameter indicates whether the HTTP request is expected to fail
        // The sixth parameter indicates whether the JSON request is expected to fail
        // The seventh parameter indicates whether an IOException is expected

        Object[][] cases = new Object[11][6];
        // See <a href="https://mihome4u.co.uk/docs/api-documentation/subdevices-api/list-all-subdevices"/a>
        // for information about the expected parameters
        cases[0][0] = EnergenieApiManagerImpl.CONTROLLER_SUBDEVICES + "/" + EnergenieApiManagerImpl.ACTION_LIST;
        cases[0][1] = MiHomeServlet.EMPTY_JSON;
        Callback firstCallback = () -> {
            return apiManager.listSubdevices();
        };
        cases[0][2] = firstCallback;
        cases[0][3] = false;
        cases[0][4] = false;
        cases[0][5] = false;

        // See <a href="https://mihome4u.co.uk/docs/api-documentation/subdevices-api/show-subdevice-information"/a>
        cases[1][0] = EnergenieApiManagerImpl.CONTROLLER_SUBDEVICES + "/" + EnergenieApiManagerImpl.ACTION_SHOW;
        Map<String, String> firstProperties = new LinkedHashMap<>();
        firstProperties.put(EnergenieApiManagerImpl.DEVICE_ID_KEY, Integer.toString(TEST_SUBDEVICE_ID));
        firstProperties.put(EnergenieApiManagerImpl.SUBDEVICE_INCLUDE_USAGE_DATA, "0");
        cases[1][1] = gson.toJson(firstProperties);
        Callback secondCallback = () -> {
            JsonDevice[] jsonDevices = new JsonDevice[1];
            jsonDevices[0] = apiManager.showSubdeviceInfo(TEST_SUBDEVICE_ID);
            return jsonDevices;
        };
        cases[1][2] = secondCallback;
        cases[1][3] = false;
        cases[1][4] = false;
        cases[1][5] = false;

        // See <a href="https://mihome4u.co.uk/docs/api-documentation/devices-api/list-all-devices"/a>
        cases[2][0] = EnergenieApiManagerImpl.CONTROLLER_DEVICES + "/" + EnergenieApiManagerImpl.ACTION_LIST;
        cases[2][1] = MiHomeServlet.EMPTY_JSON;
        Callback thirdCallback = () -> {
            return apiManager.listGateways();
        };
        cases[2][2] = thirdCallback;
        cases[2][3] = false;
        cases[2][4] = false;
        cases[2][5] = false;

        // test if failing HTTP request is handled properly. In this case the type of the request does not matter
        cases[3][0] = EnergenieApiManagerImpl.CONTROLLER_DEVICES + "/" + EnergenieApiManagerImpl.ACTION_LIST;
        cases[3][1] = MiHomeServlet.EMPTY_JSON;
        Callback fourthCallback = () -> {
            return apiManager.listGateways();
        };
        cases[3][2] = fourthCallback;
        cases[3][3] = true;
        cases[3][4] = false;
        cases[3][5] = false;

        // test if failing JSON request is handled properly. In this case the type of the request does not matter
        cases[4][0] = EnergenieApiManagerImpl.CONTROLLER_DEVICES + "/" + EnergenieApiManagerImpl.ACTION_LIST;
        cases[4][1] = MiHomeServlet.EMPTY_JSON;
        Callback fifthCallback = () -> {
            return apiManager.listGateways();
        };
        cases[4][2] = fifthCallback;
        cases[4][3] = false;
        cases[4][4] = true;
        cases[4][5] = false;

        // test if an IOException is handled properly. In this case the type of the request does not matter
        cases[5][0] = EnergenieApiManagerImpl.CONTROLLER_DEVICES + "/" + EnergenieApiManagerImpl.ACTION_LIST;
        cases[5][1] = MiHomeServlet.EMPTY_JSON;
        Callback sixthCallback = () -> {
            return apiManager.listGateways();
        };
        cases[5][2] = sixthCallback;
        cases[5][3] = false;
        cases[5][4] = false;
        cases[5][5] = true;

        return cases;
    }

    /** A Servlet that will mock the response from the Mi|Home Server */
    class MiHomeServlet extends HttpServlet {

        public static final String EMPTY_JSON = "{}";

        String expectedContent;
        String responseContent;
        int timeout;

        MiHomeServlet(String expectedContent, int timeout) {
            this.expectedContent = expectedContent;
            this.timeout = timeout;

            if (!httpRequestFailed && !jsonRequestFailed && !IOExceptionCaught) {
                this.responseContent = "{\"status\":\"success\"}";
            } else if (jsonRequestFailed) {
                this.responseContent = "{\"status\":\"not-found\"}";
            } else {
                this.responseContent = "{}";
            }
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            // Check the request
            String contentType = req.getHeader("Content-Type");
            assertEquals(contentType, RestClient.CONTENT_TYPE);

            String autorizationHeader = req.getHeader("Authorization");
            assertAuthorizationHeader(autorizationHeader);

            String jsonString = IOUtils.toString(req.getInputStream());
            assertEquals(jsonString, expectedContent);

            if (IOExceptionCaught) {
                try {
                    Thread.sleep(TEST_CONNECTION_TIMEOUT + 1000);
                } catch (InterruptedException e) {
                    logger.debug("Thread is interrupted {} ", e.getMessage(), e);
                }
            }
            if (httpRequestFailed) {
                // setting a status different from OK_200
                resp.setStatus(HttpStatus.FORBIDDEN_403);
            } else {
                resp.setStatus(HttpStatus.OK_200);
            }
            resp.setContentType(RestClient.CONTENT_TYPE);
            PrintWriter out = resp.getWriter();
            out.print(this.responseContent);
        }

        private void assertAuthorizationHeader(String header) {
            String hash = header.replace("Basic ", "");
            String authInfo = new String(Base64.getDecoder().decode(hash));
            String[] credentials = authInfo.split(":");
            assertEquals("Unexpected username", credentials[0], TEST_USERNAME);
            assertEquals("Unexpected API key", credentials[1], TEST_API_KEY);
        }

    }

    @BeforeClass
    public static void setUp() throws Exception {
        configureSslOnServer();
    }

    @Before
    public void before() throws Exception {
        registerServlet(path);
        apiManager = configureApiManager();
    }

    @After
    public void tearDown() {
        unregisterServlet(path);
        failingRequestHandler.setHttpRequestFailed(false);
        failingRequestHandler.setJsonRequestFailed(false);
        failingRequestHandler.setInputOutputException(false);
    }

    @Test
    public void test() throws Exception {
        waitForAssert(() -> {
            JsonDevice[] response = callback.execute();
            assertEquals("Unexpected HTTP status", httpRequestFailed, failingRequestHandler.isHttpRequestFailed());
            assertEquals("Unexpected IO exception handling", IOExceptionCaught,
                    failingRequestHandler.isInputOutputExceptionCaught());
            assertEquals("Unexpected JSON response handling", jsonRequestFailed,
                    failingRequestHandler.isJsonRequestFailed());
        });
    }

    protected static void configureSslOnServer() throws Exception {
        Server server = null;

        server = getServiceFromStaticContext(Server.class);
        assertNotNull("The Server is not registered as OSGi service", server);

        HttpConfiguration httpsConfig = new HttpConfiguration();
        httpsConfig.setSecureScheme(PROTOCOL);
        httpsConfig.setSecurePort(SECURE_PORT);

        // Set the SSLContext with trusted clients
        SSLContext context = SSLContextBuilder
                .create(FrameworkUtil.getBundle(EnergenieApiManagerOSGiTest.class).getBundleContext())
                .withKeyStore(KEYSTORE_PATH, KEYSTORE_NAME, KEYSTORE_PASSWORD).build();
        assertNotNull("Couldn't load key store from /" + KEYSTORE_PATH + "/" + KEYSTORE_NAME, context);
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setSslContext(context);

        SslConnectionFactory sslConnFactory = new SslConnectionFactory(sslContextFactory,
                HttpVersion.HTTP_1_1.asString());
        HttpConnectionFactory httpConnFactory = new HttpConnectionFactory(httpsConfig);

        // Configure the port and start the new server connector
        ServerConnector https = new ServerConnector(server, sslConnFactory, httpConnFactory);
        https.setHost(HOST);
        https.setPort(SECURE_PORT);
        server.addConnector(https);
        https.start();
    }

    private EnergenieApiManager configureApiManager() throws Exception {
        // Configure the EnergenieApiManager
        EnergenieApiConfiguration config = new EnergenieApiConfiguration(TEST_USERNAME, TEST_API_KEY);

        // Load the trusted key store from file
        SSLContext sslContext = SSLContextBuilder
                .create(FrameworkUtil.getBundle(EnergenieApiManagerOSGiTest.class).getBundleContext())
                .withTrustStore(KEYSTORE_PATH, KEYSTORE_NAME, KEYSTORE_PASSWORD).build();
        assertNotNull("Couldn't load key store from /" + KEYSTORE_PATH + "/" + KEYSTORE_NAME, sslContext);

        // Inject the test SSLContext
        RestClientImpl client = getServiceFromStaticContext(RestClientImpl.class);
        assertNotNull(client);
        client.getHttpClient().stop();
        client.getHttpClient().getSslContextFactory().setSslContext(sslContext);
        client.getHttpClient().start();
        client.setBaseURL(TEST_URL);
        client.setConnectionTimeout(TEST_CONNECTION_TIMEOUT);
        failingRequestHandler = new FailingRequestHandlerMock();
        return new EnergenieApiManagerImpl(config, client, failingRequestHandler);
    }

    protected void registerServlet(String path) throws ServletException, NamespaceException {
        servlet = new MiHomeServlet(requestContent, TEST_CONNECTION_TIMEOUT);
        HttpService httpService;
        httpService = getService(HttpService.class);
        assertNotNull("The HttpService cannot be found", httpService);

        HttpContext disableAuthenticationHttpContext = new HttpContext() {

            @Override
            public boolean handleSecurity(HttpServletRequest req, HttpServletResponse resp) throws IOException {
                return true;
            }

            @Override
            public String getMimeType(String arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public URL getResource(String arg0) {
                // TODO Auto-generated method stub
                return null;
            }

        };

        httpService.registerServlet(path, servlet, null, disableAuthenticationHttpContext);
    }

    protected void unregisterServlet(String path) {
        waitForAssert(() -> {
            HttpService httpService = getService(HttpService.class);
            assertNotNull(httpService);
            httpService.unregister(path);
            servlet = null;
        });
    }

    static <T> T getServiceFromStaticContext(Class<T> clazz) {
        Bundle bundle = FrameworkUtil.getBundle(EnergenieApiManagerOSGiTest.class);
        BundleContext bundleContext = bundle.getBundleContext();
        ServiceReference<T> serviceReference = bundleContext.getServiceReference(clazz);
        return serviceReference != null ? bundleContext.getService(serviceReference) : null;
    }

}
