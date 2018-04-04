/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tesla.internal.throttler;

/**
 * The {@link TimeProvider} provides time stamps
 *
 * @author Karel Goderis - Initial contribution
 */
public interface TimeProvider {
    public static final TimeProvider SYSTEM_PROVIDER = new TimeProvider() {
        @Override
        public long getCurrentTimeInMillis() {
            return System.currentTimeMillis();
        }
    };

    public long getCurrentTimeInMillis();
}
