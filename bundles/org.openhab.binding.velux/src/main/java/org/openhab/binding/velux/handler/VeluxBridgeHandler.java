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
package org.openhab.binding.velux.handler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.velux.VeluxBindingConstants;
import org.openhab.binding.velux.VeluxBindingProperties;
import org.openhab.binding.velux.bridge.VeluxBridge;
import org.openhab.binding.velux.bridge.VeluxBridgeActuators;
import org.openhab.binding.velux.bridge.VeluxBridgeDeviceStatus;
import org.openhab.binding.velux.bridge.VeluxBridgeGetFirmware;
import org.openhab.binding.velux.bridge.VeluxBridgeGetHouseStatus;
import org.openhab.binding.velux.bridge.VeluxBridgeInstance;
import org.openhab.binding.velux.bridge.VeluxBridgeLANConfig;
import org.openhab.binding.velux.bridge.VeluxBridgeProvider;
import org.openhab.binding.velux.bridge.VeluxBridgeScenes;
import org.openhab.binding.velux.bridge.VeluxBridgeSetHouseStatusMonitor;
import org.openhab.binding.velux.bridge.VeluxBridgeWLANConfig;
import org.openhab.binding.velux.bridge.common.BridgeAPI;
import org.openhab.binding.velux.bridge.common.BridgeCommunicationProtocol;
import org.openhab.binding.velux.bridge.json.JsonVeluxBridge;
import org.openhab.binding.velux.bridge.slip.SlipVeluxBridge;
import org.openhab.binding.velux.internal.VeluxBinding;
import org.openhab.binding.velux.internal.VeluxItemType;
import org.openhab.binding.velux.internal.config.VeluxBridgeConfiguration;
import org.openhab.binding.velux.internal.utils.LoggerFulltrace;
import org.openhab.binding.velux.things.VeluxExistingProducts;
import org.openhab.binding.velux.things.VeluxExistingScenes;
import org.openhab.binding.velux.things.VeluxProduct;
import org.openhab.binding.velux.things.VeluxProductPosition;
import org.openhab.binding.velux.things.VeluxProductSerialNo;
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
    private final LoggerFulltrace log = new LoggerFulltrace(logger, false);

    /*
     * ***************************
     * ***** Private Objects *****
     */

    private @Nullable ScheduledFuture<?> refreshJob = null;
    private int refreshCounter = 0;

    private VeluxBridge myJsonBridge;
    private VeluxBridge mySlipBridge;

    /*
     * **************************************
     * ***** Default visibility Objects *****
     */

    BridgeParameters bridgeParameters;
    VeluxBridge thisBridge;

    /**
     * Information retrieved by {@link VeluxBinding#VeluxBinding}.
     */
    private VeluxBridgeConfiguration veluxBridgeConfiguration = new VeluxBridgeConfiguration();

    /*
     * ************************
     * ***** Constructors *****
     */

    public VeluxBridgeHandler(final Bridge bridge) {
        super(bridge);
        logger.trace("VeluxBridgeHandler(constructor with bridge={}) called.", bridge);
        logger.debug("Creating a VeluxBridgeHandler for thing '{}'.", getThing().getUID());
        bridgeParameters = new BridgeParameters();
        logger.trace("VeluxBridgeHandler(): Initializing empty storage for existing products.");
        bridgeParameters.actuators = new VeluxBridgeActuators();
        logger.trace("VeluxBridgeHandler(): Initializing empty storage for existing scenes.");
        bridgeParameters.scenes = new VeluxBridgeScenes();
        bridgeParameters.gateway = new VeluxBridgeDeviceStatus().getChannel();
        bridgeParameters.firmware = new VeluxBridgeGetFirmware().getChannel();
        bridgeParameters.lanConfig = new VeluxBridgeLANConfig().getChannel();
        bridgeParameters.wlanConfig = new VeluxBridgeWLANConfig().getChannel();
        //
        logger.trace("VeluxBridgeHandler(): Initializing the different bridge protocols.");
        myJsonBridge = new JsonVeluxBridge(this);
        mySlipBridge = new SlipVeluxBridge(this);
        thisBridge = myJsonBridge;
        logger.trace("VeluxBridgeHandler(constructor) done.");
    }

    /*
     * ***************************
     * ***** Private Classes *****
     */

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
        VeluxBridgeActuators actuators = new VeluxBridgeActuators();

        /** Information retrieved by {@link org.openhab.binding.velux.bridge.VeluxBridgeScenes#getScenes} */
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

    /*
     * ***************************
     * ***** Private Methods *****
     */

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
        log.fulltrace("thingTypeUIDOf({}) called.", channelUID);
        return channelUID.getThingUID().getThingTypeUID();
    }

    /*
     * *****************************************************************
     * ***** Objects and Methods for interface VeluxBridgeInstance *****
     */

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

    /*
     * *****************************************************************
     * ***** Objects and Methods for interface VeluxBridgeProvider *****
     */

    @Override
    public boolean bridgeCommunicate(BridgeCommunicationProtocol communication) {
        logger.warn("bridgeCommunicate() called. Auto-generated method stub. Should never be called.");
        return false;
    }

    @Override
    public @Nullable BridgeAPI bridgeAPI() {
        logger.warn("bridgeAPI() called. . Auto-generated method stub. Should never be called.");
        return null;
    }

    /***
     *** Continuous methods
     ***/

    private synchronized void execute() {
        logger.debug("execute() called.");
        logger.trace("execute(): processing of possible HSM messages.");
        if (new VeluxBridgeGetHouseStatus().evaluateState(thisBridge)) {
            logger.trace("execute(): successfully processed of GetHouseStatus()");
        }
        logger.trace("execute(): looping through all (both child things and bridge) channels for a need of refresh.");
        for (ChannelUID channelUID : BridgeChannels.getAllChannelUIDs(this)) {
            log.fulltrace("execute(): evaluating ChannelUID {}.", channelUID);
            if (VeluxItemType.isToBeRefreshedNow(this.refreshCounter, thingTypeUIDOf(channelUID), channelUID.getId())) {
                logger.trace("execute(): refreshing item {}.", channelUID);
                handleCommand(channelUID, RefreshType.REFRESH);
            }
        }
        this.refreshCounter++;
        logger.debug("execute() done.");
    }

    @Override
    public void initialize() {
        logger.debug("initialize() called.");
        logger.info("Initializing Velux veluxBridge handler for '{}'.", getThing().getUID());
        // The framework requires you to return from this method quickly.
        // Setting the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        updateStatus(ThingStatus.UNKNOWN);

        logger.trace("initialize(): preparing background initialization task.");

        // Background initialization:
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
                    execute();
                } catch (RuntimeException e) {
                    logger.warn("Exception occurred during activated refresh scheduler: {}, {}.", e.getMessage(),
                            ExceptionUtils.getStackTrace(e));
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
        logger.trace("dispose({}) called.", this);
        logger.debug("Shutting down Velux veluxBridge '{}'.", getThing().getUID());
        if (refreshJob != null) {
            logger.trace("dispose(): stopping the refresh.");
        } // Duplicate "if" construct due to warning about potential null pointer.
        if (refreshJob != null) {
            refreshJob.cancel(true);
            logger.trace("dispose(): having stopped refresh.");
        }
        logger.trace("dispose(): initiating logout.");
        thisBridge.bridgeLogout();
        logger.trace("dispose(): shutting down JSON bridge.");
        myJsonBridge.shutdown();
        logger.trace("dispose(): shutting down SLIP bridge.");
        mySlipBridge.shutdown();
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
        logger.trace("channelLinked({}) called.", channelUID.getAsString());

        if (thing.getStatus() == ThingStatus.ONLINE) {
            logger.trace("channelLinked() refreshing channel value with help of handleCommand as Thing is online.");
            handleCommand(channelUID, RefreshType.REFRESH);
        } else {
            logger.trace("channelLinked() doing nothing as Thing is not online.");
        }
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        logger.trace("channelUnlinked({}) called.", channelUID.getAsString());
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        logger.trace("childHandlerInitialized({},{}) called.", childHandler, childThing);
        super.childHandlerInitialized(childHandler, childThing);
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        logger.trace("childHandlerDisposed({},{}) called.", childHandler, childThing);
        super.childHandlerDisposed(childHandler, childThing);
    }

    /***
     *** Reconfiguration methods
     ***/
    private synchronized void bridgeParamsUpdated() {
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
        String scenes = bridgeParameters.scenes.getChannel().existingScenes.toString(false, "\n\t");
        logger.info("Found {} scenes:\n\t{}", VeluxBindingConstants.BINDING_ID, scenes);

        logger.trace("bridgeParamsUpdated(): Fetching existing actuators/products.");
        bridgeParameters.actuators.getProducts(thisBridge);
        String products = bridgeParameters.actuators.getChannel().existingProducts.toString(false, "\n\t");
        logger.info("Found {} actuators:\n\t{}", VeluxBindingConstants.BINDING_ID, products);

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

    /**
     * In case of recognized changes in the real world, the method will
     * update the corresponding states via openHAB event bus.
     */
    private void syncChannelsWithProducts() {
        logger.trace("syncChannelsWithProducts() called.");
        if (!bridgeParameters.actuators.getChannel().existingProducts.isDirty()) {
            logger.trace("syncChannelsWithProducts() done.");
            return;
        }
        logger.trace("syncChannelsWithProducts(): existingProducts have changed.");
        outer: for (VeluxProduct product : bridgeParameters.actuators.getChannel().existingProducts
                .valuesOfModified()) {
            logger.trace("syncChannelsWithProducts(): actuator {} has changed values.",
                    product.getProductName().toString());
            for (ChannelUID channelUID : BridgeChannels.getAllChannelUIDs(this)) {
                log.fulltrace("syncChannelsWithProducts(): evaluating ChannelUID {}.", channelUID);
                String itemName = channelUID.getAsString();
                VeluxItemType itemType = VeluxItemType.getByThingAndChannel(thingTypeUIDOf(channelUID),
                        channelUID.getId());
                log.fulltrace("syncChannelsWithProducts(): evaluating VeluxItemType {}.", itemType);
                if (itemType == VeluxItemType.UNKNOWN) {
                    continue;
                }
                switch (itemType) {
                    case ACTUATOR_POSITION:
                    case ACTUATOR_STATE:
                    case ROLLERSHUTTER_POSITION:
                    case WINDOW_POSITION:
                        log.fulltrace("syncChannelsWithProducts(): found on suitable entry of VeluxItemType.");
                        break;
                    default:
                        continue;
                }
                if (!ThingProperty.exists(this, channelUID, VeluxBindingProperties.PROPERTY_ACTUATOR_SERIALNUMBER)) {
                    logger.trace("syncChannelsWithProducts(): aborting processing as actuatorSerial is not set.");
                    break;
                }
                String actuatorSerial = (String) ThingProperty.getValue(this, channelUID,
                        VeluxBindingProperties.PROPERTY_ACTUATOR_SERIALNUMBER);

                // Handle value inversion
                boolean propertyInverted = false;
                if (ThingProperty.exists(this, channelUID, VeluxBindingProperties.PROPERTY_ACTUATOR_INVERTED)) {
                    propertyInverted = (boolean) ThingProperty.getValue(this, channelUID,
                            VeluxBindingProperties.PROPERTY_ACTUATOR_INVERTED);
                }
                boolean isInverted = propertyInverted || VeluxProductSerialNo.indicatesRevertedValues(actuatorSerial);
                logger.trace("syncChannelsWithProducts(): isInverted={}.", isInverted);
                actuatorSerial = VeluxProductSerialNo.cleaned(actuatorSerial);

                logger.trace("syncChannelsWithProducts(): working on actuatorSerial {}.", actuatorSerial);
                if (product.getSerialNumber().equals(actuatorSerial)) {
                    logger.trace("syncChannelsWithProducts(): product {}/{} used within item {}.",
                            product.getProductName(), product.getSerialNumber(), itemName);
                    try {
                        VeluxProductPosition position = new VeluxProductPosition(product.getCurrentPosition());
                        if (position.isValid()) {
                            PercentType positionAsPercent = position.getPositionAsPercentType(isInverted);
                            logger.debug("syncChannelsWithProducts(): updating item {} to position {}%.", itemName,
                                    positionAsPercent);
                            updateState(channelUID, positionAsPercent);
                        } else {
                            logger.trace("syncChannelsWithProducts(): update of item {} to position {} skipped.",
                                    itemName, position.toString());
                        }
                    } catch (RuntimeException e) {
                        logger.warn("syncChannelsWithProducts(): getProducts() exception: {}.", e.getMessage());
                    }
                    break outer;
                }
            }
        }
        logger.trace("syncChannelsWithProducts(): resetting dirty flag.");
        bridgeParameters.actuators.getChannel().existingProducts.resetDirtyFlag();
        logger.trace("syncChannelsWithProducts() done.");
    }

    /**
     * General two-way communication method.
     *
     * It provides either information retrieval or information update according to the passed command.
     *
     * @param channelUID The item passed as type {@link ChannelUID} for which to following command is addressed to.
     * @param command The command passed as type {@link Command} for the mentioned item. If
     *            {@code command} is {@code null}, an information retrieval via a <B>Refresh</B> command
     *            is initiated for this item.
     */
    @Override
    public synchronized void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand({},{}) called.", channelUID.getAsString(), command);

        /*
         * ===========================================================
         * Common part
         */

        String channelId = channelUID.getId();
        State newState = null;
        String itemName = channelUID.getAsString();
        VeluxItemType itemType = VeluxItemType.getByThingAndChannel(thingTypeUIDOf(channelUID), channelUID.getId());

        if (itemType == VeluxItemType.UNKNOWN) {
            logger.warn("handleCommand(): cannot determine type of Channel {}, ignoring command {}.", channelUID,
                    command);
            logger.trace("handleCommand() aborting.");
            return;
        }

        if (veluxBridgeConfiguration.hasChanged) {
            logger.trace("handleCommand(): work on updated bridge configuration parameters.");
            bridgeParamsUpdated();
        }

        syncChannelsWithProducts();

        if (command instanceof RefreshType) {
            /*
             * ===========================================================
             * Refresh part
             */
            logger.trace("handleCommand(): work on refresh.");
            if (!itemType.isReadable()) {
                logger.debug("handleCommand(): received a Refresh command for a non-readable item.");
            } else {
                logger.trace("handleCommand(): refreshing item {} (type {}).", itemName, itemType);
                switch (itemType) {
                    // Bridge channels
                    case BRIDGE_STATUS:
                        newState = ChannelBridgeStatus.handleRefresh(channelUID, channelId, this);
                        break;
                    case BRIDGE_TIMESTAMP:
                        newState = new DecimalType(thisBridge.lastSuccessfulCommunication());
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
                    case ACTUATOR_LIMITATION:
                    case ROLLERSHUTTER_LIMITATION:
                    case WINDOW_LIMITATION:
                        // TODO: waiting for feedback from Velux engineering
                        if (false) {
                            ChannelActuatorLimitation.handleRefresh(channelUID, channelId, this);
                        }
                        break;

                    // VirtualShutter channels
                    case VSHUTTER_POSITION:
                        newState = ChannelVShutterPosition.handleRefresh(channelUID, channelId, this);
                        break;

                    default:
                        logger.trace("handleCommand(): cannot handle REFRESH on channel {} as it is of type {}.",
                                itemName, channelId);
                }
            }
            if (newState != null) {
                logger.debug("handleCommand(): updating {} ({}) to {}.", itemName, channelUID, newState);
                updateState(channelUID, newState);
            } else {
                logger.info("handleCommand({},{}): updating of item {} (type {}) failed.", channelUID.getAsString(),
                        command, itemName, itemType);
            }
        } else {
            /*
             * ===========================================================
             * Modification part
             */
            logger.trace("handleCommand(): working on item {} (type {}) with COMMAND {}.", itemName, itemType, command);
            switch (itemType) {
                // Bridge channels
                case BRIDGE_RELOAD:
                    if (command.equals(OnOffType.ON)) {
                        logger.trace("handleCommand(): about to reload informations from veluxBridge.");
                        bridgeParamsUpdated();
                    } else {
                        logger.trace("handleCommand(): ignoring OFF command.");
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
                case ACTUATOR_LIMITATION:
                case ROLLERSHUTTER_LIMITATION:
                case WINDOW_LIMITATION:
                    // TODO: waiting for feedback from Velux engineering
                    if (false) {
                        ChannelActuatorLimitation.handleCommand(channelUID, channelId, command, this);
                    }
                    break;
                // ToDo: wait for Velux to correct implementation
                // case ACTUATOR_SILENTMODE:
                // logger.warn(
                // "handleCommand() sorry, but yet not implemented: cannot handle command {} on channel {} (type {}).",
                // command, itemName, itemType);
                // break;

                // VirtualShutter channels
                case VSHUTTER_POSITION:
                    Command newValue = ChannelVShutterPosition.handleCommand(channelUID, channelId, command, this);
                    if (newValue != null) {
                        postCommand(channelUID, newValue);
                    }
                    break;

                default:
                    logger.warn("handleCommand() cannot handle command {} on channel {} (type {}).", command, itemName,
                            itemType);
            }

        }
        ThingProperty.setValue(this, VeluxBindingConstants.CHANNEL_BRIDGE_IOTIMESTAMP, thisBridge.lastCommunication());
        ThingProperty.setValue(this, VeluxBindingConstants.CHANNEL_BRIDGE_TIMESTAMP,
                thisBridge.lastSuccessfulCommunication());
        logger.trace("handleCommand() done.");
    }

}
