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
import static org.openhab.binding.linky.internal.model.EnedisTimeScale.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Base64;
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
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.linky.internal.LinkyConfiguration;
import org.openhab.binding.linky.internal.model.EnedisInfo;
import org.openhab.binding.linky.internal.model.EnedisTimeScale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

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
    private static final String LOGIN_BODY_BUILDER = "encoded=true&gx_charset=UTF-8&SunQueryParamsString=%s&IDToken1=%s&IDToken2=%s";
    private static final String API_BASE_URI = "https://espace-client-particuliers.enedis.fr/group/espace-particuliers/suivi-de-consommation";
    private static final String DATA_BODY_BUILDER = "p_p_id=lincspartdisplaycdc_WAR_lincspartcdcportlet&p_p_lifecycle=2&p_p_resource_id=%s&_lincspartdisplaycdc_WAR_lincspartcdcportlet_dateDebut=%s&_lincspartdisplaycdc_WAR_lincspartcdcportlet_dateFin=%s";
    private static final DateTimeFormatter ENEDIS_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int HTTP_DEFAULT_TIMEOUT_MS = (int) TimeUnit.SECONDS.toMillis(5);
    // private static final Builder LOGIN_BODY_BUILDER = new FormBody.Builder().add("encoded", "true")
    // .add("gx_charset", "UTF-8").add("SunQueryParamsString",
    // Base64.getEncoder().encodeToString("realm=particuliers".getBytes(StandardCharsets.UTF_8)));

    // private final OkHttpClient client = new OkHttpClient.Builder().followRedirects(false)
    // .cookieJar(new LinkyCookieJar()).build();
    private final Gson GSON = new Gson();

    private @NonNullByDefault({}) ScheduledFuture<?> refreshJob;
    private final WeekFields weekFields;

    public LinkyHandler(Thing thing, LocaleProvider localeProvider) {
        super(thing);
        weekFields = WeekFields.of(localeProvider.getLocale());
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Linky handler.");

        LinkyConfiguration config = getConfigAs(LinkyConfiguration.class);
        logger.debug("config username = '{}'", config.username);

        try {
            // Request requestLogin = new Request.Builder().url(LOGIN_BASE_URI)
            // .post(LOGIN_BODY_BUILDER.add("IDToken1", config.username).add("IDToken2", config.password).build())
            // .build();
            // client.newCall(requestLogin).execute().close();
            String requestContent = String.format(LOGIN_BODY_BUILDER,
                    Base64.getEncoder().encodeToString("realm=particuliers".getBytes(StandardCharsets.UTF_8)),
                    URLEncoder.encode(config.username, StandardCharsets.UTF_8.name()),
                    URLEncoder.encode(config.password, StandardCharsets.UTF_8.name()));
            InputStream stream = new ByteArrayInputStream(requestContent.getBytes(StandardCharsets.UTF_8));
            logger.debug("executeUrl POST {} requestContent {}", LOGIN_BASE_URI, requestContent);
            HttpUtil.executeUrl("POST", LOGIN_BASE_URI, stream, "application/x-www-form-urlencoded",
                    HTTP_DEFAULT_TIMEOUT_MS);
            stream.close();

            // first call to test connection and to get needed cookies
            // getEnedisInfo(DAILY, LocalDate.now(), LocalDate.now());

            updateStatus(ThingStatus.ONLINE);
            refreshJob = scheduler.scheduleWithFixedDelay(() -> {
                updateLinkyData();
            }, 0, config.refreshInterval, TimeUnit.MINUTES);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
    }

    /**
     * Request new data and updates channels
     */
    private void updateLinkyData() {
        final LocalDate today = LocalDate.now();

        LocalDate rangeStart = today.minusDays(9);
        EnedisInfo result = getEnedisInfo(DAILY, rangeStart, today);
        if (result != null && result.success()) {
            int jump = result.getDecalage();
            while (rangeStart.getDayOfWeek() != weekFields.getFirstDayOfWeek()) {
                rangeStart = rangeStart.plusDays(1);
                jump++;
            }

            int lastWeekNumber = rangeStart.get(weekFields.weekOfWeekBasedYear());

            double lastWeek = 0;
            double thisWeek = 0;
            double yesterday = -1;
            while (jump < result.getData().size()) {
                double consumption = result.getData().get(jump).valeur;
                if (consumption > 0) {
                    if (rangeStart.get(weekFields.weekOfWeekBasedYear()) == lastWeekNumber) {
                        lastWeek += consumption;
                    } else {
                        thisWeek += consumption;
                    }
                    yesterday = consumption;
                }
                jump++;
                rangeStart = rangeStart.plusDays(1);
            }

            updateKwhChannel(YESTERDAY, yesterday);
            updateKwhChannel(THIS_WEEK, thisWeek);
            updateKwhChannel(LAST_WEEK, lastWeek);
        }

        double lastMonth = -1;
        double thisMonth = -1;
        result = getEnedisInfo(MONTHLY, today.withDayOfMonth(1).minusMonths(1), today);
        if (result != null && result.success()) {
            lastMonth = result.getData().stream().filter(EnedisInfo.Data::isPositive).findFirst().get().valeur;
            thisMonth = result.getData().stream().filter(EnedisInfo.Data::isPositive).reduce((first, second) -> second)
                    .get().valeur;
        }
        updateKwhChannel(LAST_MONTH, lastMonth);
        updateKwhChannel(THIS_MONTH, thisMonth);

        double thisYear = -1;
        double lastYear = -1;
        result = getEnedisInfo(YEARLY, LocalDate.of(today.getYear() - 1, 1, 1), today);
        if (result != null && result.success()) {
            int elementQuantity = result.getData().size();
            thisYear = elementQuantity > 0 ? result.getData().get(elementQuantity - 1).valeur : -1;
            lastYear = elementQuantity > 1 ? result.getData().get(elementQuantity - 2).valeur : -1;
        }
        updateKwhChannel(LAST_YEAR, lastYear);
        updateKwhChannel(THIS_YEAR, thisYear);
    }

    private void updateKwhChannel(String channelId, double consumption) {
        updateState(channelId,
                consumption != -1 ? new QuantityType<>(consumption, SmartHomeUnits.KILOWATT_HOUR) : UnDefType.UNDEF);
    }

    private @Nullable EnedisInfo getEnedisInfo(EnedisTimeScale timeScale, LocalDate from, LocalDate to) {
        EnedisInfo result = null;

        // FormBody formBody = new FormBody.Builder().add("p_p_id", "lincspartdisplaycdc_WAR_lincspartcdcportlet")
        // .add("p_p_lifecycle", "2").add("p_p_resource_id", timeScale.getId())
        // .add("_lincspartdisplaycdc_WAR_lincspartcdcportlet_dateDebut", from.format(ENEDIS_DATE_FORMAT))
        // .add("_lincspartdisplaycdc_WAR_lincspartcdcportlet_dateFin", to.format(ENEDIS_DATE_FORMAT)).build();
        //
        // Request requestData = new Request.Builder().url(API_BASE_URI).post(formBody).build();
        // try (Response response = client.newCall(requestData).execute()) {
        // if (response.body() != null) {
        // String body = response.body().string();
        // result = GSON.fromJson(body, EnedisInfo.class);
        // }
        // response.close();
        // } catch (IOException e) {
        // logger.warn("Exception calling Enedis API : {} - {}", e.getClass().getCanonicalName(), e.getMessage());
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        // }
        String requestContent = String.format(DATA_BODY_BUILDER, timeScale.getId(), from.format(ENEDIS_DATE_FORMAT),
                to.format(ENEDIS_DATE_FORMAT));
        InputStream stream = new ByteArrayInputStream(requestContent.getBytes(StandardCharsets.UTF_8));
        logger.debug("executeUrl POST {} requestContent {}", API_BASE_URI, requestContent);
        try {
            String jsonResponse = HttpUtil.executeUrl("POST", API_BASE_URI, stream, "application/x-www-form-urlencoded",
                    HTTP_DEFAULT_TIMEOUT_MS);
            if (jsonResponse != null) {
                result = GSON.fromJson(jsonResponse, EnedisInfo.class);
            }
        } catch (IOException e) {
            logger.warn("Exception calling Enedis API : {} - {}", e.getClass().getCanonicalName(), e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
        try {
            stream.close();
        } catch (IOException e) {
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
            updateLinkyData();
        } else {
            logger.debug("The Linky binding is read-only and can not handle command {}", command);
        }
    }

}
