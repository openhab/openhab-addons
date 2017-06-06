/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.energenie.api.manager

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.openhab.binding.energenie.internal.api.manager.EnergenieApiManagerImpl.*
import groovy.json.JsonOutput

import javax.net.ssl.SSLContext
import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.apache.commons.io.IOUtils
import org.eclipse.jetty.http.HttpStatus
import org.eclipse.jetty.http.HttpVersion
import org.eclipse.jetty.server.HttpConfiguration
import org.eclipse.jetty.server.HttpConnectionFactory
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.server.SslConnectionFactory
import org.eclipse.jetty.util.ssl.SslContextFactory
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.openhab.binding.energenie.internal.api.EnergenieDeviceTypes
import org.openhab.binding.energenie.internal.api.manager.*
import org.openhab.binding.energenie.internal.rest.*
import org.openhab.binding.energenie.internal.ssl.SSLContextBuilder
import org.openhab.binding.energenie.test.FailingRequestHandlerMock
import org.osgi.framework.FrameworkUtil
import org.osgi.service.http.HttpContext
import org.osgi.service.http.HttpService
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.google.gson.JsonObject
/**
 * Test class for {@link EnergenieApiManager}
 *
 * @author Svilen Valkanov
 */

@RunWith(Parameterized.class)
class EnergenieApiManagerOSGiTest extends OSGiTest {
    //Parameters for the parameterized test

    /** Path of the test servlet */
    private def path

    /** Content of the request that will be send to the server */
    private def requestContent

    /** A callback object that executes the request */
    private Callback callback

    /** HTTP status of the server's response */
    private def status

    /** Determines if the status of the HTTP request is expected to be anything different from OK_200 */
    private def httpRequestFailed

    /** Determines if the status of the JSON request is expected to be anything different from "status":"success" */
    private def jsonRequestFailed

    /** Determines if an IOException is expected to be thrown */
    private def IOExceptionCaught

    /** An instance of the {@link FailingRequestHandler} */
    private FailingRequestHandlerMock failingRequestHandler

    private Logger logger = LoggerFactory.getLogger(EnergenieApiManagerOSGiTest.class)

    // Server information
    public static final String PROTOCOL = "https"
    public static final String HOST = "localhost"
    public static final int SECURE_PORT = 9443;
    public static final String TEST_URL = "${PROTOCOL}://${HOST}:${SECURE_PORT}";

    // Authentication data
    public static final String TEST_USERNAME = "test@gmail.com";
    public static final String TEST_API_KEY = "61481151528df0db511ajg5efdf0e3f362b15a";

    // Key store info
    /** Path relative to the bundle root directory */
    public static final String KEYSTORE_PATH = "SSL"
    public static final String KEYSTORE_NAME = "keystore"
    public static final String KEYSTORE_PASSWORD = "mihome"

    //Devices info
    private static int TEST_DEVICE_ID = 5464
    private static int TEST_SUBDEVICE_ID = 4515
    private static final String TEST_LABEL = "label"
    private static final String TEST_DEVICE_AUTH_CODE = "authCode"
    private static final EnergenieDeviceTypes TEST_DEVICE_TYPE = EnergenieDeviceTypes.MOTION_SENSOR

    public static final int TEST_CONNECTION_TIMEOUT = 1000;

    HttpServlet servlet
    static EnergenieApiManager apiManager

    public EnergenieApiManagerOSGiTest(def path, def requestContent, Callback callback, def HTTPRequestFailed, def jsonRequestFailed, def IOExceptionCaught) {
        this.path = path;
        this.requestContent = requestContent;
        this.callback = callback;
        this.httpRequestFailed = HTTPRequestFailed;
        this.jsonRequestFailed = jsonRequestFailed;
        this.IOExceptionCaught = IOExceptionCaught;
    }

    @Parameterized.Parameters
    public static Collection getParams() {
        // Each case tests specific request to the Mi|Home REST API
        // The first parameter is the path where the server expects to receive the request
        // The second parameter is the content that the server expects to receive (parameters stored in JSON String)
        // The third parameter is a callback object that executes the request
        // The fourth parameter indicates whether the request should be successful
        // The fifth parameter indicates whether the HTTP request is expected to fail
        // The sixth parameter indicates whether the JSON request is expected to fail
        // The seventh parameter indicates whether an IOException is expected

        def cases = new Object[11][6]
        // See <a href="https://mihome4u.co.uk/docs/api-documentation/subdevices-api/list-all-subdevices"/a>
        // for information about the expected parameters
        cases[0][0] = "/${CONTROLLER_SUBDEVICES}/${ACTION_LIST}"
        cases[0][1] = MiHomeServlet.EMPTY_JSON
        cases[0][2] = [execute: { -> return apiManager.listSubdevices()}] as Callback
        cases[0][3] = false;
        cases[0][4] = false;
        cases[0][5] = false;


        // See <a href="https://mihome4u.co.uk/docs/api-documentation/subdevices-api/show-subdevice-information"/a>
        cases[1][0] = "/${CONTROLLER_SUBDEVICES}/${ACTION_SHOW}"
        cases[1][1] = JsonOutput.toJson([id: TEST_SUBDEVICE_ID, include_usage_data: 0])
        cases[1][2] = [execute: { -> return apiManager.showSubdeviceInfo(TEST_SUBDEVICE_ID)}] as Callback
        cases[1][3] = false;
        cases[1][4] = false;
        cases[1][5] = false;

        // See <a href="https://mihome4u.co.uk/docs/api-documentation/subdevices-api/register-a-new-subdevice"/a>
        cases[2][0] = "/${CONTROLLER_SUBDEVICES}/${ACTION_CREATE}"
        cases[2][1] = JsonOutput.toJson([device_id: TEST_DEVICE_ID, device_type: TEST_DEVICE_TYPE.toString()])
        cases[2][2] = [execute: { -> return apiManager.registerSubdevice(TEST_DEVICE_ID,TEST_DEVICE_TYPE)}] as Callback
        cases[2][3] = false;
        cases[2][4] = false;
        cases[2][5] = false;

        // See <a href="https://mihome4u.co.uk/docs/api-documentation/subdevices-api/update-a-subdevice"/a>
        cases[3][0] = "/${CONTROLLER_SUBDEVICES}/${ACTION_UPDATE}"
        cases[3][1] = JsonOutput.toJson([id: TEST_SUBDEVICE_ID, label: TEST_LABEL])
        cases[3][2] = [execute: { -> return apiManager.updateSubdevice(TEST_SUBDEVICE_ID,TEST_LABEL)}] as Callback
        cases[3][3] = false;
        cases[3][4] = false;
        cases[3][5] = false;

        // See <a href="https://mihome4u.co.uk/docs/api-documentation/subdevices-api/delete-a-subdevice"/a>
        cases[4][0] = "/${CONTROLLER_SUBDEVICES}/${ACTION_DELETE}"
        cases[4][1] = JsonOutput.toJson([id: TEST_SUBDEVICE_ID])
        cases[4][2] = [execute: { -> return apiManager.unregisterSubdevice(TEST_SUBDEVICE_ID)}] as Callback
        cases[4][3] = false;
        cases[4][4] = false;
        cases[4][5] = false;

        // See <a href="https://mihome4u.co.uk/docs/api-documentation/devices-api/register-a-new-device"/a>
        cases[5][0] = "/${CONTROLLER_DEVICES}/${ACTION_CREATE}"
        cases[5][1] = JsonOutput.toJson([device_type: "gateway", label: TEST_LABEL, auth_code: TEST_DEVICE_AUTH_CODE])
        cases[5][2] = [execute: { -> return apiManager.registerGateway(TEST_LABEL,TEST_DEVICE_AUTH_CODE)}] as Callback
        cases[5][3] = false;
        cases[5][4] = false;
        cases[5][5] = false;

        // See <a href="https://mihome4u.co.uk/docs/api-documentation/devices-api/list-all-devices"/a>
        cases[6][0] = "/${CONTROLLER_DEVICES}/${ACTION_LIST}"
        cases[6][1] = MiHomeServlet.EMPTY_JSON
        cases[6][2] = [execute: { -> return apiManager.listGateways()}] as Callback
        cases[6][3] = false;
        cases[6][4] = false;
        cases[6][5] = false;

        // See <a href="https://mihome4u.co.uk/docs/api-documentation/devices-api/delete-a-device"/a>
        cases[7][0] = "/${CONTROLLER_DEVICES}/${ACTION_DELETE}"
        cases[7][1] = JsonOutput.toJson([id: TEST_DEVICE_ID])
        cases[7][2] = [execute: { -> return apiManager.unregisterGateway(TEST_DEVICE_ID)}] as Callback
        cases[7][3] = false;
        cases[7][4] = false;
        cases[7][5] = false;

        // test if failing HTTP request is handled properly. In this case the type of the request does not matter
        cases[8][0] = "/${CONTROLLER_DEVICES}/${ACTION_LIST}"
        cases[8][1] = MiHomeServlet.EMPTY_JSON
        cases[8][2] = [execute: { -> return apiManager.listGateways()}] as Callback
        cases[8][3] = true;
        cases[8][4] = false;
        cases[8][5] = false;

        // test if failing JSON request is handled properly. In this case the type of the request does not matter
        cases[9][0] = "/${CONTROLLER_DEVICES}/${ACTION_LIST}"
        cases[9][1] = MiHomeServlet.EMPTY_JSON
        cases[9][2] = [execute: { -> return apiManager.listGateways()}] as Callback
        cases[9][3] = false;
        cases[9][4] = true;
        cases[9][5] = false;

        // test if an IOException is handled properly. In this case the type of the request does not matter
        cases[10][0] = "/${CONTROLLER_DEVICES}/${ACTION_LIST}"
        cases[10][1] = MiHomeServlet.EMPTY_JSON
        cases[10][2] = [execute: { -> return apiManager.listGateways()}] as Callback
        cases[10][3] = false;
        cases[10][4] = false;
        cases[10][5]= true;

        return cases
    }

    /** A Servlet that will mock the response from the Mi|Home Server */
    class MiHomeServlet extends HttpServlet {

        public static final String EMPTY_JSON = "{}"

        String expectedContent
        String responseContent
        int timeout

        MiHomeServlet (String expectedContent, int timeout) {
            this.expectedContent = expectedContent
            this.timeout = timeout

            if(!httpRequestFailed && !jsonRequestFailed && !IOExceptionCaught) {
                this.responseContent = "{\"status\":\"success\"}"
            } else if(jsonRequestFailed){
                this.responseContent ="{\"status\":\"not-found\"}"
            } else {
                this.responseContent ="{}"
            }
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            //Check the request
            String  contentType = req.getHeader("Content-Type")
            assertThat contentType, is(equalTo(RestClient.CONTENT_TYPE))

            String autorizationHeader = req.getHeader("Authorization")
            assertAuthorizationHeader(autorizationHeader)

            String jsonString = IOUtils.toString(req.getInputStream())
            assertThat jsonString,is(equalTo(expectedContent))

            if(IOExceptionCaught) {
                sleep(TEST_CONNECTION_TIMEOUT + 1000)
            }
            if(httpRequestFailed) {
                // setting a status different from OK_200
                resp.setStatus(HttpStatus.FORBIDDEN_403)
            } else {
                resp.setStatus(HttpStatus.OK_200)
            }
            resp.setContentType(RestClient.CONTENT_TYPE)
            PrintWriter out = resp.getWriter()
            out.print(this.responseContent)
        }

        private boolean assertAuthorizationHeader(String header) {
            String hash = header.replace("Basic ","")
            String authInfo = new String(Base64.getDecoder().decode(hash));
            String [] credentials = authInfo.split(":")
            assertThat "Unexpected username", credentials[0], is(equalTo(TEST_USERNAME))
            assertThat "Unexpected API key", credentials[1], is(equalTo(TEST_API_KEY))
        }

    }

    interface Callback {
        JsonObject execute();
    }

    @BeforeClass
    public static void setUp() {
        configureSslOnServer()

    }

    @Before
    public void before() {
        registerServlet(path)
        apiManager = configureApiManager()
    }

    @After
    public void tearDown (){
        unregisterServlet(path)
        failingRequestHandler.setHttpRequestFailed(false)
        failingRequestHandler.setJsonRequestFailed(false)
        failingRequestHandler.setIOExceptionCaught(false)
    }

    @Test
    public void test() throws Exception {
        waitForAssert {
            String response = callback.execute()
            assertThat "Unexpected HTTP status", httpRequestFailed, is(failingRequestHandler.isHttpRequestFailed())
            assertThat "Unexpected IO exception handling", IOExceptionCaught, is(failingRequestHandler.isIOExceptionCaught())
            assertThat "Unexpected JSON response handling", jsonRequestFailed, is(failingRequestHandler.isJsonRequestFailed())
        }
    }
    protected static void configureSslOnServer() {
        Server server = null

        server = getServiceFromStaticContext(Server)
        assertThat ("The Server is not registered as OSGi service", server, is(notNullValue()))

        HttpConfiguration httpsConfig = new HttpConfiguration()
        httpsConfig.setSecureScheme(PROTOCOL)
        httpsConfig.setSecurePort(SECURE_PORT)

        // Set the SSLContext with trusted clients
        SSLContext context = SSLContextBuilder.create(FrameworkUtil.getBundle(EnergenieApiManagerOSGiTest).getBundleContext()).withKeyManagers(KEYSTORE_PATH,KEYSTORE_NAME,KEYSTORE_PASSWORD).build()
        assertThat  "Couldn't load key store from /${KEYSTORE_PATH}/${KEYSTORE_NAME}", context, is(notNullValue())
        SslContextFactory sslContextFactory =  new SslContextFactory()
        sslContextFactory.setSslContext(context)

        SslConnectionFactory sslConnFactory = new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString())
        HttpConnectionFactory httpConnFactory = new HttpConnectionFactory(httpsConfig)

        //Configure the port and start the new server connector
        ServerConnector https = new ServerConnector(server, sslConnFactory, httpConnFactory)
        https.setHost(HOST)
        https.setPort(SECURE_PORT)
        server.addConnector(https)
        https.start()
    }


    private EnergenieApiManager configureApiManager() {
        // Configure the EnergenieApiManager
        EnergenieApiConfiguration config = new EnergenieApiConfiguration(TEST_USERNAME, TEST_API_KEY);

        // Load the trusted key store from file
        SSLContext sslContext = SSLContextBuilder.create(FrameworkUtil.getBundle(EnergenieApiManagerOSGiTest).getBundleContext()).withTrustManagers(KEYSTORE_PATH,KEYSTORE_NAME,KEYSTORE_PASSWORD).build()
        assertThat "Couldn't load key store from /${KEYSTORE_PATH}/${KEYSTORE_NAME}", sslContext, is(notNullValue())

        //Inject the test SSLContext
        RestClient client = getServiceFromStaticContext(RestClient)
        assertThat client, is(notNullValue())
        client.httpClient.stop()
        client.httpClient.getSslContextFactory().setSslContext(sslContext)
        client.httpClient.start()
        client.setBaseURL(TEST_URL)
        client.setConnectionTimeout(TEST_CONNECTION_TIMEOUT)
        failingRequestHandler = new FailingRequestHandlerMock();
        return new EnergenieApiManagerImpl(config, client, failingRequestHandler);
    }

    protected void registerServlet(String path){
        servlet = new MiHomeServlet(requestContent, TEST_CONNECTION_TIMEOUT)
        HttpService httpService
        httpService = getService(HttpService)
        assertThat ("The HttpService cannot be found", httpService, is(notNullValue()))

        def disableAuthenticationHttpContext = [handleSecurity: { HttpServletRequest request, HttpServletResponse response ->
                return true
            }] as HttpContext
        httpService.registerServlet(path, servlet, null, disableAuthenticationHttpContext)
    }

    protected void unregisterServlet(String path){
        waitForAssert {
            HttpService httpService = getService(HttpService)
            assertThat httpService, is(notNullValue())
            httpService.unregister(path)
            servlet = null
        }
    }


    static <T> T getServiceFromStaticContext(Class<T> clazz) {
        def bundle = FrameworkUtil.getBundle(EnergenieApiManagerOSGiTest);
        def bundleContext = bundle.getBundleContext()
        def serviceReference = bundleContext.getServiceReference(clazz.name)
        return serviceReference ? bundleContext.getService(serviceReference) : null
    }
}
