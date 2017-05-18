package org.openhab.binding.evohome.internal.api;

import org.openhab.binding.evohome.configuration.EvohomeGatewayConfiguration;
import org.openhab.binding.evohome.internal.api.handlers.AuthenticationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvohomeApiClient {
    private static final Logger logger = LoggerFactory.getLogger(EvohomeApiClient.class);

    private AuthenticationHandler authenticationHandler = new AuthenticationHandler();
    private EvohomeGatewayConfiguration configuration = null;

    public EvohomeApiClient(EvohomeGatewayConfiguration configuration) {
        this.configuration = configuration;
    }

    public boolean login() {
        logger.debug("Calling EvoHome login");
        return authenticationHandler.login(configuration.username, configuration.password, configuration.applicationId);
    }

    public void logout() {
        // userInfo = null;
    }

}
