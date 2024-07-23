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
package org.openhab.binding.ephemeris.internal.discovery;

import static org.openhab.binding.ephemeris.internal.EphemerisBindingConstants.*;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EphemerisDiscoveryService} creates default available Ephemeris Things.
 *
 * @author Gaël L'hopital - Initial Contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.ephemeris")
public class EphemerisDiscoveryService extends AbstractDiscoveryService {
    private static final int DISCOVER_TIMEOUT_SECONDS = 2;
    private static final String LOCAL = "local";
    private static final ThingUID HOLIDAY_THING = new ThingUID(THING_TYPE_HOLIDAY, LOCAL);
    private static final ThingUID WEEKEND_THING = new ThingUID(THING_TYPE_WEEKEND, LOCAL);

    private final Logger logger = LoggerFactory.getLogger(EphemerisDiscoveryService.class);

    private Optional<ScheduledFuture<?>> discoveryJob = Optional.empty();

    @Activate
    public EphemerisDiscoveryService(final @Reference LocaleProvider localeProvider,
            final @Reference TranslationProvider i18nProvider, @Nullable Map<String, Object> configProperties) {
        super(Set.of(THING_TYPE_HOLIDAY, THING_TYPE_WEEKEND), DISCOVER_TIMEOUT_SECONDS, true);
        this.localeProvider = localeProvider;
        this.i18nProvider = i18nProvider;
        activate(configProperties);
    }

    @Override
    protected void startScan() {
        logger.debug("Starting Ephemeris discovery scan");
        createResults();
    }

    @Override
    protected void startBackgroundDiscovery() {
        discoveryJob = Optional.of(scheduler.schedule(this::createResults, 2, TimeUnit.SECONDS));
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stopping Ephemeris device background discovery");
        discoveryJob.ifPresent(job -> job.cancel(true));
        discoveryJob = Optional.empty();
    }

    public void createResults() {
        thingDiscovered(DiscoveryResultBuilder.create(HOLIDAY_THING).build());
        thingDiscovered(DiscoveryResultBuilder.create(WEEKEND_THING).build());
    }
}
