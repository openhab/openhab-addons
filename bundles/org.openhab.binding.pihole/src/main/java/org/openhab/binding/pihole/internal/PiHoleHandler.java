/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.pihole.internal;

import static java.util.concurrent.TimeUnit.*;
import static org.openhab.binding.pihole.internal.PiHoleBindingConstants.Channels.*;
import static org.openhab.binding.pihole.internal.PiHoleBindingConstants.Channels.DisableEnable.ENABLE;
import static org.openhab.core.library.unit.Units.PERCENT;
import static org.openhab.core.thing.ThingStatus.OFFLINE;
import static org.openhab.core.thing.ThingStatus.ONLINE;
import static org.openhab.core.thing.ThingStatus.UNKNOWN;
import static org.openhab.core.thing.ThingStatusDetail.*;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.pihole.internal.rest.AdminService;
import org.openhab.binding.pihole.internal.rest.JettyAdminService;
import org.openhab.binding.pihole.internal.rest.model.DnsStatistics;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PiHoleHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public class PiHoleHandler extends BaseThingHandler implements AdminService {
    private static final int HTTP_DELAY_SECONDS = 1;
    private final Logger logger = LoggerFactory.getLogger(PiHoleHandler.class);
    private final Object lock = new Object();
    private final TimeZoneProvider timeZoneProvider;
    private final HttpClient httpClient;

    private @Nullable AdminService adminService;
    private @Nullable DnsStatistics dnsStatistics;
    private @Nullable ScheduledFuture<?> scheduledFuture;

    public PiHoleHandler(Thing thing, TimeZoneProvider timeZoneProvider, HttpClient httpClient) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(UNKNOWN);

        var config = getConfigAs(PiHoleConfiguration.class);

        if (config.refreshIntervalSeconds <= 0) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR, "@text/handler.init.wrongInterval");
            return;
        }

        URI hostname;
        try {
            hostname = new URI(config.hostname);
        } catch (URISyntaxException e) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR,
                    "@text/handler.init.invalidHostname[\"" + config.hostname + "\"]");
            return;
        }
        if (config.token.isEmpty()) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR, "@text/handler.init.noToken");
            return;
        }
        adminService = new JettyAdminService(config.token, hostname, httpClient);
        scheduledFuture = scheduler.scheduleWithFixedDelay(this::update, 0, config.refreshIntervalSeconds, SECONDS);

        // do not set status here, the background task will do it.
    }

    private void update() {
        var local = adminService;
        if (local == null) {
            return;
        }

        // this block can be called from at least 2 threads
        // check disableBlocking method
        synchronized (lock) {
            try {
                logger.debug("Refreshing DnsStatistics from Pi-hole");
                local.summary().ifPresent(statistics -> dnsStatistics = statistics);
                refresh();
                updateStatus(ONLINE);
            } catch (Exception e) {
                logger.debug("Error occurred when refreshing DnsStatistics from Pi-hole", e);
                updateStatus(OFFLINE, COMMUNICATION_ERROR, e.getLocalizedMessage());
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refresh();
            return;
        }

        if (DISABLE_ENABLE_CHANNEL.equals(channelUID.getId())) {
            if (command instanceof StringType stringType) {
                var value = DisableEnable.valueOf(stringType.toString());
                try {
                    switch (value) {
                        case DISABLE -> disableBlocking(0);
                        case FOR_10_SEC -> disableBlocking(10);
                        case FOR_30_SEC -> disableBlocking(30);
                        case FOR_5_MIN -> disableBlocking(MINUTES.toSeconds(5));
                        case ENABLE -> enableBlocking();
                    }
                } catch (PiHoleException ex) {
                    logger.debug("Cannot invoke {} on channel {}", value, channelUID, ex);
                    updateStatus(OFFLINE, COMMUNICATION_ERROR, ex.getLocalizedMessage());
                }
            }
        }
    }

    private void refresh() {
        var localDnsStatistics = dnsStatistics;
        if (localDnsStatistics == null) {
            return;
        }

        updateDecimalState(DOMAINS_BEING_BLOCKED_CHANNEL, localDnsStatistics.domainsBeingBlocked());
        updateDecimalState(DNS_QUERIES_TODAY_CHANNEL, localDnsStatistics.dnsQueriesToday());
        updateDecimalState(ADS_BLOCKED_TODAY_CHANNEL, localDnsStatistics.adsBlockedToday());
        updateDecimalState(UNIQUE_DOMAINS_CHANNEL, localDnsStatistics.uniqueDomains());
        updateDecimalState(QUERIES_FORWARDED_CHANNEL, localDnsStatistics.queriesForwarded());
        updateDecimalState(QUERIES_CACHED_CHANNEL, localDnsStatistics.queriesCached());
        updateDecimalState(CLIENTS_EVER_SEEN_CHANNEL, localDnsStatistics.clientsEverSeen());
        updateDecimalState(UNIQUE_CLIENTS_CHANNEL, localDnsStatistics.uniqueClients());
        updateDecimalState(DNS_QUERIES_ALL_TYPES_CHANNEL, localDnsStatistics.dnsQueriesAllTypes());
        updateDecimalState(REPLY_UNKNOWN_CHANNEL, localDnsStatistics.replyUnknown());
        updateDecimalState(REPLY_NODATA_CHANNEL, localDnsStatistics.replyNoData());
        updateDecimalState(REPLY_NXDOMAIN_CHANNEL, localDnsStatistics.replyNXDomain());
        updateDecimalState(REPLY_CNAME_CHANNEL, localDnsStatistics.replyCName());
        updateDecimalState(REPLY_IP_CHANNEL, localDnsStatistics.replyIP());
        updateDecimalState(REPLY_DOMAIN_CHANNEL, localDnsStatistics.replyDomain());
        updateDecimalState(REPLY_RRNAME_CHANNEL, localDnsStatistics.replyRRName());
        updateDecimalState(REPLY_SERVFAIL_CHANNEL, localDnsStatistics.replyServFail());
        updateDecimalState(REPLY_REFUSED_CHANNEL, localDnsStatistics.replyRefused());
        updateDecimalState(REPLY_NOTIMP_CHANNEL, localDnsStatistics.replyNotImp());
        updateDecimalState(REPLY_OTHER_CHANNEL, localDnsStatistics.replyOther());
        updateDecimalState(REPLY_DNSSEC_CHANNEL, localDnsStatistics.replyDNSSEC());
        updateDecimalState(REPLY_NONE_CHANNEL, localDnsStatistics.replyNone());
        updateDecimalState(REPLY_BLOB_CHANNEL, localDnsStatistics.replyBlob());
        updateDecimalState(DNS_QUERIES_ALL_REPLIES_CHANNEL, localDnsStatistics.dnsQueriesAllTypes());
        updateDecimalState(PRIVACY_LEVEL_CHANNEL, localDnsStatistics.privacyLevel());

        var adsPercentageToday = localDnsStatistics.adsPercentageToday();
        if (adsPercentageToday != null) {
            var state = new QuantityType<>(new BigDecimal(adsPercentageToday.toString()), PERCENT);
            updateState(ADS_PERCENTAGE_TODAY_CHANNEL, state);
        }
        updateState(ENABLED_CHANNEL, OnOffType.from(localDnsStatistics.enabled()));
        if (localDnsStatistics.enabled()) {
            updateState(DISABLE_ENABLE_CHANNEL, new StringType(ENABLE.toString()));
        }
        var gravityLastUpdated = localDnsStatistics.gravityLastUpdated();
        if (gravityLastUpdated != null) {
            var absolute = gravityLastUpdated.absolute();
            if (absolute != null) {
                var instant = Instant.ofEpochSecond(absolute);
                var zonedDateTime = instant.atZone(timeZoneProvider.getTimeZone());
                updateState(GRAVITY_LAST_UPDATE, new DateTimeType(zonedDateTime));
            }
            var fileExists = gravityLastUpdated.fileExists();
            if (fileExists != null) {
                updateState(GRAVITY_FILE_EXISTS, OnOffType.from(fileExists));
            }
        }
    }

    private void updateDecimalState(String channelID, @Nullable Integer value) {
        if (value == null) {
            return;
        }
        updateState(channelID, new DecimalType(value));
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(PiHoleActions.class);
    }

    @Override
    public void dispose() {
        adminService = null;
        dnsStatistics = null;
        var localScheduledFuture = scheduledFuture;
        if (localScheduledFuture != null) {
            localScheduledFuture.cancel(true);
            scheduledFuture = null;
        }
        super.dispose();
    }

    @Override
    public Optional<DnsStatistics> summary() throws PiHoleException {
        var local = adminService;
        if (local == null) {
            throw new IllegalStateException("AdminService not initialized");
        }
        return local.summary();
    }

    @Override
    public void disableBlocking(long seconds) throws PiHoleException {
        var local = adminService;
        if (local == null) {
            throw new IllegalStateException("AdminService not initialized");
        }
        local.disableBlocking(seconds);
        // update the summary to get the value of DISABLED_CHANNEL channel
        scheduler.schedule(this::update, HTTP_DELAY_SECONDS, SECONDS);
        if (seconds > 0) {
            // update the summary to get the value of ENABLED_CHANNEL channel
            // after the X seconds it probably will be true again
            scheduler.schedule(this::update, seconds + HTTP_DELAY_SECONDS, SECONDS);
        }
    }

    @Override
    public void enableBlocking() throws PiHoleException {
        var local = adminService;
        if (local == null) {
            throw new IllegalStateException("AdminService not initialized");
        }
        local.enableBlocking();
        // update the summary to get the value of DISABLED_CHANNEL channel
        scheduler.schedule(this::update, HTTP_DELAY_SECONDS, SECONDS);
    }
}
