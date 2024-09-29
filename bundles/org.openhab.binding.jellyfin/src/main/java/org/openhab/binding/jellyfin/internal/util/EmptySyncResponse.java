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
package org.openhab.binding.jellyfin.internal.util;

import org.eclipse.jdt.annotation.NonNullByDefault;

import kotlin.Unit;

/**
 * The {@link EmptySyncResponse} util to consume util to consume sdk api calls with no content.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public class EmptySyncResponse extends SyncResponse<Unit> {
}
