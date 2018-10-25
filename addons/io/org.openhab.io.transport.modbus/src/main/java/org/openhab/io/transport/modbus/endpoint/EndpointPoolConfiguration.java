/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.modbus.endpoint;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.StandardToStringStyle;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Class representing pooling related configuration of a single endpoint
 *
 * This class implements equals hashcode constract, and thus is suitable for use as keys in HashMaps, for example.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class EndpointPoolConfiguration {

    /**
     * How long should be the minimum duration between previous transaction end and the next transaction with the same
     * endpoint.
     *
     * In milliseconds.
     */
    private long interTransactionDelayMillis;

    /**
     * How long should be the minimum duration between connection-establishments from the pool (with same endpoint). In
     * milliseconds.
     */
    private long interConnectDelayMillis;

    /**
     * How many times we want to try connecting to the endpoint before giving up. One means that connection
     * establishment is tried once.
     */
    private int connectMaxTries = 1;

    /**
     * Re-connect connection every X milliseconds. Negative means that connection is not disconnected automatically.
     * One can use 0ms to denote reconnection after every transaction (default).
     */
    private int reconnectAfterMillis;

    /**
     * How long before we give up establishing the connection. In milliseconds. Default of 0 means that system/OS
     * default is respected.
     */
    private int connectTimeoutMillis;

    private static StandardToStringStyle toStringStyle = new StandardToStringStyle();

    static {
        toStringStyle.setUseShortClassName(true);
    }

    public long getInterConnectDelayMillis() {
        return interConnectDelayMillis;
    }

    public void setInterConnectDelayMillis(long interConnectDelayMillis) {
        this.interConnectDelayMillis = interConnectDelayMillis;
    }

    public int getConnectMaxTries() {
        return connectMaxTries;
    }

    public void setConnectMaxTries(int connectMaxTries) {
        this.connectMaxTries = connectMaxTries;
    }

    public int getReconnectAfterMillis() {
        return reconnectAfterMillis;
    }

    public void setReconnectAfterMillis(int reconnectAfterMillis) {
        this.reconnectAfterMillis = reconnectAfterMillis;
    }

    public long getInterTransactionDelayMillis() {
        return interTransactionDelayMillis;
    }

    public void setInterTransactionDelayMillis(long interTransactionDelayMillis) {
        this.interTransactionDelayMillis = interTransactionDelayMillis;
    }

    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public void setConnectTimeoutMillis(int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(2149, 3117).append(interTransactionDelayMillis).append(interConnectDelayMillis)
                .append(connectMaxTries).append(reconnectAfterMillis).append(connectTimeoutMillis).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, toStringStyle)
                .append("interTransactionDelayMillis", interTransactionDelayMillis)
                .append("interConnectDelayMillis", interConnectDelayMillis).append("connectMaxTries", connectMaxTries)
                .append("reconnectAfterMillis", reconnectAfterMillis)
                .append("connectTimeoutMillis", connectTimeoutMillis).toString();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        EndpointPoolConfiguration rhs = (EndpointPoolConfiguration) obj;
        return new EqualsBuilder().append(interTransactionDelayMillis, rhs.interTransactionDelayMillis)
                .append(interConnectDelayMillis, rhs.interConnectDelayMillis)
                .append(connectMaxTries, rhs.connectMaxTries).append(reconnectAfterMillis, rhs.reconnectAfterMillis)
                .append(connectTimeoutMillis, rhs.connectTimeoutMillis).isEquals();
    }

}
