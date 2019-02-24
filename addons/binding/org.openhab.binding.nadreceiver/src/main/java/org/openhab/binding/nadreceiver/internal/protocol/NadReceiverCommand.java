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
package org.openhab.binding.nadreceiver.internal.protocol;

/**
 * Class consolidating "NAD operation type" and "NAD operation" to get a proper command to execute
 *
 * @author Marc Chételat - Initial contribution
 *
 */
public class NadReceiverCommand {
    private final NadReceiverCommandType type;
    private final NadReceiverOperation operation;
    private final String value;
    private final Integer number;

    /**
     * Used to set values to operation with a number
     *
     * @param type
     * @param operation
     * @param value
     * @param number
     */
    public NadReceiverCommand(NadReceiverCommandType type, NadReceiverOperation operation, String value,
            Integer number) {
        if (operation == null || type == null || number == null || value == null) {
            throw new IllegalArgumentException("Operation or type or value or number cannot be null");
        }
        if (operation.equals(NadReceiverOperation.QUERY)) {
            throw new IllegalArgumentException("Operation QUERY and value is not possible");
        }
        this.operation = operation;
        this.type = type;
        this.value = value;
        this.number = number;
    }

    /**
     * Used to query status of value(s)
     *
     * @param type
     * @param operation
     * @param number
     */
    public NadReceiverCommand(NadReceiverCommandType type, NadReceiverOperation operation, Integer number) {
        if (operation == null || type == null || number == null) {
            throw new IllegalArgumentException("Operation or type or number cannot be null");
        }
        if (operation.equals(NadReceiverOperation.EXECUTE)) {
            throw new IllegalArgumentException("Operation EXECUTE and value is not possible");
        }
        this.operation = operation;
        this.type = type;
        this.value = null;
        this.number = number;
    }

    public NadReceiverCommand(NadReceiverCommandType type, NadReceiverOperation operation, String value) {
        if (operation == null || type == null || value == null) {
            throw new IllegalArgumentException("Operation or type or value cannot be null");
        }
        if (operation.equals(NadReceiverOperation.QUERY)) {
            throw new IllegalArgumentException("Operation QUERY and value is not possible");
        }
        if (type.toString().contains("#")) {
            throw new IllegalArgumentException("NadReceiverCommandType has char # but no number");
        }

        this.operation = operation;
        this.type = type;
        this.value = value;
        this.number = null;
    }

    public NadReceiverCommand(NadReceiverCommandType type, NadReceiverOperation operation) {
        if (operation == null || type == null) {
            throw new IllegalArgumentException("operation or type cannot be null");
        }
        if (type.toString().contains("#")) {
            throw new IllegalArgumentException("NadReceiverCommandType has char # but no number");
        }

        this.operation = operation;
        this.type = type;
        this.value = null;
        this.number = null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        if (type.toString().contains("#") && number != null) {
            builder.append(type.toString().replace("#", number.toString()));
        } else {
            builder.append(type.toString());
        }

        builder.append(operation.toString());

        if (value != null) {
            builder.append(value);
        }

        return builder.toString();
    }
}
