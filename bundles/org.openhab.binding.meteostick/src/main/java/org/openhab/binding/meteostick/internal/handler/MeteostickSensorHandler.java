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
package org.openhab.binding.meteostick.internal.handler;

import static org.openhab.binding.meteostick.internal.MeteostickBindingConstants.*;
import static org.openhab.core.library.unit.MetricPrefix.MILLI;
import static org.openhab.core.library.unit.SIUnits.*;
import static org.openhab.core.library.unit.Units.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.scheduler.CronScheduler;
import org.openhab.core.scheduler.ScheduledCompletableFuture;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MeteostickSensorHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Chris Jackson - Initial contribution
 * @author John Cocula - Added variable spoon size, UoM, wind stats, bug fixes
 * @author Cor Hoogendoorn - Added option for wind vanes not facing North and cumulative rainfall for today
 * @author Jens Erat - Harden packet parsing and validate received values
 */
public class MeteostickSensorHandler extends BaseThingHandler implements MeteostickEventListener {
    private static final String DAILY_MIDNIGHT = "1 0 0 * * ? *";

    // Plausibility bounds, chosen to reject physically impossible values while accepting any real weather reading.
    private static final BigDecimal MIN_TEMPERATURE = new BigDecimal("-100");
    private static final BigDecimal MAX_TEMPERATURE = new BigDecimal("100");
    private static final BigDecimal MIN_HUMIDITY = new BigDecimal("0");
    private static final BigDecimal MAX_HUMIDITY = new BigDecimal("100");
    private static final BigDecimal MIN_WIND_SPEED = new BigDecimal("0");
    private static final BigDecimal MAX_WIND_SPEED = new BigDecimal("200");
    private static final int MIN_WIND_DIRECTION = 0;
    private static final int MAX_WIND_DIRECTION = 360;
    private static final int MIN_RAIN_COUNTER = 0;
    private static final int MAX_RAIN_COUNTER = 255;
    private static final BigDecimal MIN_SOLAR_POWER = new BigDecimal("0");
    private static final BigDecimal MAX_SOLAR_POWER = new BigDecimal("100");
    private static final double MIN_SIGNAL = -200;
    private static final double MAX_SIGNAL = 0;
    private final CronScheduler cronScheduler;
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_DAVIS);

    private final Logger logger = LoggerFactory.getLogger(MeteostickSensorHandler.class);

    private int channel = 0;
    private int deltawinddir = 0;
    private int rainspoonold = -1;
    private BigDecimal rainfallToday = BigDecimal.ZERO;
    private BigDecimal spoon = new BigDecimal(PARAMETER_SPOON_DEFAULT);
    private MeteostickBridgeHandler bridgeHandler;
    private RainHistory rainHistory = new RainHistory(HOUR_IN_MSEC);
    private WindHistory windHistory = new WindHistory(2 * 60 * 1000); // 2 minutes
    private ScheduledFuture<?> rainHourlyJob;
    private ScheduledCompletableFuture<?> rainMidnightJob;
    private ScheduledFuture<?> wind2MinJob;
    private ScheduledFuture<?> offlineTimerJob;

    private Date lastData;

    public MeteostickSensorHandler(Thing thing, final CronScheduler scheduler) {
        super(thing);
        this.cronScheduler = scheduler;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing MeteoStick handler.");

        channel = ((BigDecimal) getConfig().get(PARAMETER_CHANNEL)).intValue();

        spoon = (BigDecimal) getConfig().get(PARAMETER_SPOON);
        if (spoon == null) {
            spoon = new BigDecimal(PARAMETER_SPOON_DEFAULT);
        }

        deltawinddir = ((BigDecimal) getConfig().get(PARAMETER_WINDVANE)).intValue();

        logger.debug("Initializing MeteoStick handler - Channel {}, Spoon size {} mm, Wind vane offset {} °", channel,
                spoon, deltawinddir);

        Runnable rainRunnable = () -> {
            BigDecimal rainfall = rainHistory.getTotal();
            rainfall.setScale(1, RoundingMode.DOWN);
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_RAIN_LASTHOUR),
                    new QuantityType<>(rainfall, MILLI(METRE)));
        };

        // Scheduling a job on each hour to update the last hour rainfall
        long start = HOUR_IN_SEC - ((System.currentTimeMillis() % HOUR_IN_MSEC) / 1000);
        rainHourlyJob = scheduler.scheduleWithFixedDelay(rainRunnable, start, HOUR_IN_SEC, TimeUnit.SECONDS);

        // Scheduling a job at midnight to reset today's rainfall to 0
        rainMidnightJob = cronScheduler.schedule(this::dailyJob, DAILY_MIDNIGHT);

        Runnable windRunnable = () -> {
            WindStats stats = windHistory.getStats();
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_WIND_SPEED_LAST2MIN_AVERAGE),
                    new QuantityType<>(stats.averageSpeed, METRE_PER_SECOND));
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_WIND_SPEED_LAST2MIN_MAXIMUM),
                    new QuantityType<>(stats.maxSpeed, METRE_PER_SECOND));
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_WIND_DIRECTION_LAST2MIN_AVERAGE),
                    new QuantityType<>(stats.averageDirection, DEGREE_ANGLE));
        };

        // Scheduling a job to run every two minutes to update wind statistics
        wind2MinJob = scheduler.scheduleWithFixedDelay(windRunnable, 2, 2, TimeUnit.MINUTES);

        updateStatus(ThingStatus.UNKNOWN);
    }

    @Override
    public void dispose() {
        if (rainHourlyJob != null) {
            rainHourlyJob.cancel(true);
        }

        if (rainMidnightJob != null) {
            rainMidnightJob.cancel(true);
        }

        if (wind2MinJob != null) {
            wind2MinJob.cancel(true);
        }

        if (offlineTimerJob != null) {
            offlineTimerJob.cancel(true);
        }

        if (bridgeHandler != null) {
            bridgeHandler.unsubscribeEvents(channel, this);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("MeteoStick handler {}: bridgeStatusChanged to {}", channel, bridgeStatusInfo);
        if (bridgeStatusInfo.getStatus() != ThingStatus.ONLINE) {
            logger.debug("MeteoStick handler {}: bridgeStatusChanged but bridge offline", channel);
            updateStatus(ThingStatus.OFFLINE);
            return;
        }

        bridgeHandler = (MeteostickBridgeHandler) getBridge().getHandler();

        if (channel != 0) {
            if (bridgeHandler != null) {
                bridgeHandler.subscribeEvents(channel, this);
            }
        }

        // Put the thing online and start our "no data" timer
        updateStatus(ThingStatus.ONLINE);
        startTimeoutCheck();
    }

    private void processSignalStrength(double dbm) {
        int strength;

        if (dbm > -60) {
            strength = 4;
        } else if (dbm > -70) {
            strength = 3;
        } else if (dbm > -80) {
            strength = 2;
        } else if (dbm > -90) {
            strength = 1;
        } else {
            strength = 0;
        }

        updateState(new ChannelUID(getThing().getUID(), CHANNEL_SIGNAL_STRENGTH), new DecimalType(strength));
    }

    private void processBattery(boolean batteryLow) {
        OnOffType state = OnOffType.from(batteryLow);

        updateState(new ChannelUID(getThing().getUID(), CHANNEL_LOW_BATTERY), state);
    }

    @Override
    public void onDataReceived(String[] data) {
        logger.debug("MeteoStick received channel {}: {}", channel, String.join(" ", data));
        updateStatus(ThingStatus.ONLINE);
        lastData = new Date();

        startTimeoutCheck();

        if (data.length == 0) {
            return;
        }

        switch (data[0]) {
            case "R": // Rain
                processRain(data);
                break;
            case "W": // Wind
                processWind(data);
                break;
            case "T": // Temperature
                processTemperature(data);
                break;
            case "P": // Solar panel power
                processPower(data);
                break;
        }
    }

    private void processRain(String[] data) {
        // Computed rain packet: R <channel> <counter> <signal> [<low battery>]
        if (data.length < 4) {
            logger.debug("MeteoStick received short rain packet on channel {}: {}", channel, String.join(" ", data));
            return;
        }

        int rain;
        double signal;
        try {
            rain = Integer.parseInt(data[2]);
            signal = Double.parseDouble(data[3]);
        } catch (NumberFormatException e) {
            logger.debug("MeteoStick received rain packet with non-numeric values on channel {}: {}", channel,
                    String.join(" ", data));
            return;
        }

        if (rain < MIN_RAIN_COUNTER || rain > MAX_RAIN_COUNTER || signal < MIN_SIGNAL || signal > MAX_SIGNAL) {
            logger.debug("MeteoStick received rain packet with implausible values on channel {}: {}", channel,
                    String.join(" ", data));
            return;
        }

        updateState(new ChannelUID(getThing().getUID(), CHANNEL_RAIN_RAW), new DecimalType(rain));
        processSignalStrength(signal);
        processBattery(data.length == 5);

        rain &= 0x7F;
        int totalspoon = 0;
        if (rainspoonold < 0) {
            rainspoonold = rain;
        }
        if (rain < rainspoonold) {
            totalspoon = 128 - rainspoonold + rain;
        } else {
            totalspoon = rain - rainspoonold;
        }

        BigDecimal rainincrease = BigDecimal.valueOf(totalspoon).multiply(spoon);
        rainHistory.put(rainincrease);

        BigDecimal rainfall = rainHistory.getTotal();
        rainfall.setScale(1, RoundingMode.DOWN);
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_RAIN_CURRENTHOUR),
                new QuantityType<>(rainfall, MILLI(METRE)));

        rainfallToday = rainfallToday.add(rainincrease);
        rainfallToday.setScale(1, RoundingMode.DOWN);
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_RAIN_TODAY),
                new QuantityType<>(rainfallToday, MILLI(METRE)));
        rainspoonold = rain;
    }

    private void processWind(String[] data) {
        // Computed wind packet: W <channel> <speed> <direction> <signal> [<low battery>]
        if (data.length < 5) {
            logger.debug("MeteoStick received short wind packet on channel {}: {}", channel, String.join(" ", data));
            return;
        }

        BigDecimal windSpeed;
        int windDirection;
        double signal;
        try {
            windSpeed = new BigDecimal(data[2]);
            windDirection = Integer.parseInt(data[3]);
            signal = Double.parseDouble(data[4]);
        } catch (NumberFormatException e) {
            logger.debug("MeteoStick received wind packet with non-numeric values on channel {}: {}", channel,
                    String.join(" ", data));
            return;
        }

        if (!isWithin(windSpeed, MIN_WIND_SPEED, MAX_WIND_SPEED) || windDirection < MIN_WIND_DIRECTION
                || windDirection > MAX_WIND_DIRECTION || signal < MIN_SIGNAL || signal > MAX_SIGNAL) {
            logger.debug("MeteoStick received wind packet with implausible values on channel {}: {}", channel,
                    String.join(" ", data));
            return;
        }

        updateState(new ChannelUID(getThing().getUID(), CHANNEL_WIND_SPEED),
                new QuantityType<>(windSpeed, METRE_PER_SECOND));
        if (deltawinddir != 0) {
            if (windDirection < (360 - deltawinddir)) {
                windDirection += deltawinddir;
            } else {
                windDirection -= (360 - deltawinddir);
            }
        }
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_WIND_DIRECTION),
                new QuantityType<>(windDirection, DEGREE_ANGLE));

        windHistory.put(windSpeed, windDirection);

        processSignalStrength(signal);
        processBattery(data.length == 6);
    }

    private void processTemperature(String[] data) {
        // Computed temperature packet: T <channel> <temperature> <humidity> <signal> [<low battery>]
        if (data.length < 5) {
            logger.debug("MeteoStick received short temperature packet on channel {}: {}", channel,
                    String.join(" ", data));
            return;
        }

        BigDecimal temperature;
        BigDecimal humidity;
        double signal;
        try {
            temperature = new BigDecimal(data[2]);
            humidity = new BigDecimal(data[3]);
            signal = Double.parseDouble(data[4]);
        } catch (NumberFormatException e) {
            logger.debug("MeteoStick received temperature packet with non-numeric values on channel {}: {}", channel,
                    String.join(" ", data));
            return;
        }

        if (!isWithin(temperature, MIN_TEMPERATURE, MAX_TEMPERATURE) || !isWithin(humidity, MIN_HUMIDITY, MAX_HUMIDITY)
                || signal < MIN_SIGNAL || signal > MAX_SIGNAL) {
            logger.debug("MeteoStick received temperature packet with implausible values on channel {}: {}", channel,
                    String.join(" ", data));
            return;
        }

        updateState(new ChannelUID(getThing().getUID(), CHANNEL_OUTDOOR_TEMPERATURE),
                new QuantityType<>(temperature.setScale(1), CELSIUS));

        updateState(new ChannelUID(getThing().getUID(), CHANNEL_HUMIDITY), new DecimalType(humidity.setScale(1)));

        processSignalStrength(signal);
        processBattery(data.length == 6);
    }

    private void processPower(String[] data) {
        // Computed solar panel packet: P <channel> <power> <signal> [<low battery>]
        if (data.length < 4) {
            logger.debug("MeteoStick received short power packet on channel {}: {}", channel, String.join(" ", data));
            return;
        }

        BigDecimal power;
        double signal;
        try {
            power = new BigDecimal(data[2]);
            signal = Double.parseDouble(data[3]);
        } catch (NumberFormatException e) {
            logger.debug("MeteoStick received power packet with non-numeric values on channel {}: {}", channel,
                    String.join(" ", data));
            return;
        }

        if (!isWithin(power, MIN_SOLAR_POWER, MAX_SOLAR_POWER) || signal < MIN_SIGNAL || signal > MAX_SIGNAL) {
            logger.debug("MeteoStick received power packet with implausible values on channel {}: {}", channel,
                    String.join(" ", data));
            return;
        }

        updateState(new ChannelUID(getThing().getUID(), CHANNEL_SOLAR_POWER), new DecimalType(power.setScale(1)));

        processSignalStrength(signal);
        processBattery(data.length == 5);
    }

    private static boolean isWithin(BigDecimal value, BigDecimal min, BigDecimal max) {
        return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
    }

    class SlidingTimeWindow<T> {
        private long period = 0;
        protected final SortedMap<Long, T> storage = Collections.synchronizedSortedMap(new TreeMap<>());

        /**
         *
         * @param period window period in milliseconds
         */
        public SlidingTimeWindow(long period) {
            this.period = period;
        }

        public void put(T value) {
            storage.put(System.currentTimeMillis(), value);
        }

        public void removeOldEntries() {
            long old = System.currentTimeMillis() - period;
            synchronized (storage) {
                for (Iterator<Long> iterator = storage.keySet().iterator(); iterator.hasNext();) {
                    long time = iterator.next();
                    if (time < old) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    class RainHistory extends SlidingTimeWindow<BigDecimal> {

        public RainHistory(long period) {
            super(period);
        }

        public BigDecimal getTotal() {
            removeOldEntries();
            BigDecimal raintotalmap = BigDecimal.ZERO;
            synchronized (storage) {
                for (BigDecimal value : storage.values()) {

                    raintotalmap = raintotalmap.add(value);

                }
            }

            return raintotalmap;
        }
    }

    /**
     * Store the wind direction as an east-west vector and a north-south vector
     * so that an average direction can be calculated based on the wind speed
     * at the time of the direction sample.
     */
    class WindSample {
        double speed;
        double ewVector;
        double nsVector;

        public WindSample(BigDecimal speed, int directionDegrees) {
            this.speed = speed.doubleValue();
            double direction = Math.toRadians(directionDegrees);
            this.ewVector = this.speed * Math.sin(direction);
            this.nsVector = this.speed * Math.cos(direction);
        }
    }

    class WindStats {
        BigDecimal averageSpeed;
        int averageDirection;
        BigDecimal maxSpeed;
    }

    class WindHistory extends SlidingTimeWindow<WindSample> {

        public WindHistory(long period) {
            super(period);
        }

        public void put(BigDecimal speed, int directionDegrees) {
            put(new WindSample(speed, directionDegrees));
        }

        public WindStats getStats() {
            removeOldEntries();

            double ewSum = 0;
            double nsSum = 0;
            double totalSpeed = 0;
            double maxSpeed = 0;
            int size = 0;
            synchronized (storage) {
                size = storage.size();
                for (WindSample sample : storage.values()) {
                    ewSum += sample.ewVector;
                    nsSum += sample.nsVector;
                    totalSpeed += sample.speed;
                    if (sample.speed > maxSpeed) {
                        maxSpeed = sample.speed;
                    }
                }
            }

            WindStats stats = new WindStats();

            stats.averageDirection = (int) Math.toDegrees(Math.atan2(ewSum, nsSum));
            if (stats.averageDirection < 0) {
                stats.averageDirection += 360;
            }

            stats.averageSpeed = new BigDecimal(size > 0 ? totalSpeed / size : 0).setScale(3, RoundingMode.HALF_DOWN);

            stats.maxSpeed = new BigDecimal(maxSpeed).setScale(3, RoundingMode.HALF_DOWN);

            return stats;
        }
    }

    private synchronized void startTimeoutCheck() {
        Runnable pollingRunnable = () -> {
            String detail;
            if (lastData == null) {
                detail = "No data received";
            } else {
                detail = "No data received since " + lastData.toString();
            }
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, detail);
        };

        if (offlineTimerJob != null) {
            offlineTimerJob.cancel(true);
        }

        // Scheduling a job on each hour to update the last hour rainfall
        offlineTimerJob = scheduler.schedule(pollingRunnable, 90, TimeUnit.SECONDS);
    }

    private void dailyJob() {
        // Daily job to reset the daily rain accumulation
        rainfallToday = BigDecimal.ZERO;
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_RAIN_TODAY),
                new QuantityType<>(rainfallToday, MILLI(METRE)));
    }
}
