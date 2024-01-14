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
package org.openhab.binding.nikobus.internal.handler;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikobus.internal.utils.Utils;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NikobusRollershutterModuleHandler} is responsible for communication between Nikobus
 * rollershutter-controller and binding.
 *
 * @author Boris Krivonog - Initial contribution
 */
@NonNullByDefault
public class NikobusRollershutterModuleHandler extends NikobusModuleHandler {
    private final Logger logger = LoggerFactory.getLogger(NikobusRollershutterModuleHandler.class);
    private final List<PositionEstimator> positionEstimators = new CopyOnWriteArrayList<>();
    private final Map<String, DirectionConfiguration> directionConfigurations = new ConcurrentHashMap<>();

    public NikobusRollershutterModuleHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();

        if (thing.getStatus() == ThingStatus.OFFLINE) {
            return;
        }

        positionEstimators.clear();
        directionConfigurations.clear();

        for (Channel channel : thing.getChannels()) {
            PositionEstimatorConfig config = channel.getConfiguration().as(PositionEstimatorConfig.class);
            if (config.delay >= 0 && config.duration > 0) {
                positionEstimators.add(new PositionEstimator(channel.getUID(), config));
            }

            DirectionConfiguration configuration = config.reverse ? DirectionConfiguration.REVERSED
                    : DirectionConfiguration.NORMAL;
            directionConfigurations.put(channel.getUID().getId(), configuration);
        }

        logger.debug("Position estimators for {} = {}", thing.getUID(), positionEstimators);
    }

    @Override
    public void dispose() {
        positionEstimators.forEach(PositionEstimator::destroy);
        super.dispose();
    }

    @Override
    protected int valueFromCommand(String channelId, Command command) {
        Optional<PositionEstimator> positionEstimator = getPositionEstimator(channelId);
        if (command instanceof DecimalType decimalCommand) {
            return positionEstimator.map(estimator -> estimator.processSetPosition(decimalCommand.intValue()))
                    .orElseThrow(() -> {
                        throw new IllegalArgumentException(
                                "Received position request but no estimation configured for channel " + channelId);
                    });
        }
        int result = convertCommandToValue(channelId, command);
        positionEstimator.ifPresent(PositionEstimator::cancelStopMovement);
        return result;
    }

    @Override
    protected State stateFromValue(String channelId, int value) {
        if (value == 0x00) {
            return OnOffType.OFF;
        }
        DirectionConfiguration configuration = getDirectionConfiguration(channelId);
        if (value == configuration.up) {
            return UpDownType.UP;
        }
        if (value == configuration.down) {
            return UpDownType.DOWN;
        }
        throw new IllegalArgumentException("Unexpected value " + value + " received");
    }

    @Override
    protected void updateState(ChannelUID channelUID, State state) {
        logger.debug("updateState {} {}", channelUID, state);

        getPositionEstimator(channelUID.getId()).ifPresentOrElse(estimator -> {
            if (state == UpDownType.UP) {
                estimator.start(-1);
            } else if (state == UpDownType.DOWN) {
                estimator.start(1);
            } else if (state == OnOffType.OFF) {
                estimator.stop();
            } else {
                logger.debug("Unexpected state update '{}' for '{}'", state, channelUID);
            }
        }, () -> super.updateState(channelUID, state));
    }

    private void updateState(ChannelUID channelUID, int percent) {
        super.updateState(channelUID, new PercentType(percent));
    }

    protected int convertCommandToValue(String channelId, Command command) {
        if (command == StopMoveType.STOP) {
            return 0x00;
        }
        if (command == UpDownType.DOWN || command == StopMoveType.MOVE) {
            return getDirectionConfiguration(channelId).down;
        }
        if (command == UpDownType.UP) {
            return getDirectionConfiguration(channelId).up;
        }
        throw new IllegalArgumentException("Command '" + command + "' not supported");
    }

    private Optional<PositionEstimator> getPositionEstimator(String channelId) {
        return positionEstimators.stream().filter(estimator -> channelId.equals(estimator.getChannelUID().getId()))
                .findFirst();
    }

    private DirectionConfiguration getDirectionConfiguration(String channelId) {
        DirectionConfiguration configuration = directionConfigurations.get(channelId);
        if (configuration == null) {
            throw new IllegalArgumentException("Direction configuration not found for " + channelId);
        }
        return configuration;
    }

    public static class PositionEstimatorConfig {
        public int duration = -1;
        public int delay = 5;
        public boolean reverse = false;
    }

    private class PositionEstimator {
        private static final int updateIntervalInSec = 1;
        private final ChannelUID channelUID;
        private final int durationInMillis;
        private final int delayInMillis;
        private int position = 0;
        private int turnOffMillis = 0;
        private long startTimeMillis = 0;
        private int direction = 0;
        private @Nullable Future<?> updateEstimateFuture;
        private @Nullable Future<?> stopMovementFuture;

        PositionEstimator(ChannelUID channelUID, PositionEstimatorConfig config) {
            this.channelUID = channelUID;

            // Configuration is in seconds, but we operate with ms.
            durationInMillis = config.duration * 1000;
            delayInMillis = config.delay * 1000;
        }

        public ChannelUID getChannelUID() {
            return channelUID;
        }

        public void destroy() {
            Utils.cancel(updateEstimateFuture);
            updateEstimateFuture = null;
            cancelStopMovement();
        }

        public void start(int direction) {
            stop();
            synchronized (this) {
                this.direction = direction;
                turnOffMillis = delayInMillis + durationInMillis;
                startTimeMillis = System.currentTimeMillis();
            }
            updateEstimateFuture = scheduler.scheduleWithFixedDelay(() -> {
                updateEstimate();
                if (turnOffMillis <= 0) {
                    handleCommand(channelUID, StopMoveType.STOP);
                }
            }, updateIntervalInSec, updateIntervalInSec, TimeUnit.SECONDS);
        }

        public void stop() {
            Utils.cancel(updateEstimateFuture);
            updateEstimate();
            synchronized (this) {
                this.direction = 0;
                startTimeMillis = 0;
            }
        }

        public int processSetPosition(int percent) {
            if (percent < 0 || percent > 100) {
                throw new IllegalArgumentException("Position % out of range - expecting [0, 100] but got " + percent
                        + " for " + channelUID.getId());
            }

            cancelStopMovement();

            int newPosition = (int) ((double) percent * (double) durationInMillis / 100.0 + 0.5);
            int delta = position - newPosition;

            logger.debug("Position set command {} for {}: delta = {}, current pos: {}, new position: {}", percent,
                    channelUID, delta, position, newPosition);

            if (delta == 0) {
                return convertCommandToValue(channelUID.getId(), StopMoveType.STOP);
            }

            int time = Math.abs(delta);
            if (percent == 0 || percent == 100) {
                time += 5000; // Make sure we get to completely open/closed position.
            }

            stopMovementFuture = scheduler.schedule(() -> {
                handleCommand(channelUID, StopMoveType.STOP);
            }, time, TimeUnit.MILLISECONDS);

            return convertCommandToValue(channelUID.getId(), delta > 0 ? UpDownType.UP : UpDownType.DOWN);
        }

        public void cancelStopMovement() {
            Utils.cancel(stopMovementFuture);
            stopMovementFuture = null;
        }

        private void updateEstimate() {
            int direction;
            int ellapsedMillis;

            synchronized (this) {
                direction = this.direction;
                if (startTimeMillis == 0) {
                    ellapsedMillis = 0;
                } else {
                    long currentTimeMillis = System.currentTimeMillis();
                    ellapsedMillis = (int) (currentTimeMillis - startTimeMillis);
                    startTimeMillis = currentTimeMillis;
                }
            }

            turnOffMillis -= ellapsedMillis;
            position = Math.min(durationInMillis, Math.max(0, ellapsedMillis * direction + position));
            int percent = (int) ((double) position / (double) durationInMillis * 100.0 + 0.5);

            logger.debug(
                    "Update estimate for '{}': position = {}, percent = {}, elapsed = {}ms, duration = {}ms, delay = {}ms, turnOff = {}ms",
                    channelUID, position, percent, ellapsedMillis, durationInMillis, delayInMillis, turnOffMillis);

            updateState(channelUID, percent);
        }

        @Override
        public String toString() {
            return "PositionEstimator('" + channelUID + "', duration = " + durationInMillis + "ms, delay = "
                    + delayInMillis + "ms)";
        }
    }

    private static class DirectionConfiguration {
        final int up;
        final int down;

        static final DirectionConfiguration NORMAL = new DirectionConfiguration(1, 2);
        static final DirectionConfiguration REVERSED = new DirectionConfiguration(2, 1);

        private DirectionConfiguration(int up, int down) {
            this.up = up;
            this.down = down;
        }
    }
}
