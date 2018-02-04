/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hyperion.internal.protocol.ng;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Component} is a POJO for a component in the Hyperion.ng server.
 *
 * @author Daniel Walters - Initial contribution
 */
public class Component {

    @SerializedName("name")
    private String name;

    @SerializedName("enabled")
    private boolean enabled;

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
