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
package org.openhab.binding.hasslink.internal.entity.util;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hasslink.internal.api.dto.ServiceCall;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.types.Command;
import org.openhab.core.util.ColorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * Utility class to map openHAB {@link Command} instances into Home Assistant {@link ServiceCall} objects.
 * <p>
 * Provides concise static transformation methods to streamline entity command routing inside
 * {@code switch} expressions.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public final class CommandMapper {

    private static final Gson GSON = new Gson();
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandMapper.class);
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_ONLY_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    /**
     * Maps an entity action and its parameters to a Home Assistant service name.
     */
    @FunctionalInterface
    public interface MappedServiceFunction {
        /**
         * Maps an action and optional argument string to a target Home Assistant service name.
         *
         * @param action the leading action string before the colon (e.g., "clean_segments")
         * @param arg the argument string after the colon (e.g., "1,2,3"), or {@code null} if absent
         * @param params container for building the service call payload map
         * @return the target service name, or {@code null} if unmapped
         */
        @Nullable
        String map(String action, @Nullable String arg, ServiceCallParams params);
    }

    /**
     * Collects parameters used to build a Home Assistant service call payload.
     */
    public static final class ServiceCallParams {
        private final Map<String, Object> payload = new HashMap<>();

        /**
         * Adds a parameter key-value pair to the Home Assistant service payload.
         */
        public ServiceCallParams put(String key, Object value) {
            this.payload.put(key, value);
            return this;
        }

        public Map<String, Object> getPayload() {
            return payload;
        }
    }

    private CommandMapper() {
        // Utility class
    }

    /**
     * Maps {@link OnOffType} commands to domain {@code turn_on} or {@code turn_off} service calls.
     *
     * @param command the incoming openHAB command
     * @param domain the target Home Assistant domain
     * @param entityId the target entity ID
     * @return an {@link Optional} containing the service call if matched, otherwise empty
     */
    public static Optional<ServiceCall> onOff(Command command, String domain, String entityId) {
        return onOffCustom(command, domain, "turn_on", "turn_off", entityId);
    }

    /**
     * Maps {@link OnOffType} commands to specific custom service names for ON and OFF actions.
     *
     * If the service name for ON or OFF is null or blank, no service call will be generated for that action.
     *
     * @param command the incoming openHAB command
     * @param domain the target Home Assistant domain
     * @param onService the service name to invoke for {@link OnOffType#ON}
     * @param offService the service name to invoke for {@link OnOffType#OFF}
     * @param entityId the target entity ID
     * @return an {@link Optional} containing the service call if matched, otherwise empty
     */
    public static Optional<ServiceCall> onOffCustom(Command command, String domain, @Nullable String onService,
            @Nullable String offService, String entityId) {
        if (command instanceof OnOffType onOff) {
            String service = onOff == OnOffType.ON ? onService : offService;
            if (service != null && !service.isBlank()) {
                return Optional.of(new ServiceCall(domain, service, entityId));
            }
        }
        return Optional.empty();
    }

    /**
     * Maps an {@link OnOffType} command to a service call taking a boolean parameter payload.
     *
     * @param command the incoming openHAB command
     * @param domain the target Home Assistant domain (e.g., "fan")
     * @param service the Home Assistant service name (e.g., "oscillate")
     * @param paramKey the parameter key expected in the service call (e.g., "oscillating")
     * @param entityId the target entity ID
     * @return an {@link Optional} containing the service call if matched, otherwise empty
     */
    public static Optional<ServiceCall> onOffBoolean(Command command, String domain, String service, String paramKey,
            String entityId) {
        if (command instanceof OnOffType onOff) {
            boolean value = onOff == OnOffType.ON;
            return Optional.of(new ServiceCall(domain, service, entityId, Map.of(paramKey, value)));
        }
        return Optional.empty();
    }

    /**
     * Maps {@link OnOffType} commands to a service call containing a single boolean parameter payload.
     *
     * @param command the incoming openHAB command
     * @param domain the target Home Assistant domain
     * @param service the target service name
     * @param paramKey the parameter key for the boolean payload
     * @param entityId the target entity ID
     * @return an {@link Optional} containing the service call if matched, otherwise empty
     */
    public static Optional<ServiceCall> onOffParam(Command command, String domain, String service, String paramKey,
            String entityId) {
        if (command instanceof OnOffType onOff) {
            boolean value = onOff == OnOffType.ON;
            return Optional.of(new ServiceCall(domain, service, entityId, Map.of(paramKey, value)));
        }
        return Optional.empty();
    }

    /**
     * Maps {@link StringType} commands to a service call with a single parameter payload.
     *
     * @param command the incoming openHAB command
     * @param domain the target Home Assistant domain
     * @param service the service name
     * @param paramKey the parameter key for the string value
     * @param entityId the target entity ID
     * @return an {@link Optional} containing the service call if matched, otherwise empty
     */
    public static Optional<ServiceCall> onString(Command command, String domain, String service, String paramKey,
            String entityId) {
        if (command instanceof StringType cmd) {
            return Optional.of(new ServiceCall(domain, service, entityId, Map.of(paramKey, cmd.toString())));
        }
        return Optional.empty();
    }

    /**
     * Maps a {@link StringType} command directly as the service name to execute.
     *
     * @param command the incoming openHAB command
     * @param domain the target Home Assistant domain
     * @param entityId the target entity ID
     * @return an {@link Optional} containing the service call if matched, otherwise empty
     */
    public static Optional<ServiceCall> onStringService(Command command, String domain, String entityId) {
        if (command instanceof StringType stringType) {
            String service = stringType.toString().trim();
            if (!service.isBlank()) {
                return Optional.of(new ServiceCall(domain, service, entityId));
            }
        }
        return Optional.empty();
    }

    /**
     * Maps a {@link StringType} command to a target service call using a mapping function.
     *
     * @param command the incoming openHAB command
     * @param domain the target Home Assistant domain
     * @param entityId the target entity ID
     * @param mapper function that maps the input string command to a valid service name (returns {@code null} if
     *            unmapped)
     * @return an {@link Optional} containing the service call if matched and mapped, otherwise empty
     */
    public static Optional<ServiceCall> onStringMapped(Command command, String domain, String entityId,
            Function<String, @Nullable String> mapper) {
        if (command instanceof StringType stringType) {
            String service = mapper.apply(stringType.toString());
            if (service != null && !service.isBlank()) {
                return Optional.of(new ServiceCall(domain, service, entityId));
            }
        }
        return Optional.empty();
    }

    /**
     * Maps a {@link StringType} command formatted as {@code "action:arg"} or {@code "action"} to a target service call.
     *
     * @param command the incoming openHAB command
     * @param domain the target Home Assistant domain
     * @param entityId the target entity ID
     * @param mapper function mapping action and argument to a target service name while populating payload parameters
     * @return an {@link Optional} containing the service call if matched and mapped, otherwise empty
     */
    public static Optional<ServiceCall> onParameterizedStringMapped(Command command, String domain, String entityId,
            MappedServiceFunction mapper) {
        if (command instanceof StringType stringType) {
            String raw = stringType.toString();
            String[] parts = raw.split(":", 2);
            String action = parts[0].trim().toLowerCase();
            String arg = parts.length > 1 && !parts[1].isBlank() ? parts[1].trim() : null;

            ServiceCallParams params = new ServiceCallParams();
            String service = mapper.map(action, arg, params);

            if (service != null && !service.isBlank()) {
                Map<String, Object> payload = params.getPayload();
                if (payload.isEmpty()) {
                    return Optional.of(new ServiceCall(domain, service, entityId));
                } else {
                    return Optional.of(new ServiceCall(domain, service, entityId, payload));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Maps a {@link StringType} command containing a JSON object into a service payload {@link Map}.
     *
     * @param command the incoming openHAB command containing a JSON string.
     *            The string must represent a valid JSON map object (e.g., {@code {"key": "value"}}).
     * @param domain the target Home Assistant domain
     * @param service the service name to invoke
     * @param entityId the target entity ID
     * @return an {@link Optional} containing the service call with the parsed payload map if valid, otherwise empty
     */
    public static Optional<ServiceCall> onJsonString(Command command, String domain, String service, String entityId) {
        if (command instanceof StringType cmd) {
            String jsonStr = cmd.toString().trim();
            if (!jsonStr.isBlank()) {
                try {
                    Map<String, Object> payload = GSON.fromJson(jsonStr, new TypeToken<Map<String, Object>>() {
                    }.getType());
                    if (payload != null) {
                        return Optional.of(new ServiceCall(domain, service, entityId, payload));
                    }
                } catch (JsonSyntaxException e) {
                    // Invalid JSON object string
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Maps {@link DecimalType} commands to a service call with a single numeric parameter payload.
     *
     * @param command the incoming openHAB command
     * @param domain the target Home Assistant domain
     * @param service the service name
     * @param paramKey the parameter key for the decimal value
     * @param entityId the target entity ID
     * @return an {@link Optional} containing the service call if matched, otherwise empty
     */
    public static Optional<ServiceCall> onDecimal(Command command, String domain, String service, String paramKey,
            String entityId) {
        if (command instanceof DecimalType decimal) {
            return Optional.of(new ServiceCall(domain, service, entityId, Map.of(paramKey, decimal.doubleValue())));
        }
        return Optional.empty();
    }

    /**
     * Maps an incoming openHAB {@link QuantityType} command to a {@link ServiceCall}.
     * <p>
     * If {@code targetUnit} is specified (non-null and non-blank), this method attempts to convert
     * the quantity to that unit. If the unit is unparseable or dimension-incompatible, the command
     * is rejected by returning {@link Optional#empty()} to prevent sending unconverted values.
     *
     * @param command the openHAB {@link Command} received by the channel
     * @param targetUnit the expected target unit string (e.g., "°C", "%"), or {@code null} if no conversion is required
     * @param domain the Home Assistant domain (e.g., "climate", "number")
     * @param service the Home Assistant service name (e.g., "set_temperature", "set_value")
     * @param paramKey the payload parameter key expected in the service call (e.g., "temperature", "value")
     * @param entityId the target Home Assistant entity ID (e.g., "climate.living_room")
     * @return an {@link Optional} containing the constructed {@link ServiceCall}, or {@link Optional#empty()} on
     *         failure or mismatch
     */
    public static Optional<ServiceCall> onQuantity(Command command, @Nullable String targetUnit, String domain,
            String service, String paramKey, String entityId) {

        if (!(command instanceof QuantityType<?> quantity)) {
            return Optional.empty();
        }

        // Abort if target unit is unknown to prevent sending un-converted values
        if (targetUnit == null || targetUnit.isBlank()) {
            LOGGER.debug("Target unit is null or blank; skipping quantity command mapping for entity {}.", entityId);
            return Optional.empty();
        }

        QuantityType<?> converted = quantity.toInvertibleUnit(targetUnit);
        if (converted == null) {
            // Unit conversion failed (incompatible dimensions/units); abort safely
            LOGGER.warn("Failed to convert quantity {} to target unit {}; skipping command mapping for entity {}.",
                    quantity, targetUnit, entityId);
            return Optional.empty();
        }

        return Optional.of(new ServiceCall(domain, service, entityId, Map.of(paramKey, converted.doubleValue())));
    }

    /**
     * Maps an incoming openHAB {@link Command} to a {@link ServiceCall} containing a numeric payload.
     * <p>
     * This method attempts to process the command as a {@link DecimalType} first, performing unit
     * conversion to the target unit if specified. If the command is not a {@link QuantityType}
     * (or if unit conversion yields no result), it falls back to extracting a numeric value from
     * a {@link DecimalType} or other numeric command types.
     *
     * @param command the openHAB {@link Command} received by the channel (e.g., {@link QuantityType},
     *            {@link DecimalType})
     * @param targetUnit the target unit of measurement expected by Home Assistant (e.g., "°C", "%"), or {@code null}
     * @param domain the Home Assistant domain (e.g., "climate", "number")
     * @param service the Home Assistant service name (e.g., "set_temperature", "set_value")
     * @param paramKey the payload parameter key expected in the service call (e.g., "temperature", "value")
     * @param entityId the target Home Assistant entity ID (e.g., "climate.living_room")
     * @return an {@link Optional} containing the constructed {@link ServiceCall}, or {@link Optional#empty()} if
     *         unmappable
     */
    public static Optional<ServiceCall> onDecimalOrQuantity(Command command, @Nullable String targetUnit, String domain,
            String service, String paramKey, String entityId) {
        return onDecimal(command, domain, service, paramKey, entityId)
                .or(() -> onQuantity(command, targetUnit, domain, service, paramKey, entityId));
    }

    /**
     * Maps {@link UpDownType} commands to cover-style {@code open_cover} or {@code close_cover} services.
     *
     * @param command the incoming openHAB command
     * @param domain the target Home Assistant domain
     * @param entityId the target entity ID
     * @return an {@link Optional} containing the service call if matched, otherwise empty
     */
    public static Optional<ServiceCall> onUpDown(Command command, String domain, @Nullable String upService,
            @Nullable String downService, String entityId) {
        if (command instanceof UpDownType upDown) {
            if (upDown == UpDownType.UP && upService != null && !upService.isBlank()) {
                return Optional.of(new ServiceCall(domain, upService, entityId));
            } else if (upDown == UpDownType.DOWN && downService != null && !downService.isBlank()) {
                return Optional.of(new ServiceCall(domain, downService, entityId));
            }
        }
        return Optional.empty();
    }

    /**
     * Maps {@link StopMoveType} commands to cover-style {@code stop_cover} or {@code move_cover} services.
     *
     * @param command the incoming openHAB command
     * @param domain the target Home Assistant domain
     * @param stopService the service name for {@link StopMoveType#STOP}
     * @param moveService the service name for {@link StopMoveType#MOVE}
     * @param entityId the target entity ID
     * @return an {@link Optional} containing the service call if matched, otherwise empty
     */
    public static Optional<ServiceCall> onStopMove(Command command, String domain, @Nullable String stopService,
            @Nullable String moveService, String entityId) {
        if (command instanceof StopMoveType stopMove) {
            if (stopMove == StopMoveType.STOP && stopService != null && !stopService.isBlank()) {
                return Optional.of(new ServiceCall(domain, stopService, entityId));
            } else if (stopMove == StopMoveType.MOVE && moveService != null && !moveService.isBlank()) {
                return Optional.of(new ServiceCall(domain, moveService, entityId));
            }
        }
        return Optional.empty();
    }

    /**
     * Maps {@link PercentType} commands to a service call with a single integer or percentage parameter payload.
     *
     * @param command the incoming openHAB command
     * @param domain the target Home Assistant domain
     * @param service the service name
     * @param paramKey the parameter key for the percentage value
     * @param entityId the target entity ID
     * @return an {@link Optional} containing the service call if matched, otherwise empty
     */
    public static Optional<ServiceCall> onPercent(Command command, String domain, String service, String paramKey,
            String entityId) {
        if (command instanceof PercentType percent) {
            return Optional.of(new ServiceCall(domain, service, entityId, Map.of(paramKey, percent.intValue())));
        }
        return Optional.empty();
    }

    /**
     * Maps {@link PercentType} commands to a service call, scaling the percentage value from 0–100% to a floating-point
     * double range (e.g., 0.0 to 1.0).
     *
     * @param command the incoming openHAB command
     * @param domain the target Home Assistant domain
     * @param service the service name
     * @param paramKey the parameter key for the scaled double value
     * @param entityId the target entity ID
     * @param min the lower range bound corresponding to 0%
     * @param max the upper range bound corresponding to 100%
     * @return an {@link Optional} containing the service call if matched, otherwise empty
     */
    public static Optional<ServiceCall> onPercentScaled(Command command, String domain, String service, String paramKey,
            String entityId, double min, double max) {
        if (command instanceof PercentType percent) {
            double scaled = min + (percent.doubleValue() / 100.0) * (max - min);
            return Optional.of(new ServiceCall(domain, service, entityId, Map.of(paramKey, scaled)));
        }
        return Optional.empty();
    }

    /**
     * Maps {@link PercentType} commands to a service call with an inverted percentage parameter (0% -> 100, 100% -> 0).
     * Ideal for blinds, covers, or inverted valve positioning.
     *
     * @param command the incoming openHAB command
     * @param domain the target Home Assistant domain
     * @param service the service name
     * @param paramKey the parameter key for the inverted percentage value
     * @param entityId the target entity ID
     * @return an {@link Optional} containing the service call if matched, otherwise empty
     */
    public static Optional<ServiceCall> onPercentInverted(Command command, String domain, String service,
            String paramKey, String entityId) {
        if (command instanceof PercentType percent) {
            int inverted = 100 - percent.intValue();
            return Optional.of(new ServiceCall(domain, service, entityId, Map.of(paramKey, inverted)));
        }
        return Optional.empty();
    }

    /**
     * Maps {@link PercentType} commands to a service call with an inverted and scaled double precision value
     * (0% -> max, 100% -> min).
     *
     * @param command the incoming openHAB command
     * @param domain the target Home Assistant domain
     * @param service the service name
     * @param paramKey the parameter key for the inverted and scaled value
     * @param entityId the target entity ID
     * @param min the lower bound corresponding to 100%
     * @param max the upper bound corresponding to 0%
     * @return an {@link Optional} containing the service call if matched, otherwise empty
     */
    public static Optional<ServiceCall> onPercentInvertedScaled(Command command, String domain, String service,
            String paramKey, String entityId, double min, double max) {
        if (command instanceof PercentType percent) {
            double invertedPercent = (100.0 - percent.doubleValue()) / 100.0;
            double scaled = min + invertedPercent * (max - min);
            return Optional.of(new ServiceCall(domain, service, entityId, Map.of(paramKey, scaled)));
        }
        return Optional.empty();
    }

    /**
     * Maps {@link OpenClosedType} commands to domain {@code open_cover} or {@code close_cover} services.
     *
     * @param command the incoming openHAB command
     * @param domain the target Home Assistant domain
     * @param entityId the target entity ID
     * @return an {@link Optional} containing the service call if matched, otherwise empty
     */
    public static Optional<ServiceCall> onOpenClosed(Command command, String domain, String entityId) {
        if (command instanceof OpenClosedType openClosed) {
            String service = openClosed == OpenClosedType.OPEN ? "open_cover" : "close_cover";
            return Optional.of(new ServiceCall(domain, service, entityId));
        }
        return Optional.empty();
    }

    /**
     * Maps {@link HSBType} commands to Home Assistant {@code hs_color} ([hue, saturation])
     * and {@code brightness} (0–255) parameters.
     */
    public static Optional<ServiceCall> onHSB(Command command, String domain, String entityId) {
        if (command instanceof HSBType hsb) {
            int brightness = Math.round((hsb.getBrightness().floatValue() / 100.0f) * 255.0f);
            List<Double> hs = List.of(hsb.getHue().doubleValue(), hsb.getSaturation().doubleValue());
            return Optional
                    .of(new ServiceCall(domain, "turn_on", entityId, Map.of("hs_color", hs, "brightness", brightness)));
        }
        return Optional.empty();
    }

    /**
     * Maps {@link HSBType} commands to 0–255 RGB color parameters in a Home Assistant service call.
     */
    public static Optional<ServiceCall> onHSBToRGB(Command command, String domain, String service, String paramKey,
            String entityId) {
        if (command instanceof HSBType hsb) {
            PercentType[] rgbPercent = ColorUtil.hsbToRgbPercent(hsb);
            List<Integer> rgb = List.of( //
                    Math.round((rgbPercent[0].floatValue() / 100.0f) * 255.0f), //
                    Math.round((rgbPercent[1].floatValue() / 100.0f) * 255.0f), //
                    Math.round((rgbPercent[2].floatValue() / 100.0f) * 255.0f) //
            );
            return Optional.of(new ServiceCall(domain, service, entityId, Map.of(paramKey, rgb)));
        }
        return Optional.empty();
    }

    /**
     * Maps a {@link DateTimeType} command to a service call, formatting the time portion as {@code "HH:mm:ss"}.
     *
     * @param command the incoming openHAB command
     * @param domain the target Home Assistant domain
     * @param service the target service name
     * @param paramKey the parameter key for the time string payload (e.g. "value")
     * @param entityId the target entity ID
     * @return an {@link Optional} containing the service call if matched, otherwise empty
     */
    public static Optional<ServiceCall> onDateTimeToTime(Command command, String domain, String service,
            String paramKey, String entityId) {
        if (command instanceof DateTimeType dateTime) {
            String timeValue = TIME_ONLY_FORMATTER.format(dateTime.getZonedDateTime(ZoneId.systemDefault()));
            return Optional.of(new ServiceCall(domain, service, entityId, Map.of(paramKey, timeValue)));
        }
        return Optional.empty();
    }

    /**
     * Maps a {@link DateTimeType} command to a service call, formatting the date portion as {@code "yyyy-MM-dd"}.
     *
     * @param command the incoming openHAB command
     * @param domain the target Home Assistant domain
     * @param service the target service name
     * @param paramKey the parameter key for the date string payload (e.g. "value")
     * @param entityId the target entity ID
     * @return an {@link Optional} containing the service call if matched, otherwise empty
     */
    public static Optional<ServiceCall> onDateTimeToDate(Command command, String domain, String service,
            String paramKey, String entityId) {
        if (command instanceof DateTimeType dateTime) {
            String dateValue = DATE_ONLY_FORMATTER.format(dateTime.getZonedDateTime(ZoneId.systemDefault()));
            return Optional.of(new ServiceCall(domain, service, entityId, Map.of(paramKey, dateValue)));
        }
        return Optional.empty();
    }

    /**
     * Maps a {@link DateTimeType} command to a service call, formatting as an ISO-8601 date-time string.
     *
     * @param command the incoming openHAB command
     * @param domain the target Home Assistant domain
     * @param service the service name
     * @param paramKey the parameter key for the date-time string payload (e.g. "datetime" or "value")
     * @param entityId the target entity ID
     * @return an {@link Optional} containing the service call if matched, otherwise empty
     */
    public static Optional<ServiceCall> onDateTime(Command command, String domain, String service, String paramKey,
            String entityId) {
        if (command instanceof DateTimeType dateTime) {
            String dtValue = DATE_TIME_FORMATTER.format(dateTime.getZonedDateTime(ZoneId.systemDefault()));
            return Optional.of(new ServiceCall(domain, service, entityId, Map.of(paramKey, dtValue)));
        }
        return Optional.empty();
    }

    /**
     * Converts a comma-separated string into a list of trimmed, non-empty string values.
     *
     * @param csv the input comma-separated string (e.g. {@code "1, 2, 3"})
     * @return a list containing non-empty trimmed string elements, or an empty list if input is null/blank
     */
    public static List<String> csvToList(@Nullable String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(",")) //
                .map(String::trim) //
                .filter(s -> !s.isEmpty()) //
                .toList();
    }
}
