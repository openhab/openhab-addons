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
package org.openhab.binding.hue.internal;

import java.util.List;

/**
 * Detailed group information.
 *
 * @author Q42 - Initial contribution
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 */
public class FullGroup extends Group {
    private State action;
    private List<String> lights;

    FullGroup() {
    }

    /**
     * Returns the last sent state update to the group.
     * This does not have to reflect the current state of the group.
     *
     * @return last state update
     */
    public State getAction() {
        return action;
    }

    /**
     * Returns a list of the lights in the group.
     *
     * @return lights in the group
     */
    public List<HueObject> getLights() {
        return Util.idsToLights(lights);
    }
}
