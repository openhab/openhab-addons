package org.openhab.binding.blueiris.discovery;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.blueiris.BlueIrisBindingConstants;
import org.openhab.binding.blueiris.handler.BlueIrisBridgeHandler;
import org.openhab.binding.blueiris.internal.control.Connection;
import org.openhab.binding.blueiris.internal.data.CamListReply;
import org.openhab.binding.blueiris.internal.data.CamListRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the discovery stuff.
 *
 * @author David Bennett - Initial COntribution
 *
 */
public class BlueIrisDiscoveryService extends AbstractDiscoveryService {
    private Logger logger = LoggerFactory.getLogger(BlueIrisDiscoveryService.class);
    private BlueIrisBridgeHandler bridge;

    public BlueIrisDiscoveryService(BlueIrisBridgeHandler bridge) throws IllegalArgumentException {
        super(60);
        this.bridge = bridge;
    }

    /**
     * Start scanning stuff. Need to start a thread to do this.
     */
    @Override
    protected void startScan() {
        final ThingUID bridgeUid = bridge.getThing().getUID();
        final Connection connection = bridge.getConnection();
        Thread discoveryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                CamListRequest request = new CamListRequest();
                if (connection.sendCommand(request)) {
                    for (CamListReply.Data data : request.getReply().getCameras()) {

                        ThingUID cameraUID = new ThingUID(BlueIrisBindingConstants.THING_TYPE_CAMERA, bridgeUid,
                                data.getOptionsValue());
                        DiscoveryResult result = DiscoveryResultBuilder.create(cameraUID).withBridge(bridgeUid)
                                .withLabel(data.getOptionsDisplay())
                                .withProperty(BlueIrisBindingConstants.PROPERTY_SHORT_NAME, data.getOptionsValue())
                                .build();
                        thingDiscovered(result);
                        logger.info("Found camera {}", data.getOptionsDisplay());
                    }
                }

            }

        });
        discoveryThread.start();
        logger.info("Started Discovery thread");
    }

}
