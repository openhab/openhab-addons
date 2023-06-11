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
package org.openhab.binding.minecraft.internal.message.data.commands;

/**
 * A command that targets a sign.
 *
 * @author Mattias Markehed - Initial contribution
 */
public class SignCommandData {

    public static final String COMMAND_SIGN_ACTIVE = "COMMAND_SIGN_ACTIVE";

    private String type;
    private String signName;
    private String value;

    public SignCommandData() {
    }

    public SignCommandData(String type, String signName, String value) {
        this.type = type;
        this.signName = signName;
        this.value = value;
    }

    /**
     * Get the type of command.
     *
     * @return the type of command.
     */
    public String getType() {
        return type;
    }

    /**
     * The name of the sign that the command targets.
     *
     * @return name of sign
     */
    public String getSignName() {
        return signName;
    }

    /**
     * The command value sent.
     *
     * @return command value.
     */
    public String getValue() {
        return value;
    }
}
