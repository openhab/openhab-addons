/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.util.Arrays;
import java.util.HashMap;

import org.openhab.binding.pjlinkdevice.internal.device.command.ResponseException;

/**
 * @author Nils Schnabel - Initial contribution
 */
public class Input {

    enum InputType {
        RGB,
        VIDEO,
        DIGITAL,
        STORAGE,
        NETWORK;

        public String getText() {
            final HashMap<InputType, String> texts = new HashMap<InputType, String>();
            texts.put(RGB, "RGB");
            texts.put(VIDEO, "Video");
            texts.put(DIGITAL, "Digital");
            texts.put(STORAGE, "Storage");
            texts.put(NETWORK, "Network");
            return texts.get(this);
        }

        public static InputType parseString(String value) throws ResponseException {
            final HashMap<String, InputType> codes = new HashMap<String, InputType>();
            codes.put("1", RGB);
            codes.put("2", VIDEO);
            codes.put("3", DIGITAL);
            codes.put("4", STORAGE);
            codes.put("5", NETWORK);

            InputType result = codes.get(value.substring(0, 1));
            if (result == null) {
                throw new ResponseException("Unknown input channel type: " + value);
            }

            return result;
        }
    }

    public Input(String value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
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
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    String value;

    public InputType getInputType() throws ResponseException {
        return InputType.parseString(this.value);
    }

    public String getInputNumber() throws ResponseException {
        return this.value.substring(1, 2);
    }

    public void validate() throws ResponseException {
        if (this.value.length() != 2) {
            throw new ResponseException("Illegal input description: " + value);
        }
        this.getInputType();
        if (!Arrays.asList(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" })
                .contains(this.getInputNumber())) {
            throw new ResponseException("Illegal channel number: " + this.value.substring(1, 2));
        }
    }

    public String getValue() {
        return this.value;
    }

    public String getPJLinkRepresentation() {
        return this.value;
    }

    public String getText() throws ResponseException {
        return this.getInputType().getText() + " " + this.getInputNumber();
    }
}
