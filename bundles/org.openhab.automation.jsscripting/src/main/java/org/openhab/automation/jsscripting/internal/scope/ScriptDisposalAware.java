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

package org.openhab.automation.jsscripting.internal.scope;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Specifies that an object is aware of script disposal events
 *
 * @author Jonathan Gilbert - Initial contribution
 */
@NonNullByDefault
public interface ScriptDisposalAware {

    /**
     * Indicates that the script has been disposed
     *
     * @param scriptIdentifier the identifier for the script
     */
    void unload(String scriptIdentifier);
}
