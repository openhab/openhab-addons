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

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;

/**
 * Implements a datetime value.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class DateTimeValue extends Value {
    public DateTimeValue() {
        super(CoreItemFactory.DATETIME, List.of(DateTimeType.class, StringType.class));
    }

    @Override
    public DateTimeType parseCommand(Command command) throws IllegalArgumentException {
        if (command instanceof DateTimeType dateTimeCommand) {
            return dateTimeCommand;
        } else {
            return DateTimeType.valueOf(command.toString());
        }
    }

    @Override
    public String getMQTTpublishValue(Command command, @Nullable String pattern) {
        String formatPattern = pattern;
        if (formatPattern == null || "%s".contentEquals(formatPattern)) {
            return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(((DateTimeType) command).getZonedDateTime());
        }
        return String.format(formatPattern, ((DateTimeType) command).getZonedDateTime());
    }
}
