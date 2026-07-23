/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.peblar.internal;

import static org.openhab.binding.peblar.internal.PeblarBindingConstants.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
class PeblarHandler extends BaseThingHandler {

    interface PeblarRunnable {
        void run() throws PeblarApiException;
    }

    private final Logger logger = LoggerFactory.getLogger(PeblarHandler.class);

    private final HttpClient httpClient;

    private @Nullable PeblarApiClient apiClient;
    private @Nullable ScheduledFuture<?> pollingJob;

    // Cached API responses — updated on every poll, read by updateChannel()
    private @Nullable PeblarMeterDTO meterDTO;
    private @Nullable PeblarEvInterfaceDTO evInterfaceDTO;
    private @Nullable PeblarSystemDTO systemDTO;

    public PeblarHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        final PeblarConfiguration config = getConfigAs(PeblarConfiguration.class);

        if (config.hostname.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/addon.peblar.error.configuration.invalid.hostname");
            return;
        }
        if (config.apiToken.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/addon.peblar.error.configuration.invalid.apitoken");
            return;
        }

        apiClient = new PeblarApiClient(httpClient, config.hostname, config.apiToken);
        updateStatus(ThingStatus.UNKNOWN);

        pollingJob = scheduler.scheduleWithFixedDelay(this::pollAll, 0, config.refreshInterval, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        final ScheduledFuture<?> job = pollingJob;

        if (job != null) {
            job.cancel(true);
            pollingJob = null;
        }
        apiClient = null;
        meterDTO = null;
        evInterfaceDTO = null;
        systemDTO = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        final PeblarApiClient client = apiClient;

        if (client == null) {
            return;
        }
        if (command instanceof RefreshType) {
            updateChannel(channelUID.getId());
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_CHARGE_CURRENT_LIMIT -> handleChargeCurrentLimit(client, command);
            case CHANNEL_FORCE_1_PHASE ->
                run(() -> handleForce1Phase(client, command), "Command failed for " + CHANNEL_FORCE_1_PHASE);
            default -> logger.debug("Channel {} is read-only or unknown", channelUID.getId());
        }
    }

    private void handleChargeCurrentLimit(PeblarApiClient client, Command command) {
        final long milliAmps;
        if (command instanceof QuantityType<?> qty) {
            // Convert to mA if needed
            final QuantityType<?> mA = qty.toUnit(MetricPrefix.MILLI(Units.AMPERE));
            if (mA == null) {
                logger.warn("Cannot convert {} to milliamperes", command);
                return;
            }
            milliAmps = mA.longValue();
        } else if (command instanceof DecimalType dt) {
            // Treat unit-less numbers as amperes (openHAB default for ElectricCurrent) and convert to mA for the API
            milliAmps = dt.toBigDecimal().movePointRight(3).longValue();
        } else {
            logger.warn("Unsupported command type {} for chargeCurrentLimit", command.getClass());
            return;
        }
        run(() -> {
            client.setChargeCurrentLimit(milliAmps);
            logger.debug("Set chargeCurrentLimit to {} mA", milliAmps);
            pollAll();
        }, "Command failed for " + CHANNEL_CHARGE_CURRENT_LIMIT);
    }

    private void handleForce1Phase(PeblarApiClient client, Command command) throws PeblarApiException {
        if (command instanceof OnOffType onOff) {
            client.setForce1Phase(onOff == OnOffType.ON);
            logger.debug("Set force1Phase to {}", onOff);
        } else {
            logger.warn("Unsupported command type {} for force1Phase", command.getClass());
        }
    }

    private void pollAll() {
        final PeblarApiClient client = apiClient;

        if (client == null) {
            return;
        }
        run(() -> {
            meterDTO = client.getMeter();
            evInterfaceDTO = client.getEvInterface();
            systemDTO = client.getSystem();
            CHANNELS_ALL.forEach(this::updateChannel);
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
        }, "Polling failed: ");
    }

    private void updateChannel(String channelId) {
        final @Nullable PeblarMeterDTO meterData = this.meterDTO;
        final @Nullable PeblarEvInterfaceDTO evInterfaceData = this.evInterfaceDTO;
        final @Nullable PeblarSystemDTO systemData = this.systemDTO;
        final @Nullable State state = switch (channelId) {
            // ── Meter ────────────────────────────────────────────────────────
            case CHANNEL_CURRENT_PHASE1 -> dataOrUndef(meterData, d -> milliAmp(d.currentPhase1));
            case CHANNEL_CURRENT_PHASE2 -> dataOrUndef(meterData, d -> milliAmp(d.currentPhase2));
            case CHANNEL_CURRENT_PHASE3 -> dataOrUndef(meterData, d -> milliAmp(d.currentPhase3));
            case CHANNEL_VOLTAGE_PHASE1 -> dataOrUndef(meterData, d -> volt(d.voltagePhase1));
            case CHANNEL_VOLTAGE_PHASE2 -> dataOrUndef(meterData, d -> volt(d.voltagePhase2));
            case CHANNEL_VOLTAGE_PHASE3 -> dataOrUndef(meterData, d -> volt(d.voltagePhase3));
            case CHANNEL_POWER_PHASE1 -> dataOrUndef(meterData, d -> watt(d.powerPhase1));
            case CHANNEL_POWER_PHASE2 -> dataOrUndef(meterData, d -> watt(d.powerPhase2));
            case CHANNEL_POWER_PHASE3 -> dataOrUndef(meterData, d -> watt(d.powerPhase3));
            case CHANNEL_POWER_TOTAL -> dataOrUndef(meterData, d -> watt(d.powerTotal));
            case CHANNEL_ENERGY_TOTAL -> dataOrUndef(meterData, d -> wattHour(d.energyTotal));
            case CHANNEL_ENERGY_SESSION -> dataOrUndef(meterData, d -> wattHour(d.energySession));
            // ── EV Interface ─────────────────────────────────────────────────
            case CHANNEL_CP_STATE -> dataOrUndef(evInterfaceData, d -> string(d.cpState));
            case CHANNEL_LOCK_STATE -> dataOrUndef(evInterfaceData, d -> onOff(d.lockState));
            case CHANNEL_CHARGE_CURRENT_LIMIT -> dataOrUndef(evInterfaceData, d -> milliAmp(d.chargeCurrentLimit));
            case CHANNEL_CHARGE_CURRENT_LIMIT_SOURCE ->
                dataOrUndef(evInterfaceData, d -> string(d.chargeCurrentLimitSource));
            case CHANNEL_CHARGE_CURRENT_LIMIT_ACTUAL ->
                dataOrUndef(evInterfaceData, d -> milliAmp(d.chargeCurrentLimitActual));
            case CHANNEL_FORCE_1_PHASE -> dataOrUndef(evInterfaceData, d -> onOff(d.force1Phase));
            // ── System ───────────────────────────────────────────────────────
            case CHANNEL_PRODUCT_PN -> dataOrUndef(systemData, d -> string(d.productPn));
            case CHANNEL_PRODUCT_SN -> dataOrUndef(systemData, d -> string(d.productSn));
            case CHANNEL_FIRMWARE_VERSION -> dataOrUndef(systemData, d -> string(d.firmwareVersion));
            case CHANNEL_WLAN_SIGNAL_STRENGTH ->
                dataOrUndef(systemData, d -> quantity(d.wlanSignalStrength, Units.DECIBEL_MILLIWATTS));
            case CHANNEL_CELLULAR_SIGNAL_STRENGTH ->
                dataOrUndef(systemData, d -> quantity(d.cellularSignalStrength, Units.DECIBEL_MILLIWATTS));
            case CHANNEL_UPTIME -> dataOrUndef(systemData, d -> quantity(d.uptime, Units.SECOND));
            case CHANNEL_PHASE_COUNT -> dataOrUndef(systemData, d -> decimal(d.phaseCount));
            default -> null;
        };

        if (state == null) {
            logger.debug("updateChannel: unhandled channel '{}'", channelId);
        }
        updateState(channelId, state == null ? UnDefType.NULL : state);
    }

    private synchronized void run(PeblarRunnable runner, String debugErrorMessage) {
        try {
            runner.run();
        } catch (PeblarApiAuthenticationException e) {
            logger.debug("{}", debugErrorMessage, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        } catch (PeblarApiException e) {
            logger.debug("{}", debugErrorMessage, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private static State string(@Nullable String value) {
        return dataOrUndef(value, StringType::new);
    }

    private static State onOff(@Nullable Boolean value) {
        return dataOrUndef(value, OnOffType::from);
    }

    private static <Q extends Quantity<Q>> State quantity(@Nullable Number value, Unit<Q> unit) {
        return dataOrUndef(value, v -> new QuantityType<>(v, unit));
    }

    private static State decimal(@Nullable Number value) {
        return dataOrUndef(value, DecimalType::new);
    }

    private static State milliAmp(@Nullable Long value) {
        return quantity(value, MetricPrefix.MILLI(Units.AMPERE));
    }

    private static State volt(@Nullable Integer value) {
        return quantity(value, Units.VOLT);
    }

    private static State watt(@Nullable Long value) {
        return quantity(value, Units.WATT);
    }

    private static State wattHour(@Nullable Long value) {
        return quantity(value, Units.WATT_HOUR);
    }

    private static <T> State dataOrUndef(@Nullable T data, Function<T, State> function) {
        return data == null ? UnDefType.UNDEF : function.apply(data);
    }
}
