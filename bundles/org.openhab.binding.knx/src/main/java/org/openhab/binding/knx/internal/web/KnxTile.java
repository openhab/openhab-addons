/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.knx.internal.web;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ui.tiles.Tile;
import org.osgi.service.component.annotations.Component;

/**
 * The link to the page that Main UI lists under "Other Apps".
 *
 * @author Stamate Viorel - Initial contribution
 */
@Component(service = Tile.class)
@NonNullByDefault
public class KnxTile implements Tile {
    static final String IMAGE = "img/tile.svg";

    private static final String URL = KnxWebServlet.ALIAS + "/";

    @Override
    public String getName() {
        return "KNX";
    }

    @Override
    public String getUrl() {
        return URL;
    }

    @Override
    public @Nullable String getOverlay() {
        return null;
    }

    @Override
    public String getImageUrl() {
        return URL + IMAGE;
    }
}
