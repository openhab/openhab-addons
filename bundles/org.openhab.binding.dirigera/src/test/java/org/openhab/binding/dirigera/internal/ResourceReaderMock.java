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
package org.openhab.binding.dirigera.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dirigera.internal.interfaces.ResourceProvider;

/**
 * The {@link ResourceReaderMock} to handle file base resource reading for tests
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ResourceReaderMock extends ResourceReader {

    public static void setProvider(ResourceProvider provider) {
        ResourceReader.provider = provider;
    }
}
