package org.openhab.voice.cmusphinx.internal.test;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.FileAudioStream;
import org.eclipse.smarthome.core.voice.RecognitionStopEvent;
import org.eclipse.smarthome.core.voice.STTEvent;
import org.eclipse.smarthome.core.voice.STTListener;
import org.eclipse.smarthome.core.voice.SpeechRecognitionErrorEvent;
import org.eclipse.smarthome.core.voice.SpeechRecognitionEvent;
import org.junit.Test;
import org.openhab.voice.cmusphinx.internal.CMUSphinxSTTService;

public class CMUSphinxSTTServiceTest {

    private boolean done = false;
    private final String resourcesBasePath = "I:/dev/stt";

    @Test
    public void test_frFR() throws Exception {
        CMUSphinxSTTService service = new CMUSphinxSTTService();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("locale", "fr-FR");
        properties.put("acousticModelPath", this.resourcesBasePath + "/fr/cmusphinx-fr-ptm-5.2");
        properties.put("dictionaryPath", this.resourcesBasePath + "/fr/fr.dict");
        // properties.put("languageModelPath", this.resourcesBasePath + "/fr/french-small.lm");
        properties.put("grammarPath", this.resourcesBasePath + "/fr/grammar/");
        properties.put("grammarName", "digits_months");
        service.activate(properties);

        STTListener sttListener = new STTListener() {

            @Override
            public void sttEventReceived(STTEvent sttEvent) {
                System.out.println(sttEvent.getClass().getName());

                if (sttEvent instanceof SpeechRecognitionEvent) {
                    SpeechRecognitionEvent speechRecognitionEvent = (SpeechRecognitionEvent) sttEvent;
                    System.out.println(String.format("confidence: %f, transcript: %s",
                            speechRecognitionEvent.getConfidence(), speechRecognitionEvent.getTranscript()));
                }
                if (sttEvent instanceof SpeechRecognitionErrorEvent) {
                    System.out.println("ERROR: " + ((SpeechRecognitionErrorEvent) sttEvent).getMessage());
                    done = true;
                }
                if (sttEvent instanceof RecognitionStopEvent) {
                    done = true;
                }
            }
        };

        FileAudioStream audioStream = new FileAudioStream(
                new File(this.getClass().getResource("/177269__sergeeo__numbers-in-french-16k.wav").getFile()),
                new AudioFormat("WAV", "PCM_SIGNED", false, 16, 512000, 16000L));

        service.recognize(sttListener, audioStream, new Locale("fr-FR"), null);

        while (!done) {
            Thread.sleep(1000);
        }

        //////////////////////////////////

        Thread.sleep(2000);
        done = false;

        audioStream = new FileAudioStream(
                new File(this.getClass().getResource("/177267__sergeeo__french-months-16k.wav").getFile()),
                new AudioFormat("WAV", "PCM_SIGNED", false, 16, 512000, 16000L));

        service.recognize(sttListener, audioStream, new Locale("fr-FR"), null);

        while (!done) {
            Thread.sleep(1000);
        }
    }

    @Test
    public void test_deDE() throws Exception {
        Logger log = LogManager.getLogManager().getLogger("");
        for (Handler h : log.getHandlers()) {
            h.setLevel(Level.INFO);
        }
        java.util.logging.Logger.getLogger("").setLevel(java.util.logging.Level.SEVERE);
        CMUSphinxSTTService service = new CMUSphinxSTTService();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("locale", "de-DE");
        properties.put("acousticModelPath", this.resourcesBasePath + "/de/cmusphinx-de-ptm-voxforge-5.2");
        properties.put("dictionaryPath", this.resourcesBasePath + "/de/cmusphinx-voxforge-de.dic");
        // properties.put("languageModelPath", this.resourcesBasePath + "/de/cmusphinx-voxforge-de.lm.bin");
        properties.put("grammarPath", this.resourcesBasePath + "/de/grammar/");
        properties.put("grammarName", "digits_months");
        service.activate(properties);

        STTListener sttListener = new STTListener() {

            @Override
            public void sttEventReceived(STTEvent sttEvent) {
                System.out.println(sttEvent.getClass().getName());

                if (sttEvent instanceof SpeechRecognitionEvent) {
                    SpeechRecognitionEvent speechRecognitionEvent = (SpeechRecognitionEvent) sttEvent;
                    System.out.println(String.format("confidence: %f, transcript: %s",
                            speechRecognitionEvent.getConfidence(), speechRecognitionEvent.getTranscript()));
                }
                if (sttEvent instanceof SpeechRecognitionErrorEvent) {
                    System.out.println("ERROR: " + ((SpeechRecognitionErrorEvent) sttEvent).getMessage());
                    done = true;
                }
                if (sttEvent instanceof RecognitionStopEvent) {
                    done = true;
                }
            }
        };

        FileAudioStream audioStream = new FileAudioStream(
                new File(this.getClass().getResource("/177266__sergeeo__german-months-16k.wav").getFile()),
                new AudioFormat("WAV", "PCM_SIGNED", false, 16, 512000, 16000L));

        service.recognize(sttListener, audioStream, new Locale("de-DE"), null);

        while (!done) {
            Thread.sleep(1000);
        }

        //////////////////////////////////

        Thread.sleep(2000);
        done = false;

        audioStream = new FileAudioStream(
                new File(this.getClass().getResource("/69250__reinsamba__numbers-german-male-16k.wav").getFile()),
                new AudioFormat("WAV", "PCM_SIGNED", false, 16, 512000, 16000L));

        service.recognize(sttListener, audioStream, new Locale("de-DE"), null);

        while (!done) {
            Thread.sleep(1000);
        }
    }

    @Test
    public void test_enUS() throws Exception {
        CMUSphinxSTTService service = new CMUSphinxSTTService();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("locale", "en-US");
        properties.put("acousticModelPath", this.resourcesBasePath + "/en/cmusphinx-en-us-ptm-5.2");
        properties.put("dictionaryPath", this.resourcesBasePath + "/en/cmudict-en-us.dict");
        properties.put("languageModelPath", this.resourcesBasePath + "/en/en-70k-0.2-pruned.lm");
        service.activate(properties);

        STTListener sttListener = new STTListener() {

            @Override
            public void sttEventReceived(STTEvent sttEvent) {
                System.out.println(sttEvent.getClass().getName());

                if (sttEvent instanceof SpeechRecognitionEvent) {
                    SpeechRecognitionEvent speechRecognitionEvent = (SpeechRecognitionEvent) sttEvent;
                    System.out.println(String.format("confidence: %f, transcript: %s",
                            speechRecognitionEvent.getConfidence(), speechRecognitionEvent.getTranscript()));
                }
                if (sttEvent instanceof SpeechRecognitionErrorEvent) {
                    System.out.println("ERROR: " + ((SpeechRecognitionErrorEvent) sttEvent).getMessage());
                    done = true;
                }
                if (sttEvent instanceof RecognitionStopEvent) {
                    done = true;
                }
            }
        };

        FileAudioStream audioStream = new FileAudioStream(
                new File(this.getClass().getResource("/217135__catman933__voice-16k.wav").getFile()),
                new AudioFormat("WAV", "PCM_SIGNED", false, 16, 512000, 16000L));

        service.recognize(sttListener, audioStream, new Locale("en-US"), null);

        while (!done) {
            Thread.sleep(1000);
        }
    }

}
