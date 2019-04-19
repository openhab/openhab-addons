package org.openhab.binding.omnilink;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum SystemType {
    OMNI(16, 30),
    LUMINA(36, 37);

    private final Set<Integer> modelNumbers;

    SystemType(Integer... modelNumbers) {
        this.modelNumbers = new HashSet<Integer>(Arrays.asList(modelNumbers));
    }

    public static SystemType getType(int modelNumber) {
        return Arrays.stream(values()).filter(s -> s.modelNumbers.contains(modelNumber)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown Model"));
    }

}