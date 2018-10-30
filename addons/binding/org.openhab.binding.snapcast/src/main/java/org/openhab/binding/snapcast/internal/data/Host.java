/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.snapcast.internal.data;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Host} is a data structure for the api communication.
 * It provides some informations about the host.
 *
 * @author Steffen Brandemann - Initial contribution
 */
public class Host {

    @SerializedName("name")
    private String name;

    @SerializedName("ip")
    private String ip;

    @SerializedName("mac")
    private String mac;

    @SerializedName("os")
    private String os;

    @SerializedName("arch")
    private String arch;

    public Host() {
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the ip
     */
    public String getIp() {
        return ip;
    }

    /**
     * @param ip the ip to set
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * @return the mac
     */
    public String getMac() {
        return mac;
    }

    /**
     * @param mac the mac to set
     */
    public void setMac(String mac) {
        this.mac = mac;
    }

    /**
     * @return the os
     */
    public String getOs() {
        return os;
    }

    /**
     * @param os the os to set
     */
    public void setOs(String os) {
        this.os = os;
    }

    /**
     * @return the arch
     */
    public String getArch() {
        return arch;
    }

    /**
     * @param arch the arch to set
     */
    public void setArch(String arch) {
        this.arch = arch;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((arch == null) ? 0 : arch.hashCode());
        result = prime * result + ((ip == null) ? 0 : ip.hashCode());
        result = prime * result + ((mac == null) ? 0 : mac.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((os == null) ? 0 : os.hashCode());
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
        Host other = (Host) obj;
        if (arch == null) {
            if (other.arch != null) {
                return false;
            }
        } else if (!arch.equals(other.arch)) {
            return false;
        }
        if (ip == null) {
            if (other.ip != null) {
                return false;
            }
        } else if (!ip.equals(other.ip)) {
            return false;
        }
        if (mac == null) {
            if (other.mac != null) {
                return false;
            }
        } else if (!mac.equals(other.mac)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (os == null) {
            if (other.os != null) {
                return false;
            }
        } else if (!os.equals(other.os)) {
            return false;
        }
        return true;
    }

}
