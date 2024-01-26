/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static java.util.Base64.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * MimeEncoder
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class MimeEncode {

    public byte[] encode(byte[] src) {
        return getMimeEncoder().encode(src);
    }

    public String encodeToString(byte[] src) {
        return getMimeEncoder().encodeToString(src);
    }

    public byte[] decode(byte[] src) {
        return getMimeDecoder().decode(src);
    }

    public byte[] decode(String src) {
        return getMimeDecoder().decode(src);
    }
}
