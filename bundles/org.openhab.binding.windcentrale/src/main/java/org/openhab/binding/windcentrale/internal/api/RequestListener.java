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
package org.openhab.binding.windcentrale.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface for listeners that want to monitor if {@link WindcentraleAPI} requests error or succeed.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public interface RequestListener {

    void onError(Exception exception);

    void onSuccess();
}
