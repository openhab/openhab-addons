/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.robonect.internal.model;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Simple POJO for deserialize the list of errors from the errors command.
 * 
 * @author Marco Meyer - Initial contribution
 */
@NonNullByDefault
public class ErrorList extends RobonectAnswer {

    private @Nullable List<ErrorEntry> errors;

    /**
     * Returns the list of errors.
     *
     * @return the list of errors.
     */
    public @Nullable List<ErrorEntry> getErrors() {
        return errors;
    }
}
