/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kodi.internal.config;

/**
 * Channel configuration from openHab.
 *
 * @author Christoph Weitkamp - Added channels for opening PVR TV or Radio streams
 * 
 */

public class KodiChannelConfig {
    private String group;

    public String getGroup() {
        return group;
    }

    public void setGroup(final String group) {
        this.group = group;
    }
}
