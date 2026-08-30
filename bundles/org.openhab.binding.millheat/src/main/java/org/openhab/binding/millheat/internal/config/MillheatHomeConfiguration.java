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
package org.openhab.binding.millheat.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link MillheatHomeConfiguration} class contains home thing configuration parameters.
 *
 * @author Arne Seime - Initial contribution
 * @author Petter L. H. Eide - Identifiers are cloud API UUIDs
 */
@NonNullByDefault
public class MillheatHomeConfiguration {

    /**
     * House UUID as issued by the cloud API. Numeric identifiers from the old service are not valid
     * here; re-run discovery to obtain the new value.
     */
    public @Nullable String homeId;

    @Override
    public String toString() {
        return "MillheatHomeConfiguration [homeId=" + homeId + "]";
    }
}
