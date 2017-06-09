package org.openhab.voice.pollytts.internal.cloudapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.voice.TTSException;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.polly.AmazonPollyClient;
import com.amazonaws.services.polly.AmazonPollyClientBuilder;
import com.amazonaws.services.polly.model.DescribeVoicesRequest;
import com.amazonaws.services.polly.model.DescribeVoicesResult;
import com.amazonaws.services.polly.model.Voice;

/**
 * This class implements a client interface to the amazon polly service. It is a
 * static class so the Credential setup and voice identification is only performed
 * on initialization
 *
 * @author Robert Hillman - initial interface,
 */
public class PollyClientConfig {

    // Keys come from ConfigAdmin
    private static final String CONFIG_ACCESS_KEY = "accessKey";
    private static final String CONFIG_SECRET_KEY = "secretKey";
    private static final String CONFIG_REGION = "serviceRegion";
    private static String accessKey = null;
    private static String secretKey = null;
    private static String regionVal = null;

    public static AmazonPollyClient polly;
    public static List<Voice> pollyVoices;
    // translation function from unique voice label to voice id
    public static HashMap<String, String> labelToID = new HashMap<String, String>();

    public PollyClientConfig(String accessKey, String secretKey, String regionVal) throws TTSException {
        accessKey = PollyClientConfig.accessKey;
        secretKey = PollyClientConfig.secretKey;
        regionVal = PollyClientConfig.regionVal;
        PollyInit();
    }

    public PollyClientConfig(Map<String, Object> config) throws TTSException {
        System.out.println(config);

        if (config != null) {

            PollyClientConfig.accessKey = config.containsKey(CONFIG_ACCESS_KEY)
                    ? config.get(CONFIG_ACCESS_KEY).toString() : null;
            PollyClientConfig.secretKey = config.containsKey(CONFIG_SECRET_KEY)
                    ? config.get(CONFIG_SECRET_KEY).toString() : null;
            PollyClientConfig.regionVal = config.containsKey(CONFIG_REGION) ? config.get(CONFIG_REGION).toString()
                    : null;
        }
        System.out.println(PollyClientConfig.accessKey);
        System.out.println(PollyClientConfig.secretKey);
        System.out.println(PollyClientConfig.regionVal);
        PollyInit();
    }

    private void PollyInit() throws TTSException {
        // Validate access key
        if (PollyClientConfig.accessKey == null) {
            throw new TTSException("Missing access key, configure it first before using");
        }
        // Validate secret key
        if (PollyClientConfig.secretKey == null) {
            throw new TTSException("Missing secret key, configure it first before using");
        }
        // "us-east-1" ex.
        if (PollyClientConfig.regionVal == null) {
            throw new TTSException("Missing user region, configure it first before using");
        }

        AWSCredentials credentials = new BasicAWSCredentials(PollyClientConfig.accessKey, PollyClientConfig.secretKey);

        polly = (AmazonPollyClient) AmazonPollyClientBuilder.standard().withRegion(regionVal)
                .withCredentials(new AWSStaticCredentialsProvider(credentials)).build();

        DescribeVoicesRequest describeVoicesRequest = new DescribeVoicesRequest();
        // ask Amazon Polly to describe available TTS voices.
        DescribeVoicesResult describeVoicesResult = polly.describeVoices(describeVoicesRequest);
        pollyVoices = describeVoicesResult.getVoices();

        for (Voice voice : PollyClientConfig.pollyVoices) {
            labelToID.put(voice.getName(), voice.getId());
        }
    }

}
