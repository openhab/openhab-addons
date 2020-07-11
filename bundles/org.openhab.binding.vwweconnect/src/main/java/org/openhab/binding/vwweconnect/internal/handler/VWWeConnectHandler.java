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
package org.openhab.binding.vwweconnect.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.vwweconnect.internal.DeviceStatusListener;
import org.openhab.binding.vwweconnect.internal.VWWeConnectSession;
import org.openhab.binding.vwweconnect.internal.VehicleConfiguration;
import org.openhab.binding.vwweconnect.internal.model.BaseVehicle;
import org.openhab.binding.vwweconnect.internal.model.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class and handler for some of the different thing types that VWCarNet provides.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VWWeConnectHandler extends BaseThingHandler implements DeviceStatusListener {

    protected final Logger logger = LoggerFactory.getLogger(VWWeConnectHandler.class);

    protected @NonNullByDefault({}) VehicleConfiguration config;

    public VWWeConnectHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("VWWeConnectHandler handleCommand, channel: {}, command: {}", channelUID, command);
        if (command instanceof RefreshType) {
            Bridge bridge = getBridge();
            if (bridge != null) {
                BridgeHandler bridgeHandler = bridge.getHandler();
                if (bridgeHandler != null) {
                    // Lets do an immediate session refresh by calling bridge
                    bridgeHandler.handleCommand(channelUID, command);
                }
            }
            String vin = config.vin;
            VWWeConnectSession session = getSession();
            if (session != null && vin != null) {
                // In the mean time update vehicle with current status of session
                BaseVehicle thing = session.getVWWeConnectThing(vin);
                if (thing != null) {
                    update(thing);
                }
            }
        } else {
            logger.warn("Unknown command! {}", command);
        }
    }

    protected void scheduleImmediateRefresh(int refreshDelay) {
        logger.debug("scheduleImmediateRefresh on thing: {}", thing);
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() != null) {
            VWWeConnectBridgeHandler vbh = (VWWeConnectBridgeHandler) bridge.getHandler();
            if (vbh != null) {
                vbh.scheduleImmediateRefresh(refreshDelay);
            }
        }
    }

    protected @Nullable VWWeConnectSession getSession() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            VWWeConnectBridgeHandler vbh = (VWWeConnectBridgeHandler) bridge.getHandler();
            if (vbh != null) {
                return vbh.getSession();
            }
        }
        return null;
    }

    @Override
    public void initialize() {
        logger.debug("initialize on thing: {}", thing);

        config = getConfigAs(VehicleConfiguration.class);
        if (config.vin == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Vehicle is missing VIN");
        }

        // Set status to UNKNOWN and let background task set status
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            Bridge bridge = getBridge();
            if (bridge != null) {
                this.bridgeStatusChanged(bridge.getStatusInfo());
            }
        });
    }

    @Override
    public void dispose() {
        logger.debug("dispose on thing: {}", thing);
        Bridge bridge = getBridge();
        if (bridge != null) {
            VWWeConnectBridgeHandler vbh = (VWWeConnectBridgeHandler) bridge.getHandler();
            if (vbh != null) {
                VWWeConnectSession session = getSession();
                if (session != null) {
                    session.unregisterDeviceStatusListener(this);
                }
            }
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged bridgeStatusInfo: {}", bridgeStatusInfo);
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            Bridge bridge = getBridge();
            if (bridge != null) {
                VWWeConnectBridgeHandler vbh = (VWWeConnectBridgeHandler) bridge.getHandler();
                if (vbh != null) {
                    VWWeConnectSession session = getSession();
                    String vin = config.vin;
                    if (session != null && vin != null) {
                        BaseVehicle vehicle = session.getVWWeConnectThing(vin);
                        if (vehicle != null) {
                            update(vehicle);
                            session.registerDeviceStatusListener(this);
                        }
                    }
                }
            }
        }
        super.bridgeStatusChanged(bridgeStatusInfo);
    }

    @Override
    public void onDeviceStateChanged(@Nullable BaseVehicle vehicle) {
        logger.trace("onDeviceStateChanged on vehicle: {}", vehicle);
        if (vehicle != null) {
            if (vehicle instanceof Vehicle) {
                Vehicle theVehicle = (Vehicle) vehicle;
                if (config.vin != null && config.vin.equals((theVehicle.getCompleteVehicleJson().getVin()))) {
                    update(theVehicle);
                }
            } else {
                logger.debug("Update failed, unknown vehicle type.");
            }
        }
    }

    public synchronized void update(BaseVehicle thing) {
        logger.debug("Update on base class. {}", thing);
    }

    @Override
    public void onDeviceRemoved(@Nullable BaseVehicle thing) {
        logger.trace("onDeviceRemoved on thing: {}", thing);
    }

    @Override
    public void onDeviceAdded(@Nullable BaseVehicle thing) {
        logger.trace("onDeviceAdded on thing: {}", thing);
    }
}
