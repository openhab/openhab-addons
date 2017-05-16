package org.openhab.binding.evohome.internal.api;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.evohome.configuration.EvohomeGatewayConfiguration;
import org.openhab.binding.evohome.internal.api.models.LoginRequest;
import org.openhab.binding.evohome.internal.api.models.LoginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class EvohomeApiClient {

    private static final Logger logger = LoggerFactory.getLogger(EvohomeApiClient.class);

    private static final String ROOT = "https://tccna.honeywell.com/WebAPI/api";

    private static final String SESSION_URL = ROOT + "/Session";
    private static final String DATA_URL = ROOT + "/locations?userId=%s&allData=True";

    private EvohomeGatewayConfiguration configuration = null;
    private LoginResponse userInfo = null;

    public EvohomeApiClient(EvohomeGatewayConfiguration configuration) {
        this.configuration = configuration;
    }

    @SuppressWarnings("unchecked")
    private <TIn, TOut> TOut doRequest(HttpMethod method, String url, TIn requestContainer, TOut out) {
        ContentResponse response = null;

        try {
            SslContextFactory sslContextFactory = new SslContextFactory();
            HttpClient httpClient = new HttpClient(sslContextFactory);
            httpClient.start();

            if (requestContainer == null) {
                logger.debug("Do request '" + method + "' for url '" + url + "'");
                response = httpClient.newRequest(url).method(method).send();
            } else {
                Gson gson = new GsonBuilder().create();
                String json = gson.toJson(requestContainer);
                logger.debug("Do request '" + method + "' for url '" + url + "' with data '" + json + "'");
                response = httpClient.newRequest(url).method(method)
                        .content(new StringContentProvider(gson.toJson(requestContainer)), "application/json").send();
            }

            String reply = response.getContentAsString();
            logger.debug("Got reply: " + reply);
            out = (TOut) new Gson().fromJson(reply, out.getClass());

        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.error("Error in handling request", e);
        } catch (Exception e) {
            logger.error("Generic error in handling request", e);
        }

        return out;
    }

    public boolean login() {
        logger.debug("Calling EvoHome login");
        LoginRequest loginRequest = new LoginRequest(configuration.username, configuration.password,
                configuration.applicationId);
        userInfo = new LoginResponse();
        userInfo = doRequest(HttpMethod.POST, SESSION_URL, loginRequest, userInfo);

        return userInfo != null;
    }

    public void logout() {
        userInfo = null;
    }

    // public DataModelResponse[] getData() {
    // logger.debug("Calling EvoHome getData()");
    // if (loginResponse == null) {
    // logger.error("Not logged in when calling getData()");
    // return null;
    // }
    // try {
    // String dataUrlWithUserId = String.format(DATA_URL, loginResponse.getUserInfo().getUserId());
    // System.out.println(dataUrlWithUserId);
    // Properties httpHeaders = new Properties();
    // httpHeaders.put("content-type", "application/json");
    // httpHeaders.put("Accept", "application/json");
    // httpHeaders.put("sessionId", loginResponse.getSessionId());
    //
    // String response = HttpUtil.executeUrl("GET", dataUrlWithUserId, httpHeaders, null, "application/json",
    // 10000);
    //
    // if (response != null) {
    // logger.debug("GetData Response[{}]", response);
    // System.out.println(response);
    // DataModelResponse[] dataResponse = jsonMapper.readValue(response, DataModelResponse[].class);
    // return dataResponse;
    // }
    // } catch (JsonParseException e) {
    // logger.error("Error calling GetData", e);
    // } catch (JsonMappingException e) {
    // logger.error("Error calling GetData", e);
    // } catch (IOException e) {
    // logger.error("Error calling GetData", e);
    // }
    // return null;
    // }
}
