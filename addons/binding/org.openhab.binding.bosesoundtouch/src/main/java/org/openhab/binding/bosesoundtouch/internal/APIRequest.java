/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch.internal;

/**
 * The {@link APIRequest} class is holding all OperationModes
 *
 * @author Thomas Traunbauer
 */
public enum APIRequest {
    VOLUME("volume"),
    PRESETS("presets"),
    NOW_PLAYING("now_playing"),
    ZONE("getZone"),
    BASS("bass"),
    SOURCES("sources");

    private String name;

    private APIRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}