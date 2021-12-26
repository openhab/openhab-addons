/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.blink.internal.handler;

/**
 * The {@link EventListener} interface must be implemented by all things connected to the account bridge. Its methods
 * get called whenever an account level event occurs that may affect multiple things at once, thus giving things the
 * chance to update their internal state.
 *
 * @author Sascha Volkenandt - Initial contribution
 */
public interface EventListener {

    void handleHomescreenUpdate();
}
