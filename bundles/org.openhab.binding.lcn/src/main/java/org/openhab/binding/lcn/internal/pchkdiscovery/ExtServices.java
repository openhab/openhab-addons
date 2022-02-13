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
package org.openhab.binding.lcn.internal.pchkdiscovery;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Used for deserializing the XML response of the LCN-PCHK discovery protocol.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class ExtServices {
    private final ExtService ExtService;

    public ExtServices(ExtService extService) {
        ExtService = extService;
    }

    public ExtService getExtService() {
        return ExtService;
    }
}
