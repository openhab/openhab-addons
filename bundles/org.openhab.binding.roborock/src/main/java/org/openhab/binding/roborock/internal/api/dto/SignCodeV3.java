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
package org.openhab.binding.roborock.internal.api;

/**
 * Class for holding the set of parameters used by the SignCodeV3 response
 *
 * @author Paul Smedley - Initial Contribution
 *
 */

public class SignCodeV3 {
    public String code = "";
    public String message = "";
    public SignCodeV3Data data;

    public class SignCodeV3Data {
        public String k = "";
    }

    public SignCodeV3() {
    }
}
