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
package org.openhab.io.hueemulation.internal.upnp;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A self test report includes the tested address, a success flag and
 * if the service at the given address is actually ours.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class SelfTestReport {
    public final String address;
    public final boolean reachable;
    public final boolean isOurs;

    SelfTestReport(String address, boolean testReport, boolean isOurs) {
        this.address = address;
        this.reachable = testReport;
        this.isOurs = isOurs;
    }

    /**
     * A failed address is not reachable and not ours
     */
    public static SelfTestReport failed(String address) {
        return new SelfTestReport(address, false, false);
    }
}
