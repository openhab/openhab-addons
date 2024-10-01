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
package org.openhab.automation.jsscripting.internal;

import java.util.Optional;

import org.graalvm.polyglot.Value;

/**
 * Locates modules from a module name
 *
 * @author Jonathan Gilbert - Initial contribution
 */
public interface ModuleLocator {
    Optional<Value> locateModule(String name);
}
