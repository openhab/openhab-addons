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
package org.openhab.binding.digitalstrom.internal.lib.structure;

/**
 * The {@link Group} represents a digitalSTROM-Group.
 *
 * @author Alexander Betker - Initial contribution
 * @author Michael Ochel - add java-doc
 * @author Matthias Siegele - add java-doc
 */
public interface Group {

    /**
     * Returns the group id of this {@link Group}.
     *
     * @return group id
     */
    short getGroupID();

    /**
     * Returns the name of this {@link Group}.
     *
     * @return group name
     */
    String getGroupName();
}
