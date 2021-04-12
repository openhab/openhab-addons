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
package org.openhab.binding.homewizard.internal;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link HomeWizardHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Daniël van Os - Initial contribution
 */
@NonNullByDefault
public class HomeWizardHandler extends BaseThingHandler {

    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private HomeWizardConfiguration config = new HomeWizardConfiguration();
    private @Nullable ScheduledFuture<?> pollingJob;

    private String apiURL = "";
    private String meterModel = "";
    private int meterVersion = 0;

    /**
     * Constructor
     *
     * @param thing The thing to handle
     */
    public HomeWizardHandler(Thing thing) {
        super(thing);
    }

    /**
     * Not listening to any commands.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    /**
     * If a host has been specified start polling it
     */
    @Override
    public void initialize() {
        config = getConfigAs(HomeWizardConfiguration.class);
        if (configure()) {
            pollingJob = scheduler.scheduleWithFixedDelay(this::pollingCode, 0, config.refreshDelay, TimeUnit.SECONDS);
        }
    }

    /**
     * Check the current configuration
     *
     * @return true if the configuration is ok to start polling, false otherwise
     */
    private boolean configure() {
        if (config.ipAddress.trim().isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Missing ipAddress/host configuration");
            return false;
        } else {
            updateStatus(ThingStatus.UNKNOWN);
            apiURL = String.format("http://%s/api/v1/data", config.ipAddress.trim());
            return true;
        }
    }

    /**
     * dispose: stop the poller
     */
    @Override
    public void dispose() {
        var job = pollingJob;
        if (job != null) {
            job.cancel(true);
        }
        pollingJob = null;
    }

    /**
     * The actual polling loop
     */
    private void pollingCode() {
        final String result;

        try {
            result = HttpUtil.executeUrl("GET", apiURL, 30000);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    String.format("Unable to query P1 Meter: %s", e.getMessage()));
            return;
        }

        if (result.trim().isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "P1 Meter API returned empty status");
            return;
        }

        P1Payload payload = gson.fromJson(result, P1Payload.class);
        if (payload == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Unable to parse response from P1 meter");
            return;
        }

        if ("".equals(payload.getMeterModel())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Results from API are empty");
            return;
        }

        updateStatus(ThingStatus.ONLINE);

        if (!meterModel.equals(payload.getMeterModel())) {
            meterModel = payload.getMeterModel();
            updateProperty(HomeWizardBindingConstants.PROPERTY_METER_MODEL, meterModel);
        }

        if (meterVersion != payload.getSmrVersion()) {
            meterVersion = payload.getSmrVersion();
            updateProperty(HomeWizardBindingConstants.PROPERTY_METER_VERSION, String.format("%d", meterVersion));
        }

        updateState(HomeWizardBindingConstants.CHANNEL_ENERGY_IMPORT_T1,
                new QuantityType<>(payload.getTotalEnergyImportT1Kwh(), Units.KILOWATT_HOUR));
        updateState(HomeWizardBindingConstants.CHANNEL_ENERGY_IMPORT_T2,
                new QuantityType<>(payload.getTotalEnergyImportT2Kwh(), Units.KILOWATT_HOUR));
        updateState(HomeWizardBindingConstants.CHANNEL_ENERGY_EXPORT_T1,
                new QuantityType<>(payload.getTotalEnergyExportT1Kwh(), Units.KILOWATT_HOUR));
        updateState(HomeWizardBindingConstants.CHANNEL_ENERGY_EXPORT_T2,
                new QuantityType<>(payload.getTotalEnergyExportT2Kwh(), Units.KILOWATT_HOUR));

        updateState(HomeWizardBindingConstants.CHANNEL_ACTIVE_POWER,
                new QuantityType<>(payload.getActivePowerW(), Units.WATT));
        updateState(HomeWizardBindingConstants.CHANNEL_ACTIVE_POWER_L1,
                new QuantityType<>(payload.getActivePowerL1W(), Units.WATT));
        updateState(HomeWizardBindingConstants.CHANNEL_ACTIVE_POWER_L2,
                new QuantityType<>(payload.getActivePowerL2W(), Units.WATT));
        updateState(HomeWizardBindingConstants.CHANNEL_ACTIVE_POWER_L3,
                new QuantityType<>(payload.getActivePowerL3W(), Units.WATT));

        updateState(HomeWizardBindingConstants.CHANNEL_TOTAL_GAS,
                new QuantityType<>(payload.getTotalGasM3(), SIUnits.CUBIC_METRE));

        // 210119164000
        long dtv = payload.getGasTimestamp();
        long seconds = dtv % 100;

        dtv /= 100;
        long minutes = dtv % 100;

        dtv /= 100;
        long hours = dtv % 100;

        dtv /= 100;
        long day = dtv % 100;

        dtv /= 100;
        long month = dtv % 100;

        dtv /= 100;
        long year = dtv + 2000; // Where (When?) have I seen this before?

        DateTimeType dtt = DateTimeType
                .valueOf(String.format("%04d-%02d-%02dT%02d:%02d:%02d", year, month, day, hours, minutes, seconds));
        updateState(HomeWizardBindingConstants.CHANNEL_GAS_TIMESTAMP, dtt);
    }
}
