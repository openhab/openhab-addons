/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.innogysmarthome.internal.client.entity;

import com.google.api.client.util.Key;

/**
 * The {@link Constant} entity is used for {@link Action}s.
 *
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class Constant {

    @Key("value")
    public Object value;

    public Constant(Object value) {
        this.value = value;
    }
}
