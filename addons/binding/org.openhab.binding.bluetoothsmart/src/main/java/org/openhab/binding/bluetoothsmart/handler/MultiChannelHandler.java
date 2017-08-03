package org.openhab.binding.bluetoothsmart.handler;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.bluetoothsmart.BluetoothSmartBindingConstants;
import org.openhab.binding.bluetoothsmart.internal.BluetoothSmartUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sputnikdev.bluetooth.URL;
import org.sputnikdev.bluetooth.gattparser.BluetoothGattParser;
import org.sputnikdev.bluetooth.gattparser.CharacteristicFormatException;
import org.sputnikdev.bluetooth.gattparser.FieldHolder;
import org.sputnikdev.bluetooth.gattparser.GattRequest;
import org.sputnikdev.bluetooth.manager.CharacteristicGovernor;
import org.sputnikdev.bluetooth.manager.ValueListener;

class MultiChannelHandler implements ChannelHandler, ValueListener {

    private Logger logger = LoggerFactory.getLogger(MultiChannelHandler.class);

    private final BluetoothSmartHandler handler;
    private final URL url;
    private final String[] flags;

    MultiChannelHandler(BluetoothSmartHandler handler, URL characteristicURL, String[] flags) {
        this.handler = handler;
        this.url = characteristicURL;
        this.flags = flags;
    }

    @Override
    public void init() { }

    public void initChannels() {
        if (BluetoothSmartUtils.hasNotificationAccess(flags)) {
            getGovernor().addValueListener(this);
        }
        updateChannels();
    }

    public void dispose() { }

    @Override
    public URL getURL() {
        return url;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) { }

    @Override
    public void handleUpdate(ChannelUID channelUID, State newState) {
        String fieldID = channelUID.getIdWithoutGroup();
        String characteristicID = BluetoothSmartUtils.getChannelUID(url);
        if (fieldID.startsWith(characteristicID)) {
            Channel fieldChannel = this.handler.getThing().getChannel(channelUID.getIdWithoutGroup());
            String fieldName = fieldChannel.getProperties().get(BluetoothSmartBindingConstants.PROPERTY_FIELD_NAME);
            updateThing(fieldName, newState);
        }
    }

    @Override
    public void changed(byte[] value) {
        Map<String, FieldHolder> fields = parseCharacteristic(value);
        for (FieldHolder holder : fields.values()) {
            updateState(getChannel(holder), holder);
        }
    }

    private void updateChannels() {
        if (BluetoothSmartUtils.hasReadAccess(flags)) {
            Map<String, FieldHolder> holders = parseCharacteristic();
            for (FieldHolder holder : holders.values()) {
                updateState(getChannel(holder), holder);
            }
        }
    }

    private void updateThing(String fieldName, State state) {
        if (BluetoothSmartUtils.hasWriteAccess(flags)) {
            BluetoothGattParser gattParser = handler.getGattParser();
            GattRequest request = gattParser.prepare(url.getCharacteristicUUID());

            for (FieldHolder fieldHolder : request.getAllFieldHolders()) {
                if (fieldHolder.getField().getName().equals(fieldName)) {
                    updateHolder(fieldHolder, state);
                } else {
                    Item item = getItem(fieldHolder);
                    updateHolder(fieldHolder, item.getState());
                }
            }
            byte[] data = gattParser.serialize(request);
            if (!getGovernor().write(data)) {
                this.handler.updateStatus(ThingStatus.ONLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Could not write data to characteristic: " + getURL());
            }
        }
    }

    private Map<String, FieldHolder> parseCharacteristic() {
        byte[] value = getGovernor().read();
        return parseCharacteristic(value);
    }

    private Map<String, FieldHolder> parseCharacteristic(byte[] value) {
        try {
            return handler.getGattParser().parse(url.getCharacteristicUUID(), value).getHolders();
        } catch (CharacteristicFormatException ex) {
            logger.error("Could not parse characteristic: {}. Ignoring it...", url);
            return new LinkedHashMap<>();
        }
    }

    private void updateState(Channel channel, FieldHolder holder) {
        State state;
        if (holder.getField().getFormat().isBoolean()) {
            state = holder.getBoolean(false) ? OnOffType.ON : OnOffType.OFF;
        } else if (holder.getField().getFormat().isNumber()) {
            state = new DecimalType(holder.getString("0"));
        } else {
            state = new StringType(holder.getString(""));
        }
        handler.updateState(channel.getUID(), state);
    }

    private void updateHolder(FieldHolder holder, State state) {
        if (holder.getField().getFormat().isBoolean()) {
            holder.setBoolean(convert(state, OnOffType.class) == OnOffType.ON);
        } else if (holder.getField().getFormat().isReal()) {
            DecimalType decimalType = (DecimalType) convert(state, DecimalType.class);
            holder.setInteger(decimalType.intValue());
        } else if (holder.getField().getFormat().isDecimal()) {
            DecimalType decimalType = (DecimalType) convert(state, DecimalType.class);
            holder.setDouble(decimalType.doubleValue());
        } if (holder.getField().getFormat().isString()) {
            StringType textType = (StringType) convert(state, StringType.class);
            holder.setString(textType.toString());
        }
    }

    private State convert(State state, Class<? extends State> typeClass) {
        return state.as(typeClass);
    }

    private Channel getChannel(FieldHolder fieldHolder) {
        return this.handler.getThing().getChannel(
                BluetoothSmartUtils.getChannelUID(url.copyWith(fieldHolder.getField().getName())));
    }

    private Item getItem(FieldHolder fieldHolder) {
        String itemID = BluetoothSmartUtils.getItemUID(url.copyWith(fieldHolder.getField().getName()));
        try {
            return this.handler.getItemRegistry().getItem(itemID);
        } catch (ItemNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    private CharacteristicGovernor getGovernor() {
        return this.handler.getBluetoothManager().getCharacteristicGovernor(url);
    }
}
