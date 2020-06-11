/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.nikobus.internal.utils;

import java.util.concurrent.Future;

/**
 * The {@link Utils} class defines commonly used utility functions.
 *
 * @author Boris Krivonog - Initial contribution
 */
public class Utils {
    public static void cancel(Future<?> future) {
        if (future != null) {
            future.cancel(true);
        }
    }
}
