/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.ntp.internal.handler;

import static org.openhab.binding.ntp.internal.NtpBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * The NTP Refresh Service polls the configured timeserver with a configurable
 * interval and posts a new event of type ({@link DateTimeType}.
 *
 * The {@link NtpHandler} is responsible for handling commands, which are sent
 * to one of the channels.
 *
 * @author Marcel Verpaalen - Initial contribution OH2 ntp binding
 * @author Thomas.Eichstaedt-Engelen OH1 ntp binding (getTime routine)
 * @author Markus Rathgeb - Add locale provider
 * @author Erdoan Hadzhiyusein - Adapted the class to work with the new DateTimeType
 */

public class NtpHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(NtpHandler.class);

    /** timeout for requests to the NTP server */
    private static final int NTP_TIMEOUT = 30000;

    public static final String DATE_PATTERN_WITH_TZ = "yyyy-MM-dd HH:mm:ss z";

    /** for logging purposes */
    private final DateFormat SDF = new SimpleDateFormat(DATE_PATTERN_WITH_TZ);

    /** for publish purposes */
    private DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern(DATE_PATTERN_WITH_TZ);

    private final LocaleProvider localeProvider;

    ScheduledFuture<?> refreshJob;

    /** NTP host */
    private String hostname;
    /** NTP server port */
    private BigDecimal port;
    /** refresh interval */
    private BigDecimal refreshInterval;
    /** NTP refresh frequency */
    private BigDecimal refreshNtp = new BigDecimal(0);
    /** Timezone */
    private TimeZone timeZone;
    /** Locale */
    private Locale locale;

    /** NTP refresh counter */
    private int refreshNtpCount = 0;
    /** NTP system time delta */
    private long timeOffset;

    private ChannelUID dateTimeChannelUID;
    private ChannelUID stringChannelUID;

    public NtpHandler(final Thing thing, final LocaleProvider localeProvider) {
        super(thing);
        this.localeProvider = localeProvider;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No specific commands tied to this, but we will trigger an update
        this.refreshNtpCount = 0;
        refreshTimeDate();
    }

    @Override
    public void initialize() {
        try {
            logger.debug("Initializing NTP handler for '{}'.", getThing().getUID());

            Configuration config = getThing().getConfiguration();
            hostname = config.get(PROPERTY_NTP_SERVER_HOST).toString();
            port = (BigDecimal) config.get(PROPERTY_NTP_SERVER_PORT);
            refreshInterval = (BigDecimal) config.get(PROPERTY_REFRESH_INTERVAL);
            refreshNtp = (BigDecimal) config.get(PROPERTY_REFRESH_NTP);
            refreshNtpCount = 0;

            try {
                Object timeZoneConfigValue = config.get(PROPERTY_TIMEZONE);
                if (timeZoneConfigValue != null) {
                    timeZone = TimeZone.getTimeZone(timeZoneConfigValue.toString());
                } else {
                    timeZone = TimeZone.getDefault();
                    logger.debug("{} using default TZ '{}', because configuration property '{}' is null.",
                            getThing().getUID(), timeZone, PROPERTY_TIMEZONE);
                }
            } catch (Exception e) {
                timeZone = TimeZone.getDefault();
                logger.debug("{} using default TZ '{}' due to an occurred exception: ", getThing().getUID(), timeZone,
                        e);
            }

            try {
                Object localeStringConfigValue = config.get(PROPERTY_LOCALE);
                if (localeStringConfigValue != null) {
                    locale = new Locale(localeStringConfigValue.toString());
                } else {
                    locale = localeProvider.getLocale();
                    logger.debug("{} using default locale '{}', because configuration property '{}' is null.",
                            getThing().getUID(), locale, PROPERTY_LOCALE);
                }
            } catch (Exception e) {
                locale = localeProvider.getLocale();
                logger.debug("{} using default locale '{}' due to an occurred exception: ", getThing().getUID(), locale,
                        e);
            }
            dateTimeChannelUID = new ChannelUID(getThing().getUID(), CHANNEL_DATE_TIME);
            stringChannelUID = new ChannelUID(getThing().getUID(), CHANNEL_STRING);
            try {
                Channel stringChannel = getThing().getChannel(stringChannelUID.getId());
                if (stringChannel != null) {
                    Configuration cfg = stringChannel.getConfiguration();
                    String dateTimeFormatString = cfg.get(PROPERTY_DATE_TIME_FORMAT).toString();
                    if (!(dateTimeFormatString == null || dateTimeFormatString.isEmpty())) {
                        dateTimeFormat = DateTimeFormatter.ofPattern(dateTimeFormatString);
                    } else {
                        logger.debug("No format set in channel config for {}. Using default format.", stringChannelUID);
                        dateTimeFormat = DateTimeFormatter.ofPattern(DATE_PATTERN_WITH_TZ);
                    }
                } else {
                    logger.debug("Missing channel: '{}'", stringChannelUID.getId());
                }
            } catch (RuntimeException ex) {
                logger.debug("No channel config or invalid format for {}. Using default format. ({})", stringChannelUID,
                        ex.getMessage());
                dateTimeFormat = DateTimeFormatter.ofPattern(DATE_PATTERN_WITH_TZ);
            }
            SDF.setTimeZone(timeZone);
            dateTimeFormat.withZone(timeZone.toZoneId());

            logger.debug(
                    "Initialized NTP handler '{}' with configuration: host '{}', refresh interval {}, timezone {}, locale {}.",
                    getThing().getUID(), hostname, refreshInterval, timeZone, locale);
            startAutomaticRefresh();
        } catch (Exception ex) {
            logger.error("Error occurred while initializing NTP handler: {}", ex.getMessage(), ex);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-init-handler");
        }
    }

    @Override
    public void dispose() {
        refreshJob.cancel(true);
        super.dispose();
    }

    private void startAutomaticRefresh() {
        refreshJob = scheduler.scheduleWithFixedDelay(() -> {
            try {
                refreshTimeDate();
            } catch (Exception e) {
                logger.debug("Exception occurred during refresh: {}", e.getMessage(), e);
            }
        }, 0, refreshInterval.intValue(), TimeUnit.SECONDS);
    }

    private synchronized void refreshTimeDate() {
        if (timeZone != null && locale != null) {
            long networkTimeInMillis;
            if (refreshNtpCount <= 0) {
                networkTimeInMillis = getTime(hostname);
                timeOffset = networkTimeInMillis - System.currentTimeMillis();
                logger.debug("{} delta system time: {}", getThing().getUID(), timeOffset);
                refreshNtpCount = refreshNtp.intValue();
            } else {
                networkTimeInMillis = System.currentTimeMillis() + timeOffset;
                refreshNtpCount--;
            }

            ZonedDateTime zoned = ZonedDateTime.ofInstant(Instant.ofEpochMilli(networkTimeInMillis),
                    timeZone.toZoneId());
            updateState(dateTimeChannelUID, new DateTimeType(zoned));
            updateState(stringChannelUID, new StringType(dateTimeFormat.format(zoned)));
        } else {
            logger.debug("Not refreshing, since we do not seem to be initialized yet");
        }
    }

    /**
     * Queries the given timeserver <code>hostname</code> and returns the time
     * in milliseconds.
     *
     * @param hostname the timeserver to query
     * @return the time in milliseconds or the current time of the system if an
     *         error occurs.
     */
    public long getTime(String hostname) {
        try {
            NTPUDPClient timeClient = new NTPUDPClient();
            timeClient.setDefaultTimeout(NTP_TIMEOUT);
            InetAddress inetAddress = InetAddress.getByName(hostname);
            TimeInfo timeInfo = timeClient.getTime(inetAddress, port.intValue());
            timeInfo.computeDetails();

            long serverMillis = timeInfo.getReturnTime() + timeInfo.getOffset();
            logger.debug("{} Got time update from host '{}': {}.", getThing().getUID(), hostname,
                    SDF.format(new Date(serverMillis)));
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
            return serverMillis;
        } catch (UnknownHostException uhe) {
            logger.debug(
                    "{} The given hostname '{}' of the timeserver is unknown -> returning current sytem time instead. ({})",
                    getThing().getUID(), hostname, uhe.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error-unknown-host [\"" + (hostname == null ? "null" : hostname) + "\"]");
        } catch (IOException ioe) {
            logger.debug(
                    "{} Couldn't establish network connection to host '{}' -> returning current sytem time instead. ({})",
                    getThing().getUID(), hostname, ioe.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error-connection [\"" + (hostname == null ? "null" : hostname) + "\"]");
        }

        return System.currentTimeMillis();
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        refreshTimeDate();
    }

}
