/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.internal.ws;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.commons.io.IOUtils;

/**
 * Util class to load file content from resource files.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class ResourceFileUtils {

    static public String getFileContent(String resourceFile) {
        String result = "";

        ClassLoader classLoader = ResourceFileUtils.class.getClassLoader();
        try {
            result = IOUtils.toString(classLoader.getResourceAsStream(resourceFile));
        } catch (IOException e) {
            fail("IOException reading xml file '" + resourceFile + "': " + e);
        }

        return result;
    }
}
