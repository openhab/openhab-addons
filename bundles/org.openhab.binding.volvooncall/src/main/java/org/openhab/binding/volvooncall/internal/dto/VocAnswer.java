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
package org.openhab.binding.volvooncall.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link VocAnswer} is the base class for all Voc API requests
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public abstract class VocAnswer {

    private @Nullable String errorLabel;
    private @Nullable String errorDescription;

    public @Nullable String getErrorLabel() {
        return errorLabel;
    }

    public @Nullable String getErrorDescription() {
        return errorDescription;
    }
}
