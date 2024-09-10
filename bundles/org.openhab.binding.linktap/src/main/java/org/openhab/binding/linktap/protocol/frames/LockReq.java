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
package org.openhab.binding.linktap.protocol.frames;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link LockReq} defines the request to dismiss alerts from a given device.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class LockReq extends DeviceCmdReq {

    public LockReq() {
    }

    public LockReq(final int lock) {
        this.command = CMD_LOCKOUT_STATE;
        this.lock = lock;
    }

    /**
     * Defines the lock type to reqest
     */
    @SerializedName("lock")
    @Expose
    public int lock = DEFAULT_INT;

    public Collection<ValidationError> getValidationErrors() {
        final Collection<ValidationError> errors = super.getValidationErrors();

        if (lock < LOCK_UNLOCKED || lock > LOCK_FULL) {
            errors.add(new ValidationError("lock", "not in range " + LOCK_UNLOCKED + " -> " + LOCK_FULL));
        }
        return errors;
    }

    /**
     * Lock - 0. Device is unlocked
     */
    public static final int LOCK_UNLOCKED = 0;

    /**
     * Lock - 1. Partially locked
     */
    public static final int LOCK_PARTIALLY = 1;

    /**
     * Lock - 2. Completely locked
     */
    public static final int LOCK_FULL = 2;
}
