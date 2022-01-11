/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
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
