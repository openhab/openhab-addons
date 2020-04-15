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
package org.openhab.binding.velux.internal.handler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.velux.internal.VeluxBinding;
import org.openhab.binding.velux.internal.VeluxBindingConstants;
import org.openhab.binding.velux.internal.VeluxItemType;
import org.openhab.binding.velux.internal.bridge.VeluxBridge;
import org.openhab.binding.velux.internal.bridge.VeluxBridgeActuators;
import org.openhab.binding.velux.internal.bridge.VeluxBridgeDeviceStatus;
import org.openhab.binding.velux.internal.bridge.VeluxBridgeGetFirmware;
import org.openhab.binding.velux.internal.bridge.VeluxBridgeGetHouseStatus;
import org.openhab.binding.velux.internal.bridge.VeluxBridgeInstance;
import org.openhab.binding.velux.internal.bridge.VeluxBridgeLANConfig;
import org.openhab.binding.velux.internal.bridge.VeluxBridgeProvider;
import org.openhab.binding.velux.internal.bridge.VeluxBridgeScenes;
import org.openhab.binding.velux.internal.bridge.VeluxBridgeSetHouseStatusMonitor;
import org.openhab.binding.velux.internal.bridge.VeluxBridgeWLANConfig;
import org.openhab.binding.velux.internal.bridge.common.BridgeAPI;
import org.openhab.binding.velux.internal.bridge.common.BridgeCommunicationProtocol;
import org.openhab.binding.velux.internal.bridge.json.JsonVeluxBridge;
import org.openhab.binding.velux.internal.bridge.slip.SlipVeluxBridge;
import org.openhab.binding.velux.internal.config.VeluxBridgeConfiguration;
import org.openhab.binding.velux.internal.development.Threads;
import org.openhab.binding.velux.internal.handler.utils.ExtendedBaseBridgeHandler;
import org.openhab.binding.velux.internal.handler.utils.Thing2VeluxActuator;
import org.openhab.binding.velux.internal.handler.utils.ThingProperty;
import org.openhab.binding.velux.internal.things.VeluxExistingProducts;
import org.openhab.binding.velux.internal.things.VeluxExistingScenes;
import org.openhab.binding.velux.internal.things.VeluxProduct;
import org.openhab.binding.velux.internal.things.VeluxProduct.ProductBridgeIndex;
import org.openhab.binding.velux.internal.things.VeluxProductPosition;
import org.openhab.binding.velux.internal.utils.Localization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <B>Common interaction with the </B><I>Velux</I><B> bridge.</B>
 * <P>
 * It implements the communication between <B>OpenHAB</B> and the <I>Velux</I> Bridge:
 * <UL>
 * <LI><B>OpenHAB</B> Event Bus &rarr; <I>Velux</I> <B>bridge</B>
 * <P>
 * Sending commands and value updates.</LI>
 * </UL>
 * <UL>
 * <LI><I>Velux</I> <B>bridge</B> &rarr; <B>OpenHAB</B>:
 * <P>
 * Retrieving information by sending a Refresh command.</LI>
 * </UL>
 * <P>
 * Entry point for this class is the method
 * {@link VeluxBridgeHandler#handleCommand handleCommand}.
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
public class VeluxBridgeHandler extends ExtendedBaseBridgeHandler implements VeluxBridgeInstance, VeluxBridgeProvider {
    private final Logger logger = LoggerFactory.getLogger(VeluxBridgeHandler.class);

    // Class internal

    /**
     * Scheduler for continuous refresh by scheduleWithFixedDelay.
     */
    private @Nullable ScheduledFuture<?> refreshJob = null;

    /**
     * Counter of refresh invocations by {@link refreshJob}.
     */
    private int refreshCounter = 0;

    /**
     * Dedicated thread pool for the long-running bridge communication threads.
     */
    private ScheduledExecutorService handleScheduler = ThreadPoolManager
            .getScheduledPool(VeluxBindingConstants.BINDING_ID);

    private VeluxBridge myJsonBridge = new JsonVeluxBridge(this);
    private VeluxBridge mySlipBridge = new SlipVeluxBridge(this);

    /*
     * **************************************
     * ***** Default visibility Objects *****
     */

    VeluxBridge thisBridge = myJsonBridge;
    public BridgeParameters bridgeParameters = new BridgeParameters();
    Localization localization;

    /**
     * Mapping from ChannelUID to class Thing2VeluxActuator, which return Velux device information, probably cached.
     */
    Map<ChannelUID, Thing2VeluxActuator> channel2VeluxActuator = new ConcurrentHashMap<>();

    /**
     * Information retrieved by {@link VeluxBinding#VeluxBinding}.
     */
    private VeluxBridgeConfiguration veluxBridgeConfiguration = new VeluxBridgeConfiguration();

    /*
     * ************************
     * ***** Constructors *****
     */

    public VeluxBridgeHandler(final Bridge bridge, Localization localization) {
        super(bridge);
        logger.trace("VeluxBridgeHandler(constructor with bridge={}, localization={}) called.", bridge, localization);
        this.localization = localization;
        logger.debug("Creating a VeluxBridgeHandler for thing '{}'.", getThing().getUID());
    }

    // Private classes

    /**
     * <P>
     * Set of information retrieved from the bridge/gateway:
     * </P>
     * <UL>
     * <LI>{@link #actuators} - Already known actuators,</LI>
     * <LI>{@link #scenes} - Already on the gateway defined scenes,</LI>
     * <LI>{@link #gateway} - Current status of the gateway status,</LI>
     * <LI>{@link #firmware} - Information about the gateway firmware revision,</LI>
     * <LI>{@link #lanConfig} - Information about the gateway configuration,</LI>
     * <LI>{@link #wlanConfig} - Information about the gateway configuration.</LI>
     * </UL>
     */
    @NonNullByDefault
    public class BridgeParameters {
        /** Information retrieved by {@link VeluxBridgeActuators#getProducts} */
        public VeluxBridgeActuators actuators = new VeluxBridgeActuators();

        /** Information retrieved by {@link org.openhab.binding.velux.internal.bridge.VeluxBridgeScenes#getScenes} */
        VeluxBridgeScenes scenes = new VeluxBridgeScenes();

        /** Information retrieved by {@link VeluxBridgeDeviceStatus#retrieve} */
        VeluxBridgeDeviceStatus.Channel gateway = new VeluxBridgeDeviceStatus().getChannel();

        /** Information retrieved by {@link VeluxBridgeGetFirmware#retrieve} */
        VeluxBridgeGetFirmware.Channel firmware = new VeluxBridgeGetFirmware().getChannel();

        /** Information retrieved by {@link VeluxBridgeLANConfig#retrieve} */
        VeluxBridgeLANConfig.Channel lanConfig = new VeluxBridgeLANConfig().getChannel();

        /** Information retrieved by {@link VeluxBridgeWLANConfig#retrieve} */
        VeluxBridgeWLANConfig.Channel wlanConfig = new VeluxBridgeWLANConfig().getChannel();
    }

    // Private methods

    /**
     * Provide the ThingType for a given Channel.
     * <P>
     * Separated into this private method to deal with the deprecated method.
     * </P>
     *
     * @param channelUID for type {@link ChannelUID}.
     * @return thingTypeUID of type {@link ThingTypeUID}.
     */
    @SuppressWarnings("deprecation")
    ThingTypeUID thingTypeUIDOf(ChannelUID channelUID) {
        return channelUID.getThingUID().getThingTypeUID();
    }

    // Objects and Methods for interface VeluxBridgeInstance

    /**
     * Information retrieved by ...
     */
    @Override
    public VeluxBridgeConfiguration veluxBridgeConfiguration() {
        return veluxBridgeConfiguration;
    };

    /**
     * Information retrieved by {@link VeluxBridgeActuators#getProducts}
     */
    @Override
    public VeluxExistingProducts existingProducts() {
        return bridgeParameters.actuators.getChannel().existingProducts;
    };

    /**
     * Information retrieved by {@link VeluxBridgeScenes#getScenes}
     */
    @Override
    public VeluxExistingScenes existingScenes() {
        return bridgeParameters.scenes.getChannel().existingScenes;
    }

    // Objects and Methods for interface VeluxBridgeProvider *****

    @Override
    public boolean bridgeCommunicate(BridgeCommunicationProtocol communication) {
        logger.warn("bridgeCommunicate() called. Should never be called (as implemented by protocol-specific layers).");
        return false;
    }

    @Override
    public @Nullable BridgeAPI bridgeAPI() {
        logger.warn("bridgeAPI() called. Should never be called (as implemented by protocol-specific layers).");
        return null;
    }

    // Provisioning/Deprovisioning methods *****

    @Override
    public void initialize() {
        logger.info("Initializing Velux Bridge '{}'.", getThing().getUID());
        // The framework requires you to return from this method quickly.
        // Setting the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        logger.trace("initialize() called.");
        updateStatus(ThingStatus.UNKNOWN);
        // Take care of unusual situations...
        if (scheduler.isShutdown()) {
            logger.warn("initialize(): scheduler is shutdown, aborting the initialization of this bridge.");
            return;
        }
        if (handleScheduler.isShutdown()) {
            logger.trace("initialize(): handleScheduler is shutdown, aborting the initialization of this bridge.");
            return;
        }
        logger.trace("initialize(): preparing background initialization task.");
        // Background initialization...
        scheduler.execute(() -> {
            logger.trace("initialize.scheduled(): Further work within scheduler.execute().");
            logger.trace("initialize.scheduled(): Initializing bridge configuration parameters.");
            this.veluxBridgeConfiguration = new VeluxBinding(getConfigAs(VeluxBridgeConfiguration.class)).checked();
            logger.trace("initialize.scheduled(): work on updated bridge configuration parameters.");
            bridgeParamsUpdated();

            logger.debug("initialize.scheduled(): activated scheduler with {} milliseconds.",
                    this.veluxBridgeConfiguration.refreshMSecs);
            refreshJob = scheduler.scheduleWithFixedDelay(() -> {
                try {
                    refreshOpenHAB();
                } catch (RuntimeException e) {
                    logger.warn("Exception occurred during activated refresh scheduler: {}.", e.getMessage());
                }
            }, this.veluxBridgeConfiguration.refreshMSecs, this.veluxBridgeConfiguration.refreshMSecs,
                    TimeUnit.MILLISECONDS);
            logger.trace("initialize.scheduled(): done.");
        });
        logger.trace("initialize() done.");
    }

    /**
     * NOTE: It takes care about shutting down the connections before removal of this binding.
     */
    @Override
    public synchronized void dispose() {
        logger.info("Shutting down Velux Bridge '{}'.", getThing().getUID());
        logger.trace("dispose(): shutting down continous refresh.");
        // Just for avoidance of Potential null pointer access
        ScheduledFuture<?> currentRefreshJob = refreshJob;
        if (currentRefreshJob != null) {
            logger.trace("dispose(): stopping the refresh.");
            currentRefreshJob.cancel(true);
        }
        // Background execution of dispose
        scheduler.execute(() -> {
            logger.trace("dispose.scheduled(): (synchronous) logout initiated.");
            thisBridge.bridgeLogout();
            logger.trace("dispose.scheduled(): shutting down JSON bridge.");
            myJsonBridge.shutdown();
            logger.trace("dispose.scheduled(): shutting down SLIP bridge.");
            mySlipBridge.shutdown();
        });
        logger.trace("dispose(): calling super class.");
        super.dispose();
        logger.trace("dispose() done.");
    }

    /**
     * NOTE: It takes care by calling {@link #handleCommand} with the REFRESH command, that every used channel is
     * initialized.
     */
    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (thing.getStatus() == ThingStatus.ONLINE) {
            channel2VeluxActuator.put(channelUID, new Thing2VeluxActuator(this, channelUID));
            logger.trace("channelLinked({}) refreshing channel value with help of handleCommand as Thing is online.",
                    channelUID.getAsString());
            handleCommand(channelUID, RefreshType.REFRESH);
        } else {
            logger.trace("channelLinked({}) doing nothing as Thing is not online.", channelUID.getAsString());
        }
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        logger.trace("channelUnlinked({}) called.", channelUID.getAsString());
    }

    // Reconfiguration methods

    private void bridgeParamsUpdated() {
        logger.debug("bridgeParamsUpdated() called.");

        // Determine the appropriate bridge communication channel
        boolean validBridgeFound = false;
        if (myJsonBridge.supportedProtocols.contains(veluxBridgeConfiguration.protocol)) {
            logger.debug("bridgeParamsUpdated(): choosing JSON as communication method.");
            thisBridge = myJsonBridge;
            validBridgeFound = true;
        }
        if (mySlipBridge.supportedProtocols.contains(veluxBridgeConfiguration.protocol)) {
            logger.debug("bridgeParamsUpdated(): choosing SLIP as communication method.");
            thisBridge = mySlipBridge;
            validBridgeFound = true;
        }
        if (!validBridgeFound) {
            logger.debug("No valid protocol selected, aborting this {} binding.", VeluxBindingConstants.BINDING_ID);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/runtime.bridge-offline-no-valid-bridgeProtocol-selected");
            logger.trace("bridgeParamsUpdated() done.");
            return;
        }

        logger.trace("bridgeParamsUpdated(): Trying to authenticate towards bridge.");

        if (!thisBridge.bridgeLogin()) {
            logger.warn("{} bridge login sequence failed; expecting bridge is OFFLINE.",
                    VeluxBindingConstants.BINDING_ID);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/runtime.bridge-offline-login-sequence-failed");
            logger.trace("bridgeParamsUpdated() done.");
            return;
        }

        logger.trace("bridgeParamsUpdated(): Querying bridge state.");
        bridgeParameters.gateway = new VeluxBridgeDeviceStatus().retrieve(thisBridge);

        logger.trace("bridgeParamsUpdated(): Fetching existing scenes.");
        bridgeParameters.scenes.getScenes(thisBridge);
        logger.info("Found {} scenes:\n\t{}", VeluxBindingConstants.BINDING_ID,
                bridgeParameters.scenes.getChannel().existingScenes.toString(false, "\n\t"));
        logger.trace("bridgeParamsUpdated(): Fetching existing actuators/products.");
        bridgeParameters.actuators.getProducts(thisBridge);
        logger.info("Found {} actuators:\n\t{}", VeluxBindingConstants.BINDING_ID,
                bridgeParameters.actuators.getChannel().existingProducts.toString(false, "\n\t"));

        if (thisBridge.bridgeAPI().setHouseStatusMonitor() != null) {
            logger.trace("bridgeParamsUpdated(): Activating HouseStatusMonitor.");
            if (new VeluxBridgeSetHouseStatusMonitor().modifyHSM(thisBridge, true)) {
                logger.trace("bridgeParamsUpdated(): HSM activated.");
            } else {
                logger.warn("Activation of House-Status-Monitoring failed (might lead to a lack of status updates).");
            }
        }

        veluxBridgeConfiguration.hasChanged = false;
        logger.info("{} Bridge is online with {} scenes and {} actuators, now.", VeluxBindingConstants.BINDING_ID,
                bridgeParameters.scenes.getChannel().existingScenes.getNoMembers(),
                bridgeParameters.actuators.getChannel().existingProducts.getNoMembers());
        logger.debug("Velux veluxBridge is online, now.");
        updateStatus(ThingStatus.ONLINE);
        logger.trace("bridgeParamsUpdated() successfully finished.");
    }

    // Continuous synchronization methods

    private synchronized void refreshOpenHAB() {
        logger.debug("refreshOpenHAB() initiated by {} starting cycle {}.", Thread.currentThread(), refreshCounter);

        if (handleScheduler.isShutdown()) {
            logger.trace("refreshOpenHAB(): handleScheduler is shutdown, recreating a scheduler pool.");
            handleScheduler = ThreadPoolManager.getScheduledPool(VeluxBindingConstants.BINDING_ID);
        }

        logger.trace("refreshOpenHAB(): processing of possible HSM messages.");
        // Background execution of bridge related I/O
        handleScheduler.execute(() -> {
            logger.trace("refreshOpenHAB.scheduled() initiated by {} will process HouseStatus.",
                    Thread.currentThread());
            if (new VeluxBridgeGetHouseStatus().evaluateState(thisBridge)) {
                logger.trace("refreshOpenHAB.scheduled(): successfully processed of GetHouseStatus()");
            }
            logger.trace("refreshOpenHAB.scheduled() initiated by {} has finished.", Thread.currentThread());
        });

        logger.trace(
                "refreshOpenHAB(): looping through all (both child things and bridge) linked channels for a need of refresh.");
        for (ChannelUID channelUID : BridgeChannels.getAllLinkedChannelUIDs(this)) {
            if (VeluxItemType.isToBeRefreshedNow(refreshCounter, thingTypeUIDOf(channelUID), channelUID.getId())) {
                logger.trace("refreshOpenHAB(): refreshing channel {}.", channelUID);
                handleCommand(channelUID, RefreshType.REFRESH);
            }
        }
        logger.trace("refreshOpenHAB(): looping through properties for a need of refresh.");
        for (VeluxItemType veluxItem : VeluxItemType.getPropertyEntriesByThing(getThing().getThingTypeUID())) {
            if (VeluxItemType.isToBeRefreshedNow(refreshCounter, getThing().getThingTypeUID(),
                    veluxItem.getIdentifier())) {
                logger.trace("refreshOpenHAB(): refreshing property {}.", veluxItem.getIdentifier());
                handleCommand(new ChannelUID(getThing().getUID(), veluxItem.getIdentifier()), RefreshType.REFRESH);
            }
        }
        logger.debug("refreshOpenHAB() initiated by {} finished cycle {}.", Thread.currentThread(), refreshCounter);
        refreshCounter++;
    }

    /**
     * In case of recognized changes in the real world, the method will
     * update the corresponding states via openHAB event bus.
     */
    private void syncChannelsWithProducts() {
        if (!bridgeParameters.actuators.getChannel().existingProducts.isDirty()) {
            return;
        }
        logger.trace("syncChannelsWithProducts(): there are some existing products with changed parameters.");
        outer: for (VeluxProduct product : bridgeParameters.actuators.getChannel().existingProducts
                .valuesOfModified()) {
            logger.trace("syncChannelsWithProducts(): actuator {} has changed values.", product.getProductName());
            ProductBridgeIndex productPbi = product.getBridgeProductIndex();
            logger.trace("syncChannelsWithProducts(): bridge index is {}.", productPbi);
            for (ChannelUID channelUID : BridgeChannels.getAllLinkedChannelUIDs(this)) {
                if (!channel2VeluxActuator.containsKey(channelUID)) {
                    logger.trace("syncChannelsWithProducts(): channel {} not found.", channelUID);
                    continue;
                }
                if (!channel2VeluxActuator.get(channelUID).isKnown()) {
                    logger.trace("syncChannelsWithProducts(): channel {} not registered on bridge.", channelUID);
                    continue;
                }
                ProductBridgeIndex channelPbi = channel2VeluxActuator.get(channelUID).getProductBridgeIndex();
                if (!channelPbi.equals(productPbi)) {
                    continue;
                }
                // Handle value inversion
                boolean isInverted = channel2VeluxActuator.get(channelUID).isInverted();
                logger.trace("syncChannelsWithProducts(): isInverted is {}.", isInverted);
                VeluxProductPosition position = new VeluxProductPosition(product.getCurrentPosition());
                if (position.isValid()) {
                    PercentType positionAsPercent = position.getPositionAsPercentType(isInverted);
                    logger.debug("syncChannelsWithProducts(): updating channel {} to position {}%.", channelUID,
                            positionAsPercent);
                    updateState(channelUID, positionAsPercent);
                } else {
                    logger.trace("syncChannelsWithProducts(): update of channel {} to position {} skipped.", channelUID,
                            position);
                }
                break outer;
            }
        }
        logger.trace("syncChannelsWithProducts(): resetting dirty flag.");
        bridgeParameters.actuators.getChannel().existingProducts.resetDirtyFlag();
        logger.trace("syncChannelsWithProducts() done.");

    }

    // Processing of openHAB events

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("handleCommand({}): command {} on channel {} will be scheduled.", Thread.currentThread(), command,
                channelUID.getAsString());
        logger.debug("handleCommand({},{}) called.", channelUID.getAsString(), command);

        // Background execution of bridge related I/O
        handleScheduler.execute(() -> {
            logger.trace("handleCommand.scheduled({}) Start work with calling handleCommandScheduled().",
                    Thread.currentThread());
            handleCommandScheduled(channelUID, command);
            logger.trace("handleCommand.scheduled({}) done.", Thread.currentThread());
        });
        logger.trace("handleCommand({}) done.", Thread.currentThread());
    }

    /**
     * Normally called by {@link #handleCommand} to handle a command for a given channel with possibly long execution
     * time.
     * <p>
     * <B>NOTE:</B> This method is to be called as separated thread to ensure proper openHAB framework in parallel.
     * <p>
     *
     * @param channelUID the {@link ChannelUID} of the channel to which the command was sent,
     * @param command the {@link Command}.
     */
    private synchronized void handleCommandScheduled(ChannelUID channelUID, Command command) {
        logger.trace("handleCommandScheduled({}): command {} on channel {}.", Thread.currentThread(), command,
                channelUID.getAsString());
        logger.debug("handleCommandScheduled({},{}) called.", channelUID.getAsString(), command);

        /*
         * ===========================================================
         * Common part
         */

        if (veluxBridgeConfiguration.isProtocolTraceEnabled) {
            Threads.findDeadlocked();
        }

        String channelId = channelUID.getId();
        State newState = null;
        String itemName = channelUID.getAsString();
        VeluxItemType itemType = VeluxItemType.getByThingAndChannel(thingTypeUIDOf(channelUID), channelUID.getId());

        if (itemType == VeluxItemType.UNKNOWN) {
            logger.warn("{} Cannot determine type of Channel {}, ignoring command {}.",
                    VeluxBindingConstants.LOGGING_CONTACT, channelUID, command);
            logger.trace("handleCommandScheduled() aborting.");
            return;
        }

        // Build cache
        if (!channel2VeluxActuator.containsKey(channelUID)) {
            channel2VeluxActuator.put(channelUID, new Thing2VeluxActuator(this, channelUID));
        }

        if (veluxBridgeConfiguration.hasChanged) {
            logger.trace("handleCommandScheduled(): work on updated bridge configuration parameters.");
            bridgeParamsUpdated();
        }

        syncChannelsWithProducts();

        if (command instanceof RefreshType) {
            /*
             * ===========================================================
             * Refresh part
             */
            logger.trace("handleCommandScheduled(): work on refresh.");
            if (!itemType.isReadable()) {
                logger.debug("handleCommandScheduled(): received a Refresh command for a non-readable item.");
            } else {
                logger.trace("handleCommandScheduled(): refreshing item {} (type {}).", itemName, itemType);
                try { // expecting an IllegalArgumentException for unknown Velux device
                    switch (itemType) {
                        // Bridge channels
                        case BRIDGE_STATUS:
                            newState = ChannelBridgeStatus.handleRefresh(channelUID, channelId, this);
                            break;
                        case BRIDGE_DOWNTIME:
                            newState = new DecimalType(
                                    thisBridge.lastCommunication() - thisBridge.lastSuccessfulCommunication());
                            break;
                        case BRIDGE_FIRMWARE:
                            newState = ChannelBridgeFirmware.handleRefresh(channelUID, channelId, this);
                            break;
                        case BRIDGE_IPADDRESS:
                        case BRIDGE_SUBNETMASK:
                        case BRIDGE_DEFAULTGW:
                        case BRIDGE_DHCP:
                            newState = ChannelBridgeLANconfig.handleRefresh(channelUID, channelId, this);
                            break;
                        case BRIDGE_WLANSSID:
                        case BRIDGE_WLANPASSWORD:
                            newState = ChannelBridgeWLANconfig.handleRefresh(channelUID, channelId, this);
                            break;
                        case BRIDGE_SCENES:
                            newState = ChannelBridgeScenes.handleRefresh(channelUID, channelId, this);
                            break;
                        case BRIDGE_PRODUCTS:
                            newState = ChannelBridgeProducts.handleRefresh(channelUID, channelId, this);
                            break;
                        case BRIDGE_CHECK:
                            newState = ChannelBridgeCheck.handleRefresh(channelUID, channelId, this);
                            break;
                        // Actuator channels
                        case ACTUATOR_POSITION:
                        case ACTUATOR_STATE:
                        case ROLLERSHUTTER_POSITION:
                        case WINDOW_POSITION:
                            newState = ChannelActuatorPosition.handleRefresh(channelUID, channelId, this);
                            break;
                        case ACTUATOR_LIMIT_MINIMUM:
                        case ROLLERSHUTTER_LIMIT_MINIMUM:
                        case WINDOW_LIMIT_MINIMUM:
                            newState = ChannelActuatorLimitation.handleRefresh(channelUID, "", this);
                            break;
                        case ACTUATOR_LIMIT_MAXIMUM:
                        case ROLLERSHUTTER_LIMIT_MAXIMUM:
                        case WINDOW_LIMIT_MAXIMUM:
                            newState = ChannelActuatorLimitation.handleRefresh(channelUID, channelId, this);
                            break;

                        // VirtualShutter channels
                        case VSHUTTER_POSITION:
                            newState = ChannelVShutterPosition.handleRefresh(channelUID, channelId, this);
                            break;

                        default:
                            logger.trace(
                                    "handleCommandScheduled(): cannot handle REFRESH on channel {} as it is of type {}.",
                                    itemName, channelId);
                    }
                } catch (IllegalArgumentException e) {
                    logger.warn("Cannot handle REFRESH on channel {} as it isn't (yet) known to the bridge.", itemName);
                }
                if (newState != null) {
                    if (itemType.isChannel()) {
                        logger.debug("handleCommandScheduled(): updating channel {} to {}.", channelUID, newState);
                        updateState(channelUID, newState);
                    }
                    if (itemType.isProperty()) {
                        logger.debug("handleCommandScheduled(): updating property {} to {}.", channelUID, newState);
                        ThingProperty.setValue(this, itemType.getIdentifier(), newState.toString());

                    }
                } else {
                    logger.info("handleCommandScheduled({},{}): updating of item {} (type {}) failed.",
                            channelUID.getAsString(), command, itemName, itemType);
                }
            }
        } else {
            /*
             * ===========================================================
             * Modification part
             */
            logger.trace("handleCommandScheduled(): working on item {} (type {}) with COMMAND {}.", itemName, itemType,
                    command);
            Command newValue = null;
            try { // expecting an IllegalArgumentException for unknown Velux device
                switch (itemType) {
                    // Bridge channels
                    case BRIDGE_RELOAD:
                        if (command == OnOffType.ON) {
                            logger.trace("handleCommandScheduled(): about to reload informations from veluxBridge.");
                            bridgeParamsUpdated();
                        } else {
                            logger.trace("handleCommandScheduled(): ignoring OFF command.");
                        }
                        break;
                    case BRIDGE_DO_DETECTION:
                        ChannelBridgeDoDetection.handleCommand(channelUID, channelId, command, this);
                        break;

                    // Scene channels
                    case SCENE_ACTION:
                        ChannelSceneAction.handleCommand(channelUID, channelId, command, this);
                        break;
                    case SCENE_SILENTMODE:
                        ChannelSceneSilentmode.handleCommand(channelUID, channelId, command, this);
                        break;

                    // Actuator channels
                    case ACTUATOR_POSITION:
                    case ACTUATOR_STATE:
                    case ROLLERSHUTTER_POSITION:
                    case WINDOW_POSITION:
                        ChannelActuatorPosition.handleCommand(channelUID, channelId, command, this);
                        break;
                    case ACTUATOR_LIMIT_MINIMUM:
                    case ROLLERSHUTTER_LIMIT_MINIMUM:
                    case WINDOW_LIMIT_MINIMUM:
                        ChannelActuatorLimitation.handleCommand(channelUID, channelId, command, this);
                        break;
                    case ACTUATOR_LIMIT_MAXIMUM:
                    case ROLLERSHUTTER_LIMIT_MAXIMUM:
                    case WINDOW_LIMIT_MAXIMUM:
                        ChannelActuatorLimitation.handleCommand(channelUID, channelId, command, this);
                        break;

                    // VirtualShutter channels
                    case VSHUTTER_POSITION:
                        newValue = ChannelVShutterPosition.handleCommand(channelUID, channelId, command, this);
                        break;

                    default:
                        logger.warn("{} Cannot handle command {} on channel {} (type {}).",
                                VeluxBindingConstants.LOGGING_CONTACT, command, itemName, itemType);
                }
            } catch (IllegalArgumentException e) {
                logger.warn("Cannot handle command on channel {} as it isn't (yet) known to the bridge.", itemName);
            }
            if (newValue != null) {
                postCommand(channelUID, newValue);
            }
        }
        ThingProperty.setValue(this, VeluxBindingConstants.PROPERTY_BRIDGE_TIMESTAMP_ATTEMPT,
                new java.util.Date(thisBridge.lastCommunication()).toString());
        ThingProperty.setValue(this, VeluxBindingConstants.PROPERTY_BRIDGE_TIMESTAMP_SUCCESS,
                new java.util.Date(thisBridge.lastSuccessfulCommunication()).toString());
        logger.trace("handleCommandScheduled({}) done.", Thread.currentThread());
    }

}
