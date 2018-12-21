/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.veluxklf200.internal.handler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.veluxklf200.internal.VeluxKLF200BindingConstants;
import org.openhab.binding.veluxklf200.internal.VeluxKLF200Configuration;
import org.openhab.binding.veluxklf200.internal.commands.CommandStatus;
import org.openhab.binding.veluxklf200.internal.commands.KlfCmdEnableHomeStatusMonitor;
import org.openhab.binding.veluxklf200.internal.commands.KlfCmdGetNode;
import org.openhab.binding.veluxklf200.internal.commands.KlfCmdGetProtocol;
import org.openhab.binding.veluxklf200.internal.commands.KlfCmdGetVersion;
import org.openhab.binding.veluxklf200.internal.commands.KlfCmdPing;
import org.openhab.binding.veluxklf200.internal.commands.KlfCmdSetTime;
import org.openhab.binding.veluxklf200.internal.components.VeluxNodeType;
import org.openhab.binding.veluxklf200.internal.components.VeluxPosition;
import org.openhab.binding.veluxklf200.internal.engine.KLFCommandProcessor;
import org.openhab.binding.veluxklf200.internal.engine.KLFEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridge for managing the connection with the Velux KLF200 unit.
 *
 * @author MFK - Initial Contribution
 */
public class KLF200BridgeHandler extends BaseBridgeHandler implements KLFEventListener {

    /** The logger. */
    private Logger logger = LoggerFactory.getLogger(KLF200BridgeHandler.class);

    /** Reference to the CommandProcessor that is setup and initialised at startup */
    private KLFCommandProcessor klf200;

    /**
     * Constructor
     *
     * @param bridge the bridge
     */
    public KLF200BridgeHandler(Bridge bridge) {
        super(bridge);
    }

    /*
     * Only thing that bridge actually maintains is the connectivity item. Once connected, this is set to ON.
     *
     * @see
     * org.eclipse.smarthome.core.thing.binding.ThingHandler#handleCommand(org.eclipse.smarthome.core.thing.ChannelUID,
     * org.eclipse.smarthome.core.types.Command)
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handling bridge command: {} for channel: {}", command, channelUID);

        if (command == RefreshType.REFRESH) {
            switch (channelUID.getId()) {
                case VeluxKLF200BindingConstants.BRIDGE_CONNECTIVITY:
                    if ((null != klf200) && (klf200.isAvailable())) {
                        updateState(channelUID.getId(), OnOffType.ON);
                    } else {
                        updateState(channelUID.getId(), OnOffType.OFF);
                    }
                    break;
            }
        }
    }

    /*
     * Key initialisation tasks are the establishment of a connection to the KLF200 based on the configuration
     * parameters (host, port and password). Assuming that these parameters have been set correctly, the main cause of a
     * failure in respect of connectivity is that the KLF200 has shutdown is TCP port. According to the API
     * documentation, this can happen after a period of inactivity. As such, if a connection failure occurs, it is
     * recommenced to reboot your KLF200 unit and retry. Once a connection has been established, the {@link
     * KLFCommandProcessor} takes care of sending periodic keep-alive pings to the unit to make sure that the socket
     * doesn't close again while the binding is running.
     *
     * Once the binding is running, every 5 minutes (based on default settings), the KLF200 is queried and the state of
     * all items that have been setup in OH is updated. In theory this should not be necessarry as the binding takes
     * care of _CFM (confirmation) notifications from the KLF200 when things change. However, this is done just in case
     * something gets out of sync.
     *
     * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandler#initialize()
     */
    @Override
    public void initialize() {
        VeluxKLF200Configuration config = getConfigAs(VeluxKLF200Configuration.class);
        String err = validateConfiguration(config);
        if (null != err) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, err);
        } else {
            logger.debug("Configuration valid. Attempting to connect to KLF200.");
            this.klf200 = new KLFCommandProcessor(config.hostname, config.port, config.password);
            if (!this.klf200.initialise()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Failed to connect to the KLF200. Check log files for further diagnostic information.");
            } else {
                klf200.registerEventListener(this);
                updateStatus(ThingStatus.ONLINE);

                // Periodically refresh the state of all of the nodes / things that we are interested in.
                scheduler.scheduleWithFixedDelay(new Runnable() {
                    @Override
                    public void run() {
                        refreshKnownDevices();

                        // Also update the time in case the unit has been rebooted since the binding was loaded
                        klf200.executeCommand(new KlfCmdSetTime());
                    }
                }, config.refresh, config.refresh, TimeUnit.MINUTES);

                // Run the remainder of the post init tasks asynchronously.
                scheduler.execute(new Runnable() {
                    @Override
                    public void run() {
                        updateBridgeProperties();
                    }
                });
            }
        }
    }

    /**
     * Periodic refresh of the status of each item that has been configured in OH.
     * See {@link initialize()} comments for further information.
     */
    @SuppressWarnings("null")
    private void refreshKnownDevices() {
        logger.debug("Refreshing all KLF200 devices");
        List<Thing> things = getThing().getThings();
        for (Thing t : things) {
            // Refresh all vertical interior blinds
            if (VeluxKLF200BindingConstants.THING_TYPE_VELUX_BLIND.equals(t.getThingTypeUID())) {
                logger.debug("Refreshing {} with Id {}", t, t.getUID().getId());
                KlfCmdGetNode node = new KlfCmdGetNode((byte) Integer.valueOf(t.getUID().getId()).intValue());
                this.klf200.executeCommand(node);
                if (node.getCommandStatus() == CommandStatus.COMPLETE) {
                    if (node.getNode().getCurrentPosition().isUnknown()) {
                        logger.debug(
                                "Blind '{}' position is currentley unknown. Need to wait for an activation for KLF200 to learn its position.",
                                node.getNode().getName());
                        updateState(t.getChannel(VeluxKLF200BindingConstants.VELUX_BLIND_POSITION).getUID(),
                                UnDefType.UNDEF);
                    } else {
                        int pctClosed = node.getNode().getCurrentPosition().getPercentageClosedAsInt();
                        logger.debug("Blind '{}' is currentley {}% closed.", node.getNode().getName(), pctClosed);
                        updateState(t.getChannel(VeluxKLF200BindingConstants.VELUX_BLIND_POSITION).getUID(),
                                new PercentType(pctClosed));
                    }
                } else {
                    logger.error("Failed to retrieve information about node {}, error detail: {}", node.getNodeId(),
                            node.getCommandStatus().getErrorDetail());
                }
            }
        }
    }

    /**
     * Once connected to the KLF200 unit, its internal properties such as Hardware, software and protocol versions are
     * retrieved and updated in the properties of the bridge. This is for informational purposes only.
     */
    private void updateBridgeProperties() {
        KlfCmdGetProtocol proto = new KlfCmdGetProtocol();
        KlfCmdGetVersion ver = new KlfCmdGetVersion();
        KlfCmdPing ping = new KlfCmdPing();
        KlfCmdSetTime time = new KlfCmdSetTime();
        KlfCmdEnableHomeStatusMonitor monitor = new KlfCmdEnableHomeStatusMonitor();
        Map<String, String> properties = this.editProperties();
        this.klf200.executeCommand(ver);
        if (ver.getCommandStatus() == CommandStatus.COMPLETE) {
            properties.put("Hardware Version", ver.getHardwareVersion());
            properties.put("Software Version", ver.getSoftwareVersion());
            properties.put("Product Group", ver.getProductGroup());
            properties.put("Product Type", ver.getProductType());
        } else {
            logger.error("Unable to retrieve KLF20 Version Information: {}", ver.getCommandStatus().getErrorDetail());
        }
        this.klf200.executeCommand(proto);
        if (proto.getCommandStatus() == CommandStatus.COMPLETE) {
            properties.put("Protocol", proto.getProtocol());
        } else {
            logger.error("Unable to retrieve KLF20 Protocol Information: {}",
                    proto.getCommandStatus().getErrorDetail());
        }
        this.klf200.executeCommand(ping);
        if (ping.getCommandStatus() == CommandStatus.COMPLETE) {
            properties.put("State", ping.getGatewayState());
        } else {
            logger.error("Unable to ping the KLF200: {}", ping.getCommandStatus().getErrorDetail());
        }
        this.updateProperties(properties);
        this.klf200.executeCommand(time);
        if (time.getCommandStatus() == CommandStatus.COMPLETE) {
            logger.info("Time on the KLF200 updated to reflect current time on this system.");
        } else {
            logger.error("Unable to update the time on the KLF200: {}", time.getCommandStatus().getErrorDetail());
        }
        this.klf200.executeCommand(monitor);
        if (monitor.getCommandStatus() == CommandStatus.COMPLETE) {
            logger.info("Home status monitoring enabled.");
        } else {
            logger.error("Unable to enable the home status monitor on the KLF200: {}",
                    monitor.getCommandStatus().getErrorDetail());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandler#dispose()
     */
    @Override
    public void dispose() {
        logger.debug("Disposing of bridge handler.");
        this.klf200.shutdown();
    }

    /**
     * Validate configuration provided and in the event of issues, provide some feedback to aid the user.
     *
     * @param config config object
     * @return null in the case of validation passing, non-null if something was determined to be invalid.
     */
    private String validateConfiguration(VeluxKLF200Configuration config) {
        if (null == config.password) {
            return "A password must be specified. Please update the panel 'thing' configuration.";
        }
        if (null == config.port) {
            return "A port must be specified. Please update the panel 'thing' configuration.";
        }
        if (null == config.hostname) {
            return "An IP address or hostname must be specified. Please update the panel 'thing' configuration.";
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openhab.binding.veluxklf200.internal.engine.KLFEventListener#handleEvent(org.openhab.binding.veluxklf200.
     * internal.components.VeluxNodeType, byte, short)
     */
    @Override
    public void handleEvent(VeluxNodeType nodeType, byte nodeId, short currentPosition) {
        logger.debug("Notified of event for node type {} for specific node {}", nodeType, nodeId);
        switch (nodeType) {
            case VERTICAL_INTERIOR_BLINDS:
                handleVerticalInteriorBlindNotification(nodeId, currentPosition);
                break;
            default:
                // IMPORTANT
                // For now, only implementing notification callbacks for "Vertical Interior Blinds". In time, this
                // can be expanded to include other types as required. Throwing a runtime exception here as the only way
                // that this code can be reached is if an LFLEventNotification is updated to support different types of
                // events without a corresponding update here.
                logger.error("Unsupported notification recieved: {}", nodeType);
        }

    }

    /**
     * Handles a notification from the KLF200 in relation to the position / movement of a 'Vertical Interior Blind'.
     * These notifications are dispatched when the KLF200 broadcasts a _CFM (Confirmation) in respect of a particular
     * blind.
     *
     * @param nodeId          Node ID of the blind corresponds to the ID of the particular thing in OH
     * @param currentPosition Current position of the blind. This needs to be interpreted to derive an actual position,
     *                            see {@link VeluxPosition} in the case of a blind.
     */
    private void handleVerticalInteriorBlindNotification(byte nodeId, short currentPosition) {
        Thing thing = findThing(VeluxKLF200BindingConstants.THING_TYPE_VELUX_BLIND, nodeId);
        if (null != thing) {
            Channel ch = thing.getChannel(VeluxKLF200BindingConstants.VELUX_BLIND_POSITION);
            if (null != ch) {
                VeluxPosition position = new VeluxPosition(currentPosition);
                if (position.isUnknown()) {
                    logger.debug(
                            "Blind '{}' position is currentley unknown. Need to wait for an activation for KLF200 to learn its position.",
                            nodeId);
                    updateState(ch.getUID(), UnDefType.UNDEF);
                } else {
                    int pctClosed = position.getPercentageClosedAsInt();
                    logger.debug("Blind '{}' is currentley {}% closed.", nodeId, pctClosed);
                    updateState(ch.getUID(), new PercentType(pctClosed));
                }
            }
        }
    }

    /**
     * Given a thing UID and an instance ID of that thing, try to find it among the list of things that we manage.
     *
     * @param thingUID thing UID
     * @param instance instance ID (eg: nodeId)
     * @return The thing object or null of nothing was found.
     */
    protected Thing findThing(ThingTypeUID thingUID, int instance) {
        List<Thing> things = getThing().getThings();
        for (Thing t : things) {
            if (t.getThingTypeUID().equals(thingUID)) {
                if (VeluxKLF200BindingConstants.THING_TYPE_VELUX_BLIND.equals(thingUID)) {
                    if (String.valueOf(instance).equals(t.getUID().getId())) {
                        logger.debug("Found thing requested: {}", t);
                        return t;
                    }
                } else {
                    return t;
                }
            }
        }
        return null;
    }

    /**
     * Returns a reference to the CommandProcessor for the KLF200 unit.
     *
     * @return KLF200 Command Processor reference.
     */
    public KLFCommandProcessor getKLFCommandProcessor() {
        return this.klf200;
    }
}
