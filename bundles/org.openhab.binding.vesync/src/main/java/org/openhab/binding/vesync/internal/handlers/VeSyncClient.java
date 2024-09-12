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
package org.openhab.binding.vesync.internal.handlers;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.vesync.internal.dto.requests.VeSyncAuthenticatedRequest;
import org.openhab.binding.vesync.internal.exceptions.AuthenticationException;
import org.openhab.binding.vesync.internal.exceptions.DeviceUnknownException;

/**
 * The {@link VeSyncClient} is TBC.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public interface VeSyncClient {
    String reqV2Authorized(final String url, final String macId, final VeSyncAuthenticatedRequest requestData)
            throws AuthenticationException, DeviceUnknownException;
}
