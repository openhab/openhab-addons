package org.openhab.io.camel.internal.config;

import java.util.Map;

/**
 * This class holds Camel service configuration values.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class OpenhabCamelConfiguration {

    private static String PROPERTY_FOLDER_NAME = "folderName";
    private static String PROPERTY_CORE_POOL_SIZE = "corePoolSize";
    private static String PROPERTY_MAX_POOL_SIZE = "maxPoolSize";

    private final static String DEFAULT_FOLDER_NAME = "camel";
    private final static int DEFAULT_CORE_POOL_SIZE = 3;
    private final static int DEFAULT_MAX_POOL_SIZE = 5;

    private String folderName = DEFAULT_FOLDER_NAME;
    private int corePoolSize = DEFAULT_CORE_POOL_SIZE;
    private int maxPoolSize = DEFAULT_MAX_POOL_SIZE;

    public void applyConfig(Map<String, ?> config) throws NumberFormatException {
        if (config != null) {
            String folderNameStr = (String) config.get(PROPERTY_FOLDER_NAME);
            if (folderNameStr != null) {
                folderName = folderNameStr;
                if (folderName.equals("")) {
                    folderName = DEFAULT_FOLDER_NAME;
                }
            }
            String corePoolSizeStr = (String) config.get(PROPERTY_CORE_POOL_SIZE);
            if (corePoolSizeStr != null) {
                corePoolSize = Integer.parseInt(corePoolSizeStr);
                if (corePoolSize < 1) {
                    corePoolSize = 1;
                }
            }
            String maxPoolSizeStr = (String) config.get(PROPERTY_MAX_POOL_SIZE);
            if (maxPoolSizeStr != null) {
                maxPoolSize = Math.min(Integer.parseInt(maxPoolSizeStr), corePoolSize);
                if (maxPoolSize < 1) {
                    maxPoolSize = corePoolSize;
                }
            }
        }
    }

    public String getFolderName() {
        return folderName;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    @Override
    public String toString() {
        String str = "";

        str += "folderName = " + getFolderName();
        str += ", corePoolSize = " + getCorePoolSize();
        str += ", maxPoolSize = " + getMaxPoolSize();

        return str;
    }
}