/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.hueemulation.internal.dto;

/**
 * Hue API state object
 *
 * @author David Graeff - Initial contribution
 *
 */
public class AbstractHueState {
    public boolean reachable = true;
    public String mode = "homeautomation";

    public static enum AlertEnum {
        none,
        /** flashes light once */
        select,
        /** flashes repeatedly for 10 seconds. */
        lselect
    }

    public String alert = AlertEnum.none.name();
}
