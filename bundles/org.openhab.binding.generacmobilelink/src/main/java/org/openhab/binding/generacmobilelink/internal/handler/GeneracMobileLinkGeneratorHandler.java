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
package org.openhab.binding.generacmobilelink.internal.handler;

import static org.openhab.binding.generacmobilelink.internal.GeneracMobileLinkBindingConstants.*;

import java.util.Arrays;

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
import org.openhab.core.types.UnDefType;
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
        updateState(CHANNEL_HERO_IMAGE_URL, new StringType(apparatusDetail.heroImageUrl));
        updateState(CHANNEL_STATUS_LABEL, new StringType(apparatusDetail.statusLabel));
        updateState(CHANNEL_STATUS_TEXT, new StringType(apparatusDetail.statusText));
        updateState(CHANNEL_ACTIVATION_DATE, new DateTimeType(apparatusDetail.activationDate));
        updateState(CHANNEL_DEVICE_SSID, new StringType(apparatusDetail.deviceSsid));
        updateState(CHANNEL_STATUS, new DecimalType(apparatusDetail.apparatusStatus));
        updateState(CHANNEL_IS_CONNECTED, OnOffType.from(apparatusDetail.isConnected));
        updateState(CHANNEL_IS_CONNECTING, OnOffType.from(apparatusDetail.isConnecting));
        updateState(CHANNEL_SHOW_WARNING, OnOffType.from(apparatusDetail.showWarning));
        updateState(CHANNEL_HAS_MAINTENANCE_ALERT, OnOffType.from(apparatusDetail.hasMaintenanceAlert));
        updateState(CHANNEL_LAST_SEEN, new DateTimeType(apparatusDetail.lastSeen));
        updateState(CHANNEL_CONNECTION_TIME, new DateTimeType(apparatusDetail.connectionTimestamp));
        Arrays.stream(apparatusDetail.properties).filter(p -> p.type == 70).findFirst().ifPresent(p -> {
            try {
                updateState(CHANNEL_RUN_HOURS, new QuantityType<>(Integer.parseInt(p.value), Units.HOUR));
            } catch (NumberFormatException e) {
                logger.debug("Could not parse runHours {}", p.value);
                updateState(CHANNEL_RUN_HOURS, UnDefType.UNDEF);
            }
        });
        Arrays.stream(apparatusDetail.properties).filter(p -> p.type == 69).findFirst().ifPresent(p -> {
            try {
                updateState(CHANNEL_BATTERY_VOLTAGE, new QuantityType<>(Float.parseFloat(p.value), Units.VOLT));
            } catch (NumberFormatException e) {
                logger.debug("Could not parse batteryVoltage {}", p.value);
                updateState(CHANNEL_BATTERY_VOLTAGE, UnDefType.UNDEF);
            }
        });
        Arrays.stream(apparatusDetail.properties).filter(p -> p.type == 31).findFirst().ifPresent(p -> {
            try {
                updateState(CHANNEL_HOURS_OF_PROTECTION, new QuantityType<>(Float.parseFloat(p.value), Units.HOUR));
            } catch (NumberFormatException e) {
                logger.debug("Could not parse hoursOfProtection {}", p.value);
                updateState(CHANNEL_HOURS_OF_PROTECTION, UnDefType.UNDEF);
            }
        });
        apparatus.properties.stream().filter(p -> p.type == 3).findFirst().ifPresent(p -> {
            try {
                if (p.value.signalStrength != null) {
                    updateState(CHANNEL_SIGNAL_STRENGH, new QuantityType<>(
                            Integer.parseInt(p.value.signalStrength.replace("%", "")), Units.PERCENT));
                }
            } catch (NumberFormatException e) {
                logger.debug("Could not parse signalStrength {}", p.value.signalStrength);
                updateState(CHANNEL_SIGNAL_STRENGH, UnDefType.UNDEF);
            }
        });
    }
}
