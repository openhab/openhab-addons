/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.folderwatcher.internal.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@link WatcherCommon} class contains commonly used methods.
 *
 * @author Alexandr Salamatov - Initial contribution
 */
public class WatcherCommon {

    public static List<String> initStorage(File file) throws IOException {
        List<String> returnList = new ArrayList<>();
        if (!file.exists()) {
            Files.createDirectories(file.toPath().getParent());
            try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file))) {
                fileWriter.write("INIT");
                fileWriter.newLine();
            }
        } else {
            returnList = Files.readAllLines(file.toPath().toAbsolutePath());
        }
        return returnList;
    }

    public static void saveNewListing(List<String> newList, File listingFile) throws IOException {
        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(listingFile, true))) {
            for (String newFile : newList) {
                fileWriter.write(newFile);
                fileWriter.newLine();
            }
        }
    }
}
