/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.bambulab.internal;

import java.io.Serial;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class InitializationException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;
    private final ThingStatusDetail detail;
    private final @Nullable String description;

    public InitializationException(ThingStatusDetail detail, @Nullable String description) {
        super("%s: %s".formatted(detail, description));
        this.detail = detail;
        this.description = description;
    }

    public InitializationException(ThingStatusDetail detail, @Nullable String description, Exception ex) {
        super("%s: %s".formatted(detail, description), ex);
        this.detail = detail;
        this.description = description;
    }

    public InitializationException(ThingStatusDetail detail, Exception ex) {
        this(detail, ex.getLocalizedMessage(), ex);
    }

    public ThingStatusDetail getThingStatusDetail() {
        return detail;
    }

    public @Nullable String getDescription() {
        return description;
    }
}
