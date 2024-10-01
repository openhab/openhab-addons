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
package org.openhab.binding.pushbullet.internal.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PushbulletAuthenticationException} represents a Pushbullet authentication exception
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class PushbulletAuthenticationException extends PushbulletApiException {
    private static final long serialVersionUID = 1L;

    public PushbulletAuthenticationException(String message) {
        super(message);
    }
}
