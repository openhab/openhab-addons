/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.squeezebox.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/***
 * Enumeration of the play states of a player.
 *
 * @author Patrik Gfeller - Initial contribution
 *
 */
@NonNullByDefault
enum SqueezeBoxPlayerPlayState {
    STOP,
    PLAY,
    PAUSE
}
