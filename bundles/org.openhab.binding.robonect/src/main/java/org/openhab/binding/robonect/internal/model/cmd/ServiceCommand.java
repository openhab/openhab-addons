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
 * Command for changing the Robonect module's operating state.
 */
public class ServiceCommand implements Command {

    public enum Operation {
        REBOOT("reboot"),
        SHUTDOWN("shutdown"),
        SLEEP("sleep");

        private final String parameter;

        Operation(String parameter) {
            this.parameter = parameter;
        }
    }

    private final Operation operation;

    public ServiceCommand(Operation operation) {
        this.operation = operation;
    }

    @Override
    public String toCommandURL(String baseUrl) {
        return baseUrl + "?cmd=service&" + operation.parameter;
    }
}
