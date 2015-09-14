package org.openhab.binding.ownet.handler;

import static org.openhab.binding.ownet.OWNetBindingConstants.THING_TYPE28;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

public class OW28Handler extends OWDeviceHandler {
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE28);

    public OW28Handler(Thing thing) {
        super(thing);
        // TODO Auto-generated constructor stub
    }

}
