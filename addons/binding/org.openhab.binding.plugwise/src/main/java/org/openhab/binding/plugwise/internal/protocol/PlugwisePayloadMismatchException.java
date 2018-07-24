/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal.protocol;

import java.util.regex.Pattern;

import org.openhab.binding.plugwise.internal.protocol.field.MessageType;

/**
 * The payload of a message does not match the expected message pattern. Thrown whenever the payload of a received
 * message could not be parsed.
 *
 * @author Wouter Born - Initial contribution
 */
public class PlugwisePayloadMismatchException extends RuntimeException {

    private static final long serialVersionUID = 1160553788698072410L;

    public PlugwisePayloadMismatchException(MessageType messageType, Pattern pattern0, Pattern pattern1,
            String payload) {
        super(String.format("Plugwise %s payload mismatch: %s does not match %s or %s", messageType.name(), payload,
                pattern0.pattern(), pattern1.pattern()));
    }

    public PlugwisePayloadMismatchException(MessageType messageType, Pattern pattern, String payload) {
        super(String.format("Plugwise %s payload mismatch: %s does not match %s", messageType.name(), payload,
                pattern.pattern()));
    }

}
