/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
