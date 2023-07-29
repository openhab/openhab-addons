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
package org.openhab.binding.lametrictime.internal.api.dto;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;

import org.openhab.binding.lametrictime.internal.api.impl.DataIcon;
import org.openhab.binding.lametrictime.internal.api.impl.FileIcon;
import org.openhab.binding.lametrictime.internal.api.impl.HTTPIcon;
import org.openhab.binding.lametrictime.internal.api.impl.KeyIcon;

/**
 * Class for managing the core icons.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class Icons {
    public static Icon key(String key) {
        return new KeyIcon(key);
    }

    public static Icon http(String uri) {
        return http(URI.create(uri));
    }

    public static Icon http(URI uri) {
        return new HTTPIcon(uri);
    }

    public static Icon path(Path path) {
        return new FileIcon(path);
    }

    public static Icon file(File file) {
        return new FileIcon(file);
    }

    public static Icon data(String mimeType, byte[] data) {
        return new DataIcon(mimeType, data);
    }

    // @formatter:off
    public static Icon dollar() { return key("i34"); }
    public static Icon gmail() { return key("i43"); }
    public static Icon confirm() { return key("i59"); }
    public static Icon goOut() { return key("a68"); }
    public static Icon dog() { return key("a76"); }
    public static Icon clock() { return key("a82"); }
    public static Icon smile() { return key("a87"); }
    public static Icon lightning() { return key("i95"); }
    public static Icon facebook() { return key("a128"); }
    public static Icon home() { return key("i96"); }
    public static Icon girl() { return key("a178"); }
    public static Icon stop() { return key("i184"); }
    public static Icon heart() { return key("a230"); }
    public static Icon fade() { return key("a273"); }
    public static Icon terminal() { return key("a315"); }
    public static Icon usa() { return key("a413"); }
    public static Icon switzerland() { return key("i469"); }
    public static Icon attention() { return key("i555"); }
    public static Icon theMatrix() { return key("a653"); }
    public static Icon pizza() { return key("i1324"); }
    public static Icon christmasTree() { return key("a1782"); }
    public static Icon night() { return key("a2285"); }
    public static Icon fireworks() { return key("a2867"); }
    public static Icon beer() { return key("i3253"); }
    public static Icon tetris() { return key("a3793"); }
    public static Icon halloween() { return key("a4033"); }
    public static Icon pacman() { return key("a4584"); }

    private Icons() {}
    // @formatter:on
}
