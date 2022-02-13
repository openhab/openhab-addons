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
package org.openhab.binding.ecobee.internal.function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The delete vacation function deletes a vacation event from a thermostat. This is the
 * only way to cancel a vacation event. This method is able to remove vacation
 * events not yet started and scheduled in the future.
 *
 * @author John Cocula - Initial contribution
 * @author Mark Hilbush - Adapt for OH2/3
 */
@NonNullByDefault
public final class DeleteVacationFunction extends AbstractFunction {

    public DeleteVacationFunction(@Nullable String name) {
        super("deleteVacation");
        if (name == null) {
            throw new IllegalArgumentException("name argument is required.");
        }
        params.put("name", name);
    }
}
