/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.folderwatcher.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link LocalFolderWatcherConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Alexandr Salamatov - Initial contribution
 */
@NonNullByDefault
public class S3BucketWatcherConfiguration {
    public String s3BucketName = "";
    public String s3Path = "";
    public boolean s3Anonymous;
    public int pollIntervalS3;
    public String awsKey = "";
    public String awsSecret = "";
    public String awsRegion = "";
}
