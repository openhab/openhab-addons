/**
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

package helper.rules;

import org.eclipse.jdt.annotation.NonNullByDefault;

import java.io.Serial;

/**
 * @author Gwendal Roulleau - Initial contribution
 */
@NonNullByDefault
public class RuleParserException extends Exception {

    @Serial
    private static final long serialVersionUID = 5744217657057910494L;

    RuleParserException(String message, Throwable e) {
        super(message, e);
    }

    public RuleParserException(String message) {
        super(message);
    }
}
