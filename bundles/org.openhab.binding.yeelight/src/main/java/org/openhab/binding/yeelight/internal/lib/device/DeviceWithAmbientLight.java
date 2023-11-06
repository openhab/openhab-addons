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
package org.openhab.binding.yeelight.internal.lib.device;

/**
 * Interface for devices with background light.
 *
 * @author Viktor Koop - Initial contribution
 */
public interface DeviceWithAmbientLight {
    void setBackgroundColor(int hue, int saturation, int duration);

    void setBackgroundBrightness(int brightness, int duration);

    void setBackgroundPower(boolean on, int intDuration);
}
