/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.thing.ChannelUID;

// TODO: Auto-generated Javadoc
/**
 * The Class ScalarWebChannel.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class ScalarWebChannel {

    /** The Constant SEPARATOR. */
    private static final String SEPARATOR = "-";

    /** The Constant DELIMITER. */
    private static final String DELIMITER = "__";

    /** The Constant ENCODE. */
    private static final char[] ENCODE = new char[] { '.', '?', '=', ':', '&', '/', '\\' };

    /** The service. */
    private final String service;

    /** The encoded paths. */
    private final String[] encodedPaths;

    /** The unencoded paths. */
    private final String[] unencodedPaths;

    /** The id. */
    private final String id;

    /**
     * Instantiates a new scalar web channel.
     *
     * @param channelUID the channel UID
     */
    public ScalarWebChannel(ChannelUID channelUID) {
        this(channelUID == null ? null : channelUID.getId());
    }

    /**
     * Instantiates a new scalar web channel.
     *
     * @param parts the parts
     */
    public ScalarWebChannel(List<String> parts) {
        Objects.requireNonNull(parts);

        if (parts.size() < 2) {
            throw new IllegalArgumentException("ChannelId invalid: " + StringUtils.join(parts, ','));
        }

        service = parts.get(0);
        id = parts.get(parts.size() - 1);

        if (parts.size() == 2) {
            encodedPaths = new String[0];
            unencodedPaths = new String[0];
        } else {
            encodedPaths = new String[parts.size() - 2];
            unencodedPaths = new String[parts.size() - 2];

            for (int idx = 1; idx < parts.size() - 1; idx++) {
                unencodedPaths[idx - 1] = parts.get(idx);
                encodedPaths[idx - 1] = encode(unencodedPaths[idx - 1]);
            }
        }
    }

    /**
     * Instantiates a new scalar web channel.
     *
     * @param channelId the channel id
     */
    public ScalarWebChannel(String channelId) {
        if (StringUtils.isEmpty(channelId)) {
            throw new IllegalArgumentException("channelId cannot be null or an empty string");
        }

        final String[] parts = channelId.split(SEPARATOR);
        if (parts.length < 2) {
            throw new IllegalArgumentException("ChannelId invalid: " + channelId);
        }

        service = parts[0];
        id = parts[parts.length - 1];

        if (parts.length == 2) {
            encodedPaths = new String[0];
            unencodedPaths = new String[0];
        } else {
            encodedPaths = new String[parts.length - 2];
            unencodedPaths = new String[parts.length - 2];

            for (int idx = 1; idx < parts.length - 1; idx++) {
                encodedPaths[idx - 1] = parts[idx];
                unencodedPaths[idx - 1] = decode(encodedPaths[idx - 1]);
            }
        }
    }

    /**
     * Encode.
     *
     * @param str the str
     * @return the string
     */
    private static String encode(String str) {
        final StringBuilder sb = new StringBuilder(str.length() * 2);

        final int strLen = str.length();
        for (int idx = 0; idx < strLen; idx++) {
            final char c = str.charAt(idx);
            final int idx2 = ArrayUtils.indexOf(ENCODE, c);

            if (idx2 >= 0) {
                sb.append(DELIMITER);
                sb.append(idx2);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Decode.
     *
     * @param str the str
     * @return the string
     */
    private static String decode(String str) {
        final StringBuilder sb = new StringBuilder(str);

        int idx = sb.length();
        while (idx >= 0) {
            idx = sb.lastIndexOf(DELIMITER, idx);

            if (idx >= 0 && idx + 2 < sb.length()) {
                final int encodeIdx = sb.charAt(idx + 2) - '0';
                sb.replace(idx, idx + 3, String.valueOf(ENCODE[encodeIdx]));
            }
        }

        return sb.toString();
    }

    /**
     * Gets the service.
     *
     * @return the service
     */
    public String getService() {
        return service;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the paths.
     *
     * @return the paths
     */
    public String[] getPaths() {
        return unencodedPaths;
    }

    /**
     * Gets the channel id.
     *
     * @return the channel id
     */
    public String getChannelId() {
        return encodedPaths.length == 0 ? String.join(SEPARATOR, service, id)
                : String.join(SEPARATOR, service, String.join(SEPARATOR, encodedPaths), id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getChannelId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + Arrays.hashCode(encodedPaths);
        result = prime * result + ((service == null) ? 0 : service.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
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
        ScalarWebChannel other = (ScalarWebChannel) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (!Arrays.equals(encodedPaths, other.encodedPaths)) {
            return false;
        }
        if (service == null) {
            if (other.service != null) {
                return false;
            }
        } else if (!service.equals(other.service)) {
            return false;
        }
        return true;
    }
}
