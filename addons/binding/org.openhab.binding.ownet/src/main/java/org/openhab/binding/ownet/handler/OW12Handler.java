package org.openhab.binding.ownet.handler;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

public class OW12Handler extends OWDeviceHandler {
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(org.openhab.binding.ownet.OWNetBindingConstants.THING_TYPE12);

    public OW12Handler(Thing thing) {
        super(thing);
        // TODO Auto-generated constructor stub
    }

}
