/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.util;

import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This is a special exception that can be thrown by Callable instances
 * used by a RetryFuture.
 *
 * @author Connor Petty - Initial contribution
 *
 */
@NonNullByDefault
public class RetryException extends Exception {

    private static final long serialVersionUID = 8512275408512109328L;
    final long delay;
    final TimeUnit unit;

    public RetryException(long delay, TimeUnit unit) {
        this.delay = delay;
        this.unit = unit;
    }
}
