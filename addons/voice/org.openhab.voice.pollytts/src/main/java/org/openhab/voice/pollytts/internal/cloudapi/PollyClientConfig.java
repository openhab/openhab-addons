/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.voice.pollytts.internal.cloudapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.polly.AmazonPollyClient;
import com.amazonaws.services.polly.AmazonPollyClientBuilder;
import com.amazonaws.services.polly.model.DescribeVoicesRequest;
import com.amazonaws.services.polly.model.DescribeVoicesResult;
import com.amazonaws.services.polly.model.Voice;

/**
 * This class implements a client interface to the Amazon Polly service.
 * It is a static class so the service interface, credential setup and voice identification
 * is only performed on initialization
 *
 * @author Robert Hillman - Initial contribution
 */
public class PollyClientConfig {

    private static String accessKey;
    private static String secretKey;
    private static String regionVal;
    private static int expireDate = 30;
    private static String audioFormat;
    private static long today = 0;

    public static AmazonPollyClient pollyClientInterface;
    public static List<Voice> pollyVoices;
    // translation function from unique voice label to voice id
    public static Map<String, String> labelToID = new HashMap<>();

    /**
     * save the user unique accessKey for the service
     */
    public void setAccessKey(String key) {
        PollyClientConfig.accessKey = key;
    }

    /**
     * save the user unique secretKey for the service
     */
    public void setSecretKey(String key) {
        PollyClientConfig.secretKey = key;
    }

    /**
     * save regional location for the server to be used
     */
    public void setRegionVal(String val) {
        PollyClientConfig.regionVal = val;
    }

    /**
     * save the life time for cache files
     * 0 means forever
     */
    public void setExpireDate(int days) {
        PollyClientConfig.expireDate = days;
    }

    /**
     * get the life time for cache files
     */
    public static int getExpireDate() {
        return PollyClientConfig.expireDate;
    }

    /**
     * sets audio format specified for audio
     */
    public void setAudioFormat(String format) {
        PollyClientConfig.audioFormat = format;
    }

    /**
     * returns audio format specified for audio
     */
    public static String getAudioFormat() {
        return PollyClientConfig.audioFormat;
    }

    /**
     * get the date when cache was cleaned last
     */
    public static long getlastDelete() {
        return PollyClientConfig.today;
    }

    /**
     * set the date when cache was cleaned last
     */
    public static void setLastDelete(long today) {
        PollyClientConfig.today = today;
    }

    /**
     * Initializes the amazon service Credentials
     * as a one time event, saved for future reference
     */
    public void initPollyServiceInterface() throws AmazonServiceException {
        AWSCredentials credentials = new BasicAWSCredentials(PollyClientConfig.accessKey, PollyClientConfig.secretKey);

        pollyClientInterface = (AmazonPollyClient) AmazonPollyClientBuilder.standard().withRegion(regionVal)
                .withCredentials(new AWSStaticCredentialsProvider(credentials)).build();

        DescribeVoicesRequest describeVoicesRequest = new DescribeVoicesRequest();
        // ask Amazon Polly to describe available TTS voices.
        DescribeVoicesResult describeVoicesResult = pollyClientInterface.describeVoices(describeVoicesRequest);
        pollyVoices = describeVoicesResult.getVoices();

        // create voice to ID translation for service invocation
        for (Voice voice : PollyClientConfig.pollyVoices) {
            labelToID.put(voice.getName(), voice.getId());
        }

        // run today and ~ every 2 days if cache cleaner enabled
        today = TimeUnit.DAYS.toMillis(2) + 1;
    }

}
