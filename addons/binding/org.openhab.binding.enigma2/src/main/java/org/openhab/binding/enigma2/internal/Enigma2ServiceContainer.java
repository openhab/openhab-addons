package org.openhab.binding.enigma2.internal;

import java.util.HashMap;

public class Enigma2ServiceContainer {

    private HashMap<String, String> mapOfServices = new HashMap<String, String>();

    public void add(String serviceName, String serviceReference) {
        String cleanServiceName = Enigma2Util.cleanString(serviceName);
        String foundServiceReference = mapOfServices.get(cleanServiceName);
        if (foundServiceReference == null) {
            mapOfServices.put(cleanServiceName, serviceReference);
        }
    }

    public String get(String serviceName) {
        return mapOfServices.get(Enigma2Util.cleanString(serviceName));
    }
}
