/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hdpowerview.internal.api;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

/**
 * The position of a shade, as returned by the HD Power View HUB.
 *
 * @author Andy Lintner
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize
public class ShadePosition {

    @JsonSerialize(include = Inclusion.ALWAYS)
    public ShadePositionKind posKind1;

    @JsonSerialize(include = Inclusion.ALWAYS)
    public int position1;

    public static ShadePosition forPosition(int position) {
        return new ShadePosition(position, null);
    }

    public static ShadePosition forVane(int vane) {
        return new ShadePosition(null, vane);
    }

    ShadePosition(Integer position, Integer vane) {
        if (position != null) {
            posKind1 = ShadePositionKind.POSITION;
            position1 = position;
        } else if (vane != null) {
            posKind1 = ShadePositionKind.VANE;
            position1 = vane;
        }
    }

    ShadePosition() {

    }

    @JsonIgnore
    public int getPosition() {
        if (ShadePositionKind.POSITION.equals(posKind1)) {
            return position1;
        } else {
            return 0;
        }
    }

    @JsonIgnore
    public int getVane() {
        if (ShadePositionKind.VANE.equals(posKind1)) {
            return position1;
        } else {
            return 0;
        }
    }
}
