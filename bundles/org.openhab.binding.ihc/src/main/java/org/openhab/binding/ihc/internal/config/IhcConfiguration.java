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
package org.openhab.binding.ihc.internal.config;

/**
 * Configuration class for {@link IhcBinding} binding.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class IhcConfiguration {
    public String hostname;
    public String username;
    public String password;
    public int timeout;
    public boolean loadProjectFile;
    public boolean createChannelsAutomatically;
    public String tlsVersion;

    @Override
    public String toString() {
        return "[" + "hostname=" + hostname + ", username=" + username + ", password=******" + ", timeout=" + timeout
                + ", loadProjectFile=" + loadProjectFile + ", createChannelsAutomatically="
                + createChannelsAutomatically + ", tlsVersion=" + tlsVersion + "]";
    }
}
