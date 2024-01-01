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
package org.openhab.binding.mielecloud.internal.webservice;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Factory for creating {@link MieleWebservice} instances.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public interface MieleWebserviceFactory {
    /**
     * Creates a new {@link MieleWebservice}.
     *
     * @param configuration The configuration holding all required parameters to construct the instance.
     * @return A new {@link MieleWebservice}.
     */
    MieleWebservice create(MieleWebserviceConfiguration configuration);
}
