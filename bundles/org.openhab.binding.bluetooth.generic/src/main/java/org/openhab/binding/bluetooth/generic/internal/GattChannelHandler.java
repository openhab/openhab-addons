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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sputnikdev.bluetooth.gattparser.BluetoothGattParser;
import org.sputnikdev.bluetooth.gattparser.FieldHolder;
import org.sputnikdev.bluetooth.gattparser.GattRequest;
import org.sputnikdev.bluetooth.gattparser.GattResponse;
import org.sputnikdev.bluetooth.gattparser.spec.Characteristic;
import org.sputnikdev.bluetooth.gattparser.spec.Field;

/**
 * The GattChannelHandler handles the mapping of channels to bluetooth gatt characteristics.
 *
 * @author Connor Petty - Initial contribution
 */
@NonNullByDefault
public class GattChannelHandler {
    private Logger logger = LoggerFactory.getLogger(GattChannelHandler.class);

    private ChannelHandlerCallback callback;
    private BluetoothGattParser gattParser;
    private BluetoothCharacteristic characteristic;

    public GattChannelHandler(ChannelHandlerCallback callback, BluetoothGattParser gattParser,
            BluetoothCharacteristic characteristic) {
        this.callback = callback;
        this.gattParser = gattParser;
        this.characteristic = characteristic;
    }

    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof State) {
            State state = (State) command;
            try {
                String characteristicUUID = characteristic.getUuid().toString();
                if (gattParser.isKnownCharacteristic(characteristicUUID)) {
                    String fieldName = getFieldName(channelUID);
                    if (fieldName != null) {
                        updateCharacteristic(fieldName, state);
                    } else {
                        logger.warn("Characteristic has no field name!");
                    }
                } else if (state instanceof StringType) {
                    // unknown characteristic
                    byte[] data = HexUtils.hexToBytes(state.toString());
                    if (!callback.writeCharacteristic(characteristic, data)) {
                        callback.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "Could not write data to characteristic: " + characteristicUUID);
                    }
                }
            } catch (Exception ex) {
                callback.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Could not update bluetooth device. Error: " + ex.getMessage());
                logger.warn("Could not update bluetooth device: {} : {}", characteristic.getUuid(), ex.getMessage());
            }
        }
    }

    private void updateCharacteristic(String fieldName, State state) {
        // TODO maybe we should check if the characteristic is authenticated?
        String characteristicUUID = characteristic.getUuid().toString();

        if (gattParser.isValidForWrite(characteristicUUID)) {
            GattRequest request = gattParser.prepare(characteristicUUID);
            try {
                BluetoothChannelUtils.updateHolder(gattParser, request, fieldName, state);
                byte[] data = gattParser.serialize(request);

                if (!callback.writeCharacteristic(characteristic, data)) {
                    callback.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Could not write data to characteristic: " + characteristicUUID);
                }
            } catch (NumberFormatException ex) {
                logger.warn("Could not parse characteristic value: {} : {}", characteristicUUID, state, ex);
                callback.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Could not parse characteristic value: " + characteristicUUID + " : " + state);
            }
        }
    }

    public void handleCharacteristicUpdate(byte[] data) {
        String characteristicUUID = characteristic.getUuid().toString();
        if (gattParser.isKnownCharacteristic(characteristicUUID)) {
            GattResponse response = gattParser.parse(characteristicUUID, data);
            for (FieldHolder holder : response.getFieldHolders()) {
                Field field = holder.getField();
                ChannelUID channelUID = getChannelUID(field);
                callback.updateState(channelUID, BluetoothChannelUtils.convert(gattParser, holder));
            }
        } else {
            // this is a raw channel
            String hex = HexUtils.bytesToHex(data);
            ChannelUID channelUID = getChannelUID(null);
            callback.updateState(channelUID, new StringType(hex));
        }
    }

    public List<Channel> buildChannels() {
        List<Channel> channels = new ArrayList<>();
        String charUUID = characteristic.getUuid().toString();
        Characteristic gattChar = gattParser.getCharacteristic(charUUID);
        if (gattChar != null) {
            List<Field> fields = gattParser.getFields(charUUID);

            String label = null;
            // check if the characteristic has only on field, if so use its name as label
            if (fields.size() == 1) {
                label = gattChar.getName();
            }

            Map<String, List<Field>> fieldsMapping = fields.stream().collect(Collectors.groupingBy(Field::getName));

            for (List<Field> fieldList : fieldsMapping.values()) {
                if (fieldList.size() > 1) {
                    if (fieldList.get(0).isFlagField() || fieldList.get(0).isOpCodesField()) {
                        logger.debug("Skipping flags/op codes field: {}.", charUUID);
                    } else {
                        logger.warn("Multiple fields with the same name found: {} / {}. Skipping these fields.",
                                charUUID, fieldList.get(0).getName());
                    }
                    continue;
                }
                Field field = fieldList.get(0);

                if (isFieldSupported(field)) {
                    Channel channel = buildFieldChannel(field, label, !gattChar.isValidForWrite());
                    if (channel != null) {
                        channels.add(channel);
                    }
                } else {
                    logger.warn("GATT field is not supported: {} / {} / {}", charUUID, field.getName(),
                            field.getFormat());
                }
            }
        } else {
            channels.add(buildUnknownChannel());
        }
        return channels;
    }

    private Channel buildUnknownChannel() {
        ChannelUID channelUID = getChannelUID(null);
        ChannelTypeUID channelTypeUID = new ChannelTypeUID(BluetoothBindingConstants.BINDING_ID, "char-unknown");
        return ChannelBuilder.create(channelUID).withType(channelTypeUID).withProperties(getChannelProperties(null))
                .build();
    }

    public boolean canRead() {
        String charUUID = characteristic.getUuid().toString();
        if (gattParser.isKnownCharacteristic(charUUID)) {
            return gattParser.isValidForRead(charUUID);
        }
        // TODO: need to evaluate this from characteristic properties, but such properties aren't support yet
        return true;
    }

    public boolean canWrite() {
        String charUUID = characteristic.getUuid().toString();
        if (gattParser.isKnownCharacteristic(charUUID)) {
            return gattParser.isValidForWrite(characteristic.getUuid().toString());
        }
        // TODO: need to evaluate this from characteristic properties, but such properties aren't support yet
        return true;
    }

    private boolean isAdvanced() {
        return false;// TODO need to decide whether to implement this or not
    }

    private boolean isFieldSupported(Field field) {
        return field.getFormat() != null;
    }

    private @Nullable Channel buildFieldChannel(Field field, @Nullable String charLabel, boolean readOnly) {
        String label = charLabel != null ? charLabel : field.getName();
        String acceptedType = BluetoothChannelUtils.getItemType(field);
        if (acceptedType == null) {
            // unknown field format
            return null;
        }

        ChannelUID channelUID = getChannelUID(field);

        logger.debug("Building a new channel for a field: {}", channelUID.getId());

        ChannelTypeUID channelTypeUID = new CharacteristicChannelType(isAdvanced(), readOnly,
                characteristic.getUuid().toString(), field).toChannelTypeUID();

        return ChannelBuilder.create(channelUID, acceptedType).withType(channelTypeUID)
                .withProperties(getChannelProperties(field.getName())).withLabel(label).build();
    }

    private ChannelUID getChannelUID(@Nullable Field field) {
        StringBuilder builder = new StringBuilder();
        builder.append(GenericBindingConstants.CHANNEL_CHARACTERISTIC_PREFIX)//
                .append("-")//
                .append(characteristic.getUuid());
        if (field != null) {
            builder.append("-").append(BluetoothChannelUtils.encodeFieldName(field.getName()));
        }
        return new ChannelUID(callback.getThingUID(), builder.toString());
    }

    private @Nullable String getFieldName(ChannelUID channelUID) {
        String channelId = channelUID.getId();
        int index = channelId.lastIndexOf("-");
        if (index == -1) {
            throw new IllegalArgumentException("ChannelUID '" + channelUID + "' is not a valid GATT channel format");
        }
        String encodedFieldName = channelId.substring(index + 1);
        if (encodedFieldName.isEmpty()) {
            return null;
        }
        return BluetoothChannelUtils.decodeFieldName(encodedFieldName);
    }

    private Map<String, String> getChannelProperties(@Nullable String fieldName) {
        Map<String, String> properties = new HashMap<>();
        if (fieldName != null) {
            properties.put(GenericBindingConstants.PROPERTY_FIELD_NAME, fieldName);
        }
        properties.put(GenericBindingConstants.PROPERTY_SERVICE_UUID, characteristic.getService().getUuid().toString());
        properties.put(GenericBindingConstants.PROPERTY_CHARACTERISTIC_UUID, characteristic.getUuid().toString());
        return properties;
    }
}
