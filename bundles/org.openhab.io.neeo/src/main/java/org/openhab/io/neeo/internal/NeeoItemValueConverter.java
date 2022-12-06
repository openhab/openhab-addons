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
package org.openhab.io.neeo.internal;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.Item;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationHelper;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.openhab.core.types.TypeParser;
import org.openhab.core.types.UnDefType;
import org.openhab.io.neeo.internal.models.NeeoCapabilityType;
import org.openhab.io.neeo.internal.models.NeeoDeviceChannel;
import org.openhab.io.neeo.internal.models.NeeoItemValue;
import org.openhab.io.neeo.internal.servletservices.models.PathInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class is responsible for formatting a {@link State} to a string value. A lot of the code was 'borrowed' from
 * ItemUIRegistryImpl.java
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class NeeoItemValueConverter {
    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(NeeoItemValueConverter.class);

    /** RegEx to identify format patterns. See java.util.Formatter#formatSpecifier (without the '%' at the very end). */
    private static final String IDENTIFY_FORMAT_PATTERN_PATTERN = "%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z])";

    /** The service context */
    private final ServiceContext context;

    /**
     * Creates the convert using the {@link ServiceContext}
     *
     * @param context a non-null {@link ServiceContext}
     */
    public NeeoItemValueConverter(ServiceContext context) {
        Objects.requireNonNull(context, "context must not be null");
        this.context = context;
    }

    /**
     * Converts the {@link State} from the given {@link NeeoDeviceChannel} to a {@link NeeoItemValue}. This method will
     * convert enums to booleans and format string values to their associated pattern
     *
     * @param channel the non-null channel
     * @param state the non-null state
     * @return the non-null {@link NeeoItemValue}
     */
    public NeeoItemValue convert(NeeoDeviceChannel channel, State state) {
        Objects.requireNonNull(channel, "channel cannot be null");
        Objects.requireNonNull(state, "state cannot be null");

        final String format = channel.getValue();

        // HSBType must be done before the others since it inherits from DecimalType
        if (state instanceof HSBType) {
            final HSBType hsb = (HSBType) state;
            switch (channel.getSubType()) {
                case HUE:
                    return new NeeoItemValue(hsb.getHue().toBigDecimal());
                case BRIGHTNESS:
                    return new NeeoItemValue(hsb.getBrightness().toBigDecimal());
                case SATURATION:
                    return new NeeoItemValue(hsb.getSaturation().toBigDecimal());
                default:
                    // do nothing
            }
        }

        State convertedState = state;
        switch (channel.getType()) {
            case SENSOR_POWER:
            case SENSOR_BINARY:
            case BUTTON:
            case SWITCH:
                convertedState = state.as(OnOffType.class);
                break;
            case EXCLUDE:
                convertedState = UnDefType.UNDEF;
                break;
            case SLIDER:
                if (state instanceof PercentType) {
                    convertedState = new DecimalType(((PercentType) state).toBigDecimal());
                } else {
                    convertedState = state.as(DecimalType.class);
                }
                break;
            case SENSOR:
            case SENSOR_RANGE:
            case SENSOR_CUSTOM:
                convertedState = state;
                break;
            case TEXTLABEL:
            case IMAGEURL:
            case DIRECTORY:
                convertedState = new StringType(state.toString());
                break;
        }

        // Note: state.as can return null
        if (convertedState == null) {
            convertedState = UnDefType.UNDEF;
        }

        if (convertedState instanceof OnOffType) {
            return new NeeoItemValue(convertedState == OnOffType.ON);
        } else if (convertedState instanceof OpenClosedType) {
            return new NeeoItemValue(convertedState == OpenClosedType.OPEN);
        } else if (convertedState instanceof UpDownType) {
            return new NeeoItemValue(convertedState == UpDownType.UP);

        } else if (convertedState instanceof DecimalType) {
            if (format == null || format.isEmpty() || channel.getType() == NeeoCapabilityType.SLIDER) {
                return new NeeoItemValue(((DecimalType) convertedState).toBigDecimal());
            }
        } else if (convertedState instanceof UnDefType) {
            return new NeeoItemValue("-");
        } else if (convertedState instanceof StringType && channel.getType() == NeeoCapabilityType.IMAGEURL) {
            return new NeeoItemValue(convertedState.toString());
        }

        // Formatting must use the actual state (not converted state) to avoid
        // issues where a decimal converted to string or otherwise
        String itemValue;
        if (format != null && !format.isEmpty()) {
            if (state instanceof UnDefType) {
                itemValue = formatUndefined(format);
            } else if (state instanceof Type) {
                if (TransformationHelper.isTransform(format)) {
                    try {
                        final String transformed = TransformationHelper
                                .transform(context.getComponentContext().getBundleContext(), format, state.toString());
                        if (transformed != null) {
                            return new NeeoItemValue(transformed);
                        }
                    } catch (NoClassDefFoundError | TransformationException ex) {
                        // TransformationHelper is optional dependency, so ignore if class not found
                    }
                }

                try {
                    itemValue = ((Type) state).format(format);
                } catch (IllegalArgumentException e) {
                    logger.warn("Exception while formatting value '{}' of item {} with format '{}': {}", state,
                            channel == null ? "null" : channel.getItemName(), format, e.getMessage());
                    itemValue = "(Error)";
                }
            } else {
                itemValue = state.toString();
            }
        } else {
            itemValue = state.toString();
        }

        return new NeeoItemValue(itemValue);
    }

    /**
     * Takes the given <code>formatPattern</code> and replaces it with an analog
     * String-based pattern to replace all value Occurrences with a dash ("-").
     *
     * @param formatPattern the original pattern which will be replaces by a
     *            String pattern.
     * @return a formatted String with dashes ("-") as value replacement
     */
    private static String formatUndefined(String formatPattern) {
        String undefinedFormatPattern = formatPattern.replaceAll(IDENTIFY_FORMAT_PATTERN_PATTERN, "%1\\$s");
        try {
            return String.format(undefinedFormatPattern, "-");
        } catch (IllegalArgumentException e) {
            final Logger logger = LoggerFactory.getLogger(NeeoItemValueConverter.class);
            logger.warn("Exception while formatting undefined value [sourcePattern={}, targetPattern={}, {}]",
                    formatPattern, undefinedFormatPattern, e.getMessage(), e);
            return "(Error)";
        }
    }

    /**
     * Convert the {@link Item} and {@link PathInfo} to a {@link Command}. Buttons and switches have special handling,
     * all others are handled by {@link TypeParser#parseCommand(java.util.List, String)}
     *
     * @param item the non-null item
     * @param eventType the non-null event type
     * @return the command
     */
    @Nullable
    public static Command convert(Item item, PathInfo eventType) {
        return convert(item, eventType, eventType.getActionValue());
    }

    /**
     * Convert the {@link Item} and {@link PathInfo} to a {@link Command}. Buttons and switches have special handling,
     * all others are handled by {@link TypeParser#parseCommand(java.util.List, String)}
     *
     * @param item the non-null item
     * @param eventType the non-null event type
     * @param actionValue the possibly null, possibly empty action value
     * @return the command
     */
    @Nullable
    public static Command convert(Item item, PathInfo eventType, @Nullable String actionValue) {
        Objects.requireNonNull(item, "item cannot be null");
        Objects.requireNonNull(eventType, "eventType cannot be null");

        if (actionValue == null || actionValue.isEmpty()) {
            return null;
        }

        if (item.getAcceptedDataTypes().contains(HSBType.class)) {
            final HSBType hsbType = item.getState() instanceof HSBType ? (HSBType) item.getState() : HSBType.WHITE;
            final DecimalType hue = hsbType.getHue();
            final PercentType sat = hsbType.getSaturation();
            final PercentType bri = hsbType.getBrightness();

            try {
                switch (eventType.getSubType()) {
                    case HUE:
                        return new HSBType(new DecimalType(actionValue), sat, bri);
                    case SATURATION:
                        return new HSBType(hue, new PercentType(actionValue), bri);
                    case BRIGHTNESS:
                        return new HSBType(hue, sat, new PercentType(actionValue));
                    default:
                        break;
                }
            } catch (IllegalArgumentException e) {
                // do nothing - let it go to the other cases
            }
        }
        switch (eventType.getComponentType()) {
            case "button":
                for (Class<? extends Command> cmd : item.getAcceptedCommandTypes()) {
                    final Command c = NeeoUtil.getEnum(cmd, actionValue);
                    if (c != null) {
                        return c;
                    }
                }
                break;
            case "switch":
                if ("true".equalsIgnoreCase(actionValue)) {
                    return OnOffType.ON;
                } else if ("false".equalsIgnoreCase(actionValue)) {
                    return OnOffType.OFF;
                }
                break;
        }
        return TypeParser.parseCommand(item.getAcceptedCommandTypes(), actionValue);
    }
}
