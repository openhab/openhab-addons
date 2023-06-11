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
package org.openhab.binding.sensibo.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents a generic Sensibo controllable thing
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public abstract class Pod {
    protected String id;

    public String getId() {
        return id;
    }

    protected Pod(String id) {
        this.id = id;
    }
}
