package org.openhab.binding.evohome.internal.api;

import org.openhab.binding.evohome.configuration.EvohomeGatewayConfiguration;
import org.openhab.binding.evohome.internal.api.handlers.AuthenticationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvohomeApiClient {
    private static final Logger logger = LoggerFactory.getLogger(EvohomeApiClient.class);

    private EvohomeGatewayConfiguration configuration = null;

    private AuthenticationHandler authenticationHandler = new AuthenticationHandler();
    private AccountHandler accountHandler = new AccountHandler(authenticationHandler);

    public EvohomeApiClient(EvohomeGatewayConfiguration configuration) {
        this.configuration = configuration;
    }

    public boolean login() {
        logger.debug("Calling EvoHome login");
        boolean result = authenticationHandler.login(configuration.username, configuration.password, configuration.applicationId);
        accountHandler.getUserAccount();

        //TODO test first call here

        return result;
    }

    public void logout() {
        // userInfo = null;
    }

}
