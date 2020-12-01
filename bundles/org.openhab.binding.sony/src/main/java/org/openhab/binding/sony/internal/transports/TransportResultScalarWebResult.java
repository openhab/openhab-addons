/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.transports;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebResult;

/**
 * This class represents a {@link ScalarWebResult}
 * 
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class TransportResultScalarWebResult implements TransportResult {
    /** The scalar web result */
    private final ScalarWebResult result;

    /**
     * Constructs the result from the {@link ScalarWebResult}
     * 
     * @param result a non-null {@link ScalarWebResult}
     */
    public TransportResultScalarWebResult(final ScalarWebResult result) {
        Objects.requireNonNull(result, "result cannot be null");
        this.result = result;
    }

    /**
     * Gets the {@link ScalarWebResult}
     * 
     * @return a non-null {@link ScalarWebResult}
     */
    public ScalarWebResult getResult() {
        return result;
    }
}
