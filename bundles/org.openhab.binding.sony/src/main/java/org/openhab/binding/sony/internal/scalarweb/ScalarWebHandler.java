/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.scalarweb;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.sony.internal.AbstractThingHandler;
import org.openhab.binding.sony.internal.AccessResult;
import org.openhab.binding.sony.internal.SonyBindingConstants;
import org.openhab.binding.sony.internal.SonyUtil;
import org.openhab.binding.sony.internal.ThingCallback;
import org.openhab.binding.sony.internal.providers.SonyDefinitionProvider;
import org.openhab.binding.sony.internal.providers.SonyDynamicStateProvider;
import org.openhab.binding.sony.internal.providers.SonyModelListener;
import org.openhab.binding.sony.internal.providers.models.SonyDeviceCapability;
import org.openhab.binding.sony.internal.providers.models.SonyServiceCapability;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebMethod;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebService;
import org.openhab.binding.sony.internal.scalarweb.protocols.ScalarWebLoginProtocol;
import org.openhab.binding.sony.internal.scalarweb.protocols.ScalarWebProtocol;
import org.openhab.binding.sony.internal.scalarweb.protocols.ScalarWebProtocolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * The thing handler for a Sony Webscalar device. This is the entry point provides a full two interaction between
 * openhab and the webscalar system.
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class ScalarWebHandler extends AbstractThingHandler<ScalarWebConfig> {
    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(ScalarWebHandler.class);

    /** The tracker */
    private final ScalarWebChannelTracker tracker = new ScalarWebChannelTracker();

    /** The protocol handler being used - will be null if not initialized. */
    private final AtomicReference<@Nullable ScalarWebClient> scalarClient = new AtomicReference<>(null);

    /** The protocol handler being used - will be null if not initialized. */
    private final AtomicReference<@Nullable ScalarWebProtocolFactory<ThingCallback<String>>> protocolFactory = new AtomicReference<>(
            null);

    /** The thing callback */
    private final ThingCallback<String> callback;

    /** The transformation service to use */
    private final @Nullable TransformationService transformationService;

    /** The websocket client to use */
    private final WebSocketClient webSocketClient;

    /** The definition provider to use */
    private final SonyDefinitionProvider sonyDefinitionProvider;

    /** The dynamic state provider to use */
    private final SonyDynamicStateProvider sonyDynamicStateProvider;

    /** The definition listener */
    private final DefinitionListener definitionListener = new DefinitionListener();

    /** The OSGI properties for things */
    private final Map<String, String> osgiProperties;

    /**
     * Constructs the web handler
     *
     * @param thing a non-null thing
     * @param transformationService a possibly null transformation service
     * @param webSocketClient a non-null websocket client
     * @param sonyDefinitionProvider a non-null definition provider
     * @param sonyDynamicStateProvider a non-null dynamic state provider
     * @param osgiProperties a non-null, possibly empty list of OSGI properties
     */
    public ScalarWebHandler(final Thing thing, final @Nullable TransformationService transformationService,
            final WebSocketClient webSocketClient, final SonyDefinitionProvider sonyDefinitionProvider,
            final SonyDynamicStateProvider sonyDynamicStateProvider, final Map<String, String> osgiProperties) {
        super(thing, ScalarWebConfig.class);

        Objects.requireNonNull(thing, "thing cannot be null");
        Objects.requireNonNull(webSocketClient, "webSocketClient cannot be null");
        Objects.requireNonNull(sonyDefinitionProvider, "sonyDefinitionProvider cannot be null");
        Objects.requireNonNull(sonyDynamicStateProvider, "sonyDynamicStateProvider cannot be null");
        Objects.requireNonNull(osgiProperties, "osgiProperties cannot be null");

        this.transformationService = transformationService;
        this.webSocketClient = webSocketClient;
        this.sonyDefinitionProvider = sonyDefinitionProvider;
        this.sonyDynamicStateProvider = sonyDynamicStateProvider;
        this.osgiProperties = osgiProperties;

        callback = new ThingCallback<String>() {
            @Override
            public void statusChanged(final ThingStatus state, final ThingStatusDetail detail,
                    final @Nullable String msg) {
                updateStatus(state, detail, msg);
            }

            @Override
            public void stateChanged(final String channelId, final State newState) {
                final ThingStatus status = getThing().getStatus();
                if (status == ThingStatus.ONLINE) {
                    updateState(channelId, newState);
                } else {
                    // usually happens when we receive event notification during initialization
                    logger.trace("Ignoring state update during {}: {}={}", status, channelId, newState);
                }
            }

            @Override
            public void setProperty(final String propertyName, final @Nullable String propertyValue) {
                // change meaning of null propertyvalue
                // setProperty says remove - here we are ignoring
                if (propertyValue != null && StringUtils.isNotEmpty(propertyValue)) {
                    getThing().setProperty(propertyName, propertyValue);
                }

                // Update the discovered model name if found
                if (StringUtils.equals(propertyName, ScalarWebConstants.PROP_MODEL) && propertyValue != null
                        && StringUtils.isNotEmpty(propertyValue)) {
                    final ScalarWebConfig swConfig = getSonyConfig();
                    swConfig.setDiscoveredModelName(propertyValue);

                    final Configuration config = getConfig();
                    config.setProperties(swConfig.asProperties());

                    updateConfiguration(config);
                }
            }
        };
    }

    @Override
    protected void handleRefreshCommand(final ChannelUID channelUID) {
        Objects.requireNonNull(channelUID, "channelUID cannot be null");

        doHandleCommand(channelUID, RefreshType.REFRESH);
    }

    @Override
    protected void handleSetCommand(final ChannelUID channelUID, final Command command) {
        Objects.requireNonNull(channelUID, "channelUID cannot be null");
        Objects.requireNonNull(command, "command cannot be null");

        doHandleCommand(channelUID, command);
    }

    /**
     * Handles a command from the system. This will determine the protocol to send the command to
     * 
     * @param channelUID a non-null channel UID
     * @param command a non-null command
     */
    private void doHandleCommand(final ChannelUID channelUID, final Command command) {
        Objects.requireNonNull(channelUID, "channelUID cannot be null");
        Objects.requireNonNull(command, "command cannot be null");

        final Channel channel = getThing().getChannel(channelUID.getId());
        if (channel == null) {
            logger.debug("Channel for {} could not be found", channelUID);
            return;
        }
        final ScalarWebChannel scalarChannel = new ScalarWebChannel(channelUID, channel);

        final ScalarWebProtocolFactory<ThingCallback<String>> localProtocolFactory = protocolFactory.get();
        if (localProtocolFactory == null) {
            logger.debug("Trying to handle a channel command before a protocol factory has been created");
            return;
        }

        final ScalarWebProtocol<ThingCallback<String>> protocol = localProtocolFactory
                .getProtocol(scalarChannel.getService());
        if (protocol == null) {
            logger.debug("Unknown channel service: {} for {} and command {}", scalarChannel.getService(), channelUID,
                    command);
        } else {
            if (command instanceof RefreshType) {
                protocol.refreshChannel(scalarChannel);
            } else {
                protocol.setChannel(scalarChannel, command);
            }
        }
    }

    @Override
    protected void connect() {
        final ScalarWebConfig config = getSonyConfig();

        final String scalarWebUrl = config.getDeviceAddress();
        if (scalarWebUrl == null || StringUtils.isEmpty(scalarWebUrl)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "ScalarWeb URL is missing from configuration");
            return;
        }

        logger.debug("Attempting connection to Scalar Web device...");
        try {
            SonyUtil.checkInterrupt();

            final ScalarWebContext context = new ScalarWebContext(() -> getThing(), config, tracker, scheduler,
                    sonyDynamicStateProvider, webSocketClient, transformationService, osgiProperties);

            final ScalarWebClient client = ScalarWebClientFactory.get(scalarWebUrl, context);
            scalarClient.set(client);

            final ScalarWebLoginProtocol<ThingCallback<String>> loginHandler = new ScalarWebLoginProtocol<>(client,
                    config, callback, transformationService);

            final AccessResult result = loginHandler.login();
            SonyUtil.checkInterrupt();

            if (result == AccessResult.OK) {
                final ScalarWebProtocolFactory<ThingCallback<String>> factory = new ScalarWebProtocolFactory<>(context,
                        client, callback);

                SonyUtil.checkInterrupt();

                final ThingBuilder thingBuilder = editThing();
                thingBuilder.withChannels(getChannels(factory));
                updateThing(thingBuilder.build());

                SonyUtil.checkInterrupt();

                SonyUtil.close(protocolFactory.getAndSet(factory));
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);

                // Add a listener for model updates
                final String modelName = getModelName();
                if (modelName != null && StringUtils.isNotEmpty(modelName)) {
                    sonyDefinitionProvider.removeListener(definitionListener);
                    sonyDefinitionProvider.addListener(modelName, getThing().getThingTypeUID(), definitionListener);
                }

                this.scheduler.submit(() -> {
                    // Refresh the state right away
                    refreshState(true);

                    // after state is refreshed - write the definition
                    // (which could include dynamic state from refresh)
                    writeThingDefinition();
                    writeDeviceCapabilities(client);

                });
            } else {
                // If it's a pending access (or code not accepted), update with a configuration error
                // this prevents a reconnect (which will cancel any current registration code)
                // Note: there are other access code type errors that probably should be trapped here
                // as well - but those are the major two (probably represent 99% of the cases)
                // and we handle them separately
                if (result == AccessResult.PENDING || result == AccessResult.NOTACCEPTED) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, result.getMsg());
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, result.getMsg());
                }
            }
        } catch (final InterruptedException e) {
            logger.debug("Initialization was interrupted");
            // status would have already been set if interrupted - don't update it
            // since another instance of this will occur
        } catch (IOException | ParserConfigurationException | SAXException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error connecting to Scalar Web device (may need to turn it on manually)");
        } catch (final Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Unhandled exception connecting to Scalar Web device (may need to turn it on manually): "
                            + e.getMessage());
        }
    }

    /**
     * Helper method to get the channels to configure
     *
     * @param factory the non-null factory to use
     * @return a non-null, possibly empty list of channels
     */
    private Channel[] getChannels(final ScalarWebProtocolFactory<ThingCallback<String>> factory) {
        Objects.requireNonNull(factory, "factory cannot be null");

        final ThingUID thingUid = getThing().getUID();
        final ThingTypeUID typeUID = getThing().getThingTypeUID();
        final boolean genericThing = ScalarWebConstants.THING_TYPE_SCALAR.equals(typeUID);

        final Map<String, Channel> channels = genericThing ? new HashMap<>()
                : getThing().getChannels().stream().collect(Collectors.toMap(chl -> chl.getUID().getId(), chl -> chl));

        // Get all channel descriptors if we are generic
        // Get ONLY the app control descriptors (which are dynamic) if not
        for (final ScalarWebChannelDescriptor descriptor : factory.getChannelDescriptors(!genericThing)) {
            final Channel channel = descriptor.createChannel(thingUid).build();
            final String channelId = channel.getUID().getId();
            if (channels.containsKey(channelId)) {
                logger.debug("Channel definition already exists for {}: {}", channel.getUID().getId(), descriptor);
            } else {
                logger.debug("Creating channel: {}", descriptor);
                channels.put(channelId, channel);
            }
        }
        return channels.values().toArray(new Channel[0]);
    }

    /**
     * Helper method to get a model name
     *
     * @return a possibly null model name
     */
    private @Nullable String getModelName() {
        final String modelName = getSonyConfig().getModelName();
        if (modelName != null && StringUtils.isNotEmpty(modelName) && SonyUtil.isValidModelName(modelName)) {
            return modelName;
        }

        final String thingLabel = thing.getLabel();
        return thingLabel != null && StringUtils.isNotEmpty(thingLabel) && SonyUtil.isValidModelName(thingLabel)
                ? thingLabel
                : null;
    }

    /**
     * Helper method to write out a device capability
     *
     * @param client a non-null client
     */
    private void writeDeviceCapabilities(final ScalarWebClient client) {
        Objects.requireNonNull(client, "client cannot be null");

        final String modelName = getModelName();
        if (modelName == null || StringUtils.isEmpty(modelName)) {
            logger.debug("Could not write device capabilities file - model name was missing from properties");
        } else {
            final URL baseUrl = client.getDevice().getBaseUrl();

            final List<SonyServiceCapability> srvCapabilities = client.getDevice().getServices().stream()
                    .map(srv -> new SonyServiceCapability(srv.getServiceName(), srv.getVersion(),
                            srv.getTransport().getProtocolType(),
                            srv.getMethods().stream().sorted(ScalarWebMethod.COMPARATOR).collect(Collectors.toList()),
                            srv.getNotifications().stream().sorted(ScalarWebMethod.COMPARATOR)
                                    .collect(Collectors.toList())))
                    .collect(Collectors.toList());

            logger.debug("Writing device capability: {}", modelName);
            sonyDefinitionProvider
                    .writeDeviceCapabilities(new SonyDeviceCapability(modelName, baseUrl, srvCapabilities));
        }
    }

    /**
     * Helper method to write thing definition from our thing
     */
    private void writeThingDefinition() {
        final String modelName = getModelName();
        if (modelName == null || StringUtils.isEmpty(modelName)) {
            logger.debug("Could not write thing type file - model name was missing from properties");
        } else {
            // Only write things that are state channels, have a valid channel type and are not
            // from the app control service (which is too dynamic - what apps are installed)
            final Predicate<Channel> chlFilter = chl -> chl.getKind() == ChannelKind.STATE
                    && chl.getChannelTypeUID() != null
                    && !StringUtils.equalsIgnoreCase(chl.getUID().getGroupId(), ScalarWebService.APPCONTROL);

            logger.debug("Writing thing definition: {}", modelName);
            sonyDefinitionProvider.writeThing(SonyBindingConstants.SCALAR_THING_TYPE_PREFIX, ScalarWebConstants.CFG_URI,
                    modelName, getThing(), chlFilter);
        }
    }

    @Override
    protected void refreshState(boolean initial) {
        final ScalarWebProtocolFactory<ThingCallback<String>> protocolHandler = protocolFactory.get();
        if (protocolHandler == null) {
            logger.debug("Protocol factory wasn't set");
        } else {
            logger.debug("Refreshing all state");
            protocolHandler.refreshAllState(scheduler, initial);
        }
    }

    @Override
    protected URL getCheckStatusUrl() throws MalformedURLException {
        // If using simplifed config (where we discover stuff)
        // use the discovered ipaddress/port rather than configured
        final ScalarWebClient client = scalarClient.get();
        return client == null ? getSonyConfig().getDeviceUrl() : client.getDevice().getBaseUrl();
    }

    @Override
    public void channelUnlinked(final ChannelUID channelUID) {
        Objects.requireNonNull(channelUID, "channelUID cannot be null");

        tracker.channelUnlinked(channelUID);
        super.channelUnlinked(channelUID);
    }

    @Override
    public void channelLinked(final ChannelUID channelUID) {
        Objects.requireNonNull(channelUID, "channelUID cannot be null");
        final Channel channel = getThing().getChannel(channelUID.getId());
        if (channel == null) {
            logger.debug("channel linked called but channelUID {} could not be found", channelUID);
        } else {
            tracker.channelLinked(new ScalarWebChannel(channelUID, channel));
        }
        super.channelLinked(channelUID);
    }

    @Override
    public void dispose() {
        super.dispose();
        sonyDefinitionProvider.removeListener(definitionListener);
        SonyUtil.close(protocolFactory.getAndSet(null));
        SonyUtil.close(scalarClient.getAndSet(null));
    }

    /**
     * A listener to definition changes (ie thing type changes)
     */
    @NonNullByDefault
    private class DefinitionListener implements SonyModelListener {
        @Override
        public void thingTypeFound(final ThingTypeUID uid) {
            final String modelName = getModelName();
            // if we are resetting back to the generic version
            // or if we matched our model (going from generic to specific or updating to a new version of specific)
            // then change our thing type
            if (ScalarWebConstants.THING_TYPE_SCALAR.equals(uid)
                    || (modelName != null && StringUtils.isNotEmpty(modelName)
                            && SonyUtil.isModelMatch(uid, SonyBindingConstants.SCALAR_THING_TYPE_PREFIX, modelName))) {
                changeThingType(uid, getConfig());
            }
        }
    }
}
