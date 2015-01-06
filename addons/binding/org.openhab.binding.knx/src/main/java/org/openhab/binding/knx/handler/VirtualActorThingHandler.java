package org.openhab.binding.knx.handler;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.IndividualAddress;

public abstract class VirtualActorThingHandler extends KNXBaseThingHandler {

    public VirtualActorThingHandler(Thing thing, ItemChannelLinkRegistry itemChannelLinkRegistry) {
        super(thing, itemChannelLinkRegistry);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onGroupWrite(KNXBridgeBaseThingHandler bridge, IndividualAddress source, GroupAddress destination,
            byte[] asdu) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onGroupRead(KNXBridgeBaseThingHandler bridge, IndividualAddress source, GroupAddress destination,
            byte[] asdu) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onGroupReadResponse(KNXBridgeBaseThingHandler bridge, IndividualAddress source,
            GroupAddress destination, byte[] asdu) {
        // TODO Auto-generated method stub

    }
}
