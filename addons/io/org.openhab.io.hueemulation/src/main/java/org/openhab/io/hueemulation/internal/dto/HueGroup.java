/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.hueemulation.internal.dto;

import java.util.Collections;
import java.util.List;

/**
 * Hue API group object
 *
 * @author Dan Cunningham - Initial contribution
 */
public class HueGroup {
    public AbstractHueState state;
    public String type = "LightGroup";
    public String name;
    public List<String> lights = Collections.emptyList();
    public AbstractHueState action;

    public HueGroup(String name, AbstractHueState action) {
        this.name = name;
        this.action = action;
    }
}
