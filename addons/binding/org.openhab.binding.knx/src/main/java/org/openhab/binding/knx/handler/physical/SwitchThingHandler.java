/**
 *
 */
package org.openhab.binding.knx.handler.physical;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.knx.handler.PhysicalActorThingHandler;

import tuwien.auto.calimero.GroupAddress;

/**
 * The {@link DimmerThingHandler} is responsible for handling commands, which are
 * sent to one of the channels. It implements a KNX switch actor
 *
 * @author Karel Goderis - Initial contribution
 */
public class SwitchThingHandler extends PhysicalActorThingHandler {

    // List of all Channel ids
    public final static String CHANNEL_SWITCH = "switch";

    // List of all Configuration parameters
    public static final String SWITCH_GA = "switchGA";
    public static final String STATUS_GA = "statusGA";

    public SwitchThingHandler(Thing thing, ItemChannelLinkRegistry itemChannelLinkRegistry) {
        super(thing, itemChannelLinkRegistry);
    }

    @Override
    public void initialize() {

        try {
            if ((String) getConfig().get(SWITCH_GA) != null) {
                GroupAddress address = new GroupAddress((String) getConfig().get(SWITCH_GA));
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
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_SWITCH), (State) state);
            }
        } catch (Exception e) {
            // do nothing, we move on (either config parameter null, or wrong address format)
        }
    }

    @Override
    public String getDPT(GroupAddress destination) {
        return "1.001";
    }

    @Override
    public String getDPT(ChannelUID channelUID, Type command) {
        return "1.001";
    }

    @Override
    public String getAddress(ChannelUID channelUID, Type command) {
        if (command instanceof OnOffType) {
            return (String) getConfig().get(SWITCH_GA);
        }
        return null;
    }

    @Override
    public Type getType(ChannelUID channelUID, Type command) {
        return command;
    }
}
