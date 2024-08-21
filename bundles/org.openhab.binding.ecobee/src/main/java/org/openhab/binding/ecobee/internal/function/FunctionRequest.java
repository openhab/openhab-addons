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
package org.openhab.binding.ecobee.internal.function;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ecobee.internal.dto.SelectionDTO;

/**
 * The {@link FunctionRequest} encapsulates functions that are to be sent to the
 * Ecobee API.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class FunctionRequest {

    public FunctionRequest(SelectionDTO selection) {
        this.selection = selection;
    }

    public SelectionDTO selection;

    public @Nullable List<AbstractFunction> functions;
}
