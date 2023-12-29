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
package org.openhab.binding.fronius.internal.handler;

import java.util.Optional;

import javax.measure.Unit;

import org.openhab.binding.fronius.internal.FroniusBaseDeviceConfiguration;
import org.openhab.binding.fronius.internal.FroniusBindingConstants;
import org.openhab.binding.fronius.internal.FroniusBridgeConfiguration;
import org.openhab.binding.fronius.internal.FroniusCommunicationException;
import org.openhab.binding.fronius.internal.api.InverterRealtimeBodyData;
import org.openhab.binding.fronius.internal.api.InverterRealtimeResponse;
import org.openhab.binding.fronius.internal.api.PowerFlowRealtimeInverter;
import org.openhab.binding.fronius.internal.api.PowerFlowRealtimeResponse;
import org.openhab.binding.fronius.internal.api.PowerFlowRealtimeSite;
import org.openhab.binding.fronius.internal.api.ValueUnit;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FroniusSymoInverterHandler} is responsible for updating the data, which are
 * sent to one of the channels.
 *
 * @author Thomas Rokohl - Initial contribution
 * @author Peter Schraffl - Added device status and error status channels
 * @author Thomas Kordelle - Added inverter power, battery state of charge and PV solar yield
 * @author Jimmy Tanagra - Add powerflow autonomy, self consumption channels
 */
public class FroniusSymoInverterHandler extends FroniusBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(FroniusSymoInverterHandler.class);
    private InverterRealtimeResponse inverterRealtimeResponse;
    private PowerFlowRealtimeResponse powerFlowResponse;
    private FroniusBaseDeviceConfiguration config;

    public FroniusSymoInverterHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected String getDescription() {
        return "Fronius Symo Inverter";
    }

    @Override
    protected void handleRefresh(FroniusBridgeConfiguration bridgeConfiguration) throws FroniusCommunicationException {
        updateData(bridgeConfiguration, config);
        updateChannels();
    }

    @Override
    public void initialize() {
        config = getConfigAs(FroniusBaseDeviceConfiguration.class);
        super.initialize();
    }

    /**
     * Update the channel from the last data retrieved
     *
     * @param channelId the id identifying the channel to be updated
     * @return the last retrieved data
     */
    @Override
    protected State getValue(String channelId) {
        final String[] fields = channelId.split("#");
        if (fields.length < 1) {
            return null;
        }
        final String fieldName = fields[0];

        if (inverterRealtimeResponse != null) {
            InverterRealtimeBodyData inverterData = inverterRealtimeResponse.getBody().getData();
            switch (fieldName) {
                case FroniusBindingConstants.INVERTER_DATA_CHANNEL_PAC:
                    return getQuantityOrZero(inverterData.getPac(), Units.WATT);
                case FroniusBindingConstants.INVERTER_DATA_CHANNEL_FAC:
                    return getQuantityOrZero(inverterData.getFac(), Units.HERTZ);
                case FroniusBindingConstants.INVERTER_DATA_CHANNEL_IAC:
                    return getQuantityOrZero(inverterData.getIac(), Units.AMPERE);
                case FroniusBindingConstants.INVERTER_DATA_CHANNEL_IDC:
                    return getQuantityOrZero(inverterData.getIdc(), Units.AMPERE);
                case FroniusBindingConstants.INVERTER_DATA_CHANNEL_IDC2:
                    return getQuantityOrZero(inverterData.getIdc2(), Units.AMPERE);
                case FroniusBindingConstants.INVERTER_DATA_CHANNEL_IDC3:
                    return getQuantityOrZero(inverterData.getIdc3(), Units.AMPERE);
                case FroniusBindingConstants.INVERTER_DATA_CHANNEL_UAC:
                    return getQuantityOrZero(inverterData.getUac(), Units.VOLT);
                case FroniusBindingConstants.INVERTER_DATA_CHANNEL_UDC:
                    return getQuantityOrZero(inverterData.getUdc(), Units.VOLT);
                case FroniusBindingConstants.INVERTER_DATA_CHANNEL_UDC2:
                    return getQuantityOrZero(inverterData.getUdc2(), Units.VOLT);
                case FroniusBindingConstants.INVERTER_DATA_CHANNEL_UDC3:
                    return getQuantityOrZero(inverterData.getUdc3(), Units.VOLT);
                case FroniusBindingConstants.INVERTER_DATA_CHANNEL_PDC:
                    return calculatePower(inverterData.getUdc(), inverterData.getIdc());
                case FroniusBindingConstants.INVERTER_DATA_CHANNEL_PDC2:
                    return calculatePower(inverterData.getUdc2(), inverterData.getIdc2());
                case FroniusBindingConstants.INVERTER_DATA_CHANNEL_PDC3:
                    return calculatePower(inverterData.getUdc3(), inverterData.getIdc3());
                case FroniusBindingConstants.INVERTER_DATA_CHANNEL_DAY_ENERGY:
                    // Convert the unit to kWh for backwards compatibility with non-quantity type
                    return getQuantityOrZero(inverterData.getDayEnergy(), Units.KILOWATT_HOUR).toUnit("kWh");
                case FroniusBindingConstants.INVERTER_DATA_CHANNEL_TOTAL:
                    // Convert the unit to MWh for backwards compatibility with non-quantity type
                    return getQuantityOrZero(inverterData.getTotalEnergy(), Units.MEGAWATT_HOUR).toUnit("MWh");
                case FroniusBindingConstants.INVERTER_DATA_CHANNEL_YEAR:
                    // Convert the unit to MWh for backwards compatibility with non-quantity type
                    return getQuantityOrZero(inverterData.getYearEnergy(), Units.MEGAWATT_HOUR).toUnit("MWh");
                case FroniusBindingConstants.INVERTER_DATA_CHANNEL_DEVICE_STATUS_ERROR_CODE:
                    return new DecimalType(inverterData.getDeviceStatus().getErrorCode());
                case FroniusBindingConstants.INVERTER_DATA_CHANNEL_DEVICE_STATUS_STATUS_CODE:
                    return new DecimalType(inverterData.getDeviceStatus().getStatusCode());
                default:
                    break;
            }
        }

        if (powerFlowResponse != null) {
            PowerFlowRealtimeSite site = powerFlowResponse.getBody().getData().getSite();
            switch (fieldName) {
                case FroniusBindingConstants.POWER_FLOW_P_GRID:
                    return new QuantityType<>(site.getPgrid(), Units.WATT);
                case FroniusBindingConstants.POWER_FLOW_P_LOAD:
                    return new QuantityType<>(site.getPload(), Units.WATT);
                case FroniusBindingConstants.POWER_FLOW_P_AKKU:
                    return new QuantityType<>(site.getPakku(), Units.WATT);
                case FroniusBindingConstants.POWER_FLOW_P_PV:
                    return new QuantityType<>(site.getPpv(), Units.WATT);
                case FroniusBindingConstants.POWER_FLOW_AUTONOMY:
                    return new QuantityType<>(site.getRelAutonomy(), Units.PERCENT);
                case FroniusBindingConstants.POWER_FLOW_SELF_CONSUMPTION:
                    return new QuantityType<>(site.getRelSelfConsumption(), Units.PERCENT);
                case FroniusBindingConstants.POWER_FLOW_INVERTER_POWER:
                    return new QuantityType<>(getInverter(config.deviceId).getP(), Units.WATT);
                case FroniusBindingConstants.POWER_FLOW_INVERTER_SOC:
                    return new QuantityType<>(getInverter(config.deviceId).getSoc(), Units.PERCENT);

                // Kept for backwards compatibility
                case FroniusBindingConstants.POWER_FLOW_INVERTER_1_POWER:
                    return new QuantityType<>(getInverter(1).getP(), Units.WATT);
                case FroniusBindingConstants.POWER_FLOW_INVERTER_1_SOC:
                    return new QuantityType<>(getInverter(1).getSoc(), Units.PERCENT);
                default:
                    break;
            }
        }

        return null;
    }

    /**
     * get flow data for a specific inverter.
     *
     * @param number The inverter object of the given index
     * @return a PowerFlowRealtimeInverter object.
     */
    private PowerFlowRealtimeInverter getInverter(final int number) {
        return powerFlowResponse.getBody().getData().getInverters().get(Integer.toString(number));
    }

    /**
     * Return the value as QuantityType with the unit extracted from ValueUnit
     * or a zero QuantityType with the given unit argument when value is null
     * 
     * @param value The ValueUnit data
     * @param unit The default unit to use when value is null
     * @return a QuantityType from the given value
     */
    private QuantityType<?> getQuantityOrZero(ValueUnit value, Unit unit) {
        return Optional.ofNullable(value).map(val -> val.asQuantityType().toUnit(unit))
                .orElse(new QuantityType<>(0, unit));
    }

    /**
     * Get new data
     */
    private void updateData(FroniusBridgeConfiguration bridgeConfiguration, FroniusBaseDeviceConfiguration config)
            throws FroniusCommunicationException {
        inverterRealtimeResponse = getRealtimeData(bridgeConfiguration.hostname, config.deviceId);
        powerFlowResponse = getPowerFlowRealtime(bridgeConfiguration.hostname);
    }

    /**
     * Make the PowerFlowRealtimeDataRequest
     *
     * @param ip address of the device
     * @return {PowerFlowRealtimeResponse} the object representation of the json response
     */
    private PowerFlowRealtimeResponse getPowerFlowRealtime(String ip) throws FroniusCommunicationException {
        String location = FroniusBindingConstants.getPowerFlowDataUrl(ip);
        return collectDataFromUrl(PowerFlowRealtimeResponse.class, location);
    }

    /**
     * Make the InverterRealtimeDataRequest
     *
     * @param ip address of the device
     * @param deviceId of the device
     * @return {InverterRealtimeResponse} the object representation of the json response
     */
    private InverterRealtimeResponse getRealtimeData(String ip, int deviceId) throws FroniusCommunicationException {
        String location = FroniusBindingConstants.getInverterDataUrl(ip, deviceId);
        return collectDataFromUrl(InverterRealtimeResponse.class, location);
    }

    /**
     * Calculate the power value from the given voltage and current channels
     * 
     * @param voltage the voltage ValueUnit
     * @param current the current ValueUnit
     * @return {QuantityType<>} the power value calculated by multiplying voltage and current
     */
    private QuantityType<?> calculatePower(ValueUnit voltage, ValueUnit current) {
        QuantityType<?> qtyVoltage = getQuantityOrZero(voltage, Units.VOLT);
        QuantityType<?> qtyCurrent = getQuantityOrZero(current, Units.AMPERE);
        return qtyVoltage.multiply(qtyCurrent).toUnit(Units.WATT);
    }
}
