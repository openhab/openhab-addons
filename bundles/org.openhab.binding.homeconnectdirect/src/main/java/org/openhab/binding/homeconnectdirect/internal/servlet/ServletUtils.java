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
package org.openhab.binding.homeconnectdirect.internal.servlet;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homeconnectdirect.internal.common.utils.StringUtils;
import org.openhab.binding.homeconnectdirect.internal.handler.model.ApplianceMessage;
import org.openhab.binding.homeconnectdirect.internal.handler.model.Value;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.change.DeviceDescriptionChange;
import org.openhab.binding.homeconnectdirect.internal.servlet.model.MessageFilter;

/**
 * Servlet utility methods.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class ServletUtils {
    private ServletUtils() {
        // utils class
    }

    public static boolean filterOutMessage(ApplianceMessage message, @Nullable MessageFilter filter) {
        if (filter == null) {
            return false;
        }
        var start = filter.start();
        var end = filter.end();
        var type = filter.type();
        var resources = filter.resources();
        var actions = filter.actions();
        var valueKeys = filter.valueKeys();
        var descriptionChangeKeys = filter.descriptionChangeKeys();

        // filter by date/time
        if (start != null && message.dateTime().isBefore(roundDown(start))) {
            return true;
        }
        if (end != null && message.dateTime().isAfter(roundUp(end))) {
            return true;
        }

        // filter by type
        if (type != null && message.type() != type) {
            return true;
        }

        // filter by resources
        if (resources != null && !resources.isEmpty() && !resources.contains(message.resource())) {
            return true;
        }

        // filter by actions
        if (actions != null && !actions.isEmpty() && !actions.contains(message.action())) {
            return true;
        }

        // filter by value keys
        var inValues = false;
        if (valueKeys != null && !valueKeys.isEmpty()) {
            var values = message.values();
            if (values != null) {
                inValues = values.stream().map(Value::key).anyMatch(
                        name -> valueKeys.stream().anyMatch(search -> StringUtils.containsIgnoreCase(name, search)));
            }
        }

        // filter by description change keys
        var inDescriptions = false;
        var descriptionChanges = message.descriptionChanges();
        if (descriptionChangeKeys != null && !descriptionChangeKeys.isEmpty() && descriptionChanges != null) {
            inDescriptions = descriptionChanges.stream().map(DeviceDescriptionChange::key)
                    .anyMatch(key -> descriptionChangeKeys.stream()
                            .anyMatch(search -> StringUtils.containsIgnoreCase(key, search)));
        }

        // OR logic between value and description change keys
        if ((descriptionChangeKeys != null && !descriptionChangeKeys.isEmpty())
                || (valueKeys != null) && !valueKeys.isEmpty()) {
            return !inValues && !inDescriptions;
        }

        return false;
    }

    public static String replacePlaceholders(String template, Function<String, @Nullable String> valueProvider) {
        StringBuilder sb = new StringBuilder(template.length());
        int cursor = 0;
        while (cursor < template.length()) {
            int start = template.indexOf("${", cursor);
            if (start == -1) {
                sb.append(template, cursor, template.length());
                break;
            }
            int end = template.indexOf("}", start);
            if (end == -1) {
                sb.append(template, cursor, template.length());
                break;
            }

            sb.append(template, cursor, start);
            String key = template.substring(start + 2, end);
            String value = valueProvider.apply(key);
            sb.append(value != null ? value : "${" + key + "}");
            cursor = end + 1;
        }
        return sb.toString();
    }

    public static @Nullable Integer mapInteger(@Nullable String value) {
        if (value == null) {
            return null;
        }

        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static OffsetDateTime roundDown(OffsetDateTime dateTime) {
        return dateTime.truncatedTo(ChronoUnit.MINUTES);
    }

    private static OffsetDateTime roundUp(OffsetDateTime dateTime) {
        OffsetDateTime truncated = dateTime.truncatedTo(ChronoUnit.MINUTES);
        return truncated.plusMinutes(1).minus(1, ChronoUnit.MILLIS);
    }
}
