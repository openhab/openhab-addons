/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.canrelay.internal.protocol;

import static org.eclipse.smarthome.core.library.types.OnOffType.*;
import static org.openhab.binding.canrelay.internal.CanRelayBindingConstants.nodeAsString;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class wraps detected CanRelay light states at runtime. It represents all lights state of all CanRelays. Mind
 * that there might be up to 2 distinct instances of CanRelay on the CANBUS board, 1 for each floor, so this could be a
 * combination of lights from both. It internally calculates LightCache from the detected runtime data over CANBUS.
 *
 * CanRelay currently can have up to 30 individual lights connected to it. But this class understands CANBUS traffic
 * to translate from nodeIDs to output lights, so we internally have to assure consistency here - if 2 distinct nodeIDs
 * refer the same light, we want to keep it that way here and drop the "duplicate" mapping/light so that we do not end
 * up having duplicate lights at runtime we would need to link. They do represent 1 physical thing anyway, so from
 * openHAB concept all fine
 *
 * @author Lubos Housa - Initial Contribution
 */
public class LightStateCache {
    private static final Logger logger = LoggerFactory.getLogger(LightStateCache.class);

    // both these maps are concurrent since multiple threads could be updating and traversing it at the same time
    // for example discovery and received normal can message from light switch

    // map to keep already configured lights. Map from floor -> lightNum -> physical light (since light num is only
    // unique per CanRelay, but not per both)
    private final Map<Floor, Map<Integer, LightState>> lightNumMap = new HashMap<>();
    // this map not needed during startup, but used at runtime to keep nodeID -> physical light. nodeIDs are unique
    // across both CanRelays, so keeping simple 1-dimension map
    private final Map<Integer, LightState> nodeIdMap = new ConcurrentHashMap<>();

    public LightStateCache() {
        lightNumMap.put(Floor.GROUND, new ConcurrentHashMap<>());
        lightNumMap.put(Floor.FIRST, new ConcurrentHashMap<>());
    }

    /**
     * Adds new detected mapping at runtime to this configuration. It would internally check for existing mappings for
     * the same light number for the given floor and if such found, this mapping would be ignored to avoid duplicate
     * lights being configured as distinct things
     *
     * @param floor    the floor we are adding the mapping for
     * @param nodeID   nodeID of the mapping detected over CANBUS
     * @param lightNum light number light num detected over CANBUS
     */
    public void addMapping(Floor floor, int nodeID, int lightNum) {
        LightState lightState = lightNumMap.get(floor).get(lightNum);
        if (lightState != null) {
            logger.debug(
                    "Duplicate mapping found in floor {}. Lightnum {} is already configured as {}. Ignoring new mapping from nodeID {} to lightNum {}",
                    floor, lightNum, lightState, nodeAsString(nodeID), lightNum);
            return;
        }

        // ok so this is a new mapping, so add it
        lightState = new LightState(nodeID);
        lightNumMap.get(floor).put(lightNum, lightState);
        nodeIdMap.put(nodeID, lightState);
        logger.debug("New mapping set in floor {} from {} to light {}", floor, nodeAsString(nodeID), lightState);
    }

    /**
     * Update the light state represented by lightNum to either on or off as required
     *
     * @param floor    the floor we are adding the mapping for
     * @param lightNum lightNum to update
     * @param state    whether the respective light is currently detected as being ON in the CANBUS, OFF otherwise
     */
    public void updateLightUsingLightNum(Floor floor, int lightNum, @NonNull OnOffType state) {
        LightState lightState = lightNumMap.get(floor).get(lightNum);
        if (lightState == null) {
            logger.debug(
                    "Unable to update light state in floor {} for given lightnum {} since the respective mapping for it does not exist. Ignoring.",
                    floor, lightNum);
            return;
        }

        // ok found it, so just update it
        lightState.setState(state);
        logger.debug("Initial light state set: {}", lightState);
    }

    /**
     * Updates light state for a given nodeID using the in-passed desiredState and returns real new light state (if ON
     * or OFF, this is equal to desiredState, if desiredState is null, the returned value would be the "toggled" value
     * of current light state
     *
     * @param nodeID       nodeID to update the light state for
     * @param desiredState desired new state of the light or null if this is a "toggle" operation
     * @return new state of the light after updated
     */
    private OnOffType updateLight(@NonNull LightState lightState, OnOffType desiredState) {
        OnOffType oldState = lightState.getState(), newState = null;
        if (desiredState == null) {
            // in this case (most of the traffic) we first need to find the original value and toggle it
            newState = (oldState == ON) ? OFF : ON;
        } else {
            newState = desiredState;
        }
        lightState.setState(newState);
        logger.debug("Light {} found. Old state '{}'. Desired state '{}'. New detected state '{}'.",
                nodeAsString(lightState.getNodeID()), oldState, desiredState, newState);
        logger.debug("Light is now: {}", lightState);
        return newState;
    }

    /**
     * Updates light state for a given nodeID using the in-passed desiredState and returns real new light state
     *
     * @param nodeID       nodeID to update the light state for
     * @param desiredState desired new state of the light or null if this is a "toggle" operation
     * @return new state of the light after updated if a light was found for a given nodeID or null if no such light is
     *         known
     */
    public OnOffType updateLight(int nodeID, OnOffType desiredState) {
        LightState lightState = nodeIdMap.get(nodeID);
        if (lightState == null) {
            return null;
        }

        return updateLight(lightState, desiredState);
    }

    /**
     * Trigger to update all lights using the in-passed command for a given floor. This method would traverse through
     * all such nodes lights and call update for each of them and at the same time compose the reply as a map
     *
     * @param command command to run
     * @param floor   to run the command for
     * @return map of all OnOffType for all lights as detected (e.g. for a "toggle" aka null command)
     */
    public Map<Integer, OnOffType> updateAllLights(OnOffType command, Floor floor) {
        return nodeIdMap.values().stream().filter(light -> Floor.getFloorFromNodeID(light.getNodeID()).equals(floor))
                .collect(Collectors.toMap(lightState -> lightState.getNodeID(),
                        lightState -> updateLight(lightState, command)));
    }

    public void clear() {
        nodeIdMap.clear();
        lightNumMap.get(Floor.GROUND).clear();
        lightNumMap.get(Floor.FIRST).clear();
    }

    public boolean isEmpty() {
        return nodeIdMap.isEmpty();
    }

    /**
     * Return all lights in this cache
     *
     * @return collection of can relay lights
     */
    public Collection<LightState> getAllLights() {
        return nodeIdMap.values();
    }
}
