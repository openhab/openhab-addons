package org.openhab.binding.snapcast.discovery;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.snapcast.SnapcastBindingConstants;
import org.openhab.binding.snapcast.internal.protocol.SnapcastController;
import org.openhab.binding.snapcast.internal.types.Client;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class SnapclientDiscoveryService extends AbstractDiscoveryService {

    private ServiceRegistration<?> reg;
    private SnapcastController controller;
    private String host;
    private ThingUID id;

    public SnapclientDiscoveryService(String host, SnapcastController controller, ThingUID thingUID) {
        super(Collections.singleton(SnapcastBindingConstants.THING_TYPE_SNAPCLIENT), 2, true);
        this.host = host;
        this.controller = controller;
        this.id = thingUID;

    }

    public SnapclientDiscoveryService() {
        super(Collections.singleton(SnapcastBindingConstants.THING_TYPE_SNAPCLIENT), 2, true);
    }

    public void stop() {
        if (reg != null) {
            reg.unregister();
        }
        reg = null;
    }

    public void start(BundleContext bundleContext) {
        if (reg != null) {
            return;
        }
        reg = bundleContext.registerService(DiscoveryService.class.getName(), this, new Hashtable<String, Object>());
    }

    @Override
    protected void startScan() {
        Map<String, Object> properties = new HashMap<>(3);
        properties.put(SnapcastBindingConstants.CONFIG_HOST_NAME, host);

        for (Client client : controller.getClients()) {
            String clientName = client.getConfig().getName();
            String mac = client.getHost().getMac();
            ThingUID uid = new ThingUID(SnapcastBindingConstants.THING_TYPE_SNAPCLIENT, mac.replaceAll(":", ""));
            properties.put(SnapcastBindingConstants.CONFIG_MAC_ADDRESS, mac);
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid).withLabel(clientName)
                    .withProperties(properties).withBridge(id).withLabel(clientName).build();
            thingDiscovered(discoveryResult);
        }

    }
}
