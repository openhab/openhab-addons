/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.config;

/**
 * The {@link NetatmoDeviceConfiguration} is responsible for holding configuration
 * informations needed to access a Netatmo Device
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class NetatmoDeviceConfiguration {
    private String equipmentId;
    public long refreshInterval;

    public String getEquipmentId() {
        // Bug #3891 : Netatmo API only works with lower case device/module ids
        return equipmentId.toLowerCase();
    }
}
