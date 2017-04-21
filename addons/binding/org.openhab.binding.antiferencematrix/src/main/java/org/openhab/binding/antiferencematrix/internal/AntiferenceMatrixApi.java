package org.openhab.binding.antiferencematrix.internal;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.antiferencematrix.internal.model.InputPortDetails;
import org.openhab.binding.antiferencematrix.internal.model.OutputPortDetails;
import org.openhab.binding.antiferencematrix.internal.model.Port;
import org.openhab.binding.antiferencematrix.internal.model.PortDeserializer;
import org.openhab.binding.antiferencematrix.internal.model.PortList;
import org.openhab.binding.antiferencematrix.internal.model.Response;
import org.openhab.binding.antiferencematrix.internal.model.SystemDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This class encapsulates the API of the Matrix to make calls easier.
 *
 * @author Neil
 *
 */
public class AntiferenceMatrixApi {

    private Logger logger = LoggerFactory.getLogger(AntiferenceMatrixApi.class);

    private final String url;

    private static final String POWER_FUNCTION_TEMPLATE = "%s/CEC/%s/Output/%s";
    private static final String ON_STRING = "On";
    private static final String OFF_STRING = "Off";

    private static final String URL_TEMPLATE = "http://%s";

    private static final String SOURCE_FUNCTION_TEMPLATE = "%s/Module/Set/%s/%s";

    private static final String SYSTEM_DETAILS_TEMPLATE = "%s/System/Details";

    private static final String PORT_LIST_TEMPLATE = "%s/Port/List";

    private static final String PORT_DETAILS_TEMPLATE = "%s/Port/Details/%s/%s";
    private static final String INPUT_STRING = "Input";
    private static final String OUTPUT_STRING = "Output";

    private final String systemDetailsUrl;
    private final String portListUrl;

    private final HttpClient httpClient;
    private final Gson gson;

    private boolean initalized = false;

    /**
     * Create an instance of the API to connect to a single matrix.
     *
     * You must call start() before using the api.
     *
     */
    public AntiferenceMatrixApi(String hostname) {
        httpClient = new HttpClient();
        gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .registerTypeAdapter(Port.class, new PortDeserializer()).create();
        this.url = String.format(URL_TEMPLATE, hostname);
        this.systemDetailsUrl = String.format(SYSTEM_DETAILS_TEMPLATE, url);
        this.portListUrl = String.format(PORT_LIST_TEMPLATE, url);
    }

    /**
     * Starts the API ready for use.
     *
     * @throws Exception If the HTTP client can't be started.
     */
    public void start() throws Exception {
        httpClient.start();
        initalized = true;
    }

    /**
     * Stops the API. You can start it again by calling start();
     *
     * @throws Exception If the HTTP client can't be stopped.
     */
    public void stop() throws Exception {
        httpClient.stop();
        initalized = false;
    }

    /**
     * Turns the power on or off for the given output port.
     * Note: For this to work the TV must support CEC and have it enabled.
     *
     * @param outputId
     * @param on
     * @return The response
     */
    public Response changePower(int outputId, boolean on) {
        String url = getPowerFunction(on, outputId);
        String response = callUrl(url);
        Response matrixResponse = gson.fromJson(response, Response.class);
        if (!matrixResponse.getResult()) {
            logger.error("Error calling changePower({}, {}), error: {}", outputId, on,
                    matrixResponse.getErrorMessage());
        }
        return matrixResponse;
    }

    /**
     * Change the source for the given output to the given input.
     *
     * @param outputId The output to change
     * @param inputId The input we want to use
     * @return The response
     */
    public Response changeSource(int outputId, int inputId) {
        String url = getSourceFunction(inputId, outputId);
        String response = callUrl(url);
        Response matrixResponse = gson.fromJson(response, Response.class);
        if (!matrixResponse.getResult()) {
            logger.error("Error calling changeSource({}, {}), error: {}", outputId, inputId,
                    matrixResponse.getErrorMessage());
        }
        return matrixResponse;
    }

    /**
     * Returns the system details from the matrix
     *
     * @return The SystemDetails of the matrix
     */
    public SystemDetails getSystemDetails() {
        String jsonResponse = callUrl(systemDetailsUrl);
        SystemDetails systemDetails = gson.fromJson(jsonResponse, SystemDetails.class);
        if (!systemDetails.getResult()) {
            logger.error("Error calling getSystemDetails(), error: {}", systemDetails.getErrorMessage());
        }
        return systemDetails;
    }

    /**
     * Returns the complete port list from the matrix
     *
     * @return The Port List
     */
    public PortList getPortList() {
        String jsonResponse = callUrl(portListUrl);
        PortList portList = gson.fromJson(jsonResponse, PortList.class);
        if (!portList.getResult()) {
            logger.error("Error calling getPortList(), error: {}", portList.getErrorMessage());
        }

        return portList;
    }

    /**
     * Get Input Port Details
     *
     * @param inputId The input port Id
     * @return The ports details
     */
    public InputPortDetails getInputPortDetails(int inputId) {
        String url = getInputPortDetailsFunction(inputId);
        String jsonResponse = callUrl(url);
        InputPortDetails inputPortDetails = gson.fromJson(jsonResponse, InputPortDetails.class);
        if (!inputPortDetails.getResult()) {
            logger.error("Error calling getInputPortDetails({}), error: {}", inputId,
                    inputPortDetails.getErrorMessage());
        }
        return inputPortDetails;
    }

    /**
     * Get Output Port Details
     *
     * @param outputId The output port Id
     * @return The ports details
     */
    public OutputPortDetails getOutputPortDetails(int outputId) {
        String url = getOutputPortDetailsFunction(outputId);
        String jsonResponse = callUrl(url);
        OutputPortDetails outputPortDetails = gson.fromJson(jsonResponse, OutputPortDetails.class);
        if (!outputPortDetails.getResult()) {
            logger.error("Error calling getOutputPortDetails({}), error: {}", outputId,
                    outputPortDetails.getErrorMessage());
        }
        return outputPortDetails;
    }

    /**
     * Calls the URL and returns the response as a String.
     *
     * @param url String of URL to call
     * @return The string response.
     */
    private String callUrl(String url) {
        if (!initalized) {
            throw new RuntimeException("You must call AntiferenceMatrixApi.start() before using the API");
        }

        ContentResponse response;
        try {
            logger.debug("Calling URL: {}", url);
            Request request = httpClient.newRequest(url).method(HttpMethod.GET).timeout(30000, TimeUnit.MILLISECONDS);
            response = request.send();
            logger.debug("Response: {}", response);
            return response.getContentAsString();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    private String getPowerFunction(boolean on, int outputId) {
        String onOrOff;
        if (on) {
            onOrOff = ON_STRING;
        } else {
            onOrOff = OFF_STRING;
        }
        return String.format(POWER_FUNCTION_TEMPLATE, url, onOrOff, outputId);
    }

    private String getSourceFunction(int sourceId, int outputId) {
        return String.format(SOURCE_FUNCTION_TEMPLATE, url, sourceId, outputId);
    }

    private String getInputPortDetailsFunction(int inputId) {
        return String.format(PORT_DETAILS_TEMPLATE, url, INPUT_STRING, inputId);
    }

    private String getOutputPortDetailsFunction(int outputId) {
        return String.format(PORT_DETAILS_TEMPLATE, url, OUTPUT_STRING, outputId);
    }

}
