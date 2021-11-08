/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.tapocontrol.internal.helpers;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * MimeEncoder
 *
 * @author K4CZP3R - Initial contribution
 */
@NonNullByDefault
public class MimeEncode {

    public byte[] encode(byte[] src) {
        return java.util.Base64.getMimeEncoder().encode(src);
    }

    public String encodeToString(byte[] src) {
        return java.util.Base64.getMimeEncoder().encodeToString(src);
    }

    public byte[] decode(byte[] src) {
        return java.util.Base64.getMimeDecoder().decode(src);
    }

    public byte[] decode(String src) {
        return java.util.Base64.getMimeDecoder().decode(src);
    }
}
