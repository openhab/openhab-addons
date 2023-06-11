/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.generacmobilelink.internal.handler;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.generacmobilelink.internal.dto.GeneratorStatusDTO;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GeneracMobileLinkGeneratorHandler} is responsible for updating a generator things's channels
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class GeneracMobileLinkGeneratorHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(GeneracMobileLinkGeneratorHandler.class);
    private @Nullable GeneratorStatusDTO status;

    public GeneracMobileLinkGeneratorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateState();
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
    }

    protected void updateGeneratorStatus(GeneratorStatusDTO status) {
        this.status = status;
        updateStatus(ThingStatus.ONLINE);
        updateState();
    }

    protected void updateState() {
        final GeneratorStatusDTO localStatus = status;
        if (localStatus != null) {
            updateState("connected", OnOffType.from(localStatus.connected));
            updateState("greenLight", OnOffType.from(localStatus.greenLightLit));
            updateState("yellowLight", OnOffType.from(localStatus.yellowLightLit));
            updateState("redLight", OnOffType.from(localStatus.redLightLit));
            updateState("blueLight", OnOffType.from(localStatus.blueLightLit));
            try {
                // API returns a format like 12/20/2020
                updateState("statusDate",
                        new DateTimeType(LocalDate
                                .parse(localStatus.generatorStatusDate, DateTimeFormatter.ofPattern("MM/dd/yyyy"))
                                .atStartOfDay(ZoneId.systemDefault())));
            } catch (IllegalArgumentException | DateTimeParseException e) {
                logger.debug("Could not parse statusDate", e);
            }
            updateState("status", new StringType(localStatus.generatorStatus));
            updateState("currentAlarmDescription", new StringType(localStatus.currentAlarmDescription));
            updateState("runHours", new QuantityType<Time>(localStatus.runHours, Units.HOUR));
            updateState("exerciseHours", new QuantityType<Time>(localStatus.exerciseHours, Units.HOUR));
            updateState("fuelType", new DecimalType(localStatus.fuelType));
            updateState("fuelLevel", QuantityType.valueOf(localStatus.fuelLevel, Units.PERCENT));
            updateState("batteryVoltage", new StringType(localStatus.batteryVoltage));
            updateState("serviceStatus", OnOffType.from(localStatus.generatorServiceStatus));
        }
    }
}
