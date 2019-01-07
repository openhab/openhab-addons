/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.konnected.internal;

/**
 * Custom exception class to be thrown when number of retries is exceeded.
 *
 * @author Zachary Christiansen - Initial contribution
 */
@SuppressWarnings("serial")
public class KonnectedHttpRetryExceeded extends Exception {
    public KonnectedHttpRetryExceeded(String message, Throwable cause) {
        super(message, cause);
    }
}
