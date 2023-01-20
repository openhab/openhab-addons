/**
<<<<<<< Upstream, based on origin/main
<<<<<<< Upstream, based on origin/main
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
package org.openhab.binding.freeboxos.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

import inet.ipaddr.MACAddressString;
import inet.ipaddr.mac.MACAddress;

<<<<<<< Upstream, based on origin/main
/**
 * The {@link HostConfiguration} is responsible for holding
 * configuration informations associated to a Freebox Network Device
 * thing type
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class HostConfiguration extends ApiConsumerConfiguration {
    private String macAddress = "";

    public MACAddress getMac() {
        return new MACAddressString(macAddress).getAddress();
=======
 * Copyright (c) 2010-2022 Contributors to the openHAB project
=======
 * Copyright (c) 2010-2023 Contributors to the openHAB project
>>>>>>> 006a813 Saving work before instroduction of ArrayListDeserializer
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
package org.openhab.binding.freeboxos.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

=======
>>>>>>> e4ef5cc Switching to Java 17 records
/**
 * The {@link HostConfiguration} is responsible for holding
 * configuration informations associated to a Freebox Network Device
 * thing type
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class HostConfiguration extends ApiConsumerConfiguration {
    private String macAddress = "";

<<<<<<< Upstream, based on origin/main
    public String getMac() {
        return macAddress.toLowerCase();
>>>>>>> 46dadb1 SAT warnings handling
=======
    public MACAddress getMac() {
        return new MACAddressString(macAddress).getAddress();
>>>>>>> e4ef5cc Switching to Java 17 records
    }
}
