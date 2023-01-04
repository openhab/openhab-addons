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
package org.openhab.binding.pjlinkdevice.internal.device.command.input;

import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pjlinkdevice.internal.device.command.ResponseException;

/**
 * Describes an A/V source that can be selected on the PJLink device.
 *
 * @author Nils Schnabel - Initial contribution
 */
@NonNullByDefault
public class Input {

    private static final Pattern INPUT_NUMBER_PATTERN = Pattern.compile("[0-9A-Z]");

    enum InputType {
        RGB("RGB", '1'),
        VIDEO("Video", '2'),
        DIGITAL("Digital", '3'),
        STORAGE("Storage", '4'),
        NETWORK("Network", '5');

        private String text;
        private char code;

        private InputType(String text, char code) {
            this.text = text;
            this.code = code;
        }

        public String getText() {
            return this.text;
        }

        public static InputType parseString(String value) throws ResponseException {
            for (InputType result : InputType.values()) {
                if (result.code == value.charAt(0)) {
                    return result;
                }
            }

            throw new ResponseException("Unknown input channel type: " + value);
        }
    }

    private String value;

    public Input(String value) throws ResponseException {
        this.value = value;
        validate();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + value.hashCode();
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Input other = (Input) obj;
        if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    public InputType getInputType() throws ResponseException {
        return InputType.parseString(this.value);
    }

    public String getInputNumber() throws ResponseException {
        String inputNumber = this.value.substring(1, 2);
        if (!INPUT_NUMBER_PATTERN.matcher(inputNumber).matches()) {
            throw new ResponseException("Illegal channel number: " + inputNumber);
        }

        return inputNumber;
    }

    public void validate() throws ResponseException {
        if (this.value.length() != 2) {
            throw new ResponseException("Illegal input description: " + value);
        }
        // these method also validate
        getInputType();
        getInputNumber();
    }

    public String getValue() {
        return this.value;
    }

    public String getPJLinkRepresentation() {
        return this.value;
    }

    public String getText() throws ResponseException {
        return getInputType().getText() + " " + getInputNumber();
    }
}
