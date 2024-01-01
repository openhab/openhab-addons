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
package org.openhab.binding.hue.internal.api.dto.clip2;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.exceptions.DTOPresentButEmptyException;

/**
 * DTO for 'on' state of a light.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class OnState {
    private @Nullable Boolean on;

    /**
     * @throws DTOPresentButEmptyException to indicate that the DTO is present but empty.
     */
    public boolean isOn() throws DTOPresentButEmptyException {
        Boolean on = this.on;
        if (Objects.nonNull(on)) {
            return on;
        }
        throw new DTOPresentButEmptyException("'on' DTO is present but empty");
    }

    public OnState setOn(boolean on) {
        this.on = on;
        return this;
    }
}
