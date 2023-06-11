/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

    public <T extends AbstractHueState> T as(Class<T> type) throws ClassCastException {
        return type.cast(this);
    }
}
