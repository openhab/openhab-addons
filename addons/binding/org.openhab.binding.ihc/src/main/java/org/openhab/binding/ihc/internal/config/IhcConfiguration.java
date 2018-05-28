/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.internal.config;

/**
 * Configuration class for {@link IhcBinding} binding.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class IhcConfiguration {
    public String ip;
    public String username;
    public String password;
    public int timeout;
    public boolean loadProjectFile;
    public boolean createChannelsAutomatically;

    @Override
    public String toString() {
        return "[" + "ip=" + ip + ", username=" + username + ", password=******" + ", timeout=" + timeout
                + ", loadProjectFile=" + loadProjectFile + ", createChannelsAutomatically="
                + createChannelsAutomatically + "]";
    }
}
