/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.neeo.internal;

import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.transform.TransformationHelper;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.eclipse.smarthome.core.types.TypeParser;
import org.eclipse.smarthome.core.types.UnDefType;
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
 * @author Tim Roberts - Initial contribution
 */
public class NeeoItemValueConverter {

    /** The logger */
    private static final Logger logger = LoggerFactory.getLogger(NeeoItemValueConverter.class);

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
        if (state instanceof OnOffType) {
            return new NeeoItemValue(state == OnOffType.ON);
        } else if (state instanceof OpenClosedType) {
            return new NeeoItemValue(state == OpenClosedType.OPEN);
        } else if (state instanceof UpDownType) {
            return new NeeoItemValue(state == UpDownType.UP);
        } else if (state instanceof DecimalType) {
            if (StringUtils.isEmpty(format)) {
                return new NeeoItemValue(((DecimalType) state).toBigDecimal());
            }
        } else if (state instanceof UnDefType) {
            return new NeeoItemValue("-");
        } else if (state instanceof StringType && channel.getType() == NeeoCapabilityType.IMAGEURL) {
            return new NeeoItemValue(state.toString());
        }

        String itemValue;
        if (StringUtils.isNotEmpty(format)) {
            if (state instanceof UnDefType) {
                itemValue = formatUndefined(format);
            } else if (state instanceof Type) {
                if (TransformationHelper.isTransform(format)) {
                    try {
                        final String transformed = TransformationHelper
                                .transform(context.getComponentContext().getBundleContext(), format, state.toString());
                        return new NeeoItemValue(transformed);
                    } catch (NoClassDefFoundError ex) {
                        // TransformationHelper is optional dependency, so ignore if class not found
                    }
                }

                try {
                    itemValue = ((Type) state).format(format);
                } catch (IllegalArgumentException e) {
                    logger.warn("Exception while formatting value '{}' of item {} with format '{}': {}", state,
                            channel.getItemName(), format, e.getMessage());
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
     * Takes the given <code>formatPattern</code> and replaces it with a analog
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
    public static Command convert(Item item, PathInfo eventType) {
        Objects.requireNonNull(item, "item cannot be null");
        Objects.requireNonNull(eventType, "eventType cannot be null");

        switch (eventType.getComponentType()) {
            case "button":
                for (Class<? extends Command> cmd : item.getAcceptedCommandTypes()) {
                    final Command c = NeeoUtil.getEnum(cmd, eventType.getActionValue());
                    if (c != null) {
                        return c;
                    }
                }
                break;
            case "switch":
                if (StringUtils.equalsIgnoreCase("true", eventType.getActionValue())) {
                    return OnOffType.ON;
                } else if (StringUtils.equalsIgnoreCase("false", eventType.getActionValue())) {
                    return OnOffType.OFF;
                }
                break;
        }
        return TypeParser.parseCommand(item.getAcceptedCommandTypes(), eventType.getActionValue());
    }
}
