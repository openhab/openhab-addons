/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.robonect.internal.model.cmd;

/**
 * 
 * Interface implemented by all commands. The robonect module is called with urls like
 * http://xxx.xxx.xxx/json?cmd=[command]. The command implementation is responsible to construct the full command url.
 * 
 * 
 * @author Marco Meyer - Initial contribution
 */
public interface Command {

    /**
     * Builds the URL for this command.
     *
     * @param baseURL API base URL, used by commands that do not already contain a request URL
     * @return the complete request URL
     */
    String toCommandURL(String baseURL);
}
