/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.config;

/**
 * The {@link NetatmoThingConfiguration} is the base class for configuration
 * information held by devices and modules
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class NetatmoThingConfiguration {

    private String equipmentId;

    public String getEquipmentId() {
        // Bug #3891 : Netatmo API only works with lower case device/module ids
        return equipmentId.toLowerCase();
    }

}
