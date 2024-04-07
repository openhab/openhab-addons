/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 * <p>
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 * <p>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 * <p>
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.pihole.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.pihole.internal.rest.AdminService;
import org.openhab.binding.pihole.internal.rest.JettyAdminService;
import org.openhab.binding.pihole.internal.rest.model.DnsStatistics;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.openhab.binding.pihole.internal.PiHoleBindingConstants.Channels.ADS_BLOCKED_TODAY_CHANNEL;
import static org.openhab.binding.pihole.internal.PiHoleBindingConstants.Channels.ADS_PERCENTAGE_TODAY_CHANNEL;
import static org.openhab.binding.pihole.internal.PiHoleBindingConstants.Channels.CLIENTS_EVER_SEEN_CHANNEL;
import static org.openhab.binding.pihole.internal.PiHoleBindingConstants.Channels.DNS_QUERIES_ALL_REPLIES_CHANNEL;
import static org.openhab.binding.pihole.internal.PiHoleBindingConstants.Channels.DNS_QUERIES_ALL_TYPES_CHANNEL;
import static org.openhab.binding.pihole.internal.PiHoleBindingConstants.Channels.DNS_QUERIES_TODAY_CHANNEL;
import static org.openhab.binding.pihole.internal.PiHoleBindingConstants.Channels.DOMAINS_BEING_BLOCKED_CHANNEL;
import static org.openhab.binding.pihole.internal.PiHoleBindingConstants.Channels.ENABLED_CHANNEL;
import static org.openhab.binding.pihole.internal.PiHoleBindingConstants.Channels.PRIVACY_LEVEL_CHANNEL;
import static org.openhab.binding.pihole.internal.PiHoleBindingConstants.Channels.QUERIES_CACHED_CHANNEL;
import static org.openhab.binding.pihole.internal.PiHoleBindingConstants.Channels.QUERIES_FORWARDED_CHANNEL;
import static org.openhab.binding.pihole.internal.PiHoleBindingConstants.Channels.REPLY_BLOB_CHANNEL;
import static org.openhab.binding.pihole.internal.PiHoleBindingConstants.Channels.REPLY_CNAME_CHANNEL;
import static org.openhab.binding.pihole.internal.PiHoleBindingConstants.Channels.REPLY_DNSSEC_CHANNEL;
import static org.openhab.binding.pihole.internal.PiHoleBindingConstants.Channels.REPLY_DOMAIN_CHANNEL;
import static org.openhab.binding.pihole.internal.PiHoleBindingConstants.Channels.REPLY_IP_CHANNEL;
import static org.openhab.binding.pihole.internal.PiHoleBindingConstants.Channels.REPLY_NODATA_CHANNEL;
import static org.openhab.binding.pihole.internal.PiHoleBindingConstants.Channels.REPLY_NONE_CHANNEL;
import static org.openhab.binding.pihole.internal.PiHoleBindingConstants.Channels.REPLY_NOTIMP_CHANNEL;
import static org.openhab.binding.pihole.internal.PiHoleBindingConstants.Channels.REPLY_NXDOMAIN_CHANNEL;
import static org.openhab.binding.pihole.internal.PiHoleBindingConstants.Channels.REPLY_OTHER_CHANNEL;
import static org.openhab.binding.pihole.internal.PiHoleBindingConstants.Channels.REPLY_REFUSED_CHANNEL;
import static org.openhab.binding.pihole.internal.PiHoleBindingConstants.Channels.REPLY_RRNAME_CHANNEL;
import static org.openhab.binding.pihole.internal.PiHoleBindingConstants.Channels.REPLY_SERVFAIL_CHANNEL;
import static org.openhab.binding.pihole.internal.PiHoleBindingConstants.Channels.REPLY_UNKNOWN_CHANNEL;
import static org.openhab.binding.pihole.internal.PiHoleBindingConstants.Channels.UNIQUE_CLIENTS_CHANNEL;
import static org.openhab.binding.pihole.internal.PiHoleBindingConstants.Channels.UNIQUE_DOMAINS_CHANNEL;
import static org.openhab.core.thing.ThingStatus.OFFLINE;
import static org.openhab.core.thing.ThingStatus.ONLINE;
import static org.openhab.core.thing.ThingStatus.UNKNOWN;
import static org.openhab.core.thing.ThingStatusDetail.COMMUNICATION_ERROR;
import static org.openhab.core.thing.ThingStatusDetail.CONFIGURATION_ERROR;

/**
 * The {@link PiHoleHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public class PiHoleHandler extends BaseThingHandler implements AdminService {

    private final Logger logger = LoggerFactory.getLogger(PiHoleHandler.class);
    private final HttpClient httpClient;

    private @Nullable JettyAdminService adminService;
    private @Nullable DnsStatistics dnsStatistics;
    private @Nullable ScheduledFuture<?> scheduledFuture;

    public PiHoleHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(UNKNOWN);

        var config = getConfigAs(PiHoleConfiguration.class);
        {
            URI hostname;
            try {
                hostname = new URI(config.hostname);
            } catch (Exception e) {
                logger.error("Invalid hostname: {}", config.hostname);
                // todo i18n
                updateStatus(OFFLINE, CONFIGURATION_ERROR, "Invalid hostname: " + config.hostname);
                return;
            }
            if (config.token.isEmpty()) {
                // todo i18n
                updateStatus(OFFLINE, CONFIGURATION_ERROR, "Please pass token");
                return;
            }
            adminService = new JettyAdminService(config.token, hostname, httpClient);
        }

        if (config.refreshIntervalSeconds <= 0) {
            // todo i18n
            updateStatus(OFFLINE, CONFIGURATION_ERROR, "Refresh interval needs to be greater than 0!");
            return;
        }
        scheduledFuture = scheduler.scheduleWithFixedDelay(
                this::update, 0,
                config.refreshIntervalSeconds,
                SECONDS);

        // do not set status here, the background task will do it.
    }

    private void update() {
        var local = adminService;
        if (local == null) {
            return;
        }

        try {
            logger.debug("Refreshing DnsStatistics from PiHole");
            local.summary().ifPresent(statistics -> dnsStatistics = statistics);
            refresh();
            updateStatus(ONLINE);
        } catch (Exception e) {
            logger.debug("Error occurred when refreshing DnsStatistics from PiHole", e);
            updateStatus(OFFLINE, COMMUNICATION_ERROR, e.getLocalizedMessage());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refresh();
        }
    }

    private void refresh() {
        var localDnsStatistics = dnsStatistics;
        if (localDnsStatistics == null) {
            return;
        }

        updateDecimalState(DOMAINS_BEING_BLOCKED_CHANNEL, localDnsStatistics.getDomainsBeingBlocked());
        updateDecimalState(DNS_QUERIES_TODAY_CHANNEL, localDnsStatistics.getDnsQueriesToday());
        updateDecimalState(ADS_BLOCKED_TODAY_CHANNEL, localDnsStatistics.getAdsBlockedToday());
        updateDecimalState(UNIQUE_DOMAINS_CHANNEL, localDnsStatistics.getUniqueDomains());
        updateDecimalState(QUERIES_FORWARDED_CHANNEL, localDnsStatistics.getQueriesForwarded());
        updateDecimalState(QUERIES_CACHED_CHANNEL, localDnsStatistics.getQueriesCached());
        updateDecimalState(CLIENTS_EVER_SEEN_CHANNEL, localDnsStatistics.getClientsEverSeen());
        updateDecimalState(UNIQUE_CLIENTS_CHANNEL, localDnsStatistics.getUniqueClients());
        updateDecimalState(DNS_QUERIES_ALL_TYPES_CHANNEL, localDnsStatistics.getDnsQueriesAllTypes());
        updateDecimalState(REPLY_UNKNOWN_CHANNEL, localDnsStatistics.getReplyUnknown());
        updateDecimalState(REPLY_NODATA_CHANNEL, localDnsStatistics.getReplyNoData());
        updateDecimalState(REPLY_NXDOMAIN_CHANNEL, localDnsStatistics.getReplyNXDomain());
        updateDecimalState(REPLY_CNAME_CHANNEL, localDnsStatistics.getReplyCName());
        updateDecimalState(REPLY_IP_CHANNEL, localDnsStatistics.getReplyIP());
        updateDecimalState(REPLY_DOMAIN_CHANNEL, localDnsStatistics.getReplyDomain());
        updateDecimalState(REPLY_RRNAME_CHANNEL, localDnsStatistics.getReplyRRName());
        updateDecimalState(REPLY_SERVFAIL_CHANNEL, localDnsStatistics.getReplyServFail());
        updateDecimalState(REPLY_REFUSED_CHANNEL, localDnsStatistics.getReplyRefused());
        updateDecimalState(REPLY_NOTIMP_CHANNEL, localDnsStatistics.getReplyNotImp());
        updateDecimalState(REPLY_OTHER_CHANNEL, localDnsStatistics.getReplyOther());
        updateDecimalState(REPLY_DNSSEC_CHANNEL, localDnsStatistics.getReplyDNSSEC());
        updateDecimalState(REPLY_NONE_CHANNEL, localDnsStatistics.getReplyNone());
        updateDecimalState(REPLY_BLOB_CHANNEL, localDnsStatistics.getReplyBlob());
        updateDecimalState(DNS_QUERIES_ALL_REPLIES_CHANNEL, localDnsStatistics.getDnsQueriesAllTypes());
        updateDecimalState(PRIVACY_LEVEL_CHANNEL, localDnsStatistics.getPrivacyLevel());

        var adsPercentageToday = localDnsStatistics.getAdsPercentageToday();
        if (adsPercentageToday != null) {
            updateState(ADS_PERCENTAGE_TODAY_CHANNEL, new PercentType(new BigDecimal(adsPercentageToday.toString())));
        }
        updateState(ENABLED_CHANNEL, OnOffType.from(localDnsStatistics.getEnabled()));
    }

    private void updateDecimalState(String channelID, @Nullable Integer value) {
        if (value == null) {
            return;
        }
        updateState(channelID, new DecimalType(value));
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(PiHoleActions.class);
    }

    @Override
    public void dispose() {
        adminService = null;
        dnsStatistics = null;
        var localScheduledFuture = scheduledFuture;
        if (localScheduledFuture != null) {
            try {
                localScheduledFuture.cancel(true);
            } catch (Exception e) {
                logger.debug("Failed to cancel scheduled future", e);
            } finally {
                scheduledFuture = null;
            }
        }
        super.dispose();
    }

    @Override
    public Optional<DnsStatistics> summary() throws ExecutionException, InterruptedException, TimeoutException {
        var local = adminService;
        if (local == null) {
            throw new IllegalStateException("AdminService not initialized");
        }
        return local.summary();
    }

    @Override
    public void disableBlocking(long seconds) throws ExecutionException, InterruptedException, TimeoutException {
        var local = adminService;
        if (local == null) {
            throw new IllegalStateException("AdminService not initialized");
        }
        local.disableBlocking(seconds);
    }

    @Override
    public void enableBlocking() throws ExecutionException, InterruptedException, TimeoutException {
        var local = adminService;
        if (local == null) {
            throw new IllegalStateException("AdminService not initialized");
        }
        local.enableBlocking();
    }
}
