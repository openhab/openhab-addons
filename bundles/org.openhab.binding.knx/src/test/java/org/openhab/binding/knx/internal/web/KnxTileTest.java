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

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.web.KnxWebFiles.Entry;
import org.openhab.core.ui.tiles.Tile;

/**
 * Tests {@link KnxTile}: its four values, and that the image it names is a file the page's servlet gives.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KnxTileTest {
    private final Tile tile = new KnxTile();

    @Test
    void theFourValues() {
        assertEquals("KNX", tile.getName());
        assertEquals("/knx/", tile.getUrl());
        assertEquals("/knx/img/tile.svg", tile.getImageUrl());
        assertNull(tile.getOverlay());
    }

    /** The component annotation is gone at run time; what the build makes of it is what the framework reads. */
    @Test
    void theBuildDeclaresItAsATileService() throws IOException {
        String name = "/OSGI-INF/" + KnxTile.class.getName() + ".xml";

        try (InputStream in = KnxTile.class.getResourceAsStream(name)) {
            String description = new String(Objects.requireNonNull(in, name).readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(description.contains("<provide interface=\"" + Tile.class.getName() + "\"/>"), description);
            assertTrue(description.contains("<implementation class=\"" + KnxTile.class.getName() + "\"/>"),
                    description);
        }
    }

    /** Main UI draws the tile on any of its paths, so an address relative to that path would break the image. */
    @Test
    void theLinkIsThePagesAddressFromTheRootOfTheServer() {
        assertEquals(KnxWebServlet.ALIAS + "/", tile.getUrl());
        assertTrue(tile.getUrl().startsWith("/"), tile.getUrl());
        assertFalse(tile.getImageUrl().contains(".."), tile.getImageUrl());
        assertTrue(tile.getImageUrl().startsWith(tile.getUrl()));
    }

    @Test
    void theImageIsAFileOfThePageWithTheSvgType() throws IOException {
        String path = tile.getImageUrl().substring(tile.getUrl().length());
        KnxWebFiles files = new KnxWebFiles(KnxWebFiles.ENTRIES);

        Entry entry = Objects.requireNonNull(files.find("/" + path), path);

        assertEquals("image/svg+xml", entry.contentType());
        assertEquals("web/" + path, entry.resource());
        assertNotNull(KnxTile.class.getResource("/" + entry.resource()));
        assertArrayEquals(Files.readAllBytes(KnxWebFilesTest.WEB.resolve(path)), files.load(entry));
    }

    @Test
    void theImageIsADrawingWithoutTextAndWithoutAnythingFetched() throws IOException {
        String svg = Files.readString(KnxWebFilesTest.WEB.resolve(KnxTile.IMAGE), StandardCharsets.UTF_8);

        assertTrue(svg.startsWith("<svg " + WebSourceRulesTest.SVG_NAMESPACE + " viewBox=\"0 0 350 213\">"), svg);
        for (String part : new String[] { "<text", "<title", "<image", "<use", "<style", "<script", "href" }) {
            assertFalse(svg.contains(part), part);
        }
    }
}
