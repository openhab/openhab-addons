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
package org.openhab.binding.gme.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Response envelope returned by the GME RequestData API.
 *
 * @author Andrea Riela - Initial contribution
 */
@NonNullByDefault
public class GmeRequestDataResponse {

    public long requestId;
    public @Nullable String formatType;
    public @Nullable String resultRequest;
    public @Nullable String contentResponse;
}
