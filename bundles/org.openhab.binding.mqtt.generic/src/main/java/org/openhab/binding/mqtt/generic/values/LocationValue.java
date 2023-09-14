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
package org.openhab.binding.mqtt.generic.values;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;

/**
 * Implements a location value.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class LocationValue extends Value {
    public LocationValue() {
        super(CoreItemFactory.LOCATION, List.of(PointType.class, StringType.class));
    }

    @Override
    public String getMQTTpublishValue(Command command, @Nullable String pattern) {
        String formatPattern = pattern;
        PointType point = (PointType) command;

        if (formatPattern == null || "%s".equals(formatPattern)) {
            if (point.getAltitude().toBigDecimal().equals(BigDecimal.ZERO)) {
                formatPattern = "%2$f,%3$f";
            } else {
                formatPattern = "%2$f,%3$f,%1$f";
            }
        }
        return String.format(Locale.ROOT, formatPattern, point.getAltitude().toBigDecimal(),
                point.getLatitude().toBigDecimal(), point.getLongitude().toBigDecimal());
    }

    @Override
    public PointType parseCommand(Command command) throws IllegalArgumentException {
        if (command instanceof PointType point) {
            return point;
        } else {
            return PointType.valueOf(command.toString());
        }
    }
}
