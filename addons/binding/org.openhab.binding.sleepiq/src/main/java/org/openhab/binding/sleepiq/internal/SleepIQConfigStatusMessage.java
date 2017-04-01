/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sleepiq.internal;

/**
 * The {@link SleepIQConfigStatusMessage} defines the keys to be used for configuration status messages.
 *
 * @author Gregory Moyer - Initial contribution
 *
 */
public enum SleepIQConfigStatusMessage {

    USERNAME_MISSING("missing-username-configuration"),
    PASSWORD_MISSING("missing-password-configuration");

    private String messageKey;

    SleepIQConfigStatusMessage(final String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
