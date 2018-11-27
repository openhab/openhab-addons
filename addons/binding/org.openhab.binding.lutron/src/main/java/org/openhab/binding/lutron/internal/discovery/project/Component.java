/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.discovery.project;

/**
 * A component of an input device in a Lutron system. Generally each component of
 * the device maps to a channel of the device thing.
 *
 * @author Allan Tong - Initial contribution
 */
public class Component {
    private Integer componentNumber;
    private String type;

    public Integer getComponentNumber() {
        return componentNumber;
    }

    public ComponentType getComponentType() {
        try {
            return ComponentType.valueOf(this.type);
        } catch (Exception e) {
            return null;
        }
    }
}
