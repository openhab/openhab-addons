/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Helper for loading XML files from classpath.
 *
 * @author Tomasz Maruszak - Initial contribution
 */
public class ResponseLoader {

    public String load(String path) throws IOException {
        try (InputStream in = getClass().getResourceAsStream(path)) {
            if (in == null) {
                return null;
            }
            return IOUtils.toString(in);
        }
    }

    public String load(String path, String model) throws IOException {
        return load(String.format("/%s/%s", model, path));
    }

}
