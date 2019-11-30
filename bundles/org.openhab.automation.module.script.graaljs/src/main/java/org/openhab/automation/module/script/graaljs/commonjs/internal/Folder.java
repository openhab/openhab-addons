/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.automation.module.script.graaljs.commonjs.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

import java.util.Optional;

/**
 * Folder interface used to traverse filesystem for module loading
 *
 * @author Jonathan Gilbert - Initial contribution
 */
@NonNullByDefault
public interface Folder {
    String getPath();

    Optional<Folder> getParent();

    Optional<String> tryReadFile(String name);

    Optional<Folder> getFolder(String name);

    Optional<Folder> resolveChild(String[] elements);
}
