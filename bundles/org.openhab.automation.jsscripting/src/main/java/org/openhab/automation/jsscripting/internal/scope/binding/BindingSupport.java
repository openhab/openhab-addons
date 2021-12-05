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

package org.openhab.automation.jsscripting.internal.scope.binding;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.model.item.BindingConfigReader;

/**
 * Interface to provide access to multiple objects to support bindings
 *
 * @author Jonathan Gilbert
 */
@NonNullByDefault
interface BindingSupport {
    MetadataRegistry getMetadataRegistry();

    Map<String, BindingConfigReader> getBindingConfigReaders();
}
