package org.openhab.binding.modbus.handler;

import static org.openhab.binding.modbus.ModbusBindingConstants.THING_TYPE_ENDPOINT;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.wimpi.modbus.procimg.InputRegister;
import net.wimpi.modbus.util.BitVector;

public class EndpointHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(EndpointHandler.class);

    private int readRegister = -1;
    private int writeRegister = -1;
    Integer value = null;

    private SlaveConnector slave;

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_ENDPOINT);

    private static final String PROP_READREGISTER = "read";
    private static final String PROP_WRITEREGISTER = "write";

    public EndpointHandler(Thing thing) {
        super(thing);
        readRegister = ((BigDecimal) thing.getConfiguration().get(PROP_READREGISTER)).intValue();
        try {
            writeRegister = ((BigDecimal) thing.getConfiguration().get(PROP_READREGISTER)).intValue();
        } catch (Exception e) {
            writeRegister = readRegister;

        }
    }

    @Override
    public void initialize() {
        super.initialize();
        slave = (SlaveConnector) getBridge().getHandler();
        ThingStatus status = getBridge().getStatus();
        updateStatus(status);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channel = channelUID.getId();
        if ("switch".equals(channel)) {
            slave.setCoil(command.equals(OnOffType.ON) ? true : false, readRegister, writeRegister);
        } else if ("number".equals(channel)) {
            slave.setRegister(((DecimalType) command).intValue(), readRegister, writeRegister);
        }
        // if(channelUID.getId().equals(CHANNEL_1)) {
        // // TODO: handle command
        // }
    }

    public void update(BitVector storage) {
        boolean b = storage.getBit(readRegister);
        Integer bb = new Integer(b ? 1 : 0);
        if (!bb.equals(value) || value == null) {
            updateState(new ChannelUID(getThing().getUID(), "switch"), b ? OnOffType.ON : OnOffType.OFF);
            updateState(new ChannelUID(getThing().getUID(), "contact"),
                    b ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
            value = bb;
        }

    }

    public void update(InputRegister[] storage) {
        Integer bb = storage[readRegister].getValue();
        if (!bb.equals(value) || value == null) {
            ChannelUID c = new ChannelUID(getThing().getUID(), "number");
            updateState(c, new DecimalType(bb));
            value = bb;
        }
    }
}
