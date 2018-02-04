/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hyperion.internal.protocol.ng;

/**
 * The {@link ComponentState} is a POJO for a component state in the Hyperion.ng server.
 *
 * @author Daniel Walters - Initial contribution
 */
public class ComponentState {

    private String component;
    private boolean state;

    public void setComponent(String component) {
        this.component = component;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public String getComponent() {
        return component;
    }

    public boolean getState() {
        return state;
    }

}
