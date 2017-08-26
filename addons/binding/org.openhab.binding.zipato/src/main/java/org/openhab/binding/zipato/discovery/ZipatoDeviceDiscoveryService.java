package org.openhab.binding.zipato.discovery;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.zipato.ZipatoBindingConstants;
import org.openhab.binding.zipato.handler.ZipatoControllerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itth.zipatoclient.device.Camera;
import com.itth.zipatoclient.device.Device;
import com.itth.zipatoclient.device.Sensor;
import com.itth.zipatoclient.device.Switch;

public class ZipatoDeviceDiscoveryService extends AbstractDiscoveryService {
    private static final Logger logger = LoggerFactory.getLogger(ZipatoDeviceDiscoveryService.class);
    private ScheduledFuture<?> zipatoDiscoveryJob;
    private final ZipatoScan zipatoScan = new ZipatoScan();
    private ZipatoControllerHandler controller;

    public ZipatoControllerHandler getController() {
        return controller;
    }

    public void setController(ZipatoControllerHandler controller) {
        this.controller = controller;
    }

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(ZipatoBindingConstants.ZIPATO_THING_SENSOR_UID);

    public ZipatoDeviceDiscoveryService(ZipatoControllerHandler zipatoControllerHandler)
            throws IllegalArgumentException {
        super(SUPPORTED_THING_TYPES_UIDS, 30);
        this.controller = zipatoControllerHandler;
    }

    @Override
    protected void startScan() {
        if (controller == null || controller.getZipato() == null) {
            logger.warn("controller device not available");
            return;
        }
        try {
            Device[] devices = controller.getZipato().doListDevices().get();
            for (Device device : devices) {
                deviceDiscovered(device);
            }
            devices = controller.getZipato().doListCameras().get();
            for (Device device : devices) {
                deviceDiscovered(device);
            }
        } catch (Exception e) {
            logger.error("error fetching devices", e);
        }
    }

    public void deviceDiscovered(Device device) {
        if (device instanceof Sensor) {
            ThingUID bridgeUID = controller.getThing().getUID();
            ThingUID thingUID = new ThingUID(ZipatoBindingConstants.ZIPATO_THING_SENSOR_UID, bridgeUID,
                    device.getUuid());
            String label = device.getName();
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                    .withLabel(label).build();
            thingDiscovered(discoveryResult);
        } else if (device instanceof Camera) {
            ThingUID bridgeUID = controller.getThing().getUID();
            ThingUID thingUID = new ThingUID(ZipatoBindingConstants.ZIPATO_THING_CAMERA_UID, bridgeUID,
                    device.getUuid());
            String label = device.getName();
            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put("url_snapshot", ((Camera) device).getUrlSnapshot());
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                    .withLabel(label).withProperties(properties).build();
            thingDiscovered(discoveryResult);
        } else if (device instanceof Switch) {
            ThingUID bridgeUID = controller.getThing().getUID();
            ThingUID thingUID = new ThingUID(ZipatoBindingConstants.ZIPATO_THING_SWITCH_UID, bridgeUID,
                    device.getUuid());
            String label = device.getName();
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                    .withLabel(label).build();
            thingDiscovered(discoveryResult);
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Start Zipato device background discovery");
        if (zipatoDiscoveryJob == null || zipatoDiscoveryJob.isCancelled()) {
            zipatoDiscoveryJob = AbstractDiscoveryService.scheduler.scheduleWithFixedDelay(this.zipatoScan, 10, 30,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stop Zipato device background discovery");
        if (zipatoDiscoveryJob != null && !zipatoDiscoveryJob.isCancelled()) {
            zipatoDiscoveryJob.cancel(true);
            zipatoDiscoveryJob = null;
        }
    }

    class ZipatoScan implements Runnable {
        @Override
        public void run() {
            startScan();
        }
    }

}
