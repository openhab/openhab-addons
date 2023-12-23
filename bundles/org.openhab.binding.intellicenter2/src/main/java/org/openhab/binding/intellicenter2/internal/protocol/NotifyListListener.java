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
package org.openhab.binding.intellicenter2.internal.protocol;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Listener for receiving responses from a IntelliCenter subscription.
 *
 * @see org.openhab.binding.intellicenter2.internal.protocol.Command
 *
 * @author Valdis Rigdon - initial contribution
 */
public interface NotifyListListener {

    /**
     * The response to an IntelliCenter subscription.
     *
     * @param response the decoded response
     */
    void onNotifyList(@NonNull ResponseObject response);
}
