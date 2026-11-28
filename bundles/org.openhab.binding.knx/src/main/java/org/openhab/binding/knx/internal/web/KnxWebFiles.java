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

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The fixed table of the page's files: the only answers {@link KnxWebServlet} gives. A request never names a
 * resource; it can only pick an entry of the table by its exact path.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class KnxWebFiles {
    static final String HTML = "text/html; charset=utf-8";
    static final String CSS = "text/css; charset=utf-8";
    static final String JAVASCRIPT = "text/javascript; charset=utf-8";
    static final String SVG = "image/svg+xml";

    static final String INDEX = "index.html";

    /**
     * @param path the path below the page's address, as the folder {@code web/} holds it
     * @param resource the name of the file in the jar
     */
    record Entry(String path, String resource, String contentType) {
    }

    static final List<Entry> ENTRIES = List.of( //
            new Entry(INDEX, "web/index.html", HTML), //
            new Entry("app.css", "web/app.css", CSS), //
            new Entry(KnxTile.IMAGE, "web/" + KnxTile.IMAGE, SVG), //
            new Entry("js/api.js", "web/js/api.js", JAVASCRIPT), //
            new Entry("js/auth.js", "web/js/auth.js", JAVASCRIPT), //
            new Entry("js/core.js", "web/js/core.js", JAVASCRIPT), //
            new Entry("js/dom.js", "web/js/dom.js", JAVASCRIPT), //
            new Entry("js/finder.js", "web/js/finder.js", JAVASCRIPT), //
            new Entry("js/layout.js", "web/js/layout.js", JAVASCRIPT), //
            new Entry("js/live.js", "web/js/live.js", JAVASCRIPT), //
            new Entry("js/main.js", "web/js/main.js", JAVASCRIPT), //
            new Entry("js/model.js", "web/js/model.js", JAVASCRIPT), //
            new Entry("js/panel.js", "web/js/panel.js", JAVASCRIPT), //
            new Entry("js/pkce.js", "web/js/pkce.js", JAVASCRIPT), //
            new Entry("js/plan.js", "web/js/plan.js", JAVASCRIPT), //
            new Entry("js/properties.js", "web/js/properties.js", JAVASCRIPT), //
            new Entry("js/proposal.js", "web/js/proposal.js", JAVASCRIPT), //
            new Entry("js/router.js", "web/js/router.js", JAVASCRIPT), //
            new Entry("js/rows.js", "web/js/rows.js", JAVASCRIPT), //
            new Entry("js/sha256.js", "web/js/sha256.js", JAVASCRIPT), //
            new Entry("js/sidebar.js", "web/js/sidebar.js", JAVASCRIPT), //
            new Entry("js/shell.js", "web/js/shell.js", JAVASCRIPT), //
            new Entry("js/splitter.js", "web/js/splitter.js", JAVASCRIPT), //
            new Entry("js/sse.js", "web/js/sse.js", JAVASCRIPT), //
            new Entry("js/stream.js", "web/js/stream.js", JAVASCRIPT), //
            new Entry("js/table.js", "web/js/table.js", JAVASCRIPT), //
            new Entry("js/texts.js", "web/js/texts.js", JAVASCRIPT), //
            new Entry("js/tree.js", "web/js/tree.js", JAVASCRIPT), //
            new Entry("js/workplace.js", "web/js/workplace.js", JAVASCRIPT), //
            new Entry("js/views/buildings.js", "web/js/views/buildings.js", JAVASCRIPT), //
            new Entry("js/views/explorer.js", "web/js/views/explorer.js", JAVASCRIPT), //
            new Entry("js/views/groupaddresses.js", "web/js/views/groupaddresses.js", JAVASCRIPT), //
            new Entry("js/views/monitor.js", "web/js/views/monitor.js", JAVASCRIPT), //
            new Entry("js/views/monitorsend.js", "web/js/views/monitorsend.js", JAVASCRIPT), //
            new Entry("js/views/objects.js", "web/js/views/objects.js", JAVASCRIPT), //
            new Entry("js/views/project.js", "web/js/views/project.js", JAVASCRIPT), //
            new Entry("js/views/topology.js", "web/js/views/topology.js", JAVASCRIPT), //
            new Entry("js/views/wizard.js", "web/js/views/wizard.js", JAVASCRIPT), //
            new Entry("js/views/wizardplan.js", "web/js/views/wizardplan.js", JAVASCRIPT), //
            new Entry("js/views/wizardrun.js", "web/js/views/wizardrun.js", JAVASCRIPT));

    private record Content(byte[] bytes, String etag) {
    }

    private final Map<String, Entry> byPath = new HashMap<>();
    private final Map<Entry, Content> contents = new HashMap<>();
    private final List<Entry> missing = new ArrayList<>();

    /** Loads every entry's file once; an entry whose file cannot be read is kept apart and is never found. */
    KnxWebFiles(List<Entry> entries) {
        for (Entry entry : entries) {
            byte[] bytes = read(entry.resource());
            if (bytes == null) {
                missing.add(entry);
                continue;
            }
            byPath.put(entry.path(), entry);
            contents.put(entry, new Content(bytes, etagOf(bytes)));
        }
    }

    /** {@return the entries whose file is not in the jar} */
    List<Entry> missing() {
        return List.copyOf(missing);
    }

    /**
     * @param pathInfo the request's path below the servlet's alias, as the servlet container gives it
     * @return the entry with exactly this path, the start page for {@code /} and the empty path
     */
    @Nullable
    Entry find(@Nullable String pathInfo) {
        if (pathInfo == null) {
            return null;
        }
        if (pathInfo.isEmpty() || "/".equals(pathInfo)) {
            return byPath.get(INDEX);
        }
        return pathInfo.charAt(0) == '/' ? byPath.get(pathInfo.substring(1)) : null;
    }

    /** @throws IllegalArgumentException for an entry that {@link #find} never gives */
    byte[] load(Entry entry) {
        return content(entry).bytes().clone();
    }

    /** {@return the first 16 hex digits of the SHA-256 of the file, in quotes} */
    String etag(Entry entry) {
        return content(entry).etag();
    }

    private Content content(Entry entry) {
        Content content = contents.get(entry);
        if (content == null) {
            throw new IllegalArgumentException("not a file of the page: " + entry.path());
        }
        return content;
    }

    private static byte @Nullable [] read(String resource) {
        try (InputStream in = KnxWebFiles.class.getResourceAsStream("/" + resource)) {
            return in == null ? null : in.readAllBytes();
        } catch (IOException e) {
            return null;
        }
    }

    private static String etagOf(byte[] bytes) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            return '"' + HexFormat.of().formatHex(digest, 0, 8) + '"';
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is part of every Java runtime", e);
        }
    }
}
