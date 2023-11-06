/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.digitalstrom.internal;

import static org.openhab.binding.digitalstrom.internal.DigitalSTROMBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openhab.binding.digitalstrom.internal.discovery.DiscoveryServiceManager;
import org.openhab.binding.digitalstrom.internal.handler.BridgeHandler;
import org.openhab.binding.digitalstrom.internal.handler.CircuitHandler;
import org.openhab.binding.digitalstrom.internal.handler.DeviceHandler;
import org.openhab.binding.digitalstrom.internal.handler.SceneHandler;
import org.openhab.binding.digitalstrom.internal.handler.ZoneTemperatureControlHandler;
import org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager;
import org.openhab.binding.digitalstrom.internal.lib.manager.impl.ConnectionManagerImpl;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.constants.JSONApiResponseKeysEnum;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DigitalSTROMHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Michael Ochel - Initial contribution
 * @author Mathias Siegele - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.digitalstrom")
public class DigitalSTROMHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(DigitalSTROMHandlerFactory.class);
    private final Map<String, DiscoveryServiceManager> discoveryServiceManagers = new ConcurrentHashMap<>();

    private Map<ThingUID, BridgeHandler> bridgeHandlers;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return BridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)
                || SceneHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)
                || DeviceHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)
                || ZoneTemperatureControlHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)
                || CircuitHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID,
            ThingUID bridgeUID) {
        if (BridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            ThingUID dSSUID = getBridgeThingUID(thingTypeUID, thingUID, configuration);
            if (dSSUID != null) {
                return super.createThing(thingTypeUID, configuration, dSSUID, null);
            } else {
                logger.error("""
                        Can't generate thing UID for thing type {}\
                        , because digitalSTROM-Server is not reachable. Please check these points:
                        Are the server address and portnumber correct?
                        Is the server turned on?
                        Is the network configured correctly?\
                        """, thingTypeUID);
                return null;
            }
        }

        if (DeviceHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            ThingUID dsDeviceUID = getDeviceUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, dsDeviceUID, bridgeUID);
        }

        if (CircuitHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            ThingUID dsDeviceUID = getDeviceUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, dsDeviceUID, bridgeUID);
        }

        if (ZoneTemperatureControlHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            ThingUID zoneTempConUID = getZoneTemperatureControlUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, zoneTempConUID, bridgeUID);
        }

        if (SceneHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            ThingUID dsSceneUID = getSceneUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, dsSceneUID, bridgeUID);
        }
        throw new IllegalArgumentException(
                "The thing type " + thingTypeUID + " is not supported by the digitalSTROM binding.");
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (BridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            BridgeHandler handler = new BridgeHandler((Bridge) thing);
            if (bridgeHandlers == null) {
                bridgeHandlers = new HashMap<>();
            }
            bridgeHandlers.put(thing.getUID(), handler);
            DiscoveryServiceManager discoveryServiceManager = new DiscoveryServiceManager(handler);
            discoveryServiceManager.registerDiscoveryServices(bundleContext);
            discoveryServiceManagers.put(handler.getThing().getUID().getAsString(), discoveryServiceManager);
            return handler;
        }

        if (DeviceHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new DeviceHandler(thing);
        }

        if (CircuitHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new CircuitHandler(thing);
        }

        if (ZoneTemperatureControlHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new ZoneTemperatureControlHandler(thing);
        }

        if (SceneHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new SceneHandler(thing);
        }
        return null;
    }

    private ThingUID getDeviceUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration,
            ThingUID bridgeUID) {
        String id = (String) configuration.get(DEVICE_DSID);
        if (thingUID == null && id != null && !id.isBlank()) {
            return new ThingUID(thingTypeUID, bridgeUID, id);
        }
        return thingUID;
    }

    private ThingUID getZoneTemperatureControlUID(ThingTypeUID thingTypeUID, ThingUID thingUID,
            Configuration configuration, ThingUID bridgeUID) {
        if (thingUID == null) {
            Integer zoneID = ZoneTemperatureControlHandler.getZoneID(configuration, bridgeHandlers.get(bridgeUID));
            if (zoneID > ZoneTemperatureControlHandler.ZONE_ID_NOT_EXISTS) {
                return new ThingUID(thingTypeUID, bridgeUID, zoneID.toString());
            } else {
                switch (zoneID) {
                    case ZoneTemperatureControlHandler.ZONE_ID_NOT_EXISTS:
                        logger.error("Configured zone '{}' does not exist, please check your configuration.",
                                configuration.get(DigitalSTROMBindingConstants.ZONE_ID));
                        break;
                    case ZoneTemperatureControlHandler.ZONE_ID_NOT_SET:
                        logger.error("ZoneID is missing at your configuration.");
                        break;
                    case ZoneTemperatureControlHandler.BRIDGE_IS_NULL:
                        logger.error("Bridge is missing, can not check the zoneID.");
                        break;
                }
            }
        }
        return thingUID;
    }

    private ThingUID getSceneUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration,
            ThingUID bridgeUID) {
        if (thingUID != null) {
            return thingUID;
        }
        String sceneID = SceneHandler.getSceneID(configuration, bridgeHandlers.get(bridgeUID));
        switch (sceneID) {
            case SceneHandler.SCENE_WRONG:
                logger.error(
                        "Configured scene '{}' does not exist or can not be used, please check your configuration.",
                        configuration.get(DigitalSTROMBindingConstants.SCENE_ID));
                break;
            case SceneHandler.ZONE_WRONG:
                logger.error("Configured zone '{}' does not exist, please check your configuration.",
                        configuration.get(DigitalSTROMBindingConstants.ZONE_ID));
                break;
            case SceneHandler.GROUP_WRONG:
                logger.error("Configured group '{}' does not exist, please check your configuration.",
                        configuration.get(DigitalSTROMBindingConstants.GROUP_ID));
                break;
            case SceneHandler.NO_STRUC_MAN:
                logger.error("Waiting for building digitalSTROM model.");
                break;
            case SceneHandler.NO_SCENE:
                logger.error("No Scene-ID is set!");
                break;
            case SceneHandler.NO_BRIDGE:
                logger.error("No related bridge found!");
            default:
                return new ThingUID(thingTypeUID, bridgeUID, sceneID);
        }
        return thingUID;
    }

    private ThingUID getBridgeThingUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration) {
        if (thingUID != null) {
            return thingUID;
        }
        String dSID;
        String configValue = (String) configuration.get(DS_ID);
        if (configValue == null || configValue.isBlank()) {
            dSID = getDSSid(configuration);
            if (dSID != null) {
                configuration.put(DS_ID, dSID);
            }
        } else {
            dSID = configuration.get(DS_ID).toString();
        }
        if (dSID != null) {
            return new ThingUID(thingTypeUID, dSID);
        } else {
            return null;
        }
    }

    private String getDSSid(Configuration configuration) {
        String dSID = null;
        String hostConfigValue = (String) configuration.get(HOST);
        if (hostConfigValue != null && !hostConfigValue.isBlank()) {
            String host = hostConfigValue;
            String applicationToken = null;
            String user = null;
            String pw = null;

            String atConfigValue = (String) configuration.get(APPLICATION_TOKEN);
            if (atConfigValue != null && !atConfigValue.isBlank()) {
                applicationToken = configuration.get(APPLICATION_TOKEN).toString();
            }

            if (checkUserPassword(configuration)) {
                user = configuration.get(USER_NAME).toString();
                pw = configuration.get(PASSWORD).toString();
            }
            ConnectionManager connMan = new ConnectionManagerImpl(host, user, pw, applicationToken, false, true);
            Map<String, String> dsidMap = connMan.getDigitalSTROMAPI().getDSID(connMan.getSessionToken());
            if (dsidMap != null) {
                dSID = dsidMap.get(JSONApiResponseKeysEnum.DSID.getKey());
            }
        }
        return dSID;
    }

    private boolean checkUserPassword(Configuration configuration) {
        String userName = (String) configuration.get(USER_NAME);
        String password = (String) configuration.get(PASSWORD);
        return userName != null && !userName.isBlank() && password != null && !password.isBlank();
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof BridgeHandler) {
            String uid = thingHandler.getThing().getUID().getAsString();
            DiscoveryServiceManager discoveryServiceManager = discoveryServiceManagers.remove(uid);
            if (discoveryServiceManager != null) {
                discoveryServiceManager.unregisterDiscoveryServices(bundleContext);
            }
        }
    }
}
