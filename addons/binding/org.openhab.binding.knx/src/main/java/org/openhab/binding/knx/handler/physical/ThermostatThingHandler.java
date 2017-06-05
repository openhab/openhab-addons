/**
 *
 */
package org.openhab.binding.knx.handler.physical;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.knx.handler.PhysicalActorThingHandler;

import tuwien.auto.calimero.GroupAddress;

/**
 * The {@link ThermostatThingHandler} is responsible for handling commands, which are
 * sent to one of the channels. It implements a KNX thermostat actor
 *
 * @author Karel Goderis - Initial contribution
 */
public class ThermostatThingHandler extends PhysicalActorThingHandler {

    // List of all Channel ids
    public final static String CHANNEL_SETPOINT = "setpoint";

    // List of all Configuration parameters
    public static final String SETPOINT_GA = "setpointGA";
    public static final String STATUS_GA = "statusGA";

    public ThermostatThingHandler(Thing thing, ItemChannelLinkRegistry itemChannelLinkRegistry) {
        super(thing, itemChannelLinkRegistry);
    }

    @Override
    public void initialize() {

        try {
            if ((String) getConfig().get(SETPOINT_GA) != null) {
                GroupAddress address = new GroupAddress((String) getConfig().get(SETPOINT_GA));
                if (address != null) {
                    groupAddresses.add(address);
                }
            }
        } catch (Exception e) {
            logger.error("An exception occurred while creating a Group Address : '{}'", e.getMessage());
        }

        try {
            if ((String) getConfig().get(STATUS_GA) != null) {
                GroupAddress address = new GroupAddress((String) getConfig().get(STATUS_GA));
                if (address != null) {
                    groupAddresses.add(address);
                    if ((Boolean) getConfig().get(READ)) {
                        logger.debug("Registering {} in read Addresses", address);
                        readAddresses.add(address);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("An exception occurred while creating a Group Address : '{}'", e.getMessage());
        }

        super.initialize();
    }

    @Override
    public void processDataReceived(GroupAddress destination, Type state) {

        try {
            GroupAddress address = new GroupAddress((String) getConfig().get(STATUS_GA));
            if (address.equals(destination)) {
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_SETPOINT), (State) state);
            }
        } catch (Exception e) {
            // do nothing, we move on (either config parameter null, or wrong address format)
        }
    }

    @Override
    public String getDPT(GroupAddress destination) {
        return "9.001";
    }

    @Override
    public String getDPT(ChannelUID channelUID, Type command) {
        return "9.001";
    }

    @Override
    public String getAddress(ChannelUID channelUID, Type command) {
        if (command instanceof DecimalType) {
            return (String) getConfig().get(SETPOINT_GA);
        }
        return null;
    }

    @Override
    public Type getType(ChannelUID channelUID, Type command) {
        return command;
    }
}
