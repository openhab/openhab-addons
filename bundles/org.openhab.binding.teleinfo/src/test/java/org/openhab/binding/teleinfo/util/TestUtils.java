package org.openhab.binding.teleinfo.util;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public final class TestUtils {

    private TestUtils() {
        // private constructor
    }

    public static File getTestFile(String testResourceName) {
        URL url = TestUtils.class.getClassLoader().getResource(testResourceName);
        try {
            return new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
}
