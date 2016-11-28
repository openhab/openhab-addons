package org.openhab.binding.rf24.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Taken from http://stackoverflow.com/a/39542949/1819402
 */
public class OsChecker {
    private static boolean isWindows = false;
    private static boolean isLinux = false;
    private static boolean isHpUnix = false;
    private static boolean isPiUnix = false;
    private static boolean isSolaris = false;
    private static boolean isSunOS = false;
    private static boolean archDataModel32 = false;
    private static boolean archDataModel64 = false;

    static {
        final String os = System.getProperty("os.name").toLowerCase();
        if (os.indexOf("windows") >= 0) {
            isWindows = true;
        }
        if (os.indexOf("linux") >= 0) {
            isLinux = true;
        }
        if (os.indexOf("hp-ux") >= 0) {
            isHpUnix = true;
        }
        if (os.indexOf("hpux") >= 0) {
            isHpUnix = true;
        }
        if (os.indexOf("solaris") >= 0) {
            isSolaris = true;
        }
        if (os.indexOf("sunos") >= 0) {
            isSunOS = true;
        }
        if (System.getProperty("sun.arch.data.model").equals("32")) {
            archDataModel32 = true;
        }
        if (System.getProperty("sun.arch.data.model").equals("64")) {
            archDataModel64 = true;
        }
        if (isLinux) {
            final File file = new File("/etc", "os-release");
            try (FileInputStream fis = new FileInputStream(file);
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis))) {
                String string;
                while ((string = bufferedReader.readLine()) != null) {
                    if (string.toLowerCase().contains("raspbian")) {
                        if (string.toLowerCase().contains("name")) {
                            isPiUnix = true;
                            break;
                        }
                    }
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isRpi() {
        return isPiUnix;
    }
}
