package org.openhab.binding.ownet.handler;

import static org.openhab.binding.ownet.OWNetBindingConstants.THING_TYPE29;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.State;

public class OW29Handler extends OWDeviceHandler {
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE29);

    public OW29Handler(Thing thing) {
        super(thing);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void update(String channel, State newState) {
        ChannelUID uid = new ChannelUID(getThing().getUID(), channel);
        updateState(uid, newState);

    }

}
