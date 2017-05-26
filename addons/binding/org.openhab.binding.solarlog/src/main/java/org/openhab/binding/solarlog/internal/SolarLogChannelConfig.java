/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solarlog.internal;

/**
 * The {@link SolarLogChannelConfig} class defines a structure for a
 * Channel config. It essentially maps the JSON objects (which are index based)
 * to their corresponding channel. It also allows to set the data type of the item.
 *
 * @author Johann Richard
 */
public class SolarLogChannelConfig {
    public SolarLogChannelConfig(String id, String index, String type) {
        this.setId(id);
        this.setIndex(index);
        this.setType(type);
    }

    /**
     * @return the index
     */
    public String getIndex() {
        return index;
    }
    /**
     * @param index the index to set
     */
    public void setIndex(String index) {
        this.index = index;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    private String id;
    private String index;
    private String type;
}
