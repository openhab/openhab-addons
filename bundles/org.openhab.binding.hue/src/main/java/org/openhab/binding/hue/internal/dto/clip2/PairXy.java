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
package org.openhab.binding.hue.internal.dto.clip2;

/**
 * DTO that contains an x and y pair of floats.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
public class PairXy {
    private float x;
    private float y;

    public float[] getXY() {
        return new float[] { x, y };
    }

    public void setXY(float[] xy) {
        x = xy.length > 0 ? xy[0] : 0f;
        y = xy.length > 1 ? xy[1] : 0f;
    }
}
