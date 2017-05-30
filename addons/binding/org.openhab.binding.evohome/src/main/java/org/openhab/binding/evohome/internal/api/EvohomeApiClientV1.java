package org.openhab.binding.evohome.internal.api;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.evohome.configuration.EvohomeGatewayConfiguration;
import org.openhab.binding.evohome.internal.api.models.ControlSystem;
import org.openhab.binding.evohome.internal.api.models.v1.DataModelResponse;
import org.openhab.binding.evohome.internal.api.models.v1.LoginRequest;
import org.openhab.binding.evohome.internal.api.models.v1.LoginResponse;
import org.openhab.binding.evohome.internal.api.models.v2.response.ZoneStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class EvohomeApiClientV1 implements EvohomeApiClient {

    private final Logger logger = LoggerFactory.getLogger(EvohomeApiClientV1.class);

    private static final String ROOT = "https://tccna.honeywell.com/WebAPI/api";

    private static final String SESSION_URL = ROOT + "/Session";
    private static final String DATA_URL = ROOT + "/locations?userId=%s&allData=True";

    // private static final String DATA_URL =
    // "https://tccna.honeywell.com/WebAPI/emea/api/v1/location/installationInfo?userId=%s&includeTemperatureControlSystems=True";

    private EvohomeGatewayConfiguration configuration = null;
    private LoginResponse userInfo = null;

    private Map<String, String> headers = new HashMap<>();

    public EvohomeApiClientV1(EvohomeGatewayConfiguration configuration) {
        this.configuration = configuration;
    }

    @SuppressWarnings("unchecked")
    private <TIn, TOut> TOut doRequest(HttpMethod method, String url, TIn requestContainer, TOut out,
            HashMap<String, String> parameters) {
        ContentResponse response = null;

        try {
            SslContextFactory sslContextFactory = new SslContextFactory();
            HttpClient httpClient = new HttpClient(sslContextFactory);
            httpClient.start();

            if (requestContainer == null) {
                logger.debug("Do request '%s' for url '%s'", method, url);
                Request request = httpClient.newRequest(url).method(method);
                if (parameters != null) {
                    for (Map.Entry<String, String> param : parameters.entrySet()) {
                        request = request.param(param.getKey(), param.getValue());
                    }
                }
                if (headers != null) {
                    for (Map.Entry<String, String> header : headers.entrySet()) {
                        request = request.header(header.getKey(), header.getValue());
                    }
                }
                response = request.send();
                // response = httpClient.newRequest(url).method(method).send();
            } else {
                Gson gson = new GsonBuilder().create();
                String json = gson.toJson(requestContainer);
                logger.debug("Do request '%s' for url '%s' with data '%s'", method, url, json);
                response = httpClient.newRequest(url).method(method)
                        .content(new StringContentProvider(gson.toJson(requestContainer)), "application/json").send();
            }

            String reply = response.getContentAsString();
            logger.debug("Got reply: %s", reply);
            out = (TOut) new Gson().fromJson(reply, out.getClass());

        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.error("Error in handling request", e);
        } catch (Exception e) {
            logger.error("Generic error in handling request", e);
        }

        return out;
    }

    @Override
    public boolean login() {
        logger.debug("Calling Evohome login");
        LoginRequest loginRequest = new LoginRequest(configuration.username, configuration.password,
                configuration.applicationId);
        userInfo = new LoginResponse();
        userInfo = doRequest(HttpMethod.POST, SESSION_URL, loginRequest, userInfo, null);

        return userInfo != null;
    }

    @Override
    public void logout() {
        userInfo = null;
    }

    @Override
    public DataModelResponse[] getData() {
        logger.debug("Calling Evohome getData()");
        if (userInfo == null) {
            logger.error("Not logged in when calling getData()");
            return null;
        }
        try {
            String dataUrlWithUserId = String.format(DATA_URL, userInfo.getUserInfo().getUserId());

            HashMap<String, String> parameters = new HashMap<>();
            headers.put("content-type", "application/json");
            headers.put("Accept", "application/json");
            headers.put("sessionId", userInfo.getSessionId());

            DataModelResponse[] response = doRequest(HttpMethod.GET, dataUrlWithUserId, null,
                    new DataModelResponse[] {}, parameters);

            if (response != null) {
                logger.debug("GetData Response[{}]", response);
                System.out.println(response);
                // DataModelResponse[] dataResponse = jsonMapper.readValue(response, DataModelResponse[].class);
                // return dataResponse;
            }
        } finally {
        }
        return null;
    }

    @Override
    public ControlSystem[] getControlSystems() {
        // TODO Auto-generated method stub
        return new ControlSystem[0];
    }

    @Override
    public ControlSystem getControlSystem(int id) {
        return null;
    }

    @Override
    public ZoneStatus getHeatingZone(int locationId, int zoneId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void update() {
        // TODO Auto-generated method stub
    }

}