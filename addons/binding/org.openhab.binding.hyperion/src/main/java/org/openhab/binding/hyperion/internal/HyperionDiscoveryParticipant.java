package org.openhab.binding.hyperion.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.mdns.discovery.MDNSDiscoveryParticipant;
import org.openhab.binding.hyperion.HyperionBindingConstants;

public class HyperionDiscoveryParticipant implements MDNSDiscoveryParticipant {

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return HyperionBindingConstants.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public String getServiceType() {
        return "_hyperiond-json._tcp.local.";
    }

    @Override
    public DiscoveryResult createResult(ServiceInfo service) {

        final Map<String, Object> properties = new HashMap<>(2);
        String host = service.getHostAddresses()[0];
        int port = service.getPort();
        properties.put(HyperionBindingConstants.HOST, host);
        properties.put(HyperionBindingConstants.PORT, port);

        String friendlyName = "Hyperion Server";
        ThingUID uid = getThingUID(service);

        final DiscoveryResult result = DiscoveryResultBuilder.create(uid)
                .withThingType(HyperionBindingConstants.THING_TYPE_SERVER).withProperties(properties)
                .withLabel(friendlyName).build();
        return result;
    }

    @Override
    public ThingUID getThingUID(ServiceInfo service) {
        return new ThingUID(HyperionBindingConstants.THING_TYPE_SERVER, "server");
    }

}
