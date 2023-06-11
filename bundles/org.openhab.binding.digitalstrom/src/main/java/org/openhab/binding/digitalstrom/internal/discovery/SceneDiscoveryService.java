/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.digitalstrom.internal.discovery;

import static org.openhab.binding.digitalstrom.internal.DigitalSTROMBindingConstants.BINDING_ID;
import static org.openhab.binding.digitalstrom.internal.DigitalSTROMBindingConstants.GROUP_ID;
import static org.openhab.binding.digitalstrom.internal.DigitalSTROMBindingConstants.SCENE_ID;
import static org.openhab.binding.digitalstrom.internal.DigitalSTROMBindingConstants.ZONE_ID;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.openhab.binding.digitalstrom.internal.handler.BridgeHandler;
import org.openhab.binding.digitalstrom.internal.handler.SceneHandler;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.ApplicationGroup;
import org.openhab.binding.digitalstrom.internal.lib.structure.scene.InternalScene;
import org.openhab.binding.digitalstrom.internal.lib.structure.scene.constants.SceneEnum;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SceneDiscoveryService} discovers all digitalSTROM-scene of one supported scene-type. The scene-type has to
 * be given to the {@link #SceneDiscoveryService(BridgeHandler, ThingTypeUID)} as
 * {@link ThingTypeUID}. The supported {@link ThingTypeUID} can be found at {@link SceneHandler#SUPPORTED_THING_TYPES}
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class SceneDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(SceneDiscoveryService.class);
    private final BridgeHandler bridgeHandler;
    private final String sceneType;

    public static final int TIMEOUT = 10;

    /**
     * Creates a new {@link SceneDiscoveryService} for the given supportedThingType.
     *
     * @param bridgeHandler (must not be null)
     * @param supportedThingType (must not be null)
     * @throws IllegalArgumentException see {@link AbstractDiscoveryService#AbstractDiscoveryService(int)}
     */
    public SceneDiscoveryService(BridgeHandler bridgeHandler, ThingTypeUID supportedThingType)
            throws IllegalArgumentException {
        super(new HashSet<>(Arrays.asList(supportedThingType)), TIMEOUT, false);
        this.sceneType = supportedThingType.getId();
        this.bridgeHandler = bridgeHandler;
    }

    /**
     * Deactivates the {@link SceneDiscoveryService} and removes the {@link DiscoveryResult}s.
     */
    @Override
    public void deactivate() {
        logger.debug("deactivate discovery service for scene type {} remove thing tyspes {}", sceneType,
                super.getSupportedThingTypes());
        removeOlderResults(new Date().getTime());
    }

    @Override
    protected void startScan() {
        if (bridgeHandler != null) {
            if (bridgeHandler.getScenes() != null) {
                for (InternalScene scene : bridgeHandler.getScenes()) {
                    onSceneAddedInternal(scene);
                }
            }
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    private void onSceneAddedInternal(InternalScene scene) {
        logger.debug("{}", scene.getSceneType());
        if (scene.getSceneType().equals(sceneType)) {
            if (!ignoredScene(scene.getSceneID()) && !ignoreGroup(scene.getGroupID())) {
                ThingUID thingUID = getThingUID(scene);
                if (thingUID != null) {
                    ThingUID bridgeUID = bridgeHandler.getThing().getUID();
                    Map<String, Object> properties = new HashMap<>();
                    properties.put(ZONE_ID, scene.getZoneID());
                    properties.put(GROUP_ID, scene.getGroupID());
                    if (SceneEnum.containsScene(scene.getSceneID())) {
                        properties.put(SCENE_ID, SceneEnum.getScene(scene.getSceneID()).toString());
                    } else {
                        logger.debug("discovered scene: name '{}' with id {} have an invalid scene-ID",
                                scene.getSceneName(), scene.getID());
                    }
                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                            .withBridge(bridgeUID).withLabel(scene.getSceneName()).build();

                    thingDiscovered(discoveryResult);

                } else {
                    logger.debug("discovered unsupported scene: name '{}' with id {}", scene.getSceneName(),
                            scene.getID());
                }
            }
        }
    }

    private boolean ignoreGroup(Short groupID) {
        ApplicationGroup group = ApplicationGroup.getGroup(groupID);
        if (group != null) {
            switch (group) {
                case TEMPERATURE_CONTROL:
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

    private boolean ignoredScene(short sceneID) {
        switch (SceneEnum.getScene(sceneID)) {
            case INCREMENT:
            case DECREMENT:
            case STOP:
            case MINIMUM:
            case MAXIMUM:
            case DEVICE_ON:
            case DEVICE_OFF:
            case DEVICE_STOP:
            case AREA_1_INCREMENT:
            case AREA_1_DECREMENT:
            case AREA_1_STOP:
            case AREA_2_INCREMENT:
            case AREA_2_DECREMENT:
            case AREA_2_STOP:
            case AREA_3_INCREMENT:
            case AREA_3_DECREMENT:
            case AREA_3_STOP:
            case AREA_4_INCREMENT:
            case AREA_4_DECREMENT:
            case AREA_4_STOP:
            case AREA_STEPPING_CONTINUE:
            case ENERGY_OVERLOAD:
            case ALARM_SIGNAL:
            case AUTO_STANDBY:
            case ZONE_ACTIVE:
                return true;
            default:
                return false;
        }
    }

    private ThingUID getThingUID(InternalScene scene) {
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        ThingTypeUID thingTypeUID = new ThingTypeUID(BINDING_ID, sceneType);

        if (getSupportedThingTypes().contains(thingTypeUID)) {
            String thingSceneId = scene.getID();
            ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, thingSceneId);
            return thingUID;
        } else {
            return null;
        }
    }

    /**
     * Returns the ID of this {@link SceneDiscoveryService}.
     *
     * @return id of this service
     */
    public String getID() {
        return sceneType;
    }

    /**
     * Creates a {@link DiscoveryResult} of the given {@link InternalScene}, if the scene exists, if it is allowed to
     * use the scene
     * and if the scene is not one of the following scenes:
     * <ul>
     * <li>{@link SceneEnum#INCREMENT}</li>
     * <li>{@link SceneEnum#DECREMENT}</li>
     * <li>{@link SceneEnum#STOP}</li>
     * <li>{@link SceneEnum#MINIMUM}</li>
     * <li>{@link SceneEnum#MAXIMUM}</li>
     * <li>{@link SceneEnum#AUTO_OFF}</li>
     * <li>{@link SceneEnum#DEVICE_ON}</li>
     * <li>{@link SceneEnum#DEVICE_OFF}</li>
     * <li>{@link SceneEnum#DEVICE_STOP}</li>
     * <li>{@link SceneEnum#AREA_1_INCREMENT}</li>
     * <li>{@link SceneEnum#AREA_1_DECREMENT}</li>
     * <li>{@link SceneEnum#AREA_1_STOP}</li>
     * <li>{@link SceneEnum#AREA_2_INCREMENT}</li>
     * <li>{@link SceneEnum#AREA_2_DECREMENT}</li>
     * <li>{@link SceneEnum#AREA_2_STOP}</li>
     * <li>{@link SceneEnum#AREA_3_INCREMENT}</li>
     * <li>{@link SceneEnum#AREA_3_DECREMENT}</li>
     * <li>{@link SceneEnum#AREA_3_STOP}</li>
     * <li>{@link SceneEnum#AREA_4_INCREMENT}</li>
     * <li>{@link SceneEnum#AREA_4_DECREMENT}</li>
     * <li>{@link SceneEnum#AREA_4_STOP}</li>
     * <li>{@link SceneEnum#AREA_STEPPING_CONTINUE}</li>
     * <li>{@link SceneEnum#ENERGY_OVERLOAD}</li>
     * <li>{@link SceneEnum#ALARM_SIGNAL}</li>
     * <li>{@link SceneEnum#AUTO_STANDBY}</li>
     * <li>{@link SceneEnum#ZONE_ACTIVE}</li>
     * </ul>
     *
     * @param scene (must not be null)
     */
    public void onSceneAdded(InternalScene scene) {
        if (super.isBackgroundDiscoveryEnabled()) {
            onSceneAddedInternal(scene);
        }
    }

    /**
     * Removes the {@link DiscoveryResult} of the given {@link InternalScene}.
     *
     * @param scene (must not be null)
     */
    public void onSceneRemoved(InternalScene scene) {
        ThingUID thingUID = getThingUID(scene);
        if (thingUID != null) {
            thingRemoved(thingUID);
        }
    }
}
