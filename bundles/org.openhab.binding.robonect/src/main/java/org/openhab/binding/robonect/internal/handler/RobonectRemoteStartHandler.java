/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.robonect.internal.handler;

import static org.openhab.binding.robonect.internal.RobonectBindingConstants.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.robonect.internal.model.Remote;
import org.openhab.binding.robonect.internal.model.RemoteInfo;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles writes to the mower's remote-start API endpoints.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
final class RobonectRemoteStartHandler {

    private static final Pattern REMOTE_CHANNEL_PATTERN = Pattern
            .compile("remote-([1-5])-(name|distance|proportion|visible|path|corridor-cut)");
    private final Logger logger = LoggerFactory.getLogger(RobonectRemoteStartHandler.class);
    private final RobonectHandler handler;

    RobonectRemoteStartHandler(RobonectHandler handler) {
        this.handler = handler;
    }

    void handleCommand(String channelId, Command command) {
        Matcher matcher = REMOTE_CHANNEL_PATTERN.matcher(channelId);
        if (!matcher.matches()) {
            return;
        }
        int remoteStart = Integer.parseInt(matcher.group(1));
        String setting = matcher.group(2);
        if (setting.equals("visible") || setting.equals("path") || setting.equals("corridor-cut")) {
            updateViaWebInterface(remoteStart, setting, command);
            return;
        }
        String name = null;
        Integer distance = null;
        Integer proportion = null;
        if (setting.equals("name") && command instanceof StringType string) {
            name = string.toString();
            if (name.length() > 16) {
                logger.warn("Remote start {} name exceeds 16 characters; truncating it", remoteStart);
                name = name.substring(0, 16);
            }
        } else if ((setting.equals("distance") || setting.equals("proportion")) && command instanceof Number num) {
            try {
                if (setting.equals("distance")) {
                    int value = num instanceof QuantityType<?> quantity ? quantity.toUnit(SIUnits.METRE).intValue()
                            : num.intValue();
                    if (value < 0 || value > 300) {
                        logger.warn("Ignoring invalid remote start distance {}", value);
                        return;
                    }
                    distance = value;
                } else {
                    int value = num instanceof QuantityType<?> quantity ? quantity.toUnit(Units.PERCENT).intValue()
                            : num.intValue();
                    if (value < 0 || value > 100) {
                        logger.warn("Ignoring invalid remote start proportion {}", value);
                        return;
                    }
                    proportion = value;
                }
            } catch (ArithmeticException e) {
                logger.warn("Ignoring non-integer remote start {} value", setting);
                return;
            }
        } else {
            return;
        }
        handler.cancelCommandRefresh("remote");
        RemoteInfo result = handler.getRobonectClient().updateRemoteStart(remoteStart, name, distance, proportion);
        if (result.isSuccessful()) {
            applyRemoteInfo(result);
            handler.scheduleCommandRefresh("remote", this::refreshRemoteInfo);
        }
    }

    private void updateViaWebInterface(int remoteStart, String setting, Command command) {
        if (!handler.isUnofficialApiEnabled()) {
            logger.warn("Cannot update remote start {} {} because the unofficial API is disabled", remoteStart,
                    setting);
            return;
        }
        if ((setting.equals("visible") || setting.equals("corridor-cut")) && !(command instanceof OnOffType)
                || setting.equals("path") && !(command instanceof DecimalType)) {
            return;
        }

        RemoteInfo info = handler.getRobonectClient().getRemoteInfo();
        Remote remote = switch (remoteStart) {
            case 1 -> info.getRemoteStart1();
            case 2 -> info.getRemoteStart2();
            case 3 -> info.getRemoteStart3();
            case 4 -> info.getRemoteStart4();
            case 5 -> info.getRemoteStart5();
            default -> null;
        };
        if (!info.isSuccessful() || remote == null) {
            logger.debug("Cannot update remote start {} because its current settings are incomplete", remoteStart);
            return;
        }

        @Nullable
        String name = remote.getName();
        @Nullable
        Integer proportion = remote.getProportion();
        @Nullable
        Integer distance = remote.getDistance();
        @Nullable
        Integer path = remote.getPath();
        @Nullable
        Boolean visible = remote.getVisible();
        @Nullable
        Boolean corridorCut = remote.getCorridorcut();
        if (name == null || proportion == null || distance == null || path == null || visible == null
                || corridorCut == null) {
            logger.debug("Cannot update remote start {} because its current settings are incomplete", remoteStart);
            return;
        }

        if (setting.equals("visible")) {
            visible = command == OnOffType.ON;
        } else if (setting.equals("corridor-cut")) {
            corridorCut = command == OnOffType.ON;
        } else {
            try {
                path = ((DecimalType) command).toBigDecimal().intValueExact();
            } catch (ArithmeticException e) {
                logger.debug("Ignoring non-integer remote start path value");
                return;
            }
        }
        handler.cancelCommandRefresh("remote");
        handler.getRobonectClient().updateRemoteStartViaWebInterface(remoteStart, name, proportion, distance, path,
                visible, corridorCut);
        handler.scheduleCommandRefresh("remote", this::refreshRemoteInfo);
    }

    void refreshRemoteInfo() {
        applyRemoteInfo(handler.getRobonectClient().getRemoteInfo());
    }

    void applyRemoteInfo(RemoteInfo info) {
        if (!info.isSuccessful()) {
            logger.debug("Could not retrieve remote-start information: {}", info.getErrorMessage());
            return;
        }
        updateRemoteStart(1, info.getRemoteStart1());
        updateRemoteStart(2, info.getRemoteStart2());
        updateRemoteStart(3, info.getRemoteStart3());
        updateRemoteStart(4, info.getRemoteStart4());
        updateRemoteStart(5, info.getRemoteStart5());
    }

    private void updateRemoteStart(int number, @Nullable Remote remote) {
        if (remote == null) {
            return;
        }
        String prefix = "remote-" + number + "-";
        if (remote.getName() != null)
            handler.setState(prefix + "name", new StringType(remote.getName()));
        if (remote.getVisible() != null)
            handler.setState(prefix + "visible", OnOffType.from(remote.getVisible()));
        Integer path = remote.getPath();
        if (path != null)
            handler.setState(prefix + "path", new DecimalType(path));
        Integer proportion = remote.getProportion();
        if (proportion != null)
            handler.setState(prefix + "proportion", new QuantityType<>(proportion, Units.PERCENT));
        Integer distance = remote.getDistance();
        if (distance != null)
            handler.setState(prefix + "distance", new QuantityType<>(distance, SIUnits.METRE));
        if (remote.getCorridorcut() != null)
            handler.setState(prefix + "corridor-cut", OnOffType.from(remote.getCorridorcut()));
    }
}
