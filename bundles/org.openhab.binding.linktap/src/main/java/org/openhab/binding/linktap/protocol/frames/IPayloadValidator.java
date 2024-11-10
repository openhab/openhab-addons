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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link IPayloadValidator} when implemented for frame definitions, allows the payload's to
 * be validated as accurate data.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public interface IPayloadValidator {

    /**
     * This will return any validation errors with the payload, or otherwise
     * a empty Collection.
     *
     * @return Collection of ValidationError instances highlighting payload issues
     */
    Collection<ValidationError> getValidationErrors();

    Collection<ValidationError> EMPTY_COLLECTION = Collections
            .unmodifiableCollection(new ArrayList<ValidationError>(0));
}
