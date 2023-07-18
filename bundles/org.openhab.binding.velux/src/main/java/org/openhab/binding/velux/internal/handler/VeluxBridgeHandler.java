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
package org.openhab.binding.velux.internal.handler;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.velux.internal.VeluxBinding;
import org.openhab.binding.velux.internal.VeluxBindingConstants;
import org.openhab.binding.velux.internal.VeluxItemType;
import org.openhab.binding.velux.internal.action.VeluxActions;
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
import org.openhab.binding.velux.internal.bridge.common.RunProductCommand;
import org.openhab.binding.velux.internal.bridge.common.RunReboot;
import org.openhab.binding.velux.internal.bridge.json.JsonVeluxBridge;
import org.openhab.binding.velux.internal.bridge.slip.FunctionalParameters;
import org.openhab.binding.velux.internal.bridge.slip.SlipVeluxBridge;
import org.openhab.binding.velux.internal.config.VeluxBridgeConfiguration;
import org.openhab.binding.velux.internal.development.Threads;
import org.openhab.binding.velux.internal.factory.VeluxHandlerFactory;
import org.openhab.binding.velux.internal.handler.utils.ExtendedBaseBridgeHandler;
import org.openhab.binding.velux.internal.handler.utils.Thing2VeluxActuator;
import org.openhab.binding.velux.internal.handler.utils.ThingProperty;
import org.openhab.binding.velux.internal.things.VeluxExistingProducts;
import org.openhab.binding.velux.internal.things.VeluxExistingScenes;
import org.openhab.binding.velux.internal.things.VeluxProduct;
import org.openhab.binding.velux.internal.things.VeluxProduct.ProductBridgeIndex;
import org.openhab.binding.velux.internal.things.VeluxProductPosition;
import org.openhab.binding.velux.internal.things.VeluxProductPosition.PositionType;
import org.openhab.binding.velux.internal.utils.Localization;
import org.openhab.core.common.AbstractUID;
import org.openhab.core.common.NamedThreadFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
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

    /*
     * timeout to ensure that the binding shutdown will not block and stall the shutdown of OH itself
     */
    private static final int COMMUNICATION_TASK_MAX_WAIT_SECS = 10;

    /*
     * a modifier string to avoid the (small) risk of other tasks (outside this binding) locking on the same ip address
     * Strings.intern() object
     *
     */
    private static final String LOCK_MODIFIER = "velux.ipaddr.";

    private final Logger logger = LoggerFactory.getLogger(VeluxBridgeHandler.class);

    // Class internal

    /**
     * Scheduler for continuous refresh by scheduleWithFixedDelay.
     */
    private @Nullable ScheduledFuture<?> refreshSchedulerJob = null;

    /**
     * Counter of refresh invocations by {@link refreshSchedulerJob}.
     */
    private int refreshCounter = 0;

    /**
     * Dedicated task executor for the long-running bridge communication tasks.
     *
     * Note: there is no point in using multi threaded thread-pool here, since all the submitted (Runnable) tasks are
     * anyway forced to go through the same serial pipeline, because they all call the same class level "synchronized"
     * method to actually communicate with the KLF bridge via its one single TCP socket connection
     */
    private @Nullable ExecutorService communicationsJobExecutor = null;
    private @Nullable NamedThreadFactory threadFactory = null;

    private VeluxBridge myJsonBridge = new JsonVeluxBridge(this);
    private VeluxBridge mySlipBridge = new SlipVeluxBridge(this);
    private boolean disposing = false;

    /*
     * **************************************
     * ***** Default visibility Objects *****
     */

    public VeluxBridge thisBridge = myJsonBridge;
    public BridgeParameters bridgeParameters = new BridgeParameters();
    public Localization localization;

    /**
     * Mapping from ChannelUID to class Thing2VeluxActuator, which return Velux device information, probably cached.
     */
    public final Map<ChannelUID, Thing2VeluxActuator> channel2VeluxActuator = new ConcurrentHashMap<>();

    /**
     * Information retrieved by {@link VeluxBinding#VeluxBinding}.
     */
    private VeluxBridgeConfiguration veluxBridgeConfiguration = new VeluxBridgeConfiguration();

    private Duration offlineDelay = Duration.ofMinutes(5);
    private int initializeRetriesDone = 0;

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
    public ThingTypeUID thingTypeUIDOf(ChannelUID channelUID) {
        String[] segments = channelUID.getAsString().split(AbstractUID.SEPARATOR);
        if (segments.length > 1) {
            return new ThingTypeUID(segments[0], segments[1]);
        }
        logger.warn("thingTypeUIDOf({}) failed.", channelUID);
        return new ThingTypeUID(VeluxBindingConstants.BINDING_ID, VeluxBindingConstants.UNKNOWN_THING_TYPE_ID);
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
        // set the thing status to UNKNOWN temporarily and let the background task decide the real status
        updateStatus(ThingStatus.UNKNOWN);

        // take care of unusual situations...
        if (scheduler.isShutdown()) {
            logger.warn("initialize(): scheduler is shutdown, aborting initialization.");
            return;
        }

        logger.trace("initialize(): initialize bridge configuration parameters.");
        veluxBridgeConfiguration = new VeluxBinding(getConfigAs(VeluxBridgeConfiguration.class)).checked();

        /*
         * When a binding call to the hub fails with a communication error, it will retry the call for a maximum of
         * veluxBridgeConfiguration.retries times, where the interval between retry attempts increases on each attempt
         * calculated as veluxBridgeConfiguration.refreshMSecs * 2^retry (i.e. 1, 2, 4, 8, 16, 32 etc.) so a complete
         * retry series takes (veluxBridgeConfiguration.refreshMSecs * ((2^(veluxBridgeConfiguration.retries + 1)) - 1)
         * milliseconds. So we have to let this full retry series to have been tried (and failed), before we consider
         * the thing to be actually offline.
         */
        offlineDelay = Duration.ofMillis(
                ((long) Math.pow(2, veluxBridgeConfiguration.retries + 1) - 1) * veluxBridgeConfiguration.refreshMSecs);

        initializeRetriesDone = 0;

        scheduler.execute(() -> {
            disposing = false;
            initializeSchedulerJob();
        });
    }

    /**
     * Various initialisation actions to be executed on a background thread
     */
    private void initializeSchedulerJob() {
        /*
         * synchronize disposeSchedulerJob() and initializeSchedulerJob() based an IP address Strings.intern() object to
         * prevent overlap of initialization and disposal communications towards the same physical bridge
         */
        synchronized (LOCK_MODIFIER.concat(veluxBridgeConfiguration.ipAddress).intern()) {
            logger.trace("initializeSchedulerJob(): adopt new bridge configuration parameters.");
            bridgeParamsUpdated();

            if ((thing.getStatus() == ThingStatus.OFFLINE)
                    && (thing.getStatusInfo().getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR)) {
                if (initializeRetriesDone <= veluxBridgeConfiguration.retries) {
                    initializeRetriesDone++;
                    scheduler.schedule(() -> initializeSchedulerJob(),
                            ((long) Math.pow(2, initializeRetriesDone) * veluxBridgeConfiguration.timeoutMsecs),
                            TimeUnit.MILLISECONDS);
                }
                return;
            }

            long mSecs = veluxBridgeConfiguration.refreshMSecs;
            logger.trace("initializeSchedulerJob(): scheduling refresh at {} milliseconds.", mSecs);
            refreshSchedulerJob = scheduler.scheduleWithFixedDelay(() -> {
                refreshSchedulerJob();
            }, mSecs, mSecs, TimeUnit.MILLISECONDS);

            VeluxHandlerFactory.refreshBindingInfo();

            if (logger.isDebugEnabled()) {
                logger.debug("Velux Bridge '{}' is initialized (with {} scenes and {} actuators).", getThing().getUID(),
                        bridgeParameters.scenes.getChannel().existingScenes.getNoMembers(),
                        bridgeParameters.actuators.getChannel().existingProducts.getNoMembers());
            }
        }
    }

    @Override
    public void dispose() {
        scheduler.submit(() -> {
            disposing = true;
            disposeSchedulerJob();
        });
    }

    /**
     * Various disposal actions to be executed on a background thread
     */
    private void disposeSchedulerJob() {
        /*
         * synchronize disposeSchedulerJob() and initializeSchedulerJob() based an IP address Strings.intern() object to
         * prevent overlap of initialization and disposal communications towards the same physical bridge
         */
        synchronized (LOCK_MODIFIER.concat(veluxBridgeConfiguration.ipAddress).intern()) {
            /*
             * cancel the regular refresh polling job
             */
            ScheduledFuture<?> refreshSchedulerJob = this.refreshSchedulerJob;
            if (refreshSchedulerJob != null) {
                logger.trace("disposeSchedulerJob(): cancel the refresh polling job.");
                refreshSchedulerJob.cancel(false);
            }

            ExecutorService commsJobExecutor = this.communicationsJobExecutor;
            if (commsJobExecutor != null) {
                this.communicationsJobExecutor = null;
                logger.trace("disposeSchedulerJob(): cancel any other scheduled jobs.");
                /*
                 * remove un-started communication tasks from the execution queue; and stop accepting more tasks
                 */
                commsJobExecutor.shutdownNow();
                /*
                 * if the last bridge communication was OK, wait for already started task(s) to complete (so the bridge
                 * won't lock up); but to prevent stalling the OH shutdown process, time out after
                 * MAX_COMMUNICATION_TASK_WAIT_TIME_SECS
                 */
                if (thisBridge.lastCommunicationOk()) {
                    try {
                        if (!commsJobExecutor.awaitTermination(COMMUNICATION_TASK_MAX_WAIT_SECS, TimeUnit.SECONDS)) {
                            logger.warn("disposeSchedulerJob(): unexpected awaitTermination() timeout.");
                        }
                    } catch (InterruptedException e) {
                        logger.warn("disposeSchedulerJob(): unexpected exception awaitTermination() '{}'.",
                                e.getMessage());
                    }
                }
            }

            /*
             * if the last bridge communication was OK, deactivate HSM to prevent queueing more HSM events
             */
            if (thisBridge.lastCommunicationOk()
                    && (new VeluxBridgeSetHouseStatusMonitor().modifyHSM(thisBridge, false))) {
                logger.trace("disposeSchedulerJob(): HSM deactivated.");
            }

            /*
             * finally clean up everything else
             */
            logger.trace("disposeSchedulerJob(): shut down JSON connection interface.");
            myJsonBridge.shutdown();
            logger.trace("disposeSchedulerJob(): shut down SLIP connection interface.");
            mySlipBridge.shutdown();
            VeluxHandlerFactory.refreshBindingInfo();
            logger.debug("Velux Bridge '{}' is shut down.", getThing().getUID());
        }
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
        try {
            InetAddress bridgeAddress = InetAddress.getByName(veluxBridgeConfiguration.ipAddress);
            if (!bridgeAddress.isReachable(veluxBridgeConfiguration.timeoutMsecs)) {
                throw new IOException();
            }
        } catch (IOException e) {
            logger.debug("bridgeParamsUpdated(): Bridge ip address not reachable.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
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
        logger.debug("Found Velux scenes:\n\t{}",
                bridgeParameters.scenes.getChannel().existingScenes.toString(false, "\n\t"));
        logger.trace("bridgeParamsUpdated(): Fetching existing actuators/products.");
        bridgeParameters.actuators.getProducts(thisBridge);
        logger.debug("Found Velux actuators:\n\t{}",
                bridgeParameters.actuators.getChannel().existingProducts.toString(false, "\n\t"));

        if (thisBridge.bridgeAPI().setHouseStatusMonitor() != null) {
            logger.trace("bridgeParamsUpdated(): Activating HouseStatusMonitor.");
            if (new VeluxBridgeSetHouseStatusMonitor().modifyHSM(thisBridge, true)) {
                logger.trace("bridgeParamsUpdated(): HSM activated.");
            } else {
                logger.warn("Activation of House-Status-Monitoring failed (might lead to a lack of status updates).");
            }
        }

        updateDynamicChannels();

        veluxBridgeConfiguration.hasChanged = false;
        logger.debug("Velux veluxBridge is online, now.");
        updateStatus(ThingStatus.ONLINE);
        logger.trace("bridgeParamsUpdated() successfully finished.");
    }

    // Continuous synchronization methods

    private synchronized void refreshSchedulerJob() {
        logger.debug("refreshSchedulerJob() initiated by {} starting cycle {}.", Thread.currentThread(),
                refreshCounter);
        logger.trace("refreshSchedulerJob(): processing of possible HSM messages.");

        // Background execution of bridge related I/O
        submitCommunicationsJob(() -> {
            getHouseStatusCommsJob();
        });

        logger.trace(
                "refreshSchedulerJob(): loop through all (child things and bridge) linked channels needing a refresh");
        for (ChannelUID channelUID : BridgeChannels.getAllLinkedChannelUIDs(this)) {
            if (VeluxItemType.isToBeRefreshedNow(refreshCounter, thingTypeUIDOf(channelUID), channelUID.getId())) {
                logger.trace("refreshSchedulerJob(): refreshing channel {}.", channelUID);
                handleCommand(channelUID, RefreshType.REFRESH);
            }
        }

        logger.trace("refreshSchedulerJob(): loop through properties needing a refresh");
        for (VeluxItemType veluxItem : VeluxItemType.getPropertyEntriesByThing(getThing().getThingTypeUID())) {
            if (VeluxItemType.isToBeRefreshedNow(refreshCounter, getThing().getThingTypeUID(),
                    veluxItem.getIdentifier())) {
                logger.trace("refreshSchedulerJob(): refreshing property {}.", veluxItem.getIdentifier());
                handleCommand(new ChannelUID(getThing().getUID(), veluxItem.getIdentifier()), RefreshType.REFRESH);
            }
        }
        logger.debug("refreshSchedulerJob() initiated by {} finished cycle {}.", Thread.currentThread(),
                refreshCounter);
        refreshCounter++;
    }

    private void getHouseStatusCommsJob() {
        logger.trace("getHouseStatusCommsJob() initiated by {} will process HouseStatus.", Thread.currentThread());
        if (new VeluxBridgeGetHouseStatus().evaluateState(thisBridge)) {
            logger.trace("getHouseStatusCommsJob(): => GetHouseStatus() => updates received => synchronizing");
            syncChannelsWithProducts();
        } else {
            logger.trace("getHouseStatusCommsJob(): => GetHouseStatus() => no updates");
        }
        logger.trace("getHouseStatusCommsJob() initiated by {} has finished.", Thread.currentThread());
    }

    /**
     * In case of recognized changes in the real world, the method will
     * update the corresponding states via openHAB event bus.
     */
    private void syncChannelsWithProducts() {
        if (!bridgeParameters.actuators.getChannel().existingProducts.isDirty()) {
            logger.trace("syncChannelsWithProducts(): no existing products with changed parameters.");
            return;
        }
        logger.trace("syncChannelsWithProducts(): there are some existing products with changed parameters.");
        for (VeluxProduct product : bridgeParameters.actuators.getChannel().existingProducts.valuesOfModified()) {
            logger.trace("syncChannelsWithProducts(): actuator {} has changed values.", product.getProductName());
            ProductBridgeIndex productPbi = product.getBridgeProductIndex();
            logger.trace("syncChannelsWithProducts(): bridge index is {}.", productPbi);
            for (ChannelUID channelUID : BridgeChannels.getAllLinkedChannelUIDs(this)) {
                if (!VeluxBindingConstants.POSITION_CHANNELS.contains(channelUID.getId())) {
                    logger.trace("syncChannelsWithProducts(): skipping channel {}.", channelUID);
                    continue;
                }
                if (!channel2VeluxActuator.containsKey(channelUID)) {
                    logger.trace("syncChannelsWithProducts(): channel {} not found.", channelUID);
                    continue;
                }
                Thing2VeluxActuator actuator = channel2VeluxActuator.get(channelUID);
                if (actuator == null || !actuator.isKnown()) {
                    logger.trace("syncChannelsWithProducts(): channel {} not registered on bridge.", channelUID);
                    continue;
                }
                ProductBridgeIndex channelPbi = actuator.getProductBridgeIndex();
                if (!channelPbi.equals(productPbi)) {
                    continue;
                }
                boolean isInverted;
                VeluxProductPosition position;
                if (channelUID.getId().equals(VeluxBindingConstants.CHANNEL_VANE_POSITION)) {
                    isInverted = false;
                    position = new VeluxProductPosition(product.getVanePosition());
                } else {
                    // Handle value inversion
                    isInverted = actuator.isInverted();
                    logger.trace("syncChannelsWithProducts(): isInverted is {}.", isInverted);
                    position = new VeluxProductPosition(product.getDisplayPosition());
                }
                if (position.isValid()) {
                    PercentType positionAsPercent = position.getPositionAsPercentType(isInverted);
                    logger.debug("syncChannelsWithProducts(): updating channel {} to position {}%.", channelUID,
                            positionAsPercent);
                    updateState(channelUID, positionAsPercent);
                    continue;
                }
                logger.trace("syncChannelsWithProducts(): updating channel {} to 'UNDEFINED'.", channelUID);
                updateState(channelUID, UnDefType.UNDEF);
                continue;
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
        submitCommunicationsJob(() -> {
            handleCommandCommsJob(channelUID, command);
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
    private synchronized void handleCommandCommsJob(ChannelUID channelUID, Command command) {
        logger.trace("handleCommandCommsJob({}): command {} on channel {}.", Thread.currentThread(), command,
                channelUID.getAsString());
        logger.debug("handleCommandCommsJob({},{}) called.", channelUID.getAsString(), command);

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
            logger.trace("handleCommandCommsJob() aborting.");
            return;
        }

        // Build cache
        if (!channel2VeluxActuator.containsKey(channelUID)) {
            channel2VeluxActuator.put(channelUID, new Thing2VeluxActuator(this, channelUID));
        }

        if (veluxBridgeConfiguration.hasChanged) {
            logger.trace("handleCommandCommsJob(): work on updated bridge configuration parameters.");
            bridgeParamsUpdated();
        }

        syncChannelsWithProducts();

        if (command instanceof RefreshType) {
            /*
             * ===========================================================
             * Refresh part
             */
            logger.trace("handleCommandCommsJob(): work on refresh.");
            if (!itemType.isReadable()) {
                logger.debug("handleCommandCommsJob(): received a Refresh command for a non-readable item.");
            } else {
                logger.trace("handleCommandCommsJob(): refreshing item {} (type {}).", itemName, itemType);
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
                        case BRIDGE_ADDRESS:
                            // delete legacy property name entry (if any) and fall through
                            ThingProperty.setValue(this, VeluxBridgeConfiguration.BRIDGE_IPADDRESS, null);
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
                        case ROLLERSHUTTER_VANE_POSITION:
                            newState = ChannelActuatorPosition.handleRefresh(channelUID, channelId, this);
                            break;
                        case ACTUATOR_LIMIT_MINIMUM:
                        case ROLLERSHUTTER_LIMIT_MINIMUM:
                        case WINDOW_LIMIT_MINIMUM:
                            // note: the empty string ("") below is intentional
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
                            logger.warn("{} Cannot handle REFRESH on channel {} as it is of type {}.",
                                    VeluxBindingConstants.LOGGING_CONTACT, itemName, channelId);
                    }
                } catch (IllegalArgumentException e) {
                    logger.warn("Cannot handle REFRESH on channel {} as it isn't (yet) known to the bridge.", itemName);
                }
                if (newState != null) {
                    if (itemType.isChannel()) {
                        logger.debug("handleCommandCommsJob(): updating channel {} to {}.", channelUID, newState);
                        updateState(channelUID, newState);
                    } else if (itemType.isProperty()) {
                        // if property value is 'unknown', null it completely
                        String val = newState.toString();
                        if (VeluxBindingConstants.UNKNOWN.equals(val)) {
                            val = null;
                        }
                        logger.debug("handleCommandCommsJob(): updating property {} to {}.", channelUID, val);
                        ThingProperty.setValue(this, itemType.getIdentifier(), val);
                    }
                } else {
                    logger.warn("handleCommandCommsJob({},{}): updating of item {} (type {}) failed.",
                            channelUID.getAsString(), command, itemName, itemType);
                }
            }
        } else {
            /*
             * ===========================================================
             * Modification part
             */
            logger.trace("handleCommandCommsJob(): working on item {} (type {}) with COMMAND {}.", itemName, itemType,
                    command);
            Command newValue = null;
            try { // expecting an IllegalArgumentException for unknown Velux device
                switch (itemType) {
                    // Bridge channels
                    case BRIDGE_RELOAD:
                        if (command == OnOffType.ON) {
                            logger.trace("handleCommandCommsJob(): about to reload informations from veluxBridge.");
                            bridgeParamsUpdated();
                        } else {
                            logger.trace("handleCommandCommsJob(): ignoring OFF command.");
                        }
                        break;
                    case BRIDGE_DO_DETECTION:
                        ChannelBridgeDoDetection.handleCommand(channelUID, channelId, command, this);
                        break;

                    // Scene channels
                    case SCENE_ACTION:
                        ChannelSceneAction.handleCommand(channelUID, channelId, command, this);
                        break;

                    /*
                     * NOTA BENE: Setting of a scene silent mode is no longer supported via the KLF API (i.e. the
                     * GW_SET_NODE_VELOCITY_REQ/CFM command set is no longer supported in the API), so the binding can
                     * no longer explicitly support a Channel with such a function. Therefore the silent mode Channel
                     * type was removed from the binding implementation.
                     *
                     * By contrast scene actions can still be called with a silent mode argument, so a silent mode
                     * Configuration Parameter has been introduced as a means for the user to set this argument.
                     *
                     * Strictly speaking the following case statement will now never be called, so in theory it,
                     * AND ALL THE CLASSES BEHIND, could be deleted from the binding CODE BASE. But out of prudence
                     * it is retained anyway 'just in case'.
                     */
                    case SCENE_SILENTMODE:
                        ChannelSceneSilentmode.handleCommand(channelUID, channelId, command, this);
                        break;

                    // Actuator channels
                    case ACTUATOR_POSITION:
                    case ACTUATOR_STATE:
                    case ROLLERSHUTTER_POSITION:
                    case WINDOW_POSITION:
                    case ROLLERSHUTTER_VANE_POSITION:
                        newValue = ChannelActuatorPosition.handleCommand(channelUID, channelId, command, this);
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

        Instant lastCommunication = Instant.ofEpochMilli(thisBridge.lastCommunication());
        Instant lastSuccessfulCommunication = Instant.ofEpochMilli(thisBridge.lastSuccessfulCommunication());
        boolean lastCommunicationSucceeded = lastSuccessfulCommunication.equals(lastCommunication);
        ThingStatus thingStatus = getThing().getStatus();

        if (lastCommunicationSucceeded) {
            if (thingStatus == ThingStatus.OFFLINE || thingStatus == ThingStatus.UNKNOWN) {
                updateStatus(ThingStatus.ONLINE);
            }
        } else {
            if ((thingStatus == ThingStatus.ONLINE || thingStatus == ThingStatus.UNKNOWN)
                    && lastSuccessfulCommunication.plus(offlineDelay).isBefore(lastCommunication)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        }

        ThingProperty.setValue(this, VeluxBindingConstants.PROPERTY_BRIDGE_TIMESTAMP_ATTEMPT,
                lastCommunication.toString());
        ThingProperty.setValue(this, VeluxBindingConstants.PROPERTY_BRIDGE_TIMESTAMP_SUCCESS,
                lastSuccessfulCommunication.toString());

        logger.trace("handleCommandCommsJob({}) done.", Thread.currentThread());
    }

    /**
     * Register the exported actions
     */
    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(VeluxActions.class);
    }

    /**
     * Exported method (called by an OpenHAB Rules Action) to issue a reboot command to the hub.
     *
     * @return true if the command could be issued
     */
    public boolean rebootBridge() {
        logger.trace("runReboot() called on {}", getThing().getUID());
        RunReboot bcp = thisBridge.bridgeAPI().runReboot();
        if (bcp != null) {
            // background execution of reboot process
            submitCommunicationsJob(() -> {
                if (thisBridge.bridgeCommunicate(bcp)) {
                    logger.info("Reboot command {}successfully sent to {}", bcp.isCommunicationSuccessful() ? "" : "un",
                            getThing().getUID());
                }
            });
            return true;
        }
        return false;
    }

    /**
     * Exported method (called by an OpenHAB Rules Action) to move an actuator relative to its current position
     *
     * @param nodeId the node to be moved
     * @param relativePercent relative position change to the current position (-100% <= relativePercent <= +100%)
     * @return true if the command could be issued
     */
    public boolean moveRelative(int nodeId, int relativePercent) {
        logger.trace("moveRelative() called on {}", getThing().getUID());
        RunProductCommand bcp = thisBridge.bridgeAPI().runProductCommand();
        if (bcp != null) {
            // background execution of moveRelative
            submitCommunicationsJob(() -> {
                synchronized (bcp) {
                    bcp.setNodeIdAndParameters(nodeId,
                            new VeluxProductPosition(new PercentType(Math.abs(relativePercent))).overridePositionType(
                                    relativePercent > 0 ? PositionType.OFFSET_POSITIVE : PositionType.OFFSET_NEGATIVE),
                            null);
                    if (thisBridge.bridgeCommunicate(bcp)) {
                        logger.trace("moveRelative() command {}successfully sent to {}",
                                bcp.isCommunicationSuccessful() ? "" : "un", getThing().getUID());
                    }
                }
            });
            return true;
        }
        return false;
    }

    /**
     * If necessary initialise the communications job executor. Then check if the executor is shut down. And if it is
     * not shut down, then submit the given communications job for execution.
     */
    private void submitCommunicationsJob(Runnable communicationsJob) {
        ExecutorService commsJobExecutor = this.communicationsJobExecutor;
        if (commsJobExecutor == null) {
            commsJobExecutor = this.communicationsJobExecutor = Executors.newSingleThreadExecutor(getThreadFactory());
        }
        if (!commsJobExecutor.isShutdown()) {
            commsJobExecutor.execute(communicationsJob);
        }
    }

    /**
     * If necessary initialise the thread factory and return it
     *
     * @return the thread factory
     */
    public NamedThreadFactory getThreadFactory() {
        NamedThreadFactory threadFactory = this.threadFactory;
        if (threadFactory == null) {
            threadFactory = new NamedThreadFactory(getThing().getUID().getAsString());
        }
        return threadFactory;
    }

    /**
     * Indicates if the bridge thing is being disposed.
     *
     * @return true if the bridge thing is being disposed.
     */
    public boolean isDisposing() {
        return disposing;
    }

    /**
     * Exported method (called by an OpenHAB Rules Action) to simultaneously move the shade main position and the vane
     * position.
     *
     * @param node the node index in the bridge.
     * @param mainPosition the desired main position.
     * @param vanePosition the desired vane position.
     * @return true if the command could be issued.
     */
    public Boolean moveMainAndVane(ProductBridgeIndex node, PercentType mainPosition, PercentType vanePosition) {
        logger.trace("moveMainAndVane() called on {}", getThing().getUID());
        RunProductCommand bcp = thisBridge.bridgeAPI().runProductCommand();
        if (bcp != null) {
            VeluxProduct product = existingProducts().get(node).clone();
            FunctionalParameters functionalParameters = null;
            if (product.supportsVanePosition()) {
                int vanePos = new VeluxProductPosition(vanePosition).getPositionAsVeluxType();
                product.setVanePosition(vanePos);
                functionalParameters = product.getFunctionalParameters();
            }
            VeluxProductPosition mainPos = new VeluxProductPosition(mainPosition);
            bcp.setNodeIdAndParameters(node.toInt(), mainPos, functionalParameters);
            submitCommunicationsJob(() -> {
                if (thisBridge.bridgeCommunicate(bcp)) {
                    logger.trace("moveMainAndVane() command {}successfully sent to {}",
                            bcp.isCommunicationSuccessful() ? "" : "un", getThing().getUID());
                }
            });
            return true;
        }
        return false;
    }

    /**
     * Get the bridge product index for a given thing name.
     *
     * @param thingName the thing name
     * @return the bridge product index or ProductBridgeIndex.UNKNOWN if not found.
     */
    public ProductBridgeIndex getProductBridgeIndex(String thingName) {
        for (Entry<ChannelUID, Thing2VeluxActuator> entry : channel2VeluxActuator.entrySet()) {
            if (thingName.equals(entry.getKey().getThingUID().getAsString())) {
                return entry.getValue().getProductBridgeIndex();
            }
        }
        return ProductBridgeIndex.UNKNOWN;
    }

    /**
     * Ask all things in the hub to initialise their dynamic vane position channel if they support it.
     */
    private void updateDynamicChannels() {
        getThing().getThings().stream().forEach(thing -> {
            ThingHandler thingHandler = thing.getHandler();
            if (thingHandler instanceof VeluxHandler) {
                ((VeluxHandler) thingHandler).updateDynamicChannels(this);
            }
        });
    }
}
