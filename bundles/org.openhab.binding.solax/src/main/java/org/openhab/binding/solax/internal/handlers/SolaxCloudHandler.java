/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.solax.internal.handlers;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solax.internal.SolaxBindingConstants;
import org.openhab.binding.solax.internal.SolaxConfiguration;
import org.openhab.binding.solax.internal.connectivity.CloudHttpConnector;
import org.openhab.binding.solax.internal.connectivity.SolaxConnector;
import org.openhab.binding.solax.internal.connectivity.rawdata.cloud.CloudRawDataBean;
import org.openhab.binding.solax.internal.exceptions.SolaxUpdateException;
import org.openhab.binding.solax.internal.model.cloud.CloudInverterData;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;

/**
 * The {@link SolaxCloudHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class SolaxCloudHandler extends AbstractSolaxHandler {

    public SolaxCloudHandler(Thing thing, TranslationProvider i18nProvider, TimeZoneProvider timeZoneProvider,
            LocaleProvider localeProvider) {
        super(thing, i18nProvider, timeZoneProvider);
    }

    @Override
    protected SolaxConnector createConnector(SolaxConfiguration config) {
        return new CloudHttpConnector(config.token, config.password);
    }

    @Override
    protected void updateFromData(String rawJsonData) throws SolaxUpdateException {
        CloudRawDataBean rawCloudBean = CloudRawDataBean.fromJson(rawJsonData);
        if (!rawCloudBean.isValid()) {
            throw new SolaxUpdateException("Deserialized JSON response is not valid. Bean = {}, rawJsonData = {}",
                    rawCloudBean, rawJsonData);
        }

        if (!rawCloudBean.isSuccess() || rawCloudBean.isError()) {
            throw new SolaxUpdateException(
                    "Connection to cloud was successful but the cloud API returned error. response = {}", rawCloudBean);
        }

        updateProperties(rawCloudBean);
        updateChannels(rawCloudBean);
    }

    private void updateProperties(CloudInverterData cloudData) {
        updateProperty(Thing.PROPERTY_SERIAL_NUMBER, cloudData.getInverterSerialNumber());
        updateProperty(SolaxBindingConstants.PROPERTY_INVERTER_TYPE, cloudData.getInverterType().name());
    }

    private void updateChannels(CloudInverterData inverterData) {
        updateState(SolaxBindingConstants.CHANNEL_INVERTER_PV1_POWER,
                new QuantityType<>(inverterData.getPowerPv1(), Units.WATT));
        updateState(SolaxBindingConstants.CHANNEL_INVERTER_PV2_POWER,
                new QuantityType<>(inverterData.getPowerPv2(), Units.WATT));
        updateState(SolaxBindingConstants.CHANNEL_INVERTER_PV3_POWER,
                new QuantityType<>(inverterData.getPowerPv3(), Units.WATT));
        updateState(SolaxBindingConstants.CHANNEL_INVERTER_PV4_POWER,
                new QuantityType<>(inverterData.getPowerPv4(), Units.WATT));
        updateState(SolaxBindingConstants.CHANNEL_INVERTER_PV_TOTAL_POWER,
                new QuantityType<>(inverterData.getPVTotalPower(), Units.WATT));

        updateState(SolaxBindingConstants.CHANNEL_BATTERY_POWER,
                new QuantityType<>(inverterData.getBatteryPower(), Units.WATT));
        updateState(SolaxBindingConstants.CHANNEL_BATTERY_STATE_OF_CHARGE,
                new QuantityType<>(inverterData.getBatteryLevel(), Units.PERCENT));
        updateState(SolaxBindingConstants.CHANNEL_FEED_IN_POWER,
                new QuantityType<>(inverterData.getFeedInPower(), Units.WATT));

        updateState(SolaxBindingConstants.CHANNEL_TOTAL_FEED_IN_ENERGY,
                new QuantityType<>(inverterData.getFeedInEnergy(), Units.KILOWATT_HOUR));
        updateState(SolaxBindingConstants.CHANNEL_TOTAL_CONSUMPTION,
                new QuantityType<>(inverterData.getConsumeEnergy(), Units.KILOWATT_HOUR));
        updateState(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_POWER_METER2,
                new QuantityType<>(inverterData.getFeedInPowerM2(), Units.WATT));
        updateState(SolaxBindingConstants.CHANNEL_INVERTER_EPS_POWER_R,
                new QuantityType<>(inverterData.getEPSPowerR(), Units.WATT));
        updateState(SolaxBindingConstants.CHANNEL_INVERTER_EPS_POWER_S,
                new QuantityType<>(inverterData.getEPSPowerS(), Units.WATT));
        updateState(SolaxBindingConstants.CHANNEL_INVERTER_EPS_POWER_T,
                new QuantityType<>(inverterData.getEPSPowerT(), Units.WATT));

        updateState(SolaxBindingConstants.CHANNEL_TODAY_ENERGY,
                new QuantityType<>(inverterData.getYieldToday(), Units.KILOWATT_HOUR));
        updateState(SolaxBindingConstants.CHANNEL_TOTAL_ENERGY,
                new QuantityType<>(inverterData.getYieldTotal(), Units.KILOWATT_HOUR));

        updateState(SolaxBindingConstants.CHANNEL_INVERTER_WORKMODE,
                new StringType(inverterData.getInverterWorkMode()));

        updateState(SolaxBindingConstants.CHANNEL_TIMESTAMP, new DateTimeType(inverterData.getUploadTime(timeZone)));

        updateState(SolaxBindingConstants.CHANNEL_RAW_DATA, new StringType(inverterData.getRawData()));
    }
}
