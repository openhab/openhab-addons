package org.openhab.binding.hydrawise.internal;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.hydrawise.internal.api.HydrawiseAuthenticationException;
import org.openhab.binding.hydrawise.internal.api.HydrawiseCommandException;
import org.openhab.binding.hydrawise.internal.api.HydrawiseConnectionException;
import org.openhab.binding.hydrawise.internal.api.HydrawiseLocalApiClient;
import org.openhab.binding.hydrawise.internal.api.model.Relay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HydrawiseLocalHandler extends HydrawiseHandler {
    private final Logger logger = LoggerFactory.getLogger(HydrawiseLocalHandler.class);
    HydrawiseLocalApiClient client;

    public HydrawiseLocalHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void configure()
            throws NotConfiguredException, HydrawiseConnectionException, HydrawiseAuthenticationException {
        HydrawiseLocalConfiguration configuration = getConfig().as(HydrawiseLocalConfiguration.class);

        if (StringUtils.isBlank(configuration.host)) {
            throw new NotConfiguredException("No host specified");
        }

        if (StringUtils.isBlank(configuration.username)) {
            throw new NotConfiguredException("No user name specified");
        }

        if (StringUtils.isBlank(configuration.password)) {
            throw new NotConfiguredException("No passowrd specified");
        }

        this.refresh = configuration.refresh.intValue() > MIN_REFRESH_SECONDS ? configuration.refresh.intValue()
                : MIN_REFRESH_SECONDS;

        client = new HydrawiseLocalApiClient(configuration.host, configuration.username, configuration.password);
        pollController();

    }

    @Override
    protected void pollController() throws HydrawiseConnectionException, HydrawiseAuthenticationException {
        updateZones(client.getLocalSchedule());

    }

    @Override
    protected void sendRunCommand(int seconds, Relay relay)
            throws HydrawiseCommandException, HydrawiseConnectionException, HydrawiseAuthenticationException {
        client.runRelay(seconds, relay.getRelay());

    }

    @Override
    protected void sendRunCommand(Relay relay)
            throws HydrawiseCommandException, HydrawiseConnectionException, HydrawiseAuthenticationException {
        client.runRelay(relay.getRelay());

    }

    @Override
    protected void sendStopCommand(Relay relay)
            throws HydrawiseCommandException, HydrawiseConnectionException, HydrawiseAuthenticationException {
        client.stopRelay(relay.getRelay());

    }

}
