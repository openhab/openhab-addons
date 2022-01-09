package org.openhab.binding.lgthinq.internal.discovery;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;

import java.util.Set;

public class LGThinqDiscoveryService extends AbstractDiscoveryService
        implements DiscoveryService, ThingHandlerService {
    public LGThinqDiscoveryService(@Nullable Set<ThingTypeUID> supportedThingTypes, int timeout, boolean backgroundDiscoveryEnabledByDefault) throws IllegalArgumentException {
        super(supportedThingTypes, timeout, backgroundDiscoveryEnabledByDefault);
    }

    @Override
    protected void startScan() {

    }

    @Override
    public void setThingHandler(ThingHandler handler) {

    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return null;
    }

    @Override
    public void activate() {
        ThingHandlerService.super.activate();
    }

    @Override
    public void deactivate() {
        ThingHandlerService.super.deactivate();
    }
}
