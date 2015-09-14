package org.openhab.binding.ownet.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.ownet.handler.DiscoveryParticipant;
import org.openhab.binding.ownet.handler.OWDeviceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OWDeviceDiscoveryService extends AbstractDiscoveryService implements DiscoveryService {
    private Logger logger = LoggerFactory.getLogger(OWDeviceDiscoveryService.class);
    private ScheduledFuture<?> discoveryJob;
    Collection<String> cachedDevices = new HashSet<String>();
    Map<String, DiscoveryParticipant> handlers = new HashMap<String, DiscoveryParticipant>();

    Runnable discovery = new Runnable() {
        @Override
        public void run() {
            logger.debug("discovery() method is called!");
            Collection<String> devices = new HashSet<String>();
            for (String handlerUID : handlers.keySet()) {
                DiscoveryParticipant h = handlers.get(handlerUID);
                OWDiscoveryResult result = h.getDiscoveryResult();
                if (result != null) {
                    for (ThingUID id : result.toAdd) {
                        Map<String, Object> properties = new HashMap<>();
                        properties.put(OWDeviceHandler.PROP_ID, id.getId());
                        DiscoveryResult r = DiscoveryResultBuilder.create(id).withProperties(properties)
                                .withLabel("1-wire device").withBridge(h.getUID()).build();
                        thingDiscovered(r);
                    }
                    for (ThingUID id : result.toRemove) {
                        thingRemoved(id);
                    }
                }
            }
            cachedDevices = devices;
        }
    };

    public OWDeviceDiscoveryService() {
        super(OWDeviceHandler.SUPPORTED_THING_TYPES_UIDS, 0, true);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return OWDeviceHandler.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    protected void startBackgroundDiscovery() {
        discoveryJob = scheduler.scheduleAtFixedRate(discovery, 0, 10, TimeUnit.SECONDS);
    }

    @Override
    protected void startScan() {
        if (discoveryJob == null || discoveryJob.isCancelled()) {
            discoveryJob = scheduler.scheduleAtFixedRate(discovery, 0, 10, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopScan() {
        if (discoveryJob != null) {
            discoveryJob.cancel(true);
        }
        discoveryJob = null;
    }

    @Override
    public void addParticipant(String uid, DiscoveryParticipant handler) {
        handlers.put(uid, handler);

    }

    @Override
    public void removeParticipant(String asString) {
        handlers.remove(asString);
    }

    @Override
    public void unDiscover(ThingUID thingUID) {
        for (String handlerUID : handlers.keySet()) {
            DiscoveryParticipant h = handlers.get(handlerUID);
            if (h.unDiscover(thingUID)) {
                return;
            }
        }
    }
}
