package org.openhab.binding.evohome.internal.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvohomeApiClient {

    private static final Logger logger = LoggerFactory.getLogger(EvohomeApiClient.class);

    private static final String ROOT = "https://tccna.honeywell.com/WebAPI/api";

    private static final String SESSION_URL = ROOT + "/Session";
    private static final String DATA_URL = ROOT + "/locations?userId=%s&allData=True";

    // private final ObjectMapper jsonMapper = new ObjectMapper();
    // private LoginResponse loginResponse = null;
    // private final LoginRequest loginRequest;

    public EvohomeApiClient(String username, String password, String applicationId) {
        // this.loginRequest = new LoginRequest(username, password, applicationId);
    }

    public boolean login() {
        // logger.debug("Calling EvoHome login");
        // try {
        // byte[] json = jsonMapper.writeValueAsBytes(loginRequest);
        // InputStream contentStream = new ByteArrayInputStream(json);
        //
        // Properties httpHeaders = new Properties();
        // httpHeaders.put("content-type", "application/json");
        // httpHeaders.put("Accept", "application/json");
        //
        // String response = HttpUtil.executeUrl("POST", SESSION_URL, httpHeaders, contentStream, "application/json",
        // 10000);
        //
        // if (response != null) {
        // logger.debug("Login Response[{}]", response);
        // this.loginResponse = jsonMapper.readValue(response, LoginResponse.class);
        // return true;
        // }
        // } catch (JsonGenerationException e) {
        // logger.error("Error calling LogIn", e);
        // } catch (JsonMappingException e) {
        // logger.error("Error calling LogIn", e);
        // } catch (IOException e) {
        // logger.error("Error calling LogIn", e);
        // }
        return false;
    }

    public void logout() {

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
