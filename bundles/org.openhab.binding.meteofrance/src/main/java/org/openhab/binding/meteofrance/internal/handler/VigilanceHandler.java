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
package org.openhab.binding.meteofrance.internal.handler;

import static org.openhab.binding.meteofrance.internal.MeteoFranceBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meteofrance.internal.MeteoFranceIconProvider;
import org.openhab.binding.meteofrance.internal.config.DepartmentConfiguration;
import org.openhab.binding.meteofrance.internal.dto.BlocType;
import org.openhab.binding.meteofrance.internal.dto.Domain;
import org.openhab.binding.meteofrance.internal.dto.Hazard;
import org.openhab.binding.meteofrance.internal.dto.MeteoFrance.DomainId;
import org.openhab.binding.meteofrance.internal.dto.MeteoFrance.Period;
import org.openhab.binding.meteofrance.internal.dto.MeteoFrance.TextBlocItem;
import org.openhab.binding.meteofrance.internal.dto.MeteoFrance.TimelapsItem;
import org.openhab.binding.meteofrance.internal.dto.Risk;
import org.openhab.binding.meteofrance.internal.dto.Term;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VigilanceHandler} is responsible for updating channels
 * and querying the API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class VigilanceHandler extends BaseThingHandler implements MeteoFranceChildHandler {
    private final Logger logger = LoggerFactory.getLogger(VigilanceHandler.class);
    private final MeteoFranceIconProvider iconProvider;
    private final ZoneId systemZoneId;

    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();

    private @NonNullByDefault({}) Domain domain;

    public VigilanceHandler(Thing thing, ZoneId zoneId, MeteoFranceIconProvider iconProvider) {
        super(thing);
        this.iconProvider = iconProvider;
        this.systemZoneId = zoneId;
    }

    @Override
    public @Nullable Bridge getBridge() {
        return super.getBridge();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Vigilance Météo handler.");
        disposeJob();

        DepartmentConfiguration config = getConfigAs(DepartmentConfiguration.class);

        domain = Domain.getByApiId(config.department);
        if (Domain.UNKNOWN.equals(domain)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Wrong department: %s".formatted((config.department)));
            return;
        }
        logger.debug("config department= {}", config.department);

        updateStatus(ThingStatus.UNKNOWN);
        refreshJob = Optional
                .of(scheduler.scheduleWithFixedDelay(this::updateAndPublish, 5, config.refresh * 60, TimeUnit.SECONDS));
    }

    private void disposeJob() {
        refreshJob.ifPresent(job -> job.cancel(true));
        refreshJob = Optional.empty();
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the Vigilance Météo handler.");
        disposeJob();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateAndPublish();
        }
    }

    private void updateAndPublish() {
        getBridgeHandler().ifPresentOrElse(handler -> {
            Period period = handler.requestPeriod(Term.TODAY);
            if (period != null) {
                updateDate(OBSERVATION_TIME, period.beginValidityTime());
                updateDate(END_TIME, period.endValidityTime());
            }
            DomainId domainId = handler.requestMapData(domain, Term.TODAY);
            ZonedDateTime now = ZonedDateTime.now().withZoneSameInstant(systemZoneId);
            if (domainId != null) {
                Map<Hazard, Risk> channels = new HashMap<>(Hazard.values().length);
                Hazard.AS_SET.stream().filter(Hazard::isChannel).forEach(hazard -> channels.put(hazard, Risk.VERT));

                domainId.phenomenonItems().forEach(phenomenon -> {
                    Risk risk = phenomenon.phenomenonMaxColorId();
                    for (TimelapsItem item : phenomenon.timelapsItems()) {
                        if (item.contains(now)) {
                            risk = item.colorId();
                        }
                    }
                    channels.put(phenomenon.phenomenonId(), risk);
                });

                channels.forEach((k, v) -> updateAlert(k.channelName, v));

                TextBlocItem bloc = handler.requestTextData(domain);
                if (bloc != null) {
                    String comment = bloc.blocItems().stream().filter(bi -> bi.type().equals(BlocType.SITUATION))
                            .flatMap(bi -> bi.textItems().stream()).flatMap(ti -> ti.termItems().stream())
                            .map(term -> term.getText(now)).collect(Collectors.joining("."));
                    updateState(COMMENT, comment.isEmpty() ? UnDefType.NULL : StringType.valueOf(comment));
                }
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "No data available for the department");
            }
        }, () -> logger.warn("No viable bridge"));
    }

    private void updateAlert(String channelId, Risk value) {
        State state = value != Risk.UNKNOWN ? new DecimalType(value.riskLevel) : UnDefType.NULL;
        if (isLinked(channelId)) {
            updateState(channelId, state);
        }

        String channelIcon = channelId + "-icon";
        if (isLinked(channelIcon)) {
            InputStream icon = iconProvider.getIcon(channelId, state.toString());
            if (icon != null) {
                try {
                    State result = new RawType(icon.readAllBytes(), "image/svg+xml");
                    updateState(channelIcon, result);
                } catch (IOException e) {
                    logger.warn("Error getting icon for channel {} and value {}: {}", channelId, value, e.getMessage());
                }
            } else {
                logger.warn("Null icon returned for channel {} and state {}", channelIcon, state);
            }
        }
    }

    private void updateDate(String channelId, ZonedDateTime zonedDateTime) {
        if (isLinked(channelId)) {
            updateState(channelId, new DateTimeType(zonedDateTime.withZoneSameInstant(systemZoneId)));
        }
    }
}
