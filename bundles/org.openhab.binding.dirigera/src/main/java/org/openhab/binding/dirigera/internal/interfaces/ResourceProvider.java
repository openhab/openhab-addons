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
package org.openhab.binding.dirigera.internal.interfaces;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link ResourceProvider} interface provides methods to access resource files from main/resources
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public interface ResourceProvider {
    String getResourceFile(String resourcePath);

    String getResourceFileUncompressed(String resourcePath);
}
