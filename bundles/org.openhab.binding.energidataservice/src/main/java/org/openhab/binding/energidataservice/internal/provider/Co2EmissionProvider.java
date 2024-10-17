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
package org.openhab.binding.energidataservice.internal.provider;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.energidataservice.internal.ApiController;
import org.openhab.binding.energidataservice.internal.api.Dataset;
import org.openhab.binding.energidataservice.internal.api.DateQueryParameter;
import org.openhab.binding.energidataservice.internal.api.DateQueryParameterType;
import org.openhab.binding.energidataservice.internal.api.dto.CO2EmissionRecord;
import org.openhab.binding.energidataservice.internal.exception.DataServiceException;
import org.openhab.binding.energidataservice.internal.provider.listener.Co2EmissionListener;
import org.openhab.binding.energidataservice.internal.provider.subscription.Co2EmissionSubscription;
import org.openhab.binding.energidataservice.internal.provider.subscription.Subscription;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.scheduler.PeriodicScheduler;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Co2EmissionProvider} is responsible for fetching CO2 emission
 * data and providing it to subscribed listeners.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
@Component(service = Co2EmissionProvider.class)
public class Co2EmissionProvider extends AbstractProvider<Co2EmissionListener> {

    private static final Duration EMISSION_PROGNOSIS_JOB_INTERVAL = Duration.ofMinutes(15);
    private static final Duration EMISSION_REALTIME_JOB_INTERVAL = Duration.ofMinutes(5);

    private final Logger logger = LoggerFactory.getLogger(Co2EmissionProvider.class);
    private final PeriodicScheduler scheduler;
    private final ApiController apiController;

    private boolean realtimeEmissionsFetchedFirstTime = false;
    private @Nullable ScheduledFuture<?> refreshEmissionPrognosisFuture;
    private @Nullable ScheduledFuture<?> refreshEmissionRealtimeFuture;

    @Activate
    public Co2EmissionProvider(final @Reference PeriodicScheduler scheduler,
            final @Reference HttpClientFactory httpClientFactory, final @Reference TimeZoneProvider timeZoneProvider) {
        this.scheduler = scheduler;
        this.apiController = new ApiController(httpClientFactory.getCommonHttpClient(), timeZoneProvider);
    }

    @Deactivate
    public void deactivate() {
        stopJobs();
    }

    public void subscribe(Co2EmissionListener listener, Subscription subscription) {
        if (!(subscription instanceof Co2EmissionSubscription co2EmissionSubscription)) {
            throw new IllegalArgumentException(subscription.getClass().getName() + " is not supported");
        }
        if (!"DK1".equals(co2EmissionSubscription.getPriceArea())
                && !"DK2".equals(co2EmissionSubscription.getPriceArea())) {
            // Dataset is only for Denmark.
            throw new IllegalArgumentException("Only price areas DK1 and DK2 are supported");
        }
        subscribeInternal(listener, subscription);

        if (Co2EmissionSubscription.Type.Prognosis == co2EmissionSubscription.getType()) {
            rescheduleEmissionPrognosisJob();
        } else if (Co2EmissionSubscription.Type.Realtime == co2EmissionSubscription.getType()) {
            rescheduleEmissionRealtimeJob();
        }
    }

    public void unsubscribe(Co2EmissionListener listener, Subscription subscription) {
        unsubscribeInternal(listener, subscription);

        if (!subscriptionToListeners.keySet().stream().filter(Co2EmissionSubscription.class::isInstance)
                .map(Co2EmissionSubscription.class::cast)
                .anyMatch(s -> s.getType() == Co2EmissionSubscription.Type.Prognosis)) {
            logger.trace("Last prognosis subscriber, stop job");
            stopPrognosisJob();
        }
        if (!subscriptionToListeners.keySet().stream().filter(Co2EmissionSubscription.class::isInstance)
                .map(Co2EmissionSubscription.class::cast)
                .anyMatch(s -> s.getType() == Co2EmissionSubscription.Type.Realtime)) {
            logger.trace("Last realtime subscriber, stop job");
            stopRealtimeJob();
            realtimeEmissionsFetchedFirstTime = false;
        }
    }

    private void stopJobs() {
        stopPrognosisJob();
        stopRealtimeJob();
    }

    private void stopPrognosisJob() {
        ScheduledFuture<?> refreshEmissionPrognosisFuture = this.refreshEmissionPrognosisFuture;
        if (refreshEmissionPrognosisFuture != null) {
            refreshEmissionPrognosisFuture.cancel(true);
            this.refreshEmissionPrognosisFuture = null;
        }
    }

    private void stopRealtimeJob() {
        ScheduledFuture<?> refreshEmissionRealtimeFuture = this.refreshEmissionRealtimeFuture;
        if (refreshEmissionRealtimeFuture != null) {
            refreshEmissionRealtimeFuture.cancel(true);
            this.refreshEmissionRealtimeFuture = null;
        }
    }

    private void rescheduleEmissionPrognosisJob() {
        logger.debug("Scheduling emission prognosis refresh job now and every {}", EMISSION_PROGNOSIS_JOB_INTERVAL);

        stopPrognosisJob();
        refreshEmissionPrognosisFuture = scheduler.schedule(this::refreshCo2EmissionPrognosis, Duration.ZERO,
                EMISSION_PROGNOSIS_JOB_INTERVAL);
    }

    private void rescheduleEmissionRealtimeJob() {
        logger.debug("Scheduling emission realtime refresh job now and every {}", EMISSION_REALTIME_JOB_INTERVAL);

        stopRealtimeJob();
        refreshEmissionRealtimeFuture = scheduler.schedule(this::refreshCo2EmissionRealtime, Duration.ZERO,
                EMISSION_REALTIME_JOB_INTERVAL);
    }

    private void refreshCo2EmissionPrognosis() {
        refreshCo2Emission(Co2EmissionSubscription.Type.Prognosis);
    }

    private void refreshCo2EmissionRealtime() {
        refreshCo2Emission(Co2EmissionSubscription.Type.Realtime);
    }

    private void refreshCo2Emission(Co2EmissionSubscription.Type type) {
        try {
            for (Subscription subscription : subscriptionToListeners.keySet()) {
                if (!(subscription instanceof Co2EmissionSubscription co2EmissionSubscription)
                        || co2EmissionSubscription.getType() != type) {
                    continue;
                }

                updateCo2Emissions(co2EmissionSubscription,
                        DateQueryParameter.of(DateQueryParameterType.UTC_NOW,
                                realtimeEmissionsFetchedFirstTime || type == Co2EmissionSubscription.Type.Prognosis
                                        ? Duration.ofMinutes(-5)
                                        : Duration.ofHours(-24)));

                if (type == Co2EmissionSubscription.Type.Realtime) {
                    realtimeEmissionsFetchedFirstTime = true;
                }
            }
        } catch (DataServiceException e) {
            if (e.getHttpStatus() != 0) {
                listenerToSubscriptions.keySet().forEach(
                        listener -> listener.onCommunicationError(HttpStatus.getCode(e.getHttpStatus()).getMessage()));
            } else {
                listenerToSubscriptions.keySet().forEach(listener -> listener.onCommunicationError(e.getMessage()));
            }
            if (e.getCause() != null) {
                logger.debug("Error retrieving CO2 emissions", e);
            }
        } catch (InterruptedException e) {
            logger.debug("Emission refresh job {} interrupted", type);
            Thread.currentThread().interrupt();
            return;
        }
    }

    private void updateCo2Emissions(Co2EmissionSubscription subscription, DateQueryParameter dateQueryParameter)
            throws InterruptedException, DataServiceException {
        Dataset dataset = subscription.getType().getDataset();
        Map<String, String> properties = new HashMap<>();
        CO2EmissionRecord[] emissionRecords = apiController.getCo2Emissions(dataset, subscription.getPriceArea(),
                dateQueryParameter, properties);
        Set<Co2EmissionListener> listeners = getListeners(subscription);
        listenerToSubscriptions.keySet().forEach(listener -> listener.onPropertiesUpdated(properties));

        Instant now = Instant.now();

        if (dataset == Dataset.CO2Emission && emissionRecords.length > 0) {
            // Records are sorted descending, first record is current.
            listeners.forEach(
                    listener -> listener.onCurrentEmission(subscription.getType(), emissionRecords[0].emission()));
        }

        Map<Instant, BigDecimal> emissions = new HashMap<>();
        for (CO2EmissionRecord emissionRecord : emissionRecords) {
            emissions.put(emissionRecord.start(), emissionRecord.emission());

            if (dataset == Dataset.CO2EmissionPrognosis && now.compareTo(emissionRecord.start()) >= 0
                    && now.compareTo(emissionRecord.end()) < 0) {
                listeners.forEach(
                        listener -> listener.onCurrentEmission(subscription.getType(), emissionRecord.emission()));
            }
        }
        listeners.forEach(listener -> listener.onEmissions(subscription.getType(), emissions));
    }
}
