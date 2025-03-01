/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.senseenergy.internal.handler;

import static org.openhab.binding.senseenergy.internal.SenseEnergyBindingConstants.*;

import java.util.Arrays;
import java.util.HexFormat;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.senseenergy.internal.api.dto.SenseEnergyDatagramGetRealtime;
import org.openhab.binding.senseenergy.internal.api.dto.SenseEnergyDatagramGetSysInfo;
import org.openhab.binding.senseenergy.internal.config.SenseEnergyProxyDeviceConfiguration;
import org.openhab.binding.senseenergy.internal.handler.helpers.SenseEnergyPowerLevels;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @link { SenseEnergyProxyDeviceHandler }
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class SenseEnergyProxyDeviceHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(SenseEnergyProxyDeviceHandler.class);

    private static final byte[] OUI = new byte[] { 0x53, 0x75, 0x31 };
    private static final String PROXY_DEVICE_SW_VERSION = "1.2.5 Build 171206 Rel.085954";
    private static final String PROXY_DEVICE_HW_VERSION = "1.0";
    private static final String PROXY_DEVICE_TYPE = "IOT.SMARTPLUGSWITCH";
    private static final String PROXY_DEVICE_MODEL = "HS110(US)";

    private SenseEnergyProxyDeviceConfiguration config = new SenseEnergyProxyDeviceConfiguration();

    SenseEnergyPowerLevels powerLevels = new SenseEnergyPowerLevels();

    private ElectricalData electricalData = new ElectricalData();
    private boolean selfConfigurationChange = false;

    public SenseEnergyProxyDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(SenseEnergyProxyDeviceConfiguration.class);

        electricalData.setVoltage(config.voltage);

        Configuration c = null;
        if (config.macAddress.isBlank()) {
            byte[] mac = randomizeMAC(OUI);

            String macAddress = HexFormat.of().withDelimiter(":").formatHex(mac).toUpperCase();
            logger.debug("Spoof MAC address: {}", macAddress);

            selfConfigurationChange = true;
            c = this.editConfiguration();
            c.put(CONFIG_PARAMETER_MAC, macAddress);
            config.macAddress = macAddress;
        }

        if (!config.powerLevels.isBlank()) {
            try {
                powerLevels.parse(config.powerLevels);
            } catch (IllegalArgumentException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid power level entry");
                return;
            }
            String pretty = powerLevels.toString();
            if (!pretty.equals(config.powerLevels)) {
                selfConfigurationChange = true;
                if (c == null) {
                    c = this.editConfiguration();
                }

                c.put(CONFIG_PARAMETER_POWER_LEVELS, pretty);
            }
        }

        if (c != null) {
            this.updateConfiguration(c);
        }

        goOnline();
    }

    @Override
    public void updateStatus(ThingStatus thingStatus) {
        this.updateStatus(thingStatus, ThingStatusDetail.NONE, null);
    }

    @Override
    public void updateStatus(ThingStatus thingStatus, ThingStatusDetail thingStatusDetail) {
        this.updateStatus(thingStatus, thingStatusDetail, null);
    }

    @Override
    public void updateStatus(ThingStatus thingStatus, ThingStatusDetail thingStatusDetail,
            @Nullable String description) {
        super.updateStatus(thingStatus, thingStatusDetail, description);

        if (getBridge() instanceof Bridge bridge) {
            if (bridge.getHandler() instanceof SenseEnergyMonitorHandler monitorHandler) {
                monitorHandler.childStatusChange(this, thingStatus);
            }
        }
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        // prevent re-initialization when handler changed configuration
        if (this.selfConfigurationChange) {
            this.selfConfigurationChange = false;
            return;
        }
        super.handleConfigurationUpdate(configurationParameters);
    }

    public void goOnline() {
        if (!checkBridgeStatus()) {
            return;
        }

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Received command");
        if (command instanceof RefreshType) {
            // these are input only channels
            return;
        }

        QuantityType<Power> qt;

        switch (channelUID.getId()) {
            case CHANNEL_PROXY_DEVICE_POWER:
                if (command instanceof QuantityType<?> qtAbs) {
                    if (qtAbs.getUnit().isCompatible(Units.WATT)) {
                        // guaranteed to be compatible with Units.WATT
                        electricalData.setPower(Objects.requireNonNull(qtAbs.toUnit(Units.WATT)).floatValue());
                        logger.debug("Received power update: {} -> {}", command.toString(), electricalData.getPower());
                    }
                }

                break;
            case CHANNEL_PROXY_DEVICE_SWITCH:
            case CHANNEL_PROXY_DEVICE_DIMMER:
                if (command instanceof OnOffType onOffCommand) {
                    qt = powerLevels.getLevel((onOffCommand == OnOffType.ON) ? 100 : 0);
                    if (qt != null) {
                        electricalData.setPower(qt.floatValue());
                        logger.debug("Received switch update: {} -> {}", command.toString(), qt);
                        return;
                    } else {
                        qt = powerLevels.getLevel((onOffCommand == OnOffType.ON) ? "ON" : "OFF");
                        if (qt != null) {
                            electricalData.setPower(qt.floatValue());
                            logger.debug("Received switch update: {} -> {}", command.toString(), qt);
                            return;
                        }
                        logger.debug("No power levels specified for command: {}", command);
                    }
                } else if (command instanceof PercentType percentType) {
                    qt = powerLevels.getLevel(percentType.intValue());
                    if (qt != null) {
                        electricalData.setPower(qt.floatValue());
                        logger.debug("Received dimmer update: {} -> {}", command.toString(), qt);
                    }
                }
                break;

            case CHANNEL_PROXY_DEVICE_STATE: {
                if (command instanceof StringType stringCommand) {
                    qt = powerLevels.getLevel(stringCommand.toString());
                    if (qt != null) {
                        electricalData.setPower(qt.floatValue());
                        logger.debug("Received state update: {} -> {}", command.toString(), qt);
                    }
                }
                break;
            }
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            goOnline();
        }
    }

    public boolean checkBridgeStatus() {
        Bridge bridge = this.getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.bridge-missing");
            return false;
        }

        SenseEnergyMonitorHandler bridgeHandler = (SenseEnergyMonitorHandler) bridge.getHandler();
        if (bridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            return false;
        }

        if (bridgeHandler.getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return false;
        }

        return true;
    }

    public boolean formPowerResponse(SenseEnergyDatagramGetSysInfo getSysInfo,
            SenseEnergyDatagramGetRealtime getRealtime) {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            return false;
        }

        getSysInfo.swVersion = PROXY_DEVICE_SW_VERSION;
        getSysInfo.hwVersion = PROXY_DEVICE_HW_VERSION;
        getSysInfo.type = PROXY_DEVICE_TYPE;
        getSysInfo.model = PROXY_DEVICE_MODEL;
        getSysInfo.relayState = 1; // not sure what this does

        getSysInfo.mac = getMAC();
        getSysInfo.deviceId = getThing().getUID().getId().toString();
        getSysInfo.alias = (config.senseName.isBlank()) ? getThing().getLabel() : config.senseName;
        getSysInfo.errorCode = 0;

        getRealtime.current = Math.round(electricalData.getCurrent() * 10) / 10;
        getRealtime.voltage = Math.round(electricalData.getVoltage());
        getRealtime.power = Math.round(electricalData.getPower() * 10) / 10;
        getRealtime.errorCode = 0;

        return true;
    }

    public ElectricalData getElectricalData() {
        return this.electricalData;
    }

    public String getMAC() {
        return config.macAddress;
    }

    public byte[] randomizeMAC(byte @Nullable [] oui) {
        byte[] macAddress = new byte[6];
        Random rand = new Random();

        for (int i = 0; i < 6; i++) {
            macAddress[i] = (byte) rand.nextInt(255);
        }

        if (oui != null) {
            macAddress = Arrays.copyOf(oui, oui.length);
        }

        return macAddress;
    }

    class ElectricalData {
        private float power;
        private float voltage;
        private float current;

        public void setVoltage(float voltage) {
            this.voltage = voltage;
        }

        public void setCurrent(float current) {
            this.current = current;
            this.power = voltage * current;
        }

        public void setPower(float power) {
            this.power = power;
            this.current = (voltage != 0) ? power / voltage : 0;
        }

        public float getVoltage() {
            return voltage;
        }

        public float getCurrent() {
            return current;
        }

        public float getPower() {
            return power;
        }
    }
}
