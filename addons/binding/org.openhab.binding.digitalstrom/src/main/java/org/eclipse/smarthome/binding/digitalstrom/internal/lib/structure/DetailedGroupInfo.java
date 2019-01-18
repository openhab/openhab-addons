/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure;

import java.util.List;

/**
 * The {@link DetailedGroupInfo} represents a digitalSTROM-Group with a list of all dSUID's of the included
 * digitalSTROM-Devices.
 *
 * @author Alexander Betker - initial contributer
 * @author Michael Ochel - add java-doc
 * @author Matthias Siegele - add java-doc
 */
public interface DetailedGroupInfo extends Group {

    /**
     * Returns the list of all dSUID's of the included digitalSTROM-Devices.
     *
     * @return list of all dSUID
     */
    List<String> getDeviceList();
}
