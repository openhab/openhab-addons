package org.openhab.binding.bluetoothsmart.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.bluetoothsmart.BluetoothSmartBindingConstants;
import org.openhab.binding.bluetoothsmart.internal.BluetoothSmartUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sputnikdev.bluetooth.URL;
import org.sputnikdev.bluetooth.gattparser.BluetoothGattParser;
import org.sputnikdev.bluetooth.gattparser.spec.Characteristic;
import org.sputnikdev.bluetooth.gattparser.spec.Field;
import org.sputnikdev.bluetooth.manager.GattCharacteristic;
import org.sputnikdev.bluetooth.manager.GattService;

public class BluetoothSmartChannelBuilder {

    private Logger logger = LoggerFactory.getLogger(BluetoothSmartChannelBuilder.class);

    private final BluetoothSmartHandler handler;

    public BluetoothSmartChannelBuilder(BluetoothSmartHandler handler) {
        this.handler = handler;
    }

    Map<MultiChannelHandler, List<Channel>> buildChannels(List<GattService> services) {
        BluetoothGattParser gattParser = this.handler.getGattParser();
        Map<MultiChannelHandler, List<Channel>> result = new HashMap<>();
        for (GattService service : services) {
            for (GattCharacteristic characteristic : service.getCharacteristics()) {
                logger.info("Handling a new characteristic: {}", characteristic.getURL());

                if (!gattParser.isKnownCharacteristic(characteristic.getURL().getCharacteristicUUID())) {
                    logger.info("Skipping unknown characteristic: {} ", characteristic.getURL());
                    continue;
                }

                String[] flags = characteristic.getFlags();
                boolean readAccess = BluetoothSmartUtils.hasReadAccess(flags) ||
                        BluetoothSmartUtils.hasNotificationAccess(flags);
                boolean writeAccess = BluetoothSmartUtils.hasWriteAccess(flags);
                if (!readAccess && !writeAccess) {
                    logger.info("The characteristic {} is not supported, flags: {} ",
                            characteristic.getURL(), characteristic.getFlags());
                    continue;
                }
                if (readAccess && gattParser.isValidForRead(characteristic.getURL().getCharacteristicUUID()) || writeAccess) {
                    logger.info("Creating channels for characteristic: {}", characteristic.getURL());
                    result.put(new MultiChannelHandler(handler, characteristic.getURL(), flags),
                            buildChannels(handler.getThing(), service, characteristic));
                } else {
                    logger.info("Skipping invalid characteristic {}.", characteristic.getURL());
                }
            }
        }
        return result;
    }

    private List<Channel> buildChannels(Thing thing, GattService service, GattCharacteristic characteristic) {
        List<Channel> channels = new ArrayList<>();
        for (Field field : this.handler.getGattParser().getFields(characteristic.getURL().getCharacteristicUUID())) {
            Channel channel = buildChannel(thing, service, characteristic, field);
            channels.add(channel);
        }
        return channels;
    }

    private Channel buildChannel(Thing thing, GattService service, GattCharacteristic characteristic, Field field) {
        URL channelURL = characteristic.getURL().copyWith(field.getName());//this.handler.getURL().copyWith(service.getUUID(), characteristic.getUUID(), field.getName());
        ChannelUID channelUID = new ChannelUID(thing.getUID(), BluetoothSmartUtils.getChannelUID(channelURL));

        ChannelTypeUID channelTypeUID = new ChannelTypeUID(BluetoothSmartBindingConstants.BINDING_ID,
                BluetoothSmartBindingConstants.CHANNEL_FIELD);
        return ChannelBuilder.create(channelUID, getAcceptedItemType(field))
                .withType(channelTypeUID)
                .withProperties(getFieldProperties(field))
                .withLabel(getChannelLabel(characteristic.getURL().getCharacteristicUUID(), field))
                .build();
    }

    private Map<String, String> getFieldProperties(Field field) {
        Map<String, String> properties = new HashMap<>();
        properties.put(BluetoothSmartBindingConstants.PROPERTY_FIELD_NAME, field.getName());
        return properties;
    }

    private String getChannelLabel(String characteristicUUID, Field field) {
        Characteristic spec = this.handler.getGattParser().getCharacteristic(characteristicUUID);
        if (spec.getValue().getFields().size() > 1) {
            return spec.getName() + "/" + field.getName();
        } else {
            return spec.getName();
        }
    }

    private String getAcceptedItemType(Field field) {
        switch (field.getFormat().getType()) {
        case BOOLEAN: return "Switch";
        case UINT:
        case SINT:
        case FLOAT_IEE754:
        case FLOAT_IEE11073: return "Number";
        case UTF8S:
        case UTF16S: return "String";
        case STRUCT: return "Binary";
        default: throw new IllegalStateException();
        }
    }

}
