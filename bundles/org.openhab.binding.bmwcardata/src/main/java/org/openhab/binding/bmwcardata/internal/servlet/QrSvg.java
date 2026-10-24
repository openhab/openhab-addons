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
package org.openhab.binding.bmwcardata.internal.servlet;

import org.eclipse.jdt.annotation.NonNullByDefault;

import io.nayuki.qrcodegen.QrCode;

/**
 * Renders text as a QR code in SVG, to embed in an HTML page without serving an image.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
final class QrSvg {

    // the quiet zone around the code that scanners need, in modules
    static final int BORDER = 4;

    private QrSvg() {
        // utility class
    }

    /**
     * @param text the text to encode, e.g. a URL
     * @param label accessible name of the image, HTML escaped
     * @return an SVG element showing the QR code, dark modules on a white background so it scans in dark mode too
     */
    static String toSvg(String text, String label) {
        QrCode qr = QrCode.encodeText(text, QrCode.Ecc.MEDIUM);
        int size = qr.size + 2 * BORDER;
        StringBuilder path = new StringBuilder();
        for (int y = 0; y < qr.size; y++) {
            for (int x = 0; x < qr.size; x++) {
                if (qr.getModule(x, y)) {
                    path.append('M').append(x + BORDER).append(',').append(y + BORDER).append("h1v1h-1z");
                }
            }
        }
        return "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 " + size + " " + size
                + "\" shape-rendering=\"crispEdges\" role=\"img\" aria-label=\"" + label + "\">"
                + "<rect width=\"100%\" height=\"100%\" fill=\"#fff\"/><path d=\"" + path + "\" fill=\"#000\"/></svg>";
    }
}
