package org.openhab.binding.evcc.internal.discovery;

import static org.openhab.binding.evcc.internal.EvccBindingConstants.THING_TYPE_SERVER;

import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link EvccMDNSDiscoveryParticipant} is responsible for scanning the network for evcc instances
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
@Component(service = MDNSDiscoveryParticipant.class, configurationPid = "mdns-discovery.evcc")
public class EvccMDNSDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private static final String SERVICE_TYPE = "_http._tcp.local.";

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(THING_TYPE_SERVER);
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo serviceInfo) {
        if (!"evcc".equalsIgnoreCase(serviceInfo.getName())) {
            return null;
        }
        String[] addresses = serviceInfo.getHostAddresses();
        if (addresses.length == 0) {
            return null;
        }
        String host = addresses[0];
        int port = serviceInfo.getPort();
        ThingUID uid = getThingUID(serviceInfo);
        return uid == null ? null
                : DiscoveryResultBuilder.create(uid).withLabel("evcc instance (" + host + ")")
                        .withProperty("host", host).withProperty("port", port).build();
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo serviceInfo) {
        if (!"evcc".equalsIgnoreCase(serviceInfo.getName())) {
            return null;
        }
        String[] addresses = serviceInfo.getHostAddresses();
        if (addresses.length == 0) {
            return null;
        }
        return new ThingUID(THING_TYPE_SERVER, addresses[0].replace(".", "_"));
    }

    @Override
    public long getRemovalGracePeriodSeconds(ServiceInfo service) {
        return 0L;
    }
}
