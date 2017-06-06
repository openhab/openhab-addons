/**
 *
 */
package org.openhab.binding.knx.handler.physical;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.knx.handler.PhysicalActorThingHandler;

import tuwien.auto.calimero.GroupAddress;

/**
 * The {@link GenericThingHandler} is responsible for handling commands, which are
 * sent to one of the channels. It is a stub for Individual Addresses that are
 * discovered on the KNX bus
 *
 * @author Karel Goderis - Initial contribution
 */
public class GenericThingHandler extends PhysicalActorThingHandler {

    public GenericThingHandler(Thing thing, ItemChannelLinkRegistry itemChannelLinkRegistry) {
        super(thing, itemChannelLinkRegistry);
    }

    @Override
    public void processDataReceived(GroupAddress destination, Type state) {
        // Nothing to do here
    }

    @Override
    public String getDPT(GroupAddress destination) {
        return null;
    }

    @Override
    public String getDPT(ChannelUID channelUID, Type command) {
        return null;
    }

    @Override
    public String getAddress(ChannelUID channelUID, Type command) {
        return null;
    }

    @Override
    public Type getType(ChannelUID channelUID, Type command) {
        return command;
    }

}
