/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb.models.api;

// TODO: Auto-generated Javadoc
/**
 * The Class ContentListRequest.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class ContentListRequest {

    /** The source. */
    private final String source;

    /** The st idx. */
    private final Integer stIdx;

    /** The cnt. */
    private final Integer cnt;

    /** The type. */
    private final String type;

    /** The target. */
    private final String target;

    /**
     * Instantiates a new content list request.
     *
     * @param source the source
     */
    public ContentListRequest(String source) {
        this(source, null, null, null, null);
    }

    /**
     * Instantiates a new content list request.
     *
     * @param source the source
     * @param stIdx the st idx
     * @param cnt the cnt
     */
    public ContentListRequest(String source, int stIdx, int cnt) {
        this(source, stIdx, cnt, null, null);
    }

    /**
     * Instantiates a new content list request.
     *
     * @param source the source
     * @param stIdx the st idx
     * @param cnt the cnt
     * @param type the type
     * @param target the target
     */
    public ContentListRequest(String source, Integer stIdx, Integer cnt, String type, String target) {
        super();
        this.source = source;
        this.stIdx = stIdx;
        this.cnt = cnt;
        this.type = type;
        this.target = target;
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
     * Gets the st idx.
     *
     * @return the st idx
     */
    public Integer getStIdx() {
        return stIdx;
    }

    /**
     * Gets the cnt.
     *
     * @return the cnt
     */
    public Integer getCnt() {
        return cnt;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the target.
     *
     * @return the target
     */
    public String getTarget() {
        return target;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ContentListRequest [source=" + source + ", stIdx=" + stIdx + ", cnt=" + cnt + ", type=" + type
                + ", target=" + target + "]";
    }

}
