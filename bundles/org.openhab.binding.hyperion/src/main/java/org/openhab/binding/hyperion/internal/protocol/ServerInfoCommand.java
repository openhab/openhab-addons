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
package org.openhab.binding.hyperion.internal.protocol;

/**
 * The {@link ServerInfoCommand} is a POJO for server info commands
 * to the Hyperion server.
 *
 * @author Daniel Walters - Initial contribution
 */
public class ServerInfoCommand extends HyperionCommand {

    private static final String NAME = "serverinfo";

    public ServerInfoCommand() {
        super(NAME);
    }
}
