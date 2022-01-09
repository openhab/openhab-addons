package org.openhab.binding.lgthinq.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openhab.binding.lgthinq.errors.*;
import org.openhab.binding.lgthinq.internal.LGThinqConfiguration;
import org.openhab.core.config.core.status.ConfigStatusMessage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ConfigStatusBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.stream.Collectors.joining;
import static org.openhab.binding.lgthinq.internal.LGThinqBindingConstants.*;

public class BridgeHandler extends ConfigStatusBridgeHandler {

    static {
        var logger = LoggerFactory.getLogger(BridgeHandler.class);
        try {
            File directory = new File(THINQ_USER_DATA_FOLDER);
            if (!directory.exists()) {
                directory.mkdir();
            }
        } catch (Exception e) {
            logger.warn("Unable to setup thinq userdata directory: {}", e.getMessage());
        }
    }
    private final Logger logger = LoggerFactory.getLogger(BridgeHandler.class);
    private LGThinqConfiguration lgthinqConfig;
    private TokenManager tokenManager;

    public BridgeHandler(Bridge bridge) {
        super(bridge);
        tokenManager = TokenManager.getInstance();
    }

    final ReentrantLock pollingLock = new ReentrantLock();
    private boolean lastBridgeConnectionState = false;


    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        List resultList = new ArrayList<ConfigStatusMessage>();
        if (lgthinqConfig.username == null) {
            resultList.add(ConfigStatusMessage.Builder.error("USERNAME").withMessageKeySuffix("missing field").withArguments("username").build());
        }
        if (lgthinqConfig.password == null) {
            resultList.add(ConfigStatusMessage.Builder.error("PASSWORD").withMessageKeySuffix("missing field").withArguments("password").build());
        }
        if (lgthinqConfig.language == null) {
            resultList.add(ConfigStatusMessage.Builder.error("LANGUAGE").withMessageKeySuffix("missing field").withArguments("language").build());
        }
        if (lgthinqConfig.country == null) {
            resultList.add(ConfigStatusMessage.Builder.error("COUNTRY").withMessageKeySuffix("missing field").withArguments("country").build());

        }
        return resultList;
    }

    @Override
    public <T> T getConfigAs(Class<T> configurationClass) {
        return super.getConfigAs(configurationClass);
    }

    private String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            logger.error("This exception can't occurr: {}", e);
        }
        return null;
    }

    private String encodeUrl(String url, Map<String,String> requestParams) {
        String encodedURL = requestParams.keySet().stream()
                .map(key -> key + "=" + encodeValue(requestParams.get(key)))
                .collect(joining("&", url, ""));

        return encodedURL;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing LGThinq bridge handler.");
        lgthinqConfig = getConfigAs(LGThinqConfiguration.class);

        if (lgthinqConfig.username == null ||  lgthinqConfig.password == null ||
                lgthinqConfig.language == null || lgthinqConfig.country == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/error.mandotory-fields-missing");
            return;
        } else {
            // check if configuration file already exists
            if (tokenManager.isOauthTokenRegistered()) {
                logger.debug("File {} already exists. Skip first authentication process.");
                try {
                    // Dummy - if token is expired, then provide the refresh
                    tokenManager.getValidRegisteredToken();
                } catch (IOException e) {
                    logger.error("Error reading LGThinq TokenFile", e);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "@text/error.toke-file-corrupted") ;
                    return;
                } catch (RefreshTokenException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "@text/error.toke-refresh") ;
                    return;
                }
            } else {
                // TODO - colocar essa parte como runnable thread.
                try {
                    tokenManager.oauthFirstRegistration(lgthinqConfig.getLanguage(), lgthinqConfig.getCountry(), lgthinqConfig.getUsername(),
                            lgthinqConfig.getPassword());
                } catch (IOException ex) {
                    // unsuccefull result from call
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/error.mandotory-fields-missing");
                } catch (PreLoginException e) {
                    e.printStackTrace();
                } catch (TokenException e) {
                    e.printStackTrace();
                } catch (LGGatewayException e) {
                    e.printStackTrace();
                } catch (AccountLoginException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

    }
}
