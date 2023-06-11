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
package org.openhab.binding.draytonwiser.internal.handler;

import static org.openhab.binding.draytonwiser.internal.DraytonWiserBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.draytonwiser.internal.api.DraytonWiserApiException;
import org.openhab.binding.draytonwiser.internal.handler.SmartPlugHandler.SmartPlugData;
import org.openhab.binding.draytonwiser.internal.model.DeviceDTO;
import org.openhab.binding.draytonwiser.internal.model.DraytonWiserDTO;
import org.openhab.binding.draytonwiser.internal.model.SmartPlugDTO;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link SmartPlugHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andrew Schofield - Initial contribution
 * @author Hilbrand Bouwkamp - Simplified handler to handle null data
 */
@NonNullByDefault
public class SmartPlugHandler extends DraytonWiserThingHandler<SmartPlugData> {

    private String serialNumber = "";

    public SmartPlugHandler(final Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        serialNumber = getConfig().get("serialNumber").toString();
    }

    @Override
    protected void handleCommand(final String channelId, final Command command) throws DraytonWiserApiException {
        if (command instanceof OnOffType) {
            switch (channelId) {
                case CHANNEL_DEVICE_LOCKED:
                    setDeviceLocked(OnOffType.ON.equals(command));
                    break;
                case CHANNEL_SMARTPLUG_OUTPUT_STATE:
                    setOutputState(OnOffType.ON.equals(command));
                    break;
                case CHANNEL_SMARTPLUG_AWAY_ACTION:
                    setAwayAction(OnOffType.ON.equals(command));
                    break;
                case CHANNEL_MANUAL_MODE_STATE:
                    setManualMode(OnOffType.ON.equals(command));
                    break;
            }
        }
    }

    @Override
    protected void refresh() {
        updateState(CHANNEL_SMARTPLUG_OUTPUT_STATE, this::getOutputState);
        updateState(CHANNEL_SMARTPLUG_AWAY_ACTION, this::getAwayAction);
        updateState(CHANNEL_CURRENT_SIGNAL_RSSI, this::getSignalRSSI);
        updateState(CHANNEL_CURRENT_SIGNAL_LQI, this::getSignalLQI);
        updateState(CHANNEL_ZIGBEE_CONNECTED, this::getZigbeeConnected);
        updateState(CHANNEL_DEVICE_LOCKED, this::getDeviceLocked);
        updateState(CHANNEL_MANUAL_MODE_STATE, this::getManualModeState);
        updateState(CHANNEL_SMARTPLUG_INSTANTANEOUS_POWER, this::getInstantaneousDemand);
        updateState(CHANNEL_SMARTPLUG_ENERGY_DELIVERED, this::getCurrentSummationDelivered);
    }

    @Override
    protected @Nullable SmartPlugData collectData(final DraytonWiserDTO domainDTOProxy) {
        final SmartPlugDTO smartPlug = domainDTOProxy.getSmartPlug(serialNumber);
        final DeviceDTO device = smartPlug == null ? null
                : domainDTOProxy.getExtendedDeviceProperties(smartPlug.getId());

        return smartPlug == null || device == null ? null : new SmartPlugData(smartPlug, device);
    }

    private State getAwayAction() {
        return OnOffType.from("off".equalsIgnoreCase(getData().smartPlug.getAwayAction()));
    }

    private State getOutputState() {
        final String outputState = getData().smartPlug.getOutputState();
        return outputState == null ? UnDefType.UNDEF : OnOffType.from(outputState);
    }

    private State getSignalRSSI() {
        final Integer rssi = getData().device.getRssi();
        return rssi == null ? UnDefType.UNDEF : new QuantityType<>(rssi, Units.DECIBEL_MILLIWATTS);
    }

    private State getSignalLQI() {
        final Integer lqi = getData().device.getLqi();
        return lqi == null ? UnDefType.UNDEF : new DecimalType(lqi);
    }

    private State getZigbeeConnected() {
        return getData().device.getLqi() == null ? OnOffType.OFF : OnOffType.ON;
    }

    private State getDeviceLocked() {
        return getData().device.getDeviceLockEnabled() == null ? UnDefType.UNDEF
                : OnOffType.from(getData().device.getDeviceLockEnabled());
    }

    private void setDeviceLocked(final Boolean state) throws DraytonWiserApiException {
        getApi().setDeviceLocked(getData().device.getId(), state);
    }

    private State getManualModeState() {
        return OnOffType.from("MANUAL".equalsIgnoreCase(getData().smartPlug.getMode()));
    }

    private void setManualMode(final Boolean manualMode) throws DraytonWiserApiException {
        getApi().setSmartPlugManualMode(getData().smartPlug.getId(), manualMode);
    }

    private void setOutputState(final Boolean outputState) throws DraytonWiserApiException {
        getApi().setSmartPlugOutputState(getData().smartPlug.getId(), outputState);
    }

    private void setAwayAction(final Boolean awayAction) throws DraytonWiserApiException {
        getApi().setSmartPlugAwayAction(getData().smartPlug.getId(), awayAction);
    }

    private State getInstantaneousDemand() {
        final Integer demand = getData().smartPlug.getInstantaneousDemand();
        return demand == null ? UnDefType.UNDEF : new QuantityType<>(demand, Units.WATT);
    }

    private State getCurrentSummationDelivered() {
        final Integer delivered = getData().smartPlug.getCurrentSummationDelivered();
        return delivered == null ? UnDefType.UNDEF : new QuantityType<>(delivered, Units.WATT_HOUR);
    }

    static class SmartPlugData {
        public final SmartPlugDTO smartPlug;
        public final DeviceDTO device;

        public SmartPlugData(final SmartPlugDTO smartPlug, final DeviceDTO device) {
            this.smartPlug = smartPlug;
            this.device = device;
        }
    }
}
