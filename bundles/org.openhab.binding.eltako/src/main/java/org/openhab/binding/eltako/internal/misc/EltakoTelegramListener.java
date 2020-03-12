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
package org.openhab.binding.eltako.internal.misc;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The {@link EltakoTelegramListener} provides interface for telegram forwarding to connected things
 *
 * @author Martin Wenske - Initial contribution
 */
public interface EltakoTelegramListener {

    public void telegramReceived(int @NonNull [] packet);
}
