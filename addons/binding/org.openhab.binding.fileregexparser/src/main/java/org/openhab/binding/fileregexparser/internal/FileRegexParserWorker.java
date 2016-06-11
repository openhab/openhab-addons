/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.fileregexparser.internal;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.openhab.binding.fileregexparser.handler.FileRegexParserHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileRegexParserWorker implements Runnable {
    private static final String THING_HANDLER_THREADPOOL_NAME = "thingHandler";
    private static final String SAFE_CALL_THREADPOOL_NAME = "saveCall";
    private final Logger logger = LoggerFactory.getLogger(FileRegexParserWorker.class);
    private boolean executeWorker = false;

    protected final ExecutorService executor = ThreadPoolManager.getPool(SAFE_CALL_THREADPOOL_NAME);
    protected Future<?> future = null;

    private long filePointer = 0;
    private File fileToRead;
    private String regEx = "";
    private String line = "";
    private FileRegexParserHandler updateCallback;

    public FileRegexParserWorker(FileRegexParserHandler caller) {
        updateCallback = caller;
    }
    // TODO Auto-generated constructor stub

    public void startWorker(String fileName, String regEx) {
        logger.debug("Starting Worker thread for " + fileName);
        this.fileToRead = new File(fileName);
        this.regEx = regEx;
        filePointer = fileToRead.length();
        executeWorker = true;
        future = executor.submit(this);
        logger.debug(
                "Worker started: futureId = " + System.identityHashCode(future) + " executor: " + executor.toString());
    }

    public void stopWorker() {
        executeWorker = false;
        logger.debug("Stopping Worker thread");

        if (future != null) {
            try {
                future.get(1000, TimeUnit.MILLISECONDS);
                logger.debug("Future ended: " + System.identityHashCode(future) + ": Done: " + future.isDone()
                        + " executor: " + " executor: " + executor.toString());
            } catch (Exception e) {
                logger.debug("Future exception: " + e);
            }
        }

        /*
         * if (executor != null) {
         * executor.shutdown();
         * executor.shutdownNow();
         * }
         */

    }

    private void parseLine(String pattern, String toParse) {

        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(toParse);
        // String curGroup = new String("");
        if (m.matches()) {
            for (int i = 1; i <= m.groupCount(); i++) {
                updateCallback.updateStateReceived("matchingGroup" + i, m.group(i));

            }
        } else {
            logger.info(toParse + ": no match for " + p.toString());
        }
    }

    @Override
    public void run() {
        logger.debug("Runner run entered for file" + fileToRead.getName());
        while (executeWorker) {
            try {
                Thread.sleep(1000);
                long len = fileToRead.length();
                if (len < filePointer) {
                    // Log must have been jibbled or deleted.
                    logger.debug("File was reset. Restarting logging from start of file.");
                    filePointer = len;
                }
                RandomAccessFile raf = new RandomAccessFile(fileToRead, "r");

                raf.seek(filePointer);

                while ((line = raf.readLine()) != null) {
                    parseLine(regEx, line);
                }
                filePointer = raf.getFilePointer();
                raf.close();

            } catch (Exception e) {
                logger.error(e.toString());
                try {
                    logger.debug(fileToRead.getName() + ".Worker: Sleeping for 1 min");
                    Thread.sleep(60000);
                } catch (Exception e1) {
                    logger.error(e1.toString());
                }

            }
        }
        logger.debug(fileToRead.getName() + ".Worker: finished , futureId:" + System.identityHashCode(future));
    }

}
