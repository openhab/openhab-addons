/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hdpowerview.internal.api;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;

/**
 * A shade type, as returned by the HD Power View Hub. By all rights, this should
 * be an enum. Jackson bug #960 prevents that.
 *
 * @author Andy Lintner
 */
public class ShadePositionKind {

    public static ShadePositionKind POSITION = new ShadePositionKind(1);
    public static ShadePositionKind VANE = new ShadePositionKind(3);

    private final int key;

    ShadePositionKind(int key) {
        this.key = key;
    }

    @JsonValue
    public int getKey() {
        return key;
    }

    @JsonCreator
    public static ShadePositionKind create(int key) {
        if (key == 1) {
            return ShadePositionKind.POSITION;
        } else if (key == 3) {
            return ShadePositionKind.VANE;
        } else {
            return null;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + key;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ShadePositionKind other = (ShadePositionKind) obj;
        if (key != other.key) {
            return false;
        }
        return true;
    }
}
