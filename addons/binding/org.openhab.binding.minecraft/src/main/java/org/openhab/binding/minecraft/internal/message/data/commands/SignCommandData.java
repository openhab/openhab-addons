/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.minecraft.internal.message.data.commands;

/**
 * A command that targets a sign.
 *
 * @author Mattias Markehed
 */
public class SignCommandData {

    public static String COMMAND_SIGN_ACTIVE = "COMMAND_SIGN_ACTIVE";

    String type;
    String signName;
    String value;

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
