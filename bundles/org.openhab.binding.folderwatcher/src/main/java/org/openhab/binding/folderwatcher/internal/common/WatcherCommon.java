/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WatcherCommon} class contains commonly used methods.
 *
 * @author Alexandr Salamatov - Initial contribution
 */
@NonNullByDefault
public class WatcherCommon {
    private static final Logger logger = LoggerFactory.getLogger(WatcherCommon.class);

    private static void initFile(File file, String watchDir) throws IOException {
        logger.debug("Initializing file {} with watch directory: {}", file.getAbsolutePath(), watchDir);
        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file))) {
            fileWriter.write(watchDir);
            fileWriter.newLine();
            logger.debug("File {} initialized successfully", file.getAbsolutePath());
        }
    }

    public static List<String> initStorage(File file, String watchDir) throws IOException {
        logger.debug("Initializing storage from file: {}, watch directory: {}", file.getAbsolutePath(), watchDir);
        List<String> returnList = List.of();
        List<String> currentFileListing = List.of();
        if (!file.exists()) {
            logger.debug("Listing file does not exist, creating parent directories and initializing file");
            Files.createDirectories(file.toPath().getParent());
            initFile(file, watchDir);
        } else {
            logger.debug("Listing file exists, reading existing entries");
            currentFileListing = Files.readAllLines(file.toPath().toAbsolutePath());
            if (currentFileListing.isEmpty()) {
                logger.debug("File is empty, initializing with watch directory");
                initFile(file, watchDir);
            } else if (currentFileListing.get(0).equals(watchDir)) {
                logger.debug("File contains {} entries for matching watch directory", currentFileListing.size());
                returnList = currentFileListing;
            } else {
                logger.debug("Watch directory mismatch in file, reinitializing. Previous: {}, Current: {}",
                        currentFileListing.get(0), watchDir);
                initFile(file, watchDir);
            }
        }
        return returnList;
    }

    public static void saveNewListing(List<String> newList, File listingFile) throws IOException {
        logger.debug("Saving {} new entries to listing file: {}", newList.size(), listingFile.getAbsolutePath());
        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(listingFile, true))) {
            for (String newFile : newList) {
                fileWriter.write(newFile);
                fileWriter.newLine();
                logger.debug("Saved entry: {}", newFile);
            }
            logger.debug("Successfully saved {} entries to listing file", newList.size());
        }
    }
}
