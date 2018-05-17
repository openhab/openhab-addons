/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smappee.internal;

/**
 * Generic class ThingProperty, A tuple of Key and Value
 *
 * @param <X> the generic type
 * @param <Y> the generic type
 *
 * @author Niko Tanghe - Initial contribution
 */
public class ThingProperty extends Object {

    public final String key;
    public final Object value;

    public ThingProperty(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return "(" + key + " = " + value + ")";
    }

}