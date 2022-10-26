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
package org.openhab.binding.hdpowerview.internal.gen3.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Scene SSE event object as supplied an HD PowerView hub of Generation 3.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class SceneEvent {
    private int id;
    // private @NonNullByDefault({}) String evt;
    // private @NonNullByDefault({}) String isoDate;
    private @NonNullByDefault({}) Scene3 scene;

    public int getId() {
        return id;
    }

    public Scene3 getScene() {
        return scene;
    }
}
