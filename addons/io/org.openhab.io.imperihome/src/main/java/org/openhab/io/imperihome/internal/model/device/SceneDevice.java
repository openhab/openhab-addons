/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.imperihome.internal.model.device;

import org.eclipse.smarthome.core.items.Item;

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
