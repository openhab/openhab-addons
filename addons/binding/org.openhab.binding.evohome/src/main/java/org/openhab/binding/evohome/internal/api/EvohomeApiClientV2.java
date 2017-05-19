package org.openhab.binding.evohome.internal.api;

import org.openhab.binding.evohome.configuration.EvohomeGatewayConfiguration;
import org.openhab.binding.evohome.internal.api.handlers.AuthenticationHandler;
import org.openhab.binding.evohome.internal.api.models.v1.DataModelResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvohomeApiClientV2 implements EvohomeApiClient {
    private static final Logger logger = LoggerFactory.getLogger(EvohomeApiClientV2.class);

    private EvohomeGatewayConfiguration configuration = null;

    private AuthenticationHandler authenticationHandler = new AuthenticationHandler();
    private AccountHandler accountHandler = new AccountHandler(authenticationHandler);

    public EvohomeApiClientV2(EvohomeGatewayConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public boolean login() {
        logger.debug("Calling EvoHome login");
        boolean result = authenticationHandler.login(configuration.username, configuration.password, configuration.applicationId);
        accountHandler.getUserAccount();

        //TODO test first call here

        return result;
    }

    @Override
    public void logout() {
        // userInfo = null;
    }

    @Override
    public DataModelResponse[] getData() {
        // TODO Auto-generated method stub
        return null;
    }

}
