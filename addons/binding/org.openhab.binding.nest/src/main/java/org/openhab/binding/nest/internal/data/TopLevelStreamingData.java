/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal.data;

import com.google.gson.annotations.SerializedName;

/**
 * The top level data that is sent by Nest to a streaming REST client using SSE.
 *
 * @author Wouter Born - Replace polling with REST streaming
 */
public class TopLevelStreamingData {
    @SerializedName("path")
    private String path;
    @SerializedName("data")
    private TopLevelData data;

    public String getPath() {
        return path;
    }

    public TopLevelData getData() {
        return data;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TopLevelStreamingData [path=").append(path).append(", data=").append(data).append("]");
        return builder.toString();
    }

}
