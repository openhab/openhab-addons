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
import org.openhab.binding.netatmo.internal.messages.NetatmoModule;

/**
 * {@link NetatmoModuleHandler} is the handler for a Netatmo module (depending
 * upon a device
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 * @author Andreas Brenk - OH1 version
 *
 */
public class NetatmoModuleHandler extends AbstractEquipment {

    private NetatmoModule module;

    public NetatmoModuleHandler(Thing thing) {
        super(thing);
    }

    @Override
    public AbstractDevice getEquipment() {
        if (module == null) {
            String configModuleId = (String) getConfig().get(NetatmoBindingConstants.MODULE_ID);
            for (NetatmoModule m : getNetatmoBridgeHandler().deviceList.getModules()) {
                if (m.getId().equals(configModuleId)) {
                    module = m;
                }
            }
        }
        return this.module;
    }

    @Override
    public void dispose() {
        super.dispose();
        module = null;
    }

}
