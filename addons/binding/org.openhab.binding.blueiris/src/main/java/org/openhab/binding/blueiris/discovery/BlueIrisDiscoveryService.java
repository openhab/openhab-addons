package org.openhab.binding.blueiris.discovery;

import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.blueiris.BlueIrisBindingConstants;
import org.openhab.binding.blueiris.handler.BlueIrisBridgeHandler;
import org.openhab.binding.blueiris.handler.BridgeListener;
import org.openhab.binding.blueiris.internal.control.Connection;
import org.openhab.binding.blueiris.internal.data.CamListReply;
import org.openhab.binding.blueiris.internal.data.CamListRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Handles the discovery stuff.
 *
 * @author David Bennett - Initial COntribution
 *
 */
public class BlueIrisDiscoveryService extends AbstractDiscoveryService implements BridgeListener {
    private Logger logger = LoggerFactory.getLogger(BlueIrisDiscoveryService.class);
    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets
            .newHashSet(BlueIrisBindingConstants.THING_TYPE_BRIDGE, BlueIrisBindingConstants.THING_TYPE_CAMERA);
    private BlueIrisBridgeHandler bridge;

    public BlueIrisDiscoveryService(BlueIrisBridgeHandler bridge) throws IllegalArgumentException {
        super(60);
        this.bridge = bridge;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        // TODO Auto-generated method stub
        return SUPPORTED_THING_TYPES_UIDS;
    }

    /**
     * Start scanning stuff. Need to start a thread to do this.
     */
    @Override
    protected void startScan() {
        Thread discoveryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                final Connection connection = bridge.getConnection();
                CamListRequest request = new CamListRequest();
                if (connection.sendCommand(request)) {
                    onCamList(request.getReply());
                }
            }
        });
        discoveryThread.start();
        logger.info("Started Discovery thread");
    }

    @Override
    public void onCamList(CamListReply camListReply) {
        final ThingUID bridgeUid = bridge.getThing().getUID();
        for (CamListReply.Data data : camListReply.getCameras()) {
            ThingUID cameraUID = new ThingUID(BlueIrisBindingConstants.THING_TYPE_CAMERA, bridgeUid,
                    data.getOptionValue());
            Map<String, Object> properties = Maps.newHashMap();
            properties.put(BlueIrisBindingConstants.PROPERTY_SHORT_NAME, data.getOptionValue());
            properties.put(Thing.PROPERTY_VENDOR, "Blue Iris");
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION,
                    bridge.getConnection().getLoginReply().getData().getVersion());
            properties.put(Thing.PROPERTY_MODEL_ID, bridge.getConnection().getLoginReply().getData().getSystemName());
            DiscoveryResult result = DiscoveryResultBuilder.create(cameraUID).withBridge(bridgeUid)
                    .withLabel(data.getOptionDisplay()).withProperties(properties).build();
            thingDiscovered(result);
            logger.info("Found camera {}", data.getOptionDisplay());
        }
    }

    public void activateBridge() {
        this.bridge.addListener(this);
    }

    @Override
    protected void deactivate() {
        this.bridge.removeListener(this);
        super.deactivate();
    }
}
