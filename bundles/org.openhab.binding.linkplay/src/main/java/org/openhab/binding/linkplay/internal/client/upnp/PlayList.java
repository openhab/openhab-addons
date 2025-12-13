/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.linkplay.internal.client.upnp;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents a single playlist entry in a LinkPlay PlayQueue
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class PlayList {
    private String name = "";
    private @Nullable PlayListInfo listInfo;
    private String rawXml = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public @Nullable PlayListInfo getListInfo() {
        return listInfo;
    }

    public void setListInfo(PlayListInfo listInfo) {
        this.listInfo = listInfo;
    }

    public String getRawXml() {
        return rawXml;
    }

    public void setRawXml(String rawXml) {
        this.rawXml = rawXml;
    }
}
