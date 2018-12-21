/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.veluxklf200.internal.components;

/**
 * Representation of a Scene (Program) as defined on the KLF 200.
 *
 * @author MFK - Initial Contribution
 */
public class VeluxScene {

    /** The scene id. */
    private int sceneId;

    /** The scene name. */
    private String sceneName;

    /**
     * Instantiates a new velux scene.
     *
     * @param sceneId
     *                      the scene id
     * @param sceneName
     *                      the scene name
     */
    public VeluxScene(int sceneId, String sceneName) {
        this.sceneId = sceneId;
        this.sceneName = sceneName;
    }

    /**
     * Gets the scene id.
     *
     * @return the scene id
     */
    public int getSceneId() {
        return sceneId;
    }

    /**
     * Gets the scene name.
     *
     * @return the scene name
     */
    public String getSceneName() {
        return sceneName;
    }
}
