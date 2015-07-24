/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler;

import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.netatmo.NetatmoBindingConstants;
import org.openhab.binding.netatmo.internal.messages.AbstractDevice;
import org.openhab.binding.netatmo.internal.messages.NetatmoDevice;

/**
 * {@link NetatmoDeviceHandler} is the handler for a Netatmo device (eg Weather
 * Station) *
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 * @author Andreas Brenk - OH1 version
 *
 */
public class NetatmoDeviceHandler extends AbstractEquipment {

    private NetatmoDevice device;

    public NetatmoDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public AbstractDevice getEquipment() {
        if (device == null) {
            String configDeviceId = (String) getConfig().get(NetatmoBindingConstants.DEVICE_ID);
            for (NetatmoDevice d : getNetatmoBridgeHandler().deviceList.getDevices()) {
                if (d.getId().equals(configDeviceId)) {
                    device = d;
                }
            }
        }
        return this.device;
    }

    @Override
    public void dispose() {
        super.dispose();
        device = null;
    }

}
