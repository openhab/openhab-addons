/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.jablotron.internal.model.ja100f;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link JablotronGetPGResponse} class defines the response object for the
 * getProgrammableGates call
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class JablotronGetPGResponse {
    JablotronGetPGData data = new JablotronGetPGData();

    public JablotronGetPGData getData() {
        return data;
    }
}
