/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yeelight.internal.lib.device;

/**
 * @author Coaster Li - Initial contribution
 */
public class ColorFlowItem {
    public int duration;
    public int mode;
    public int value;
    public int brightness;

    @Override
    public String toString() {
        return "ColorFlowItem [duration=" + duration + ", mode=" + mode + ", value=" + value + ", brightness="
                + brightness + "]";
    }

}
