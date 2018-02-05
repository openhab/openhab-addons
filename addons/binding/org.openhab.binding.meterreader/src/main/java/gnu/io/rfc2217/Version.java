/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package gnu.io.rfc2217;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Contains library version information.
 * 
 * @author jserv
 */
public final class Version {

    /**
     * The version of this library.
     */
    public static final String JVSER_VERSION;

    private static final String PROPERTIES_RESOURCE = "/jvser.properties";
    private static final String VERSION_PROPERTY_NAME = "jvser.version";

    static {
        Properties properties = new Properties();
        InputStream input = Version.class.getResourceAsStream(PROPERTIES_RESOURCE);
        if (input == null) {
            throw new RuntimeException("can't find resource " + PROPERTIES_RESOURCE);
        }
        try {
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("unexpected exception", e);
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                // ignore
            }
        }
        JVSER_VERSION = properties.getProperty(VERSION_PROPERTY_NAME, "?");
    }

    private Version() {
    }
}
