package org.openhab.binding.mysensors.discovery;

import static org.openhab.binding.mysensors.MySensorsBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.mysensors.handler.MySensorsBridgeHandler;
import org.openhab.binding.mysensors.internal.MySensorsMessage;
import org.openhab.binding.mysensors.service.DiscoveryThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tim Oberföll
 *
 *         Discoveryservice for MySensors devices
 */
public class MySensorsDiscoveryService extends AbstractDiscoveryService {

    private Logger logger = LoggerFactory.getLogger(MySensorsDiscoveryService.class);

    private MySensorsBridgeHandler bridgeHandler = null;

    private DiscoveryThread discoThread = null;

    public MySensorsDiscoveryService(MySensorsBridgeHandler bridgeHandler) {
        super(SUPPORTED_THING_TYPES_UIDS, 3000, false);
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    protected void startScan() {
        if (discoThread == null) {
            discoThread = new DiscoveryThread(bridgeHandler.getBridgeConnection(), this);
        }
        discoThread.start();
    }

    public void activate() {

    }

    @Override
    public void deactivate() {
        if (discoThread == null) {
            discoThread = new DiscoveryThread(bridgeHandler.getBridgeConnection(), this);
        }
        discoThread.stop();
    }

    @Override
    protected void stopScan() {
        if (discoThread == null) {
            discoThread = new DiscoveryThread(bridgeHandler.getBridgeConnection(), this);
        }
        discoThread.stop();
    }

    public void newDevicePresented(MySensorsMessage msg) {

        // Representation Message?
        if (msg.getMsgType() == MYSENSORS_MSG_TYPE_PRESENTATION) {
            logger.debug("Representation Message received");
            logger.debug("Preparing new thing for inbox");

            ThingUID uid = null;

            // uid must not contains dots
            switch (msg.getSubType()) {
                case MYSENSORS_SUBTYPE_S_HUM:
                    uid = new ThingUID(THING_TYPE_HUMIDITY, bridgeHandler.getThing().getUID(),
                            "Humidity_" + msg.getNodeId() + "_" + msg.getChildId());
                    break;
                case MYSENSORS_SUBTYPE_S_TEMP:
                    uid = new ThingUID(THING_TYPE_TEMPERATURE, bridgeHandler.getThing().getUID(),
                            "Temperature_" + msg.getNodeId() + "_" + msg.getChildId());
                    break;
                case MYSENSORS_SUBTYPE_S_MULTIMETER:
                    uid = new ThingUID(THING_TYPE_MULTIMETER, bridgeHandler.getThing().getUID(),
                            "Multimeter_" + msg.getNodeId() + "_" + msg.getChildId());
                    break;
                case MYSENSORS_SUBTYPE_S_LIGHT:
                    uid = new ThingUID(THING_TYPE_LIGHT, bridgeHandler.getThing().getUID(),
                            "Light_" + msg.getNodeId() + "_" + msg.getChildId());
                    break;
                case MYSENSORS_SUBTYPE_S_POWER:
                    uid = new ThingUID(THING_TYPE_POWER, bridgeHandler.getThing().getUID(),
                            "Power_" + msg.getNodeId() + "_" + msg.getChildId());
                    break;
                case MYSENSORS_SUBTYPE_S_BARO:
                    uid = new ThingUID(THING_TYPE_BARO, bridgeHandler.getThing().getUID(),
                            "Baro_" + msg.getNodeId() + "_" + msg.getChildId());
                    break;
                case MYSENSORS_SUBTYPE_S_DOOR:
                    uid = new ThingUID(THING_TYPE_DOOR, bridgeHandler.getThing().getUID(),
                            "Door_" + msg.getNodeId() + "_" + msg.getChildId());
                    break;
                case MYSENSORS_SUBTYPE_S_MOTION:
                    uid = new ThingUID(THING_TYPE_MOTION, bridgeHandler.getThing().getUID(),
                            "Motion_" + msg.getNodeId() + "_" + msg.getChildId());
                    break;
                case MYSENSORS_SUBTYPE_S_SMOKE:
                    uid = new ThingUID(THING_TYPE_SMOKE, bridgeHandler.getThing().getUID(),
                            "Smoke_" + msg.getNodeId() + "_" + msg.getChildId());
                    break;
                case MYSENSORS_SUBTYPE_S_DIMMER:
                    uid = new ThingUID(THING_TYPE_DIMMER, bridgeHandler.getThing().getUID(),
                            "Dimmer_" + msg.getNodeId() + "_" + msg.getChildId());
                    break;
                case MYSENSORS_SUBTYPE_S_COVER:
                    uid = new ThingUID(THING_TYPE_COVER, bridgeHandler.getThing().getUID(),
                            "Cover_" + msg.getNodeId() + "_" + msg.getChildId());
                    break;
                case MYSENSORS_SUBTYPE_S_WIND:
                    uid = new ThingUID(THING_TYPE_WIND, bridgeHandler.getThing().getUID(),
                            "Wind_" + msg.getNodeId() + "_" + msg.getChildId());
                    break;
                case MYSENSORS_SUBTYPE_S_RAIN:
                    uid = new ThingUID(THING_TYPE_RAIN, bridgeHandler.getThing().getUID(),
                            "Rain_" + msg.getNodeId() + "_" + msg.getChildId());
                    break;
                case MYSENSORS_SUBTYPE_S_UV:
                    uid = new ThingUID(THING_TYPE_UV, bridgeHandler.getThing().getUID(),
                            "UV_" + msg.getNodeId() + "_" + msg.getChildId());
                    break;
                case MYSENSORS_SUBTYPE_S_WEIGHT:
                    uid = new ThingUID(THING_TYPE_WEIGHT, bridgeHandler.getThing().getUID(),
                            "Weight_" + msg.getNodeId() + "_" + msg.getChildId());
                    break;
                case MYSENSORS_SUBTYPE_S_DISTANCE:
                    uid = new ThingUID(THING_TYPE_DISTANCE, bridgeHandler.getThing().getUID(),
                            "Distance_" + msg.getNodeId() + "_" + msg.getChildId());
                    break;
                case MYSENSORS_SUBTYPE_S_LIGHT_LEVEL:
                    uid = new ThingUID(THING_TYPE_LIGHT_LEVEL, bridgeHandler.getThing().getUID(),
                            "Light_level_" + msg.getNodeId() + "_" + msg.getChildId());
                    break;
            }
            if (uid != null) {
                Map<String, Object> properties = new HashMap<>(2);
                properties.put(PARAMETER_NODEID, "" + msg.getNodeId());
                properties.put(PARAMETER_CHILDID, "" + msg.getChildId());
                DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                        .withLabel("MySensors Device (" + msg.getNodeId() + ";" + msg.getChildId() + ")")
                        .withBridge(bridgeHandler.getThing().getUID()).build();
                thingDiscovered(result);

                logger.debug("Discovered device submitted");
            }
        }
    }

}
