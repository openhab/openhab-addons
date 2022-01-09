package org.openhab.binding.lgthinq.lgapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openhab.binding.lgthinq.api.*;
import org.openhab.binding.lgthinq.errors.LGApiException;
import org.openhab.binding.lgthinq.lgapi.model.LGDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.util.*;

import static org.openhab.binding.lgthinq.internal.LGThinqBindingConstants.*;

public class LGApiClientServiceImpl implements LGApiClientService {
    private static final LGApiClientService instance;
    private static final Logger logger = LoggerFactory.getLogger(LGApiClientService.class);
    private ObjectMapper objectMapper = new ObjectMapper();
    private TokenManager tokenManager;

    static {
        instance = new LGApiClientServiceImpl();
    }

    private LGApiClientServiceImpl() {
        tokenManager = TokenManager.getInstance();
    }

    public static LGApiClientService getInstance() {
        return instance;
    }
    private Map<String, String> getCommonV2Headers(String language, String country, String accessToken, String userNumber) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Content-type", "application/json;charset=UTF-8");
        headers.put("x-api-key", V2_API_KEY);
        headers.put("x-client-id", V2_CLIENT_ID);
        headers.put("x-country-code", country);
        headers.put("x-language-code", language);
        headers.put("x-message-id", UUID.randomUUID().toString());
        headers.put("x-service-code", SVC_CODE);
        headers.put("x-service-phase", V2_SVC_PHASE);
        headers.put("x-thinq-app-level", V2_APP_LEVEL);
        headers.put("x-thinq-app-os", V2_APP_OS);
        headers.put("x-thinq-app-type", V2_APP_TYPE);
        headers.put("x-thinq-app-ver", V2_APP_VER);
        headers.put("x-thinq-security-key", SECURITY_KEY);
        if (accessToken != null && !accessToken.isBlank())
            headers.put("x-emp-token", accessToken);
        if (userNumber != null && !userNumber.isBlank())
            headers.put("x-user-no", userNumber);
        return headers;
    }

    @Override
    public List<LGDevice> listAccountDevices() throws LGApiException {
        try {
            TokenResult token = tokenManager.getValidRegisteredToken();
            UserInfo userInfo = token.getUserInfo();
            UriBuilder builder = UriBuilder.fromUri(token.getGatewayInfo().getApiRootV2()).path(V2_LS_PATH);
            Map<String, String> headers = getCommonV2Headers(token.getGatewayInfo().getLanguage(),
                    token.getGatewayInfo().getCountry(),
                    token.getAccessToken(),
                    token.getUserInfo().getUserNumber());
            RestResult resp = RestUtils.getCall(builder.build().toURL().toString(), headers, null);
            return handleListAccountDevicesResult(resp);
        } catch (Exception e) {
            throw new LGApiException("Erros list account devices from LG Server API", e);
        }
    }

    private List<LGDevice> handleListAccountDevicesResult(RestResult resp) throws LGApiException {
        Map<String, Object> devicesResult;
        List<LGDevice> devices;
        if (resp.getStatusCode() != 200) {
            logger.error("Error calling device list from LG Server API. The reason is:{}", resp.getJsonResponse());
            throw new LGApiException(String.format("Error calling device list from LG Server API. The reason is:%s", resp.getJsonResponse()));
        } else {
            try {
                devicesResult = objectMapper.readValue(resp.getJsonResponse(), HashMap.class);
                if (!"0000".equals(devicesResult.get("resultCode"))) {
                    throw new LGApiException(String.format("Status error getting device list. resultCode must be 0000, but was:%s",
                            devicesResult.get("resultCode")));
                }
                List<Map<String,Object>> items = (List<Map<String, Object>>) ((Map<String,Object>)devicesResult.get("result")).get("item");
                devices = objectMapper.convertValue(items, new TypeReference<List<LGDevice>>(){});
            } catch (JsonProcessingException e) {
                throw new IllegalStateException(String.format("Unknown error occurred deserializing json stream.", e));
            }

        }

        return devices;
    }
}

