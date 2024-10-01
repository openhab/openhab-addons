/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.MultiException;
import org.openhab.binding.argoclima.internal.ArgoClimaBindingConstants;
import org.openhab.binding.argoclima.internal.ArgoClimaTranslationProvider;
import org.openhab.binding.argoclima.internal.configuration.ArgoClimaConfigurationLocal;
import org.openhab.binding.argoclima.internal.configuration.ArgoClimaConfigurationLocal.ConnectionMode;
import org.openhab.binding.argoclima.internal.device.api.ArgoClimaLocalDevice;
import org.openhab.binding.argoclima.internal.device.api.IArgoClimaDeviceAPI;
import org.openhab.binding.argoclima.internal.device.passthrough.PassthroughHttpClient;
import org.openhab.binding.argoclima.internal.device.passthrough.RemoteArgoApiServerStub;
import org.openhab.binding.argoclima.internal.exception.ArgoConfigurationException;
import org.openhab.binding.argoclima.internal.exception.ArgoRemoteServerStubStartupException;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ArgoClimaHandlerLocal} is responsible for handling commands, which are
 * sent to one of the channels. Supports local device (either through direct connection or pass-through)
 *
 * @see ArgoClimaHandlerBase
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public class ArgoClimaHandlerLocal extends ArgoClimaHandlerBase<ArgoClimaConfigurationLocal> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final HttpClient commonHttpClient;
    private final TimeZoneProvider timeZoneProvider;
    private final HttpClientFactory clientFactory;

    private Optional<RemoteArgoApiServerStub> serverStub = Optional.empty();

    /**
     * C-tor
     *
     * @param thing The @code Thing} this handler serves (provided by the framework through
     *            {@link org.openhab.binding.argoclima.internal.ArgoClimaHandlerFactory ArgoClimaHandlerFactory}
     * @param clientFactory The framework's HTTP client factory (injected by the runtime to the
     *            {@code ArgoClimaHandlerFactory})
     * @param timeZoneProvider The framework's time zone provider (injected by the runtime to the
     *            {@code ArgoClimaHandlerFactory})
     * @param i18nProvider Framework's translation provider
     */
    public ArgoClimaHandlerLocal(Thing thing, HttpClientFactory clientFactory, TimeZoneProvider timeZoneProvider,
            final ArgoClimaTranslationProvider i18nProvider) {
        super(thing, ArgoClimaBindingConstants.AWAIT_DEVICE_CONFIRMATIONS_AFTER_COMMANDS,
                ArgoClimaBindingConstants.POLL_FREQUENCY_AFTER_COMMAND_SENT_LOCAL,
                ArgoClimaBindingConstants.SEND_COMMAND_RETRY_FREQUENCY_LOCAL,
                ArgoClimaBindingConstants.SEND_COMMAND_MAX_WAIT_TIME_LOCAL_DIRECT,
                ArgoClimaBindingConstants.SEND_COMMAND_MAX_WAIT_TIME_LOCAL_INDIRECT, i18nProvider);
        this.commonHttpClient = clientFactory.getCommonHttpClient();
        this.clientFactory = clientFactory;
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    protected ArgoClimaConfigurationLocal getConfigInternal() throws ArgoConfigurationException {
        try {
            var ret = getConfigAs(ArgoClimaConfigurationLocal.class); // This can **theoretically** return null if class
                                                                      // is not default-constructible (but this one is,
                                                                      // so not handling!)
            ret.initialize(i18nProvider);
            return ret;
        } catch (IllegalArgumentException ex) {
            throw ArgoConfigurationException.forInvalidParamValue("Error loading thing configuration",
                    "thing-status.argoclima.configuration.load-error", i18nProvider, ex);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * For any {@code REMOTE_API_*} <b>Connection mode</b>, this starts a new HTTP server (with its own thread pool!)
     * listening for HVAC connections.
     * <p>
     * Additionally for a {@code REMOTE_API_PROXY}, a custom HTTP client is also created, proxying the calls from device
     * to Argo servers. The device follows a strange protocol (not fully-compatible with HTTP spec, it seems), which
     * drives the need for a custom client with separate set of settings
     *
     * @implNote The intercepting proxy (if enabled) WILL asynchronously trigger channel/state/properties updates
     *           through respective callbacks (these are thread-safe!)
     */
    @Override
    protected IArgoClimaDeviceAPI initializeDeviceApi(ArgoClimaConfigurationLocal config)
            throws ArgoRemoteServerStubStartupException, ArgoConfigurationException {
        var deviceApi = new ArgoClimaLocalDevice(config, config.getHostname(), config.getHvacListenPort(),
                config.getLocalDeviceIP(), config.getDeviceCpuId(), commonHttpClient, timeZoneProvider, i18nProvider,
                this::updateChannelsFromDevice, this::updateThingStatusToOnline, this::updateThingProperties,
                thing.getUID().toString());

        if (config.getConnectionMode() == ConnectionMode.REMOTE_API_PROXY
                || config.getConnectionMode() == ConnectionMode.REMOTE_API_STUB) {
            var passthroughClient = Optional.<PassthroughHttpClient> empty();
            if (config.getConnectionMode() == ConnectionMode.REMOTE_API_PROXY) {
                // new passthrough client for PROXY mode (its lifecycle will be managed by proxy server on startup)
                passthroughClient = Optional.of(
                        new PassthroughHttpClient(Objects.requireNonNull(config.getOemServerAddress().getHostAddress()),
                                config.getOemServerPort(), clientFactory));
            }

            var simulatedServer = new RemoteArgoApiServerStub(config.getStubServerListenAddresses(),
                    config.getStubServerPort(), this.getThing().getUID().toString(), passthroughClient,
                    Optional.of(deviceApi), config.getIncludeDeviceSidePasswordsInProperties(), i18nProvider);
            serverStub = Optional.of(simulatedServer);

            try {
                simulatedServer.start();
            } catch (Exception e1) {
                var message = e1.getLocalizedMessage();
                if (e1.getCause() instanceof MultiException multiEx) {
                    // This may cause multiple exceptions in case multiple bind addresses are in use
                    var multiCause = Objects.requireNonNull(multiEx.getCause());
                    message = multiCause.toString(); // deliberately not using getLocalizedMessage, as we want the list
                }

                throw new ArgoRemoteServerStubStartupException(
                        "[{0} mode] Failed to start RPC server at port: {1,number,#}. Error: {2}",
                        "thing-status.argoclima.stub-server.start-failure", i18nProvider, config.getConnectionMode(),
                        config.getStubServerPort(), message);
            }
        }
        return deviceApi;
    }

    /**
     * {@inheritDoc}
     * <p>
     * In addition to common binding cleanup, also stops passthrough server. The custom HTTP client's lifecycle is
     * managed by the server itself, hence will be
     * disposed with it
     */
    @Override
    protected void stopRunningTasks() {
        // Stop all common tasks
        super.stopRunningTasks();

        try {
            synchronized (this) {
                serverStub.ifPresent(s -> s.shutdown());
                serverStub = Optional.empty();
            }
        } catch (Exception e) {
            logger.debug("Exception during handler disposal", e);
        }
        logger.trace("{}: Disposed", getThing().getUID().getId());
    }
}
