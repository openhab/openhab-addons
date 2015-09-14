package org.openhab.binding.ownet.handler;

import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.ownet.internal.OWDiscoveryResult;

public interface DiscoveryParticipant {

    OWDiscoveryResult getDiscoveryResult();

    ThingUID getUID();

    boolean unDiscover(ThingUID thingUID);

}
