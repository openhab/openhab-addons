package org.openhab.binding.bosesoundtouch10.internal.discovery;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.mdns.discovery.MDNSDiscoveryParticipant;
import org.openhab.binding.bosesoundtouch10.BoseSoundTouch10BindingConstants;

/**
 * The {@link BoseSoundTouch10DiscoveryParticipant} class is responsible for discovering the device.
 *
 * @author syracom - Initial contribution
 */
public class BoseSoundTouch10DiscoveryParticipant implements MDNSDiscoveryParticipant {

    private static final String SERVICE_TYPE = "_soundtouch._tcp.local.";
    private static final String SERVICE_PROTOCOL = "http";
    private static final String SERVICE_PORT = "8090";

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        Set<ThingTypeUID> uids = new HashSet<ThingTypeUID>();
        uids.add(BoseSoundTouch10BindingConstants.THING_TYPE_SOUNDTOUCH10);
        return uids;
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    public DiscoveryResult createResult(ServiceInfo service) {
        ThingUID uid = getThingUID(service);
        if (uid == null) {
            return null;
        }
        Map<String, Object> properties = new HashMap<>(1);
        String host = service.getHostAddresses()[0];
        String serviceName = service.getName();
        if (serviceName == null) {
            serviceName = "name not available";
        }
        properties.put(BoseSoundTouch10BindingConstants.DEVICEURL,
                SERVICE_PROTOCOL + "://" + host + ":" + SERVICE_PORT);
        return DiscoveryResultBuilder.create(uid)
                .withThingType(BoseSoundTouch10BindingConstants.THING_TYPE_SOUNDTOUCH10).withProperties(properties)
                .withLabel(serviceName).build();
    }

    @Override
    public ThingUID getThingUID(ServiceInfo service) {
        String macAddr = service.getPropertyString("MAC");
        if (macAddr != null) {
            return new ThingUID(BoseSoundTouch10BindingConstants.THING_TYPE_SOUNDTOUCH10, macAddr);
        } else {
            return null;
        }
    }
}