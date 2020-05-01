/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.linky.internal.handler;

import static org.openhab.binding.linky.internal.LinkyBindingConstants.*;
import static org.openhab.binding.linky.internal.model.LinkyTimeScale.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.linky.internal.ExpiringDayCache;
import org.openhab.binding.linky.internal.LinkyConfiguration;
import org.openhab.binding.linky.internal.model.LinkyConsumptionData;
import org.openhab.binding.linky.internal.model.LinkyTimeScale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import okhttp3.FormBody;
import okhttp3.FormBody.Builder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * The {@link LinkyHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 */

@NonNullByDefault
public class LinkyHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(LinkyHandler.class);

    private static final String LOGIN_BASE_URI = "https://espace-client-connexion.enedis.fr/auth/UI/Login";
    private static final String API_BASE_URI = "https://espace-client-particuliers.enedis.fr/group/espace-particuliers/suivi-de-consommation";
    private static final DateTimeFormatter API_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int REFRESH_FIRST_HOUR_OF_DAY = 5;
    private static final int REFRESH_INTERVAL_IN_MIN = 360;

    private final OkHttpClient client = new OkHttpClient.Builder().followRedirects(false)
            .cookieJar(new LinkyCookieJar()).build();
    private final Gson gson = new Gson();

    private @NonNullByDefault({}) ScheduledFuture<?> refreshJob;
    private final WeekFields weekFields;

    private final ExpiringDayCache<LinkyConsumptionData> cachedDaylyData;
    private final ExpiringDayCache<LinkyConsumptionData> cachedMonthlyData;
    private final ExpiringDayCache<LinkyConsumptionData> cachedYearlyData;

    public LinkyHandler(Thing thing, LocaleProvider localeProvider) {
        super(thing);
        this.weekFields = WeekFields.of(localeProvider.getLocale());
        this.cachedDaylyData = new ExpiringDayCache<LinkyConsumptionData>("daily cache", REFRESH_FIRST_HOUR_OF_DAY,
                () -> {
                    final LocalDate today = LocalDate.now();
                    return getConsumptionData(DAILY, today.minusDays(13), today, true);
                });
        this.cachedMonthlyData = new ExpiringDayCache<LinkyConsumptionData>("monthly cache", REFRESH_FIRST_HOUR_OF_DAY,
                () -> {
                    final LocalDate today = LocalDate.now();
                    return getConsumptionData(MONTHLY, today.withDayOfMonth(1).minusMonths(1), today, true);
                });
        this.cachedYearlyData = new ExpiringDayCache<LinkyConsumptionData>("yearly cache", REFRESH_FIRST_HOUR_OF_DAY,
                () -> {
                    final LocalDate today = LocalDate.now();
                    return getConsumptionData(YEARLY, LocalDate.of(today.getYear() - 1, 1, 1), today, true);
                });
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Linky handler.");
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.submit(this::login);

        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime nextDayFirstTimeUpdate = now.plusDays(1).withHour(REFRESH_FIRST_HOUR_OF_DAY)
                .truncatedTo(ChronoUnit.HOURS);
        refreshJob = scheduler.scheduleWithFixedDelay(this::updateData,
                ChronoUnit.MINUTES.between(now, nextDayFirstTimeUpdate) % REFRESH_INTERVAL_IN_MIN + 1,
                REFRESH_INTERVAL_IN_MIN, TimeUnit.MINUTES);
    }

    private static Builder getLoginBodyBuilder() {
        return new FormBody.Builder().add("encoded", "true").add("gx_charset", "UTF-8").add("SunQueryParamsString",
                Base64.getEncoder().encodeToString("realm=particuliers".getBytes(StandardCharsets.UTF_8)));
    }

    private synchronized boolean login() {
        logger.debug("login");

        LinkyConfiguration config = getConfigAs(LinkyConfiguration.class);
        Request requestLogin = new Request.Builder().url(LOGIN_BASE_URI)
                .post(getLoginBodyBuilder().add("IDToken1", config.username).add("IDToken2", config.password).build())
                .build();
        try (Response response = client.newCall(requestLogin).execute()) {
            if (response.isRedirect()) {
                logger.debug("Response status {} {} redirects to {}", response.code(), response.message(),
                        response.header("Location"));
            } else {
                logger.debug("Response status {} {}", response.code(), response.message());
            }
            // Do a first call to get data; this first call will fail with code 302
            getConsumptionData(DAILY, LocalDate.now(), LocalDate.now(), false);
            updateStatus(ThingStatus.ONLINE);
            return true;
        } catch (IOException e) {
            logger.debug("Exception while trying to login: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return false;
        }
    }

    /**
     * Request new data and updates channels
     */
    private void updateData() {
        updateDailyData();
        updateMonthlyData();
        updateYearlyData();
    }

    /**
     * Request new dayly/weekly data and updates channels
     */
    private synchronized void updateDailyData() {
        if (!isLinked(YESTERDAY) && !isLinked(LAST_WEEK) && !isLinked(THIS_WEEK)) {
            return;
        }

        double lastWeek = Double.NaN;
        double thisWeek = Double.NaN;
        double yesterday = Double.NaN;
        LinkyConsumptionData result = cachedDaylyData.getValue();
        if (result != null && result.success()) {
            LocalDate rangeStart = LocalDate.now().minusDays(13);
            int jump = result.getDecalage();
            while (rangeStart.getDayOfWeek() != weekFields.getFirstDayOfWeek()) {
                rangeStart = rangeStart.plusDays(1);
                jump++;
            }

            int lastWeekNumber = rangeStart.get(weekFields.weekOfWeekBasedYear());

            lastWeek = 0.0;
            thisWeek = 0.0;
            yesterday = Double.NaN;
            while (jump < result.getData().size()) {
                double consumption = result.getData().get(jump).valeur;
                if (consumption > 0) {
                    if (rangeStart.get(weekFields.weekOfWeekBasedYear()) == lastWeekNumber) {
                        lastWeek += consumption;
                        logger.trace("Consumption at index {} added to last week: {}", jump, consumption);
                    } else {
                        thisWeek += consumption;
                        logger.trace("Consumption at index {} added to current week: {}", jump, consumption);
                    }
                    yesterday = consumption;
                }
                jump++;
                rangeStart = rangeStart.plusDays(1);
            }
        } else {
            cachedDaylyData.invalidateValue();
        }
        updateKwhChannel(YESTERDAY, yesterday);
        updateKwhChannel(THIS_WEEK, thisWeek);
        updateKwhChannel(LAST_WEEK, lastWeek);
    }

    /**
     * Request new monthly data and updates channels
     */
    private synchronized void updateMonthlyData() {
        if (!isLinked(LAST_MONTH) && !isLinked(THIS_MONTH)) {
            return;
        }

        double lastMonth = Double.NaN;
        double thisMonth = Double.NaN;
        LinkyConsumptionData result = cachedMonthlyData.getValue();
        if (result != null && result.success()) {
            int jump = result.getDecalage();
            lastMonth = result.getData().get(jump).valeur;
            thisMonth = result.getData().get(jump + 1).valeur;
            if (thisMonth < 0) {
                thisMonth = 0.0;
            }
        } else {
            cachedMonthlyData.invalidateValue();
        }
        updateKwhChannel(LAST_MONTH, lastMonth);
        updateKwhChannel(THIS_MONTH, thisMonth);
    }

    /**
     * Request new yearly data and updates channels
     */
    private synchronized void updateYearlyData() {
        if (!isLinked(LAST_YEAR) && !isLinked(THIS_YEAR)) {
            return;
        }

        double thisYear = Double.NaN;
        double lastYear = Double.NaN;
        LinkyConsumptionData result = cachedYearlyData.getValue();
        if (result != null && result.success()) {
            int elementQuantity = result.getData().size();
            thisYear = elementQuantity > 0 ? result.getData().get(elementQuantity - 1).valeur : Double.NaN;
            lastYear = elementQuantity > 1 ? result.getData().get(elementQuantity - 2).valeur : Double.NaN;
        } else {
            cachedYearlyData.invalidateValue();
        }
        updateKwhChannel(LAST_YEAR, lastYear);
        updateKwhChannel(THIS_YEAR, thisYear);
    }

    private void updateKwhChannel(String channelId, double consumption) {
        logger.debug("Update channel {} with {}", channelId, consumption);
        updateState(channelId,
                !Double.isNaN(consumption) ? new QuantityType<>(consumption, SmartHomeUnits.KILOWATT_HOUR)
                        : UnDefType.UNDEF);
    }

    /**
     * Produce a report of all daily values between two dates
     *
     * @param startDay the start day of the report
     * @param endDay the end day of the report
     * @param separator the separator to be used betwwen the date and the value
     *
     * @return the report as a string
     */
    public List<String> reportValues(LocalDate startDay, LocalDate endDay, @Nullable String separator) {
        List<String> report = new ArrayList<>();
        if (startDay.getYear() == endDay.getYear() && startDay.getMonthValue() == endDay.getMonthValue()) {
            // All values in the same month
            LinkyConsumptionData result = getConsumptionData(DAILY, startDay, endDay, true);
            if (result != null && result.success()) {
                LocalDate currentDay = startDay;
                int jump = result.getDecalage();
                while (jump < result.getData().size() && !currentDay.isAfter(endDay)) {
                    double consumption = result.getData().get(jump).valeur;
                    String line = currentDay.format(DateTimeFormatter.ISO_LOCAL_DATE) + separator;
                    if (consumption >= 0) {
                        line += String.valueOf(consumption);
                    }
                    report.add(line);
                    jump++;
                    currentDay = currentDay.plusDays(1);
                }
            } else {
                LocalDate currentDay = startDay;
                while (!currentDay.isAfter(endDay)) {
                    report.add(currentDay.format(DateTimeFormatter.ISO_LOCAL_DATE) + separator);
                    currentDay = currentDay.plusDays(1);
                }
            }
        } else {
            // Concatenate the report produced for each month between the two dates
            LocalDate first = startDay;
            do {
                LocalDate last = first.withDayOfMonth(first.lengthOfMonth());
                if (last.isAfter(endDay)) {
                    last = endDay;
                }
                report.addAll(reportValues(first, last, separator));
                first = last.plusDays(1);
            } while (!first.isAfter(endDay));
        }
        return report;
    }

    private @Nullable LinkyConsumptionData getConsumptionData(LinkyTimeScale timeScale, LocalDate from, LocalDate to,
            boolean reLog) {
        logger.debug("getConsumptionData {}", timeScale);

        LinkyConsumptionData result = null;
        boolean tryRelog = false;

        FormBody formBody = new FormBody.Builder().add("p_p_id", "lincspartdisplaycdc_WAR_lincspartcdcportlet")
                .add("p_p_lifecycle", "2").add("p_p_resource_id", timeScale.getId())
                .add("_lincspartdisplaycdc_WAR_lincspartcdcportlet_dateDebut", from.format(API_DATE_FORMAT))
                .add("_lincspartdisplaycdc_WAR_lincspartcdcportlet_dateFin", to.format(API_DATE_FORMAT)).build();

        Request requestData = new Request.Builder().url(API_BASE_URI).post(formBody).build();
        try (Response response = client.newCall(requestData).execute()) {
            if (response.isRedirect()) {
                String location = response.header("Location");
                logger.debug("Response status {} {} redirects to {}", response.code(), response.message(), location);
                if (reLog && location != null && location.startsWith(LOGIN_BASE_URI)) {
                    tryRelog = true;
                }
            } else {
                String body = (response.body() != null) ? response.body().string() : null;
                logger.debug("Response status {} {} : {}", response.code(), response.message(), body);
                if (body != null && !body.isEmpty()) {
                    result = gson.fromJson(body, LinkyConsumptionData.class);
                }
            }
        } catch (IOException e) {
            logger.debug("Exception calling API : {} - {}", e.getClass().getCanonicalName(), e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        } catch (JsonSyntaxException e) {
            logger.debug("Exception while converting JSON response : {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.NONE, e.getMessage());
        }
        if (tryRelog && login()) {
            result = getConsumptionData(timeScale, from, to, false);
        }
        return result;
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the Linky handler.");

        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            logger.debug("Refreshing channel {}", channelUID.getId());
            switch (channelUID.getId()) {
                case YESTERDAY:
                case LAST_WEEK:
                case THIS_WEEK:
                    updateDailyData();
                    break;
                case LAST_MONTH:
                case THIS_MONTH:
                    updateMonthlyData();
                    break;
                case LAST_YEAR:
                case THIS_YEAR:
                    updateYearlyData();
                    break;
                default:
                    break;
            }
        } else {
            logger.debug("The Linky binding is read-only and can not handle command {}", command);
        }
    }
}
