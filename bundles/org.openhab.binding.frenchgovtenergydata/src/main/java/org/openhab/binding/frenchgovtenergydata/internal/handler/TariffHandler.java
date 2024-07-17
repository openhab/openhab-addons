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
package org.openhab.binding.frenchgovtenergydata.internal.handler;

import static org.openhab.binding.frenchgovtenergydata.internal.FrenchGovtEnergyDataBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.HttpMethod;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.frenchgovtenergydata.internal.dto.Tariff;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.CurrencyUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TariffHandler} is the base class for Tariff Things. It takes care of
 * update logic and update scheduling once a day.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public abstract class TariffHandler<T extends Tariff> extends BaseThingHandler {
    private static final String URL = "https://www.data.gouv.fr/fr/datasets/r/%s";
    private static final int REFRESH_FIRST_HOUR_OF_DAY = 0;
    private static final int REFRESH_FIRST_MINUTE_OF_DAY = 1;

    private final Logger logger = LoggerFactory.getLogger(TariffHandler.class);
    private final List<T> tariffs = new ArrayList<>();
    private final String url;

    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();
    private @Nullable String fileCache = null;
    private int puissance = 6;

    public TariffHandler(Thing thing, String dataset) {
        super(thing);
        this.url = URL.formatted(dataset);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        Object confPower = getConfig().get("puissance");
        puissance = confPower != null ? ((BigDecimal) confPower).intValue() : 6;
        refreshJob = Optional.of(scheduler.schedule(this::updateData, 1, TimeUnit.SECONDS));
    }

    @Override
    public void dispose() {
        refreshJob.ifPresent(job -> job.cancel(true));
        refreshJob = Optional.empty();
        super.dispose();
    }

    private @Nullable String readFile() {
        @Nullable
        String result = null;
        try {
            result = HttpUtil.executeUrl(HttpMethod.GET, url, 10000);
            fileCache = result;
        } catch (IOException e) {
            // Use the cache if we had an error accessing the cloud resource
            result = fileCache;
        }
        return result;
    }

    private void updateData() {
        ThingUID thingUID = getThing().getUID();
        logger.debug("Updating {} channels", thingUID);

        @Nullable
        String result = readFile();
        if (result != null) {
            List<String> lines = new ArrayList<>(Arrays.asList(result.split("\r\n")));
            lines.remove(0);

            List<T> newTariffs = interpretLines(lines).collect(Collectors.toList());
            if (!newTariffs.isEmpty()) {
                tariffs.clear();
                tariffs.addAll(newTariffs);
            }

            tariffs.stream().filter(t -> t.puissance == puissance).filter(Tariff::isActive).findFirst().ifPresentOrElse(
                    this::updateChannels,
                    () -> updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "No active tariff"));

            ZonedDateTime now = ZonedDateTime.now();
            ZonedDateTime nextUpdate = now.plusDays(1).withHour(REFRESH_FIRST_HOUR_OF_DAY)
                    .withMinute(REFRESH_FIRST_MINUTE_OF_DAY).truncatedTo(ChronoUnit.MINUTES);
            long delay = ChronoUnit.MINUTES.between(now, nextUpdate);
            logger.debug("Scheduling next {} update in {} minutes", thingUID, delay);
            refreshJob = Optional.of(scheduler.schedule(this::updateData, delay, TimeUnit.MINUTES));
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Unable to access %s".formatted(url));
        }
    }

    protected void updateChannels(T tariff) {
        updateStatus(ThingStatus.ONLINE);
        updateState(CHANNEL_TARIFF_START, new DateTimeType(tariff.dateDebut));
        updateState(CHANNEL_FIXED_HT, new QuantityType<>(tariff.fixeHT, CurrencyUnits.BASE_CURRENCY));
        updateState(CHANNEL_FIXED_TTC, new QuantityType<>(tariff.fixeTTC, CurrencyUnits.BASE_CURRENCY));
    }

    protected abstract Stream<T> interpretLines(List<String> lines);

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH.equals(command)) {
            updateData();
        }
    }
}
