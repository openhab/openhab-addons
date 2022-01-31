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
package org.openhab.binding.freebox.internal.api.model;

/**
 * The {@link FreeboxPhoneStatus} is the Java class used to map the
 * structure used by the phone API
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FreeboxPhoneStatus {
    private boolean isRinging;
    private boolean onHook;

    public boolean isRinging() {
        return isRinging;
    }

    public boolean isOnHook() {
        return onHook;
    }
}
