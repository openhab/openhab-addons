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
package org.openhab.binding.intellicenter2.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link IntelliCenter2Configuration} class contains fields mapping thing configuration parameters.
 *
 * @author Valdis Rigdon - Initial contribution
 */
@NonNullByDefault
public class IntelliCenter2Configuration {

    public String hostname = "";
    public int port = 6681;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{hostname=" + hostname + ", port=" + port + "}";
    }
}
