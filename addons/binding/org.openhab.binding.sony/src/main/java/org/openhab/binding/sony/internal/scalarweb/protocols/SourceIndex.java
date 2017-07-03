/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb.protocols;

import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannel;

// TODO: Auto-generated Javadoc
/**
 * The Class SourceIndex.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class SourceIndex {

    /** The scheme. */
    private final String scheme;

    /** The source. */
    private final String source;

    /** The index. */
    private final int index;

    /**
     * Instantiates a new source index.
     *
     * @param scheme the scheme
     * @param source the source
     * @param index the index
     */
    public SourceIndex(String scheme, String source, int index) {
        this.scheme = scheme;
        this.source = source;
        this.index = index;
    }

    /**
     * Instantiates a new source index.
     *
     * @param channel the channel
     */
    public SourceIndex(ScalarWebChannel channel) {
        final String[] paths = channel.getPaths();
        if (paths.length != 4) {
            throw new IllegalArgumentException("paths must be 4 in length");
        }

        scheme = paths[1];
        source = paths[2];
        index = Integer.parseInt(paths[3]);

    }

    /**
     * Gets the scheme.
     *
     * @return the scheme
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * Gets the source.
     *
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * Gets the index.
     *
     * @return the index
     */
    public int getIndex() {
        return index;
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
        result = prime * result + index;
        result = prime * result + ((scheme == null) ? 0 : scheme.hashCode());
        result = prime * result + ((source == null) ? 0 : source.hashCode());
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
        SourceIndex other = (SourceIndex) obj;
        if (index != other.index) {
            return false;
        }
        if (scheme == null) {
            if (other.scheme != null) {
                return false;
            }
        } else if (!scheme.equals(other.scheme)) {
            return false;
        }
        if (source == null) {
            if (other.source != null) {
                return false;
            }
        } else if (!source.equals(other.source)) {
            return false;
        }
        return true;
    }
}
