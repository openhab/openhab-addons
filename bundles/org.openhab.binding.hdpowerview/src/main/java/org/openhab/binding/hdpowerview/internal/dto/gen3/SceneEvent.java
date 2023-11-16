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
package org.openhab.binding.hdpowerview.internal.dto.gen3;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * DTO for scene SSE event object as supplied an HD PowerView Generation 3 Gateway.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class SceneEvent {
    private int id;
    private @NonNullByDefault({}) Scene scene;

    public int getId() {
        return id;
    }

    public Scene getScene() {
        return scene;
    }
}
