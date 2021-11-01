/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.miio.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.miio.internal.MiIoBindingConfiguration;
import org.openhab.binding.miio.internal.basic.BasicChannelTypeProvider;
import org.openhab.binding.miio.internal.basic.MiIoBasicDevice;
import org.openhab.binding.miio.internal.basic.MiIoDatabaseWatchService;
import org.openhab.binding.miio.internal.cloud.CloudConnector;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MiIoLumiHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class MiIoLumiHandler extends MiIoBasicHandler {

    private final Logger logger = LoggerFactory.getLogger(MiIoLumiHandler.class);
    private @Nullable MiIoGatewayHandler bridgeHandler;

    public MiIoLumiHandler(Thing thing, MiIoDatabaseWatchService miIoDatabaseWatchService,
            CloudConnector cloudConnector, ChannelTypeRegistry channelTypeRegistry,
            BasicChannelTypeProvider basicChannelTypeProvider) {
        super(thing, miIoDatabaseWatchService, cloudConnector, channelTypeRegistry, basicChannelTypeProvider);
    }

    @Override
    public void initialize() {
        super.initialize();
        isIdentified = false;

        final MiIoBindingConfiguration config = this.configuration;
        if (config != null && config.deviceId.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Missing required deviceId");
            logger.info("Missing required deviceId for {} {}", getThing().getUID(), getThing().getLabel());
            return;
        }
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "No device bridge has been configured");
            logger.info("Missing Bridge for {} {}", getThing().getUID(), getThing().getLabel());
            return;
        } else {
            logger.info("Bridge for {} {} = {} {} ({})", getThing().getUID(), getThing().getLabel(),
                    bridge.getBridgeUID(), bridge.getLabel(), bridge.getHandler());
        }

        bridgeHandler = null;
        bridgeHandler = getBridgeHandler();
        if (ThingStatus.ONLINE != bridge.getStatus()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }
        updateStatus(ThingStatus.ONLINE);
    }

    @Nullable
    MiIoGatewayHandler getBridgeHandler() {
        if (bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge != null) {
                final MiIoGatewayHandler bridgeHandler = (MiIoGatewayHandler) bridge.getHandler();
                if (bridgeHandler != null) {
                    if (!bridgeHandler.childDevices.containsKey(getThing())) {
                        logger.warn(("Adding child device {} to bridge {}. We should not see this"),
                                getThing().getUID(), bridgeHandler.getThing().getUID());
                        bridgeHandler.childDevices.forEach((k, v) -> logger.debug("Devices in bridge: {} : {}", k, v));
                        bridgeHandler.childHandlerInitialized(this, getThing());
                    }
                    this.bridgeHandler = bridgeHandler;
                    return bridgeHandler;
                } else {
                    logger.debug("Bridge is defined, but bridge handler not found.");
                }
            }
            logger.debug("Bridge is missing.");
        }
        return this.bridgeHandler;
    }

    @Override
    public String getCloudServer() {
        if (bridgeHandler != null) {
            return bridgeHandler.getCloudServer();
        } else {
            final MiIoBindingConfiguration config = this.configuration;
            return config != null ? config.cloudServer : "";
        }
    }

    // Override to inject the sender
    @Override
    protected int sendCommand(String command, String params, String cloudServer, String sender) {
        final MiIoGatewayHandler bridge = getBridgeHandler();
        if (bridge != null) {
            logger.debug("Send via bridge {} {} (Cloudserver {})", command, params, cloudServer);
            return bridge.sendCommand(command, params, cloudServer, getThing().getUID().getAsString());
        } else {
            logger.debug("Bridge handler is null. This is unexpected and prevents sending the update");
        }
        return 0;
    }

    @Override
    protected synchronized void updateData() {
        logger.debug("Periodic update for '{}' ({})", getThing().getUID().toString(), getThing().getThingTypeUID());
        try {
            if (!hasConnection() || skipUpdate() || miioCom == null) {
                // return;
            }
            checkChannelStructure();
            final MiIoBindingConfiguration config = this.configuration;
            final MiIoBasicDevice midevice = miioDevice;
            if (midevice != null && configuration != null && config != null) {
                Bridge bridge = getBridge();
                if (bridge == null || !bridge.getStatus().equals(ThingStatus.ONLINE)) {
                    logger.debug("Bridge offline, skipping regular refresh");
                    refreshCustomProperties(midevice, true);
                    return;
                }
                logger.debug("Refresh properties for lumi device {}", getThing().getLabel());
                refreshProperties(midevice, config.deviceId);
                logger.debug("Refresh Custom for lumidevice {}", getThing().getLabel());
                refreshCustomProperties(midevice, false);
            } else {
                logger.debug("Null value occured for device {}: {}", midevice, config);
            }
        } catch (Exception e) {
            logger.debug("Error while updating '{}': ", getThing().getUID().toString(), e);
        }
    }
}
