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
package org.openhab.binding.melcloud.internal.exceptions;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Signals that the MELCloud Home OAuth login flow was rejected or could not be completed, as opposed
 * to a generic network/server failure.
 *
 * <p>
 * {@link org.openhab.binding.melcloud.internal.home.handler.MelCloudHomeAccountHandler} treats this exception
 * differently from a plain {@link MelCloudCommException}: it maps to
 * {@link org.openhab.core.thing.ThingStatusDetail#CONFIGURATION_ERROR} (the user's credentials or the auth flow
 * itself are the problem) rather than {@link org.openhab.core.thing.ThingStatusDetail#COMMUNICATION_ERROR} (a
 * transient network/server problem, worth retrying).
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MelCloudHomeAuthException extends MelCloudCommException {

    private static final long serialVersionUID = 1L;

    public MelCloudHomeAuthException(String message) {
        super(message);
    }

    public MelCloudHomeAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
