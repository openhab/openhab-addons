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
package org.openhab.binding.meteoalerte.internal.handler;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.meteoalerte.internal.MeteoAlertIconProvider;
import org.openhab.binding.meteoalerte.internal.config.MeteoAlerteConfiguration;
import org.openhab.binding.meteoalerte.internal.dto.BlocItem;
import org.openhab.binding.meteoalerte.internal.dto.BlocType;
import org.openhab.binding.meteoalerte.internal.dto.Hazard;
import org.openhab.binding.meteoalerte.internal.dto.Risk;
import org.openhab.binding.meteoalerte.internal.dto.TermItem;
import org.openhab.binding.meteoalerte.internal.dto.TextBlocItem;
import org.openhab.binding.meteoalerte.internal.dto.TextItem;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MeteoAlerteHandler} is responsible for updating channels
 * and querying the API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class MeteoAlerteHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(MeteoAlerteHandler.class);
    private final MeteoAlertIconProvider iconProvider;

    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();

    public MeteoAlerteHandler(Thing thing, MeteoAlertIconProvider iconProvider) {
        super(thing);
        this.iconProvider = iconProvider;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Météo Alerte handler.");

        MeteoAlerteConfiguration config = getConfigAs(MeteoAlerteConfiguration.class);
        logger.debug("config department= {}", config.department);

        updateStatus(ThingStatus.UNKNOWN);
        refreshJob = Optional.of(
                scheduler.scheduleWithFixedDelay(() -> updateAndPublish(config.department), 0, 10, TimeUnit.MINUTES));
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the Météo Alerte handler.");

        refreshJob.ifPresent(job -> job.cancel(true));
        refreshJob = Optional.empty();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            // updateAndPublish();
        }
    }

    private void updateAndPublish(String department) {
        MeteoAlerteBridgeHandler bridgeHandler = (MeteoAlerteBridgeHandler) getBridge().getHandler();
        TextBlocItem bloc = bridgeHandler.requestData(department);
        Map<Hazard, Risk> channels = new HashMap<>();

        for (Hazard hazard : Hazard.values()) {
            if (hazard != Hazard.UNKNOWN && hazard != Hazard.ALL) {
                channels.put(hazard, Risk.VERT);
            }
        }

        updateStatus(ThingStatus.ONLINE);

        if (bloc.domain.name().equals(department)) {
            List<BlocItem> blocItems = bloc.blocItems;
            if (!blocItems.isEmpty()) {
                for (BlocItem blocItem : blocItems) {
                    if (blocItem.type == BlocType.QUALIFICATION) {
                        for (TextItem textItem : blocItem.textItems) {
                            Hazard hazard = textItem.getHazard();
                            if (hazard != Hazard.ALL && hazard != Hazard.UNKNOWN) {
                                for (TermItem termItem : textItem.termItems) {
                                    channels.put(hazard, termItem.risk);
                                }
                            }
                        }

                    }
                }
            }
        }

        channels.forEach((k, v) -> {
            updateAlert(k.channelName, v);
        });
        // updateState(COMMENT, StringType.valueOf(fields.getVigilanceComment()));
        // fields.getDateInsert().ifPresent(date -> updateDate(OBSERVATION_TIME, date));
        // fields.getDatePrevue().ifPresent(date -> updateDate(END_TIME, date));

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
            updateState(channelId, new DateTimeType(zonedDateTime));
        }
    }
}
