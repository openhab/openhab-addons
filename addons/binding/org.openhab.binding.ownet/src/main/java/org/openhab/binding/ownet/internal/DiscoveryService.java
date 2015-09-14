package org.openhab.binding.ownet.internal;

import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.ownet.handler.DiscoveryParticipant;

public interface DiscoveryService {

    void addParticipant(String id, DiscoveryParticipant handler);

    void removeParticipant(String id);

    void unDiscover(ThingUID uid);
}
