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
package org.openhab.binding.dali.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link DaliResponse} represents different types of responses to DALI
 * commands.
 *
 * @author Robert Schmid - Initial contribution
 */
@NonNullByDefault
public class DaliResponse {
    public void parse(@Nullable DaliBackwardFrame frame) {
    }

    public static class Numeric extends DaliResponse {
        public @Nullable Integer value;

        @Override
        public void parse(@Nullable DaliBackwardFrame frame) {
            if (frame != null) {
                value = frame.data;
            }
        }
    }

    public static class NumericMask extends DaliResponse.Numeric {
        public @Nullable Boolean mask;

        @Override
        public void parse(@Nullable DaliBackwardFrame frame) {
            super.parse(frame);
            if (this.value == 255) {
                this.value = null;
                this.mask = true;
            } else {
                this.mask = false;
            }
        }
    }
}
