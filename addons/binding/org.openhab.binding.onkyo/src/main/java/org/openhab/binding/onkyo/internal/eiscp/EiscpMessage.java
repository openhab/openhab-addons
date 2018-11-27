/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.onkyo.internal.eiscp;

/**
 * Class to handle Onkyo eISCP messages.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class EiscpMessage {
    private String command = "";
    private String value = "";

    private EiscpMessage(MessageBuilder messageBuilder) {
        this.command = messageBuilder.command;
        this.value = messageBuilder.value;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        String str = "[";

        str += "command=" + command;
        str += ", value=" + value;
        str += "]";

        return str;
    }

    public static class MessageBuilder {
        private String command;
        private String value;

        public MessageBuilder command(String command) {
            this.command = command;
            return this;
        }

        public MessageBuilder value(String value) {
            this.value = value;
            return this;
        }

        public EiscpMessage build() {
            return new EiscpMessage(this);
        }
    }
}
