package org.openhab.binding.foobot.internal.discovery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.foobot.internal.FoobotBindingConstants;
import org.openhab.binding.foobot.internal.FoobotHandlerFactory;
import org.openhab.binding.foobot.internal.handler.FoobotAccountHandler;
import org.openhab.binding.foobot.internal.json.FoobotDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FoobotAccountDiscoveryService} is responsible for starting the discovery procedure
 * that retrieves Foobot account and imports all registered foobot devices.
 *
 * @author George Katsis - Initial contribution
 */
public class FoobotAccountDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(FoobotAccountDiscoveryService.class);

    private static final int TIMEOUT = 5;

    private FoobotAccountHandler handler;
    private ThingUID bridgeUID;

    private ScheduledFuture<?> scanJob;

    public FoobotAccountDiscoveryService(FoobotAccountHandler handler) {
        super(FoobotHandlerFactory.DISCOVERABLE_THING_TYPE_UIDS, TIMEOUT);
        this.handler = handler;
    }

    private void retrieveFoobots() {
        List<FoobotDevice> foobots = handler.getDeviceList();

        for (FoobotDevice foobot : foobots) {
            addThing(foobot);
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        retrieveFoobots();
    }

    @Override
    protected void startScan() {
        if (this.scanJob != null) {
            scanJob.cancel(true);
        }
        this.scanJob = scheduler.schedule(() -> retrieveFoobots(), 0, TimeUnit.SECONDS);
    }

    @Override
    protected void stopScan() {
        super.stopScan();

        if (this.scanJob != null) {
            this.scanJob.cancel(true);
            this.scanJob = null;
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    private void addThing(FoobotDevice foobot) {
        logger.debug("addThing(): Adding retrieved Foobot {} to the account handler", foobot.getName());

        Map<String, Object> properties = new HashMap<>();
        ThingUID thingUID = new ThingUID(FoobotBindingConstants.THING_TYPE_FOOBOT, foobot.getUuid());
        properties.put(FoobotBindingConstants.CONFIG_APIKEY, handler.apiKey);
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, foobot.getUuid());
        properties.put(FoobotBindingConstants.CONFIG_UUID, foobot.getUuid());
        properties.put(Thing.PROPERTY_MAC_ADDRESS, foobot.getMac());
        properties.put(FoobotBindingConstants.PROPERTY_NAME, foobot.getName());

        thingDiscovered(
                DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID).withProperties(properties).build());
    }
}
