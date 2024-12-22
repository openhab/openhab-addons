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
package org.openhab.binding.bluetooth.generic.internal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.bluetooth.gattparser.BluetoothGattParser;
import org.openhab.bluetooth.gattparser.FieldHolder;
import org.openhab.bluetooth.gattparser.GattRequest;
import org.openhab.bluetooth.gattparser.spec.Enumeration;
import org.openhab.bluetooth.gattparser.spec.Field;
import org.openhab.bluetooth.gattparser.spec.FieldFormat;
import org.openhab.bluetooth.gattparser.spec.FieldType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BluetoothChannelUtils} contains utility functions used by the GattChannelHandler
 *
 * @author Vlad Kolotov - Initial contribution
 * @author Connor Petty - Modified for openHAB use
 */
@NonNullByDefault
public class BluetoothChannelUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(BluetoothChannelUtils.class);

    public static String encodeFieldID(Field field) {
        String requirements = Optional.ofNullable(field.getRequirements()).orElse(Collections.emptyList()).stream()
                .collect(Collectors.joining());
        return encodeFieldName(field.getName() + requirements);
    }

    public static String encodeFieldName(String fieldName) {
        return Base64.getEncoder().encodeToString(fieldName.getBytes(StandardCharsets.UTF_8)).replace("=", "");
    }

    public static String decodeFieldName(String encodedFieldName) {
        return new String(Base64.getDecoder().decode(encodedFieldName), StandardCharsets.UTF_8);
    }

    public static @Nullable String getItemType(Field field) {
        FieldFormat format = field.getFormat();
        if (format == null) {
            // unknown format
            return null;
        }
        switch (field.getFormat().getType()) {
            case BOOLEAN:
                return "Switch";
            case UINT:
            case SINT:
            case FLOAT_IEE754:
            case FLOAT_IEE11073:
                // BluetoothUnit unit = BluetoothUnit.findByType(field.getUnit());
                // if (unit != null) {
                // TODO
                // return "Number:" + unit.getUnit().getDimension();
                // }
                return "Number";
            case UTF8S:
            case UTF16S:
                return "String";
            case STRUCT:
                return "String";
            // unsupported format
            default:
                return null;
        }
    }

    public static State convert(BluetoothGattParser parser, FieldHolder holder) {
        State state;
        if (holder.isValueSet()) {
            if (holder.getField().getFormat().isBoolean()) {
                state = OnOffType.from(Boolean.TRUE.equals(holder.getBoolean()));
            } else {
                // check if we can use enumerations
                if (holder.getField().hasEnumerations()) {
                    Enumeration enumeration = holder.getEnumeration();
                    if (enumeration != null) {
                        if (holder.getField().getFormat().isNumber()) {
                            return new DecimalType(new BigDecimal(enumeration.getKey()));
                        } else {
                            return new StringType(enumeration.getKey().toString());
                        }
                    }
                    // fall back to simple types
                }
                if (holder.getField().getFormat().isNumber()) {
                    state = new DecimalType(holder.getBigDecimal());
                } else if (holder.getField().getFormat().isStruct()) {
                    state = new StringType(parser.parse(holder.getBytes(), 16));
                } else {
                    state = new StringType(holder.getString());
                }
            }
        } else {
            state = UnDefType.UNDEF;
        }
        return state;
    }

    public static void updateHolder(BluetoothGattParser parser, GattRequest request, String fieldName, State state) {
        Field field = request.getFieldHolder(fieldName).getField();
        FieldType fieldType = field.getFormat().getType();
        if (fieldType == FieldType.BOOLEAN) {
            OnOffType onOffType = convert(state, OnOffType.class);
            if (onOffType == null) {
                LOGGER.debug("Could not convert state to OnOffType: {} : {} : {} ", request.getCharacteristicUUID(),
                        fieldName, state);
                return;
            }
            request.setField(fieldName, onOffType == OnOffType.ON);
            return;
        }
        if (field.hasEnumerations()) {
            // check if we can use enumerations
            Enumeration enumeration = getEnumeration(field, state);
            if (enumeration != null) {
                request.setField(fieldName, enumeration);
                return;
            } else {
                LOGGER.debug("Could not convert state to enumeration: {} : {} : {} ", request.getCharacteristicUUID(),
                        fieldName, state);
            }
            // fall back to simple types
        }
        switch (fieldType) {
            case UINT:
            case SINT: {
                DecimalType decimalType = convert(state, DecimalType.class);
                if (decimalType == null) {
                    LOGGER.debug("Could not convert state to DecimalType: {} : {} : {} ",
                            request.getCharacteristicUUID(), fieldName, state);
                    return;
                }
                request.setField(fieldName, decimalType.longValue());
                return;
            }
            case FLOAT_IEE754:
            case FLOAT_IEE11073: {
                DecimalType decimalType = convert(state, DecimalType.class);
                if (decimalType == null) {
                    LOGGER.debug("Could not convert state to DecimalType: {} : {} : {} ",
                            request.getCharacteristicUUID(), fieldName, state);
                    return;
                }
                request.setField(fieldName, decimalType.doubleValue());
                return;
            }
            case UTF8S:
            case UTF16S: {
                StringType textType = convert(state, StringType.class);
                if (textType == null) {
                    LOGGER.debug("Could not convert state to StringType: {} : {} : {} ",
                            request.getCharacteristicUUID(), fieldName, state);
                    return;
                }
                request.setField(fieldName, textType.toString());
                return;
            }
            case STRUCT:
                StringType textType = convert(state, StringType.class);
                if (textType == null) {
                    LOGGER.debug("Could not convert state to StringType: {} : {} : {} ",
                            request.getCharacteristicUUID(), fieldName, state);
                    return;
                }
                String text = textType.toString().trim();
                if (text.startsWith("[")) {
                    request.setField(fieldName, parser.serialize(text, 16));
                } else {
                    request.setField(fieldName, new BigInteger(text));
                }
                return;
            // unsupported format
            default:
                return;
        }
    }

    private static @Nullable Enumeration getEnumeration(Field field, State state) {
        DecimalType decimalType = convert(state, DecimalType.class);
        if (decimalType != null) {
            try {
                return field.getEnumeration(new BigInteger(decimalType.toString()));
            } catch (NumberFormatException ignored) {
                // do nothing
            }
        }
        return null;
    }

    private static <T extends State> @Nullable T convert(State state, Class<T> typeClass) {
        return state.as(typeClass);
    }
}
