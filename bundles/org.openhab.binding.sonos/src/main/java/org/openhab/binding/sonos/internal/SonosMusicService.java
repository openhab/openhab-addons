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
package org.openhab.binding.sonos.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link SonosMusicService} is a datastructure to describe a Sonos music service
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class SonosMusicService {

    private String id;
    private String name;
    private @Nullable Integer type;

    public SonosMusicService(String id, String name, @Nullable Integer type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public SonosMusicService(String id, String name) {
        this(id, name, null);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public @Nullable Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
