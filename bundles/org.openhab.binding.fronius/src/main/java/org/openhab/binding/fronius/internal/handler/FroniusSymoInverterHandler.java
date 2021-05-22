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
package org.openhab.binding.fronius.internal.handler;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openhab.binding.fronius.internal.FroniusBaseDeviceConfiguration;
import org.openhab.binding.fronius.internal.FroniusBindingConstants;
import org.openhab.binding.fronius.internal.FroniusBridgeConfiguration;
import org.openhab.binding.fronius.internal.api.InverterRealtimeResponse;
import org.openhab.binding.fronius.internal.api.PowerFlowRealtimeInverter;
import org.openhab.binding.fronius.internal.api.PowerFlowRealtimeResponse;
import org.openhab.binding.fronius.internal.api.ValueUnit;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FroniusSymoInverterHandler} is responsible for updating the data, which are
 * sent to one of the channels.
 *
 * @author Thomas Rokohl - Initial contribution
 * @author Peter Schraffl - Added device status and error status channels
 * @author Thomas Kordelle - Added inverter power, battery state of charge and PV solar yield
 */
public class FroniusSymoInverterHandler extends FroniusBaseThingHandler {

    /* power produced/handled by the inverter. */
    public static final String INVERTER_POWER = "power";
    /* state of charge of the battery or other storage device */
    public static final String INVERTER_SOC = "soc";

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
    public void refresh(FroniusBridgeConfiguration bridgeConfiguration) {
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
    protected Object getValue(String channelId) {
        String[] fields = StringUtils.split(channelId, "#");

        String fieldName = fields[0];

        if (inverterRealtimeResponse != null) {
            switch (fieldName) {
                case FroniusBindingConstants.InverterDataChannelDayEnergy:
                    ValueUnit day = inverterRealtimeResponse.getBody().getData().getDayEnergy();
                    if (day != null) {
                        day.setUnit("kWh");
                    }
                    return day;
                case FroniusBindingConstants.InverterDataChannelPac:
                    ValueUnit pac = inverterRealtimeResponse.getBody().getData().getPac();
                    if (pac == null) {
                        pac = new ValueUnit();
                        pac.setValue(0);
                    }
                    return pac;
                case FroniusBindingConstants.InverterDataChannelTotal:
                    ValueUnit total = inverterRealtimeResponse.getBody().getData().getTotalEnergy();
                    if (total != null) {
                        total.setUnit("MWh");
                    }
                    return total;
                case FroniusBindingConstants.InverterDataChannelYear:
                    ValueUnit year = inverterRealtimeResponse.getBody().getData().getYearEnergy();
                    if (year != null) {
                        year.setUnit("MWh");
                    }
                    return year;
                case FroniusBindingConstants.InverterDataChannelFac:
                    return inverterRealtimeResponse.getBody().getData().getFac();
                case FroniusBindingConstants.InverterDataChannelIac:
                    return inverterRealtimeResponse.getBody().getData().getIac();
                case FroniusBindingConstants.InverterDataChannelIdc:
                    return inverterRealtimeResponse.getBody().getData().getIdc();
                case FroniusBindingConstants.InverterDataChannelUac:
                    return inverterRealtimeResponse.getBody().getData().getUac();
                case FroniusBindingConstants.InverterDataChannelUdc:
                    return inverterRealtimeResponse.getBody().getData().getUdc();
                case FroniusBindingConstants.InverterDataChannelDeviceStatusErrorCode:
                    return inverterRealtimeResponse.getBody().getData().getDeviceStatus().getErrorCode();
                case FroniusBindingConstants.InverterDataChannelDeviceStatusStatusCode:
                    return inverterRealtimeResponse.getBody().getData().getDeviceStatus().getStatusCode();
                default:
                    break;
            }
        }

        if (powerFlowResponse != null) {
            switch (fieldName) {
                case FroniusBindingConstants.PowerFlowpGrid:
                    return powerFlowResponse.getBody().getData().getSite().getPgrid();
                case FroniusBindingConstants.PowerFlowpLoad:
                    return powerFlowResponse.getBody().getData().getSite().getPload();
                case FroniusBindingConstants.PowerFlowpAkku:
                    return powerFlowResponse.getBody().getData().getSite().getPakku();
                case FroniusBindingConstants.PowerFlowpPv:
                    return powerFlowResponse.getBody().getData().getSite().getPpv();
                case FroniusBindingConstants.PowerFlowInverter1Power:
                    return getInverterFlowValue(INVERTER_POWER, "1");
                case FroniusBindingConstants.PowerFlowInverter1Soc:
                    return getInverterFlowValue(INVERTER_SOC, "1");
                default:
                    break;
            }
        }

        return null;
    }

    /**
     * get flow data for a specific inverter.
     *
     * @param fieldName
     * @param number
     * @return
     */
    private Object getInverterFlowValue(final String fieldName, final String number) {
        final Map<String, PowerFlowRealtimeInverter> inverters = powerFlowResponse.getBody().getData().getInverters();
        if ((inverters == null) || !inverters.containsKey(number)) {
            logger.debug("No data for inverter '" + number + "' found.");
            return null;
        }
        switch (fieldName) {
            case INVERTER_POWER:
                return inverters.get(number).getP();
            case INVERTER_SOC:
                return inverters.get(number).getSoc();
            default:
                break;
        }
        return null;
    }

    /**
     * Get new data
     */
    private void updateData(FroniusBridgeConfiguration bridgeConfiguration, FroniusBaseDeviceConfiguration config) {
        inverterRealtimeResponse = getRealtimeData(bridgeConfiguration.hostname, config.deviceId);
        powerFlowResponse = getPowerFlowRealtime(bridgeConfiguration.hostname);
    }

    /**
     * Make the PowerFlowRealtimeDataRequest
     *
     * @param ip address of the device
     * @return {PowerFlowRealtimeResponse} the object representation of the json response
     */
    private PowerFlowRealtimeResponse getPowerFlowRealtime(String ip) {
        String location = FroniusBindingConstants.POWERFLOW_REALTIME_DATA.replace("%IP%", StringUtils.trimToEmpty(ip));
        return collectDataFormUrl(PowerFlowRealtimeResponse.class, location);
    }

    /**
     * Make the InverterRealtimeDataRequest
     *
     * @param ip address of the device
     * @param deviceId of the device
     * @return {InverterRealtimeResponse} the object representation of the json response
     */
    private InverterRealtimeResponse getRealtimeData(String ip, int deviceId) {
        String location = FroniusBindingConstants.INVERTER_REALTIME_DATA_URL.replace("%IP%",
                StringUtils.trimToEmpty(ip));
        location = location.replace("%DEVICEID%", Integer.toString(deviceId));
        return collectDataFormUrl(InverterRealtimeResponse.class, location);
    }
}
