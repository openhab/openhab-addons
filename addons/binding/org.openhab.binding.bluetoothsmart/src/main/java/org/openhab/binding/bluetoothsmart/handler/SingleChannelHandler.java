package org.openhab.binding.bluetoothsmart.handler;

import java.math.BigDecimal;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sputnikdev.bluetooth.URL;
import org.sputnikdev.bluetooth.manager.NotReadyException;

abstract class SingleChannelHandler<V, S extends Command> implements ChannelHandler {

    private Logger logger = LoggerFactory.getLogger(SingleChannelHandler.class);

    protected final BluetoothSmartHandler handler;
    protected final String channelID;
    protected final boolean persistent;

    SingleChannelHandler(BluetoothSmartHandler handler, String channelID) {
        this(handler, channelID, false);
    }

    SingleChannelHandler(BluetoothSmartHandler handler, String channelID, boolean persistent) {
        this.handler = handler;
        this.channelID = channelID;
        this.persistent = persistent;
    }

    @Override public void dispose() { }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelID.equals(channelUID.getIdWithoutGroup())) {
            if (command instanceof RefreshType) {
                updateChannel(convert(getValue()));
            } else {
                V value = convert((S) command);
                updateThing(convert((S) command));
                if (persistent) {
                    persist(value);
                }
            }
        }
    }

    @Override
    public void handleUpdate(ChannelUID channelUID, State newState) { }

    @Override
    public URL getURL() {
        return this.handler.getURL();
    }

    public void init() {
        try {
            if (persistent) {
                V value = getInitialValue();
                if (value != null) {
                    init(value);
                }
            } else {
                updateChannel(getValue());
            }
        } catch (NotReadyException ex) {
            logger.info("Device is not ready {}. Thing channel could not be initialised.", getURL());
        }
    }

    void updateChannel(S command) {
        if (command != null) {
            handler.updateState(channelID, (State) command);
        }
    }

    void updateChannel(V value) {
        State state = (State) convert(value);
        if (state != null) {
            handler.updateState(channelID, (State) convert(value));
        }
    }

    abstract V convert(S command);
    abstract S convert(V value);

    void init(V value) {
        updateThing(value);
        updateChannel(value);
    }
    void updateThing(V value) {}
    V getValue() { return null; }
    V getDefaultValue() { return null; }

    private void persist(V value) {
        Configuration configuration = handler.editConfiguration();
        configuration.put(this.channelID, value);
        handler.updateConfiguration(configuration);
    }

    private V getInitialValue() {
        V result = null;

        // Only Boolean, String and BigDecimal values are supported (this is what OH returns for binding configs)
        Object stored = handler.getConfig().get(this.channelID);
        if (stored instanceof BigDecimal) {
            result = (V) (Integer) ((BigDecimal) stored).intValue();
        } else if (stored instanceof Boolean || stored instanceof String) {
            result = (V) stored;
        }

        if (result == null) {
            result = getDefaultValue();
        }
        return result;
    }

}
