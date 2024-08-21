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
package org.openhab.binding.ventaair.internal.message.action;

/**
 * Action to enable the child lock mode of the device
 *
 * @author Stefan Triller - Initial contribution
 *
 */
public class ChildLockAction implements Action {
    @SuppressWarnings("unused")
    private boolean ChildLock;

    public ChildLockAction(boolean childLockOn) {
        this.ChildLock = childLockOn;
    }
}
