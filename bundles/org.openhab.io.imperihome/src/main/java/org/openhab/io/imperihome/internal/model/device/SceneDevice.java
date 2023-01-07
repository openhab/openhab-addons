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
package org.openhab.io.imperihome.internal.model.device;

import org.openhab.core.items.Item;

/**
 * Scene activation device.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class SceneDevice extends AbstractDevice {

    public SceneDevice(Item item) {
        super(DeviceType.SCENE, item);
    }
}
