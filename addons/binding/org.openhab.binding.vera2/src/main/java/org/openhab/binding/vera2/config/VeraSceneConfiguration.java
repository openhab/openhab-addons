/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vera2.config;

import static org.openhab.binding.vera2.VeraBindingConstants.SCENE_CONFIG_ID;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * The {@link VeraSceneConfiguration} class defines the model for a scene configuration.
 *
 * @author Dmitriy Ponomarev
 */
public class VeraSceneConfiguration {
    private String sceneId;

    public String getSceneId() {
        return sceneId;
    }

    public void setSceneId(String sceneId) {
        this.sceneId = sceneId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append(SCENE_CONFIG_ID, this.getSceneId()).toString();
    }
}
