/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.wizlighting.internal.entities;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;

/**
 * This POJO represents one Scene Request Param
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
public class SceneRequestParam extends DimmingRequestParam {
    private int sceneId;

    public SceneRequestParam(Command command) {
        super(100);
        if (command instanceof StringType) {
            this.setSceneId(Integer.parseInt(command.toString()));
        }
    }

    public int getSceneId() {
        return sceneId;
    }

    public void setSceneId(int sceneId) {
        this.sceneId = sceneId;
    }

}
