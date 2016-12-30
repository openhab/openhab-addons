/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solarlog.handler;

/**
 * The {@link SolarLogChannelConfig} class defines a structure for a
 * Channel config. It essentially maps the JSON objects (which are index based)
 * to their corresponding channel. It also allows to set the data type of the item.
 *
 * @author Johann Richard
 */
public class SolarLogChannelConfig {
    public SolarLogChannelConfig(String id, String index, String type) {
        this.id = id;
        this.index = index;
        this.type = type;
    }

    String id;
    String index;
    String type;
}
