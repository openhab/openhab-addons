/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.Arrays;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.ElectricPotential;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.generacmobilelink.internal.dto.Apparatus;
import org.openhab.binding.generacmobilelink.internal.dto.ApparatusDetail;
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

    private @Nullable Apparatus apparatus;
    private @Nullable ApparatusDetail apparatusDetail;

    /**
     * @param thing
     */
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

    protected void updateGeneratorStatus(Apparatus apparatus, ApparatusDetail apparatusDetail) {
        this.apparatus = apparatus;
        this.apparatusDetail = apparatusDetail;
        updateStatus(ThingStatus.ONLINE);
        updateState();
    }

    private void updateState() {
        Apparatus apparatus = this.apparatus;
        ApparatusDetail apparatusDetail = this.apparatusDetail;
        if (apparatus == null || apparatusDetail == null) {
            return;
        }
        updateState("heroImageUrl", new StringType(apparatusDetail.heroImageUrl));
        updateState("statusLabel", new StringType(apparatusDetail.statusLabel));
        updateState("statusText", new StringType(apparatusDetail.statusText));
        updateState("activationDate", new DateTimeType(apparatusDetail.activationDate));
        updateState("deviceSsid", new StringType(apparatusDetail.deviceSsid));
        updateState("status", new DecimalType(apparatusDetail.apparatusStatus));
        updateState("isConnected", OnOffType.from(apparatusDetail.isConnected));
        updateState("isConnecting", OnOffType.from(apparatusDetail.isConnecting));
        updateState("showWarning", OnOffType.from(apparatusDetail.showWarning));
        updateState("hasMaintenanceAlert", OnOffType.from(apparatusDetail.hasMaintenanceAlert));
        updateState("lastSeen", new DateTimeType(apparatusDetail.lastSeen));
        updateState("connectionTime", new DateTimeType(apparatusDetail.connectionTimestamp));
        Arrays.stream(apparatusDetail.properties).filter(p -> p.type == 70).findFirst().ifPresent(p -> {
            try {
                updateState("runHours", new QuantityType<Time>(Integer.parseInt(p.value), Units.HOUR));
            } catch (NumberFormatException e) {
                logger.debug("Could not parse runHours {}", p.value);
            }
        });
        Arrays.stream(apparatusDetail.properties).filter(p -> p.type == 69).findFirst().ifPresent(p -> {
            try {
                updateState("batteryVoltage",
                        new QuantityType<ElectricPotential>(Float.parseFloat(p.value), Units.VOLT));
            } catch (NumberFormatException e) {
                logger.debug("Could not parse runHours {}", p.value);
            }
        });
        Arrays.stream(apparatusDetail.properties).filter(p -> p.type == 31).findFirst().ifPresent(p -> {
            try {
                updateState("hoursOfProtection", new QuantityType<Time>(Float.parseFloat(p.value), Units.HOUR));
            } catch (NumberFormatException e) {
                logger.debug("Could not parse hoursOfProtection {}", p.value);
            }
        });
        apparatus.properties.stream().filter(p -> p.type == 3).findFirst().ifPresent(p -> {
            try {
                if (p.value.signalStrength != null) {
                    updateState("signalStrength", new QuantityType<Dimensionless>(
                            Integer.parseInt(p.value.signalStrength.replaceAll("%", "")), Units.PERCENT));
                }
            } catch (NumberFormatException e) {
                logger.debug("Could not parse signalStrength {}", p.value.signalStrength);
            }
        });
    }
}
