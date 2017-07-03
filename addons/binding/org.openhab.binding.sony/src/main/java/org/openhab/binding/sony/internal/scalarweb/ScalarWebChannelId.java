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

import org.apache.commons.lang.StringUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class ScalarWebChannelId.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class ScalarWebChannelId {

    /** The Constant SEPARATOR. */
    public final static char SEPARATOR = '#';

    /** The parts. */
    private final String[] parts;

    /**
     * Instantiates a new scalar web channel id.
     *
     * @param channelIds the channel ids
     */
    public ScalarWebChannelId(String channelIds) {
        parts = channelIds.split("\\.");
    }

    /**
     * Instantiates a new scalar web channel id.
     *
     * @param parts the parts
     */
    private ScalarWebChannelId(String[] parts) {
        this.parts = parts;
    }

    /**
     * From.
     *
     * @param id the id
     * @return the scalar web channel id
     */
    public ScalarWebChannelId from(String id) {
        if (parts[0] == id) {
            return new ScalarWebChannelId(Arrays.copyOfRange(parts, 1, parts.length));
        } else {
            return new ScalarWebChannelId((String[]) null);
        }
    }

    /**
     * With.
     *
     * @param id the id
     * @return the scalar web channel id
     */
    public ScalarWebChannelId with(String id) {
        if (parts == null) {
            return new ScalarWebChannelId(new String[] { id });
        } else {
            String[] result = Arrays.copyOf(parts, parts.length + 1);
            result[parts.length] = id;
            return new ScalarWebChannelId(result);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return StringUtils.join(parts, SEPARATOR);
    }
}
