/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.internal.config;

import org.openhab.binding.zoneminder.internal.RefreshPriority;

/**
 * base class containing Configuration in openHAB
 *
 * @author Martin S. Eskildsen - Initial contribution
 */
public abstract class ZoneMinderConfig {
    public abstract String getConfigId();

    protected RefreshPriority getRefreshPriorityEnum(String configValue) {
        RefreshPriority priority = RefreshPriority.fromConfigValue(configValue);
        if (priority != RefreshPriority.UNKNOWN) {
            return priority;
        }
        return RefreshPriority.UNKNOWN;
    }
}
