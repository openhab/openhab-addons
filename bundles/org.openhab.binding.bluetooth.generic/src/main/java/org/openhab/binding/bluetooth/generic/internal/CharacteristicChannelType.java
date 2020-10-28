/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sputnikdev.bluetooth.gattparser.BluetoothGattParser;
import org.sputnikdev.bluetooth.gattparser.spec.Field;

/**
 * This class handles the formatting/parsing of the generated channel types.
 *
 * @author Connor Petty - Initial contribution
 *
 */
@NonNullByDefault
public class CharacteristicChannelType {

    private static final String CHANNEL_TYPE_NAME_PATTERN = "characteristic-%s-%s-%s-%s";
    private static final Logger logger = LoggerFactory.getLogger(CharacteristicChannelType.class);

    public final boolean advanced;
    public final boolean readOnly;
    public final String characteristicUUID;
    public final Field field;

    public CharacteristicChannelType(boolean advanced, boolean readOnly, String characteristicUUID, Field field) {
        super();
        this.advanced = advanced;
        this.readOnly = readOnly;
        this.characteristicUUID = characteristicUUID;
        this.field = field;
    }

    public static boolean isValidUID(ChannelTypeUID channelTypeUID) {
        if (!channelTypeUID.getBindingId().equals(BluetoothBindingConstants.BINDING_ID)) {
            return false;
        }
        String channelID = channelTypeUID.getId();
        if (!channelID.startsWith("characteristic")) {
            return false;
        }
        if (channelID.length() < 68) {
            return false;
        }
        if (channelID.charAt(21) != '-') {
            return false;
        }
        if (channelID.charAt(30) != '-') {
            return false;
        }
        if (channelID.charAt(67) != '-') {
            return false;
        }
        return true;
    }

    public static @Nullable CharacteristicChannelType fromChannelTypeUID(BluetoothGattParser gattParser,
            ChannelTypeUID channelTypeUID) {
        if (!isValidUID(channelTypeUID)) {
            return null;
        }
        String channelID = channelTypeUID.getId();
        boolean advanced = "advncd".equals(channelID.substring(15, 21));
        boolean readOnly = "readable".equals(channelID.substring(22, 30));
        String characteristicUUID = channelID.substring(31, 67);
        String fieldName = channelID.substring(68, channelID.length());

        if (gattParser.isKnownCharacteristic(characteristicUUID)) {
            List<Field> fields = gattParser.getFields(characteristicUUID).stream()
                    .filter(field -> BluetoothChannelUtils.encodeFieldID(field).equals(fieldName))
                    .collect(Collectors.toList());

            if (fields.size() > 1) {
                logger.warn("Multiple fields with the same name found: {} / {}. Skipping them.", characteristicUUID,
                        fieldName);
                return null;
            }

            Field field = fields.get(0);
            return new CharacteristicChannelType(advanced, readOnly, characteristicUUID, field);
        }
        return null;
    }

    public ChannelTypeUID toChannelTypeUID() {
        String channelType = String.format(CHANNEL_TYPE_NAME_PATTERN, advanced ? "advncd" : "simple",
                readOnly ? "readable" : "writable", characteristicUUID, BluetoothChannelUtils.encodeFieldID(field));

        return new ChannelTypeUID(BluetoothBindingConstants.BINDING_ID, channelType);
    }
}
