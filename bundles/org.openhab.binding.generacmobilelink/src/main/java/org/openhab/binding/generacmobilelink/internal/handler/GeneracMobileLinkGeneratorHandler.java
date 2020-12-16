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
package org.openhab.binding.generacmobilelink.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.generacmobilelink.internal.api.GeneratorStatus;
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

/**
 * The {@link GeneracMobileLinkGeneratorHandler} is responsible for updating a generator things's channels
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class GeneracMobileLinkGeneratorHandler extends BaseThingHandler {
    private @Nullable GeneratorStatus status;

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
    }

    protected void updateGeneratorStatus(GeneratorStatus status) {
        this.status = status;
        updateStatus(ThingStatus.ONLINE);
        updateState();
    }

    protected void updateState() {
        final GeneratorStatus localStatus = status;
        if (localStatus != null) {
            if (localStatus.connected != null) {
                updateState("connected", OnOffType.from(localStatus.connected));
            }
            if (localStatus.greenLightLit != null) {
                updateState("greenLight", OnOffType.from(localStatus.greenLightLit));
            }
            if (localStatus.yellowLightLit != null) {
                updateState("yellowLight", OnOffType.from(localStatus.yellowLightLit));
            }
            if (localStatus.redLightLit != null) {
                updateState("redLight", OnOffType.from(localStatus.redLightLit));
            }
            if (localStatus.blueLightLit != null) {
                updateState("blueLight", OnOffType.from(localStatus.blueLightLit));
            }
            if (localStatus.generatorStatusDate != null) {
                updateState("statusDate", new StringType(localStatus.generatorStatusDate));
            }
            if (localStatus.generatorStatus != null) {
                updateState("status", new StringType(localStatus.generatorStatus));
            }
            if (localStatus.currentAlarmDescription != null) {
                updateState("currentAlarmDescription", new StringType(localStatus.currentAlarmDescription));
            }
            if (localStatus.runHours != null) {
                updateState("runHours", QuantityType.valueOf(localStatus.runHours, Units.HOUR));
            }
            if (localStatus.exerciseHours != null) {
                updateState("exerciseHours", QuantityType.valueOf(localStatus.exerciseHours, Units.HOUR));
            }
            if (localStatus.fuelType != null) {
                updateState("fuelType", new DecimalType(localStatus.fuelType));
            }
            if (localStatus.fuelLevel != null) {
                updateState("fuelLevel", QuantityType.valueOf(localStatus.fuelLevel, Units.PERCENT));
            }
            if (localStatus.batteryVoltage != null) {
                updateState("batteryVoltage", new StringType(localStatus.batteryVoltage));
            }
            if (localStatus.generatorServiceStatus != null) {
                updateState("serviceStatus", OnOffType.from(localStatus.generatorServiceStatus));
            }
        }
    }
}
