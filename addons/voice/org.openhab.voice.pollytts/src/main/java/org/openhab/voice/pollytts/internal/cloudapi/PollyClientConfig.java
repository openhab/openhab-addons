package org.openhab.voice.pollytts.internal.cloudapi;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * static class so the service interface, Credential setup and voice identification
 * is only performed on initialization
 *
 * @author Robert Hillman - initial interface
 */
public class PollyClientConfig {

    private final Logger logger = LoggerFactory.getLogger(PollyClientConfig.class);

    private static String accessKey = null;
    private static String secretKey = null;
    private static String regionVal = null;
    private static int expireDate = 30;
    private static String audioFormat = null;
    private static long today = 0;

    public static AmazonPollyClient polly;
    public static List<Voice> pollyVoices;
    // translation function from unique voice label to voice id
    public static HashMap<String, String> labelToID = new HashMap<String, String>();

    public PollyClientConfig() {

    }

    public void setAccessKey(String key) {
        PollyClientConfig.accessKey = key;
    }

    public void setSecretKey(String key) {
        PollyClientConfig.secretKey = key;
    }

    public void setRegionVal(String val) {
        PollyClientConfig.regionVal = val;
    }

    public void setExpireDate(int days) {
        PollyClientConfig.expireDate = days;
    }

    static public int getExpireDate() {
        return PollyClientConfig.expireDate;
    }

    public void setAudioFormat(String format) {
        PollyClientConfig.audioFormat = format;
    }

    static public String getAudioFormat() {
        return PollyClientConfig.audioFormat;
    }

    static public long getlastDelete() {
        return PollyClientConfig.today;
    }

    static public void setLastDelete(long today) {
        PollyClientConfig.today = today;
        ;
    }

    public boolean initPollyServiceInterface() {
        // config file correct
        boolean configOK = true;

        // Validate access key
        if (PollyClientConfig.accessKey == null) {
            logger.error("Failed to activate PollyTTS: Missing access key, configure it first before using");
            configOK = false;
        }
        // Validate secret key
        if (PollyClientConfig.secretKey == null) {
            logger.error("Failed to activate PollyTTS: Missing secret key, configure it first before using");
            configOK = false;
        }
        // "us-east-1" ex.
        if (PollyClientConfig.regionVal == null) {
            logger.error("Failed to activate PollyTTS: Missing user region, configure it first before using");
            configOK = false;
        }
        if (!PollyClientConfig.audioFormat.equals("disabled") && !PollyClientConfig.audioFormat.equals("mp3")
                && !PollyClientConfig.audioFormat.equals("ogg")) {
            logger.error("Failed to activate PollyTTS:  Invalid Audio Format override specified in cfg: {}",
                    PollyClientConfig.audioFormat);
            configOK = false;
        }
        if (!configOK) {
            return false;
        }

        // service interface not created
        boolean initialized = false;
        try {
            AWSCredentials credentials = new BasicAWSCredentials(PollyClientConfig.accessKey,
                    PollyClientConfig.secretKey);

            polly = (AmazonPollyClient) AmazonPollyClientBuilder.standard().withRegion(regionVal)
                    .withCredentials(new AWSStaticCredentialsProvider(credentials)).build();

            DescribeVoicesRequest describeVoicesRequest = new DescribeVoicesRequest();
            // ask Amazon Polly to describe available TTS voices.
            DescribeVoicesResult describeVoicesResult = polly.describeVoices(describeVoicesRequest);
            pollyVoices = describeVoicesResult.getVoices();

            // create voice to ID translation for service invocation
            for (Voice voice : PollyClientConfig.pollyVoices) {
                labelToID.put(voice.getName(), voice.getId());
            }

            // Initialize expired check date to 172800000 +1
            // run today and ~ every 2 days if cache cleaner enabled
            // 1 day = 24 * 60 * 60 * 1000 = 86,400,000
            today = 172800001;

            initialized = true;
        } catch (Throwable t) {
            logger.error("Failed to activate PollyTTS: {}", t.getMessage(), t);
        }
        return initialized;
    }

}
