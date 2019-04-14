/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
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
        try {
            result = IOUtils.toString(ResourceFileUtils.class.getClassLoader().getResourceAsStream(resourceFile));
        } catch (IOException e) {
            fail("IOException reading xml file '" + resourceFile + "': " + e);
        }
        return result;
    }
}
