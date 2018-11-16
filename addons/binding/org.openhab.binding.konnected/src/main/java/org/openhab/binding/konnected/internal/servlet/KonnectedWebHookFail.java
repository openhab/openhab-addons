/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.konnected.internal.servlet;

/**
 * Custom exception class to be thrown by servlet when unable to start.
 *
 * @author Zachary Christiansen - Initial contribution
 */
@SuppressWarnings("serial")
public class KonnectedWebHookFail extends Exception {
    public KonnectedWebHookFail(String message, Throwable cause) {
        super(message, cause);
    }
}
