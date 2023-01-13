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
package org.openhab.binding.freeboxos.internal.api.airmedia.receiver;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.MediaAction;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.MediaType;

/**
 * The {@link AirMediaReceiverRequest} is the Java class used to map the "AirMediaReceiverRequest" structure used by the
 * sending request to an AirMedia receiver API
 *
 * https://dev.freebox.fr/sdk/os/airmedia/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AirMediaReceiverRequest {
    protected final MediaAction action;
    protected final MediaType mediaType;
    protected final String password;
    protected final int position;
    protected @Nullable String media;

    AirMediaReceiverRequest(String password, MediaAction action, MediaType type) {
        this.password = password;
        this.action = action;
        this.mediaType = type;
        this.position = 0;
    }

    AirMediaReceiverRequest(String password, MediaAction action, MediaType type, String media) {
        this(password, action, type);
        this.media = media;
    }
}
