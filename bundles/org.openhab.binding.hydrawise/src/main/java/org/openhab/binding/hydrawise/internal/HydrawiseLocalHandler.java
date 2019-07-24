/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.hydrawise.internal;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.hydrawise.internal.api.HydrawiseAuthenticationException;
import org.openhab.binding.hydrawise.internal.api.HydrawiseCommandException;
import org.openhab.binding.hydrawise.internal.api.HydrawiseConnectionException;
import org.openhab.binding.hydrawise.internal.api.HydrawiseLocalApiClient;
import org.openhab.binding.hydrawise.internal.api.model.Relay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HydrawiseLocalHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class HydrawiseLocalHandler extends HydrawiseHandler {
    private final Logger logger = LoggerFactory.getLogger(HydrawiseLocalHandler.class);
    HydrawiseLocalApiClient client;

    public HydrawiseLocalHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        client = new HydrawiseLocalApiClient(httpClient);
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

        logger.trace("Connecting to host {}", configuration.host);
        client.setCredentials(configuration.host, configuration.username, configuration.password);
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

    @Override
    protected void sendRunAllCommand()
            throws HydrawiseCommandException, HydrawiseConnectionException, HydrawiseAuthenticationException {
        client.runAllRelays();
    }

    @Override
    protected void sendRunAllCommand(int seconds)
            throws HydrawiseCommandException, HydrawiseConnectionException, HydrawiseAuthenticationException {
        client.runAllRelays(seconds);
    }

    @Override
    protected void sendStopAllCommand()
            throws HydrawiseCommandException, HydrawiseConnectionException, HydrawiseAuthenticationException {
        client.stopAllRelays();
    }
}
