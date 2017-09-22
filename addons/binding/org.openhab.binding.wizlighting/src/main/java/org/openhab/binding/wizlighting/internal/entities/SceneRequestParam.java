/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
