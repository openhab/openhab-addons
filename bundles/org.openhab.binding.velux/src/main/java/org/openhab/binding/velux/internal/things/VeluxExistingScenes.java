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
package org.openhab.binding.velux.internal.things;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velux.internal.VeluxBindingConstants;
import org.openhab.binding.velux.internal.things.VeluxScene.SceneName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Combined set of scene informations provided by the <B>Velux</B> bridge,
 * which can be used for later interactions.
 * <P>
 * The following class access methods exist:
 * <UL>
 * <LI>{@link VeluxExistingScenes#isRegistered} for querying existence of a {@link VeluxScene},</LI>
 * <LI>{@link VeluxExistingScenes#register} for storing a {@link VeluxScene},</LI>
 * <LI>{@link VeluxExistingScenes#get} for retrieval of a {@link VeluxScene},</LI>
 * <LI>{@link VeluxExistingScenes#values} for retrieval of all {@link VeluxScene}s,</LI>
 * <LI>{@link VeluxExistingScenes#getNoMembers} for retrieval of the number of all {@link VeluxScene}s,</LI>
 * <LI>{@link VeluxExistingScenes#toString} for a descriptive string representation.</LI>
 * </UL>
 *
 * @see VeluxScene
 *
 * @author Guenther Schreiner - initial contribution.
 */
@NonNullByDefault
public class VeluxExistingScenes {
    private final Logger logger = LoggerFactory.getLogger(VeluxExistingScenes.class);

    // Type definitions, class-internal variables

    private Map<String, VeluxScene> existingScenesBySceneName;
    private int memberCount;

    // Constructor methods

    public VeluxExistingScenes() {
        existingScenesBySceneName = new ConcurrentHashMap<>();
        memberCount = 0;
        logger.trace("VeluxExistingScenes(constructor) done.");
    }

    // Class access methods

    public boolean isRegistered(SceneName sceneName) {
        logger.trace("isRegistered({}) returns {}.", sceneName,
                existingScenesBySceneName.containsKey(sceneName.toString()) ? "true" : "false");
        return existingScenesBySceneName.containsKey(sceneName.toString());
    }

    public boolean isRegistered(VeluxScene scene) {
        return isRegistered(scene.getName());
    }

    public boolean register(VeluxScene newScene) {
        if (isRegistered(newScene)) {
            logger.trace("register() ignoring scene {} as already known.", newScene);
            return false;
        }
        logger.trace("register() registering new scene {}.", newScene);
        existingScenesBySceneName.put(newScene.getName().toString(), newScene);
        memberCount++;
        return true;
    }

    public VeluxScene get(SceneName sceneName) {
        logger.trace("get({}) called.", sceneName);
        if (!isRegistered(sceneName)) {
            return VeluxScene.UNKNOWN;
        }
        return existingScenesBySceneName.getOrDefault(sceneName.toString(), VeluxScene.UNKNOWN);
    }

    public VeluxScene[] values() {
        return existingScenesBySceneName.values().toArray(new VeluxScene[0]);
    }

    public int getNoMembers() {
        logger.trace("getNoMembers() returns {}.", memberCount);
        return memberCount;
    }

    public String toString(boolean showSummary, String delimiter) {
        StringBuilder sb = new StringBuilder();

        if (showSummary) {
            sb.append(memberCount).append(" members: ");
        }
        for (VeluxScene scene : this.values()) {
            sb.append(scene.toString()).append(delimiter);
        }
        if (sb.lastIndexOf(delimiter) > 0) {
            sb.deleteCharAt(sb.lastIndexOf(delimiter));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return toString(true, VeluxBindingConstants.OUTPUT_VALUE_SEPARATOR);
    }
}
