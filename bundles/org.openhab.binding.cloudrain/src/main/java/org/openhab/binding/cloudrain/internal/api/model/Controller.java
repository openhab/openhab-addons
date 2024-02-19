/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cloudrain.internal.api.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Controller} class represents Cloudrain controller devices
 *
 * @author Till Koellmann - Initial contribution
 */
@NonNullByDefault
public class Controller extends CloudrainAPIItem {

    /**
     * Create a new Controller with the required attributes. Useful for test implementations. Typically objects of
     * this type will be created through reflection by the GSON library when parsing the JSON response of the API
     *
     * @param controllerId the ID of the controller
     * @param controllerName the name of the controller
     */
    public Controller(String controllerId, String controllerName) {
        super(controllerId, controllerName);
    }
}
