/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.wemo.internal.handler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.UpnpService;
import org.openhab.binding.wemo.internal.InsightParser;
import org.openhab.binding.wemo.internal.WemoBindingConstants;
import org.openhab.binding.wemo.internal.WemoPowerBank;
import org.openhab.binding.wemo.internal.config.WemoInsightConfiguration;
import org.openhab.binding.wemo.internal.http.WemoHttpCall;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WemoInsightHandler} is responsible for handling commands for
 * a WeMo Insight Switch.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class WemoInsightHandler extends WemoHandler {

    private final Logger logger = LoggerFactory.getLogger(WemoInsightHandler.class);
    private final Map<String, String> stateMap = new ConcurrentHashMap<String, String>();

    private WemoPowerBank wemoPowerBank = new WemoPowerBank();
    private int currentPowerSlidingSeconds;
    private int currentPowerDeltaTrigger;

    public WemoInsightHandler(Thing thing, UpnpIOService upnpIOService, UpnpService upnpService,
            WemoHttpCall wemoHttpCaller) {
        super(thing, upnpIOService, upnpService, wemoHttpCaller);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing WemoInsightHandler for thing '{}'", thing.getUID());

        WemoInsightConfiguration configuration = getConfigAs(WemoInsightConfiguration.class);
        currentPowerSlidingSeconds = configuration.currentPowerSlidingSeconds;
        currentPowerDeltaTrigger = configuration.currentPowerDeltaTrigger;
        wemoPowerBank = new WemoPowerBank(currentPowerSlidingSeconds);

        updateStatus(ThingStatus.UNKNOWN);
        super.initialize();
    }

    @Override
    public void dispose() {
        super.dispose();
        wemoPowerBank.clear();
    }

    @Override
    public void onValueReceived(@Nullable String variable, @Nullable String value, @Nullable String service) {
        logger.debug("Received pair '{}':'{}' (service '{}') for thing '{}'",
                new Object[] { variable, value, service, this.getThing().getUID() });

        updateStatus(ThingStatus.ONLINE);

        if (!"BinaryState".equals(variable) && !"InsightParams".equals(variable)) {
            return;
        }

        if (variable != null && value != null) {
            this.stateMap.put(variable, value);
        }

        if (value != null && value.length() > 1) {
            String insightParams = stateMap.get(variable);

            if (insightParams != null) {
                InsightParser parser = new InsightParser(insightParams);
                Map<String, State> results = parser.parse();
                for (Entry<String, State> entry : results.entrySet()) {
                    String channel = entry.getKey();
                    State state = entry.getValue();

                    logger.trace("New InsightParam {} '{}' for device '{}' received", channel, state,
                            getThing().getUID());
                    updateState(channel, state);
                    if (channel.equals(WemoBindingConstants.CHANNEL_CURRENT_POWER_RAW)
                            && state instanceof QuantityType) {
                        QuantityType<?> power = state.as(QuantityType.class);
                        if (power != null) {
                            updateCurrentPower(power);
                        }
                    }
                }

                // Update helper channel onStandBy by checking if currentPower > standByLimit.
                var standByLimit = (QuantityType<?>) results.get(WemoBindingConstants.CHANNEL_STAND_BY_LIMIT);
                if (standByLimit != null) {
                    QuantityType<?> currentPower = wemoPowerBank.getPreviousCurrentPower();
                    if (currentPower != null) {
                        updateState(WemoBindingConstants.CHANNEL_ON_STAND_BY,
                                OnOffType.from(currentPower.intValue() <= standByLimit.intValue()));
                    }
                }
            }
        }
    }

    private boolean updateCurrentPower(QuantityType<?> power) {
        double value = power.doubleValue();
        var roundedValueState = new QuantityType<>(new BigDecimal(value).setScale(0, RoundingMode.HALF_UP),
                power.getUnit());
        if (currentPowerSlidingSeconds == 0 || currentPowerDeltaTrigger == 0) {
            updateState(WemoBindingConstants.CHANNEL_CURRENT_POWER, roundedValueState);
            return true;
        }

        wemoPowerBank.apply(value);
        double averageValue = wemoPowerBank.getCalculatedAverage(value);

        var roundedAverageValueState = new QuantityType<>(
                new BigDecimal(averageValue).setScale(0, RoundingMode.HALF_UP), power.getUnit());

        if (roundedValueState.equals(wemoPowerBank.getPreviousCurrentPower())) {
            // No change, skip.
            return false;
        }

        double roundedValue = roundedValueState.doubleValue();
        QuantityType<?> previousCurrentPower = wemoPowerBank.getPreviousCurrentPower();

        if (previousCurrentPower == null) {
            // Always update initially.
            return updateCurrentPowerBalanced(roundedValue);
        }
        double previousRoundedValue = previousCurrentPower.doubleValue();
        if (roundedValue < previousRoundedValue - currentPowerDeltaTrigger
                || roundedValue > previousRoundedValue + currentPowerDeltaTrigger) {
            // Update immediately when delta is > 1 W.
            return updateCurrentPowerBalanced(roundedValue);
        }
        if (roundedValueState.equals(roundedAverageValueState)) {
            // Update when rounded value has stabilized.
            return updateCurrentPowerBalanced(roundedValue);
        }
        return false;
    }

    private boolean updateCurrentPowerBalanced(double power) {
        var state = new QuantityType<>(power, Units.WATT);
        updateState(WemoBindingConstants.CHANNEL_CURRENT_POWER, state);
        wemoPowerBank.setPreviousCurrentPower(state);
        return true;
    }
}
