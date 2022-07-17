/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.argoclima.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.argoclima.internal.ArgoClimaBindingConstants;
import org.openhab.binding.argoclima.internal.configuration.ArgoClimaConfigurationRemote;
import org.openhab.binding.argoclima.internal.device.api.ArgoClimaRemoteDevice;
import org.openhab.binding.argoclima.internal.device.api.IArgoClimaDeviceAPI;
import org.openhab.binding.argoclima.internal.exception.ArgoConfigurationException;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Thing;

/**
 * The {@link ArgoClimaHandlerRemote} is responsible for handling commands, which are
 * sent to one of the channels. Supports remote device (talking to Argo servers)
 *
 * @see ArgoClimaHandlerBase
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public class ArgoClimaHandlerRemote extends ArgoClimaHandlerBase<ArgoClimaConfigurationRemote> {
    private final HttpClient client;
    private final TimeZoneProvider timeZoneProvider;

    /**
     * C-tor
     *
     * @param thing The @code Thing} this handler serves (provided by the framework through
     *            {@link org.openhab.binding.argoclima.internal.ArgoClimaHandlerFactory ArgoClimaHandlerFactory}
     * @param clientFactory The framework's HTTP client factory (injected by the runtime to the
     *            {@code ArgoClimaHandlerFactory})
     * @param timeZoneProvider The framework's time zone provider (injected by the runtime to the
     *            {@code ArgoClimaHandlerFactory})
     */
    public ArgoClimaHandlerRemote(Thing thing, HttpClientFactory clientFactory, TimeZoneProvider timeZoneProvider) {
        super(thing, ArgoClimaBindingConstants.AWAIT_DEVICE_CONFIRMATIONS_AFTER_COMMANDS,
                ArgoClimaBindingConstants.POLL_FREQUENCY_AFTER_COMMAND_SENT_REMOTE,
                ArgoClimaBindingConstants.SEND_COMMAND_RETRY_FREQUENCY_REMOTE,
                ArgoClimaBindingConstants.SEND_COMMAND_MAX_WAIT_TIME_REMOTE,
                ArgoClimaBindingConstants.SEND_COMMAND_MAX_WAIT_TIME_REMOTE);
        this.client = clientFactory.getCommonHttpClient();
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    protected ArgoClimaConfigurationRemote getConfigInternal() throws ArgoConfigurationException {
        try {
            return getConfigAs(ArgoClimaConfigurationRemote.class);
        } catch (IllegalArgumentException ex) {
            throw new ArgoConfigurationException("Error loading thing configuration", "", ex);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Initializes API state. Since this mode uses shared HTTP client, this is not creating any new resources, merely
     * initializes state
     */
    @Override
    protected IArgoClimaDeviceAPI initializeDeviceApi(ArgoClimaConfigurationRemote config)
            throws ArgoConfigurationException {
        return new ArgoClimaRemoteDevice(config, this.client, this.timeZoneProvider, config.getOemServerAddress(),
                config.getOemServerPort(), config.getUsername(), config.getPasswordMD5Hash(),
                this::updateThingProperties);
    }
}
