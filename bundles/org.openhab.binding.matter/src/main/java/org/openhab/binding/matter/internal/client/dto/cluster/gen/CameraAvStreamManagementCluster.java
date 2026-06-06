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
// AUTO-GENERATED, DO NOT EDIT!

package org.openhab.binding.matter.internal.client.dto.cluster.gen;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;

/**
 * CameraAvStreamManagement
 *
 * @author Dan Cunningham - Initial contribution
 */
public class CameraAvStreamManagementCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0551;
    public static final String CLUSTER_NAME = "CameraAvStreamManagement";
    public static final String CLUSTER_PREFIX = "cameraAvStreamManagement";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_MAX_CONCURRENT_ENCODERS = "maxConcurrentEncoders";
    public static final String ATTRIBUTE_MAX_ENCODED_PIXEL_RATE = "maxEncodedPixelRate";
    public static final String ATTRIBUTE_VIDEO_SENSOR_PARAMS = "videoSensorParams";
    public static final String ATTRIBUTE_NIGHT_VISION_USES_INFRARED = "nightVisionUsesInfrared";
    public static final String ATTRIBUTE_MIN_VIEWPORT_RESOLUTION = "minViewportResolution";
    public static final String ATTRIBUTE_RATE_DISTORTION_TRADE_OFF_POINTS = "rateDistortionTradeOffPoints";
    public static final String ATTRIBUTE_MAX_CONTENT_BUFFER_SIZE = "maxContentBufferSize";
    public static final String ATTRIBUTE_MICROPHONE_CAPABILITIES = "microphoneCapabilities";
    public static final String ATTRIBUTE_SPEAKER_CAPABILITIES = "speakerCapabilities";
    public static final String ATTRIBUTE_TWO_WAY_TALK_SUPPORT = "twoWayTalkSupport";
    public static final String ATTRIBUTE_SNAPSHOT_CAPABILITIES = "snapshotCapabilities";
    public static final String ATTRIBUTE_MAX_NETWORK_BANDWIDTH = "maxNetworkBandwidth";
    public static final String ATTRIBUTE_CURRENT_FRAME_RATE = "currentFrameRate";
    public static final String ATTRIBUTE_HDR_MODE_ENABLED = "hdrModeEnabled";
    public static final String ATTRIBUTE_SUPPORTED_STREAM_USAGES = "supportedStreamUsages";
    public static final String ATTRIBUTE_ALLOCATED_VIDEO_STREAMS = "allocatedVideoStreams";
    public static final String ATTRIBUTE_ALLOCATED_AUDIO_STREAMS = "allocatedAudioStreams";
    public static final String ATTRIBUTE_ALLOCATED_SNAPSHOT_STREAMS = "allocatedSnapshotStreams";
    public static final String ATTRIBUTE_STREAM_USAGE_PRIORITIES = "streamUsagePriorities";
    public static final String ATTRIBUTE_SOFT_RECORDING_PRIVACY_MODE_ENABLED = "softRecordingPrivacyModeEnabled";
    public static final String ATTRIBUTE_SOFT_LIVESTREAM_PRIVACY_MODE_ENABLED = "softLivestreamPrivacyModeEnabled";
    public static final String ATTRIBUTE_HARD_PRIVACY_MODE_ON = "hardPrivacyModeOn";
    public static final String ATTRIBUTE_NIGHT_VISION = "nightVision";
    public static final String ATTRIBUTE_NIGHT_VISION_ILLUM = "nightVisionIllum";
    public static final String ATTRIBUTE_VIEWPORT = "viewport";
    public static final String ATTRIBUTE_SPEAKER_MUTED = "speakerMuted";
    public static final String ATTRIBUTE_SPEAKER_VOLUME_LEVEL = "speakerVolumeLevel";
    public static final String ATTRIBUTE_SPEAKER_MAX_LEVEL = "speakerMaxLevel";
    public static final String ATTRIBUTE_SPEAKER_MIN_LEVEL = "speakerMinLevel";
    public static final String ATTRIBUTE_MICROPHONE_MUTED = "microphoneMuted";
    public static final String ATTRIBUTE_MICROPHONE_VOLUME_LEVEL = "microphoneVolumeLevel";
    public static final String ATTRIBUTE_MICROPHONE_MAX_LEVEL = "microphoneMaxLevel";
    public static final String ATTRIBUTE_MICROPHONE_MIN_LEVEL = "microphoneMinLevel";
    public static final String ATTRIBUTE_MICROPHONE_AGC_ENABLED = "microphoneAgcEnabled";
    public static final String ATTRIBUTE_IMAGE_ROTATION = "imageRotation";
    public static final String ATTRIBUTE_IMAGE_FLIP_HORIZONTAL = "imageFlipHorizontal";
    public static final String ATTRIBUTE_IMAGE_FLIP_VERTICAL = "imageFlipVertical";
    public static final String ATTRIBUTE_LOCAL_VIDEO_RECORDING_ENABLED = "localVideoRecordingEnabled";
    public static final String ATTRIBUTE_LOCAL_SNAPSHOT_RECORDING_ENABLED = "localSnapshotRecordingEnabled";
    public static final String ATTRIBUTE_STATUS_LIGHT_ENABLED = "statusLightEnabled";
    public static final String ATTRIBUTE_STATUS_LIGHT_BRIGHTNESS = "statusLightBrightness";

    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * Indicates the maximum number of concurrent encoders supported by the camera.
     */
    public Integer maxConcurrentEncoders; // 0 uint8 R V
    /**
     * Indicates the maximum data rate in encoded pixels per second that the camera can produce given the hardware
     * encoder resources it has. This value is manufacturer specified.
     * If the camera supports Snapshots and requires hardware encoder resources to produce those Snapshots, then this
     * attribute shall be present, and a manufacturer specific value shall be present in each SnapshotCapabilities
     * MaxFrameRate entry that requires hardware resources to produce.
     */
    public Integer maxEncodedPixelRate; // 1 uint32 R V
    /**
     * Indicates the set of video sensor parameters for the camera. These include the video sensor dimensions, its frame
     * rate and HDR capabilities.
     */
    public VideoSensorParamsStruct videoSensorParams; // 2 VideoSensorParamsStruct R V
    /**
     * Indicates if the night vision mode is infrared based or not. A value of TRUE indicates infrared mode with a cut
     * filter being used. A value of FALSE indicates color is used. When infrared is active and the resulting content is
     * in black and white, the BlackAndWhiteActive field inside any produced AVMetadataStruct shall be TRUE.
     */
    public Boolean nightVisionUsesInfrared; // 3 bool R V
    /**
     * Indicates the minimum resolution (width and height) in pixels that the camera allows for its viewport.
     * The choice of the minimum viewport width and height is, typically, directed towards maintaining the best image
     * quality (reduced distortion) for a given size of the video sensor, for different camera functions, e.g., digital
     * zoom. Furthermore, the minimum viewport size and the video sensor size also dictate the upscaling capabilities
     * and requirements of the image processor.
     */
    public VideoResolutionStruct minViewportResolution; // 4 VideoResolutionStruct R V
    /**
     * This attribute shall list the set of rate distortion trade-off points between resolution, frame rate and bitrate
     * for each supported hardware encoder.
     */
    public List<RateDistortionTradeOffPointsStruct> rateDistortionTradeOffPoints; // 5 list R V
    /**
     * Indicates the maximum size of the content buffer in bytes. This buffer holds the compressed and/or raw content
     * for audio/video pre-roll, queued transmissions, the current frame for each snapshot stream, and the metadata
     * context for recording events. For devices which support more than one encoder, the device shall evenly allocate
     * this buffer space amongst all streams that utilize pre-roll content such as the Push AV Stream Transport Cluster.
     */
    public Integer maxContentBufferSize; // 6 uint32 R V
    /**
     * Indicates the audio capabilities of the microphone in terms of the codec used, supported sample rates and the
     * number of channels.
     */
    public AudioCapabilitiesStruct microphoneCapabilities; // 7 AudioCapabilitiesStruct R V
    /**
     * Indicates the audio capabilities of the speaker in terms of the supported codecs, sample rates, and the number of
     * channels when a speaker is present.
     */
    public AudioCapabilitiesStruct speakerCapabilities; // 8 AudioCapabilitiesStruct R V
    /**
     * Indicates the type of two-way talk support the device has, e.g., NotSupported, HalfDuplex, or FullDuplex.
     */
    public TwoWayTalkSupportTypeEnum twoWayTalkSupport; // 9 TwoWayTalkSupportTypeEnum R V
    /**
     * Indicates the list of supported snapshot capabilities the device has. This list is a set of entries for image
     * codec, resolution, maximum frame rate, hardware encoder, and encoded pixels.
     */
    public List<SnapshotCapabilitiesStruct> snapshotCapabilities; // 10 list R V
    /**
     * Indicates the maximum network bandwidth in bits per second that the device would consume for the transmission of
     * its media streams.
     */
    public Integer maxNetworkBandwidth; // 11 uint32 R V
    /**
     * Indicates the current logical frame rate of the sensor in frames per second.
     */
    public Integer currentFrameRate; // 12 uint16 R V
    /**
     * This attribute indicates the currently selected High Dynamic Range (HDR) mode. A value of TRUE indicates that HDR
     * video capturing is enabled. Otherwise, HDR video capturing is disabled.
     */
    public Boolean hdrModeEnabled; // 13 bool RW M
    /**
     * Indicates the list of Stream Usages that are supported by the camera. Manufacturers shall provide a usages list
     * that is appropriate to their product. If a usage is found in this list, then it can be used in the
     * StreamUsagePriorities attribute. The ordering and values of this list shall match the values found in
     * StreamUsagePriorities after a factory reset.
     */
    public List<StreamUsageEnum> supportedStreamUsages; // 14 list R V
    /**
     * Indicates the list of allocated video streams on the device.
     */
    public List<VideoStreamStruct> allocatedVideoStreams; // 15 list R V
    /**
     * Indicates the list of allocated audio streams on the device.
     */
    public List<AudioStreamStruct> allocatedAudioStreams; // 16 list R V
    /**
     * Indicates the list of allocated snapshot streams on the device.
     */
    public List<SnapshotStreamStruct> allocatedSnapshotStreams; // 17 list R V
    /**
     * Indicates a list of the video stream usages represented in a ranked order of their priorities, starting with
     * Index 0 having the stream usage type with the highest priority. See Resource Management and Stream Priorities for
     * further details. Only usages found in the SupportedStreamUsages attribute can be included. To change the
     * contents, use the SetStreamPriorities command. Manufacturers shall provide a default ranked priorities list that
     * is appropriate to their product and this default ranking shall exactly match the contents of
     * SupportedStreamUsages. Clients can use the contents of the SupportedStreamUsages to restore this default state if
     * the contents have been changed by the SetStreamPriorities command.
     */
    public List<StreamUsageEnum> streamUsagePriorities; // 18 list R V
    /**
     * This attribute indicates the current value of the soft privacy mode for transports using the Stream Usage types
     * Recording and Analysis. A value of TRUE indicates that delivery of video frames and audio samples from any
     * streams to these transports is skipped. A value of TRUE also indicates that no new transports using these stream
     * usage values can be created or started. When FALSE, these transports can be resumed or started, and have video
     * frames and audio samples delivered.
     * When this attribute is set to TRUE, any active WebRTC transports using these stream usage types shall terminate
     * the session by calling End using WebRTCEndReasonEnum PrivacyMode.
     */
    public Boolean softRecordingPrivacyModeEnabled; // 19 bool RW VO
    /**
     * This attribute indicates the current value of the soft privacy mode for transports using the Stream Usage type
     * LiveView. A value of TRUE indicates that delivery of video frames and audio samples from any streams to these
     * transports is skipped. A value of TRUE also indicates that no new transports using this stream usage type can be
     * created or started. When FALSE, these transports can be resumed or started, and have video frames and audio
     * samples delivered.
     * When this attribute is set to TRUE, any active WebRTC transports using this stream usage type shall terminate the
     * session by calling End using WebRTCEndReasonEnum PrivacyMode.
     */
    public Boolean softLivestreamPrivacyModeEnabled; // 20 bool RW VO
    /**
     * This attribute indicates the current value of the hard privacy mode for all streams. This is controlled via a
     * physical button or switch, potentially. A value of TRUE indicates that all streams are currently paused. When
     * FALSE, the streams may resume if they are not already paused by their corresponding soft privacy mode.
     */
    public Boolean hardPrivacyModeOn; // 21 bool R V
    /**
     * This attribute indicates the currently selected Night Vision mode. A value of Off means the device will never
     * activate its Night Vision mode of operation. A value of On means the Night Vision mode of operation is always
     * active. A value of Auto means the device will automatically move between active and inactive based on the light
     * level it detects.
     */
    public TriStateAutoEnum nightVision; // 22 TriStateAutoEnum RW M
    /**
     * This attribute indicates the currently selected the Night Vision Illumination mode. A value of Off means the
     * device will never activate its built-in Night Vision Illumination. A value of On means the built-in Night Vision
     * Illumination is always active. A value of Auto means the device will automatically enable its built-in Night
     * Vision Illumination based on the light level it detects.
     */
    public TriStateAutoEnum nightVisionIllum; // 23 TriStateAutoEnum RW M
    /**
     * This attribute shall be a ViewportStruct representing the viewport to apply to all streams.
     * The coordinate values represent the upper left corner and lower right corner coordinates of the source rectangle
     * on the sensor. The coordinate values are within the two-dimensional Cartesian plane of size SensorWidth and
     * SensorHeight (See VideoSensorParamsStruct in the VideoSensorParams) with the origin (0,0) being the upper left
     * corner, positive X and Y values moving right and down across the Cartesian plane respectively, and (SensorWidth,
     * SensorHeight) being the lower right corner.
     * When changing the Viewport, the aspect ratio of the sensor as indicated in the VideoSensorParams attribute SHOULD
     * be preserved.
     * After a factory reset, this shall default to {0, 0, SensorWidth,SensorHeight}, using the SensorWidth and
     * SensorHeight fields from the VideoSensorParams attribute.
     * When this attribute is changed, all Viewport values found in DPTZStreams shall be updated to the new values set
     * here.
     */
    public ViewportStruct viewport; // 24 ViewportStruct RW M
    /**
     * This attribute indicates whether the speaker is currently muted or not. A value of TRUE indicates that the
     * speaker has been muted and shall not play anything. A value of FALSE indicates that the Speaker is enabled.
     */
    public Boolean speakerMuted; // 25 bool RW M
    /**
     * This attribute indicates the current volume level of the speaker.
     */
    public Integer speakerVolumeLevel; // 26 uint8 RW M
    /**
     * This attribute indicates the maximum value of the SpeakerVolumeLevel that can be assigned.
     */
    public Integer speakerMaxLevel; // 27 uint8 R M
    /**
     * This attribute indicates the minimum value of the SpeakerVolumeLevel that can be assigned.
     */
    public Integer speakerMinLevel; // 28 uint8 R M
    /**
     * This attribute indicates whether the microphone is currently muted or not. A value of TRUE indicates that the
     * microphone has been muted. In this state, the microphone data shall be replaced with all 0 bits, representing
     * silence. A value of FALSE indicates that the microphone is On and is capable of transmitting audio.
     */
    public Boolean microphoneMuted; // 29 bool RW M
    /**
     * This attribute indicates the current gain or volume level of the microphone.
     */
    public Integer microphoneVolumeLevel; // 30 uint8 RW M
    /**
     * This attribute indicates the maximum value of the MicrophoneVolumeLevel that can be assigned.
     */
    public Integer microphoneMaxLevel; // 31 uint8 R M
    /**
     * This attribute indicates the minimum value of the MicrophoneVolumeLevel that can be assigned.
     */
    public Integer microphoneMinLevel; // 32 uint8 R M
    /**
     * This attribute indicates the currently selected AGC (Automatic Gain Control) mode for the microphone. A value of
     * TRUE indicates that microphone AGC is enabled. Otherwise, it is disabled.
     */
    public Boolean microphoneAgcEnabled; // 33 bool RW M
    /**
     * This attribute indicates the amount of clockwise rotation in degrees that the image has been subjected to.
     */
    public Integer imageRotation; // 34 uint16 RW M
    /**
     * This attribute indicates whether the image has been flipped horizontally or not. A value of TRUE indicates that
     * the image has been flipped horizontally.
     */
    public Boolean imageFlipHorizontal; // 35 bool RW M
    /**
     * This attribute indicates whether the image has been flipped vertically or not. A value of TRUE indicates that the
     * image has been flipped vertically.
     */
    public Boolean imageFlipVertical; // 36 bool RW M
    /**
     * This attribute indicates whether local storage based video recording is enabled. A value of TRUE indicates that
     * local storage based video recording has been enabled.
     */
    public Boolean localVideoRecordingEnabled; // 37 bool RW M
    /**
     * This attribute indicates whether local storage based snapshot recording is enabled. A value of TRUE indicates
     * that local storage based snapshot recording has been enabled.
     */
    public Boolean localSnapshotRecordingEnabled; // 38 bool RW M
    /**
     * This attribute indicates whether the status light has been enabled or not. A value of TRUE indicates the status
     * light has been enabled. When enabled, the camera may use it for visual signaling purposes to indicate various
     * states of the camera.
     */
    public Boolean statusLightEnabled; // 39 bool RW M
    /**
     * This attribute indicates the brightness level of the status light.
     */
    public ThreeLevelAutoEnum statusLightBrightness; // 40 ThreeLevelAutoEnum RW M

    // Structs
    /**
     * This struct is used to define a video sensor and its characteristics.
     */
    public static class VideoSensorParamsStruct {
        /**
         * This field shall indicate the practical width of the video sensor in pixels. This value is used for various
         * purposes such as resolution control, boundaries for the Zone Management Cluster and digital Pan/Tilt/Zoom
         * commands in the Camera AV Settings User Level Management.
         */
        public Integer sensorWidth; // uint16
        /**
         * This field shall indicate the practical height of the video sensor in pixels. This value is used for various
         * purposes such as resolution control, boundaries for the Zone Management Cluster and digital Pan/Tilt/Zoom
         * commands in the Camera AV Settings User Level Management.
         */
        public Integer sensorHeight; // uint16
        /**
         * This field shall indicate the maximum frame rate, in frames per second, that the video sensor is capable of
         * supporting.
         */
        public Integer maxFps; // uint16
        /**
         * This field shall indicate the maximum frame rate, in frames per second, that the video sensor is capable of
         * supporting when HDR is enabled. The value may be less than or equal to the MaxFPS.
         */
        public Integer maxHdrfps; // uint16

        public VideoSensorParamsStruct(Integer sensorWidth, Integer sensorHeight, Integer maxFps, Integer maxHdrfps) {
            this.sensorWidth = sensorWidth;
            this.sensorHeight = sensorHeight;
            this.maxFps = maxFps;
            this.maxHdrfps = maxHdrfps;
        }
    }

    /**
     * This object defines the resolution parameters in pixels which can be used for defining the resolutions of
     * different video streams.
     */
    public static class VideoResolutionStruct {
        /**
         * This field shall indicate the width, in number of pixels, for a frame.
         */
        public Integer width; // uint16
        /**
         * This field shall indicate the height, in number of pixels, for a frame.
         */
        public Integer height; // uint16

        public VideoResolutionStruct(Integer width, Integer height) {
            this.width = width;
            this.height = height;
        }
    }

    /**
     * This struct is used to define a set of parameters of the hardware video encoder that alter the rate distortion
     * trade-off points. The points are expressed as the minimum bitrate and resolution for each supported codec type.
     */
    public static class RateDistortionTradeOffPointsStruct {
        /**
         * This field shall indicate the type of video codec based on the supported VideoCodecEnum types.
         */
        public VideoCodecEnum codec; // VideoCodecEnum
        /**
         * This field shall indicate the resolution in pixels for a specific rate distortion trade-off point.
         */
        public VideoResolutionStruct resolution; // VideoResolutionStruct
        /**
         * This field shall indicate the minimum bitrate for a specific rate distortion trade-off point expressed as
         * bits per second.
         */
        public Integer minBitRate; // uint32

        public RateDistortionTradeOffPointsStruct(VideoCodecEnum codec, VideoResolutionStruct resolution,
                Integer minBitRate) {
            this.codec = codec;
            this.resolution = resolution;
            this.minBitRate = minBitRate;
        }
    }

    /**
     * This struct is used to define the set of parameters that characterize a snapshot image that is used to build a
     * snapshot stream.
     */
    public static class SnapshotCapabilitiesStruct {
        /**
         * This field shall indicate the resolution in pixels of the snapshot image.
         */
        public VideoResolutionStruct resolution; // VideoResolutionStruct
        /**
         * This field shall indicate the maximum frame rate in frames per second of the snapshot stream.
         */
        public Integer maxFrameRate; // uint16
        /**
         * This field shall indicate the format of the snapshot image, e.g., JPEG, as specified in ImageCodecEnum.
         */
        public ImageCodecEnum imageCodec; // ImageCodecEnum
        /**
         * This field shall indicate if this entry requires using any resources from the available MaxEncodedPixelRate.
         * If true, clients need to include this entry's Resolution and MaxFrameRate in the calculation for determining
         * overall stream allocation resources.
         */
        public Boolean requiresEncodedPixels; // bool
        /**
         * This field shall indicate if this entry requires using a hardware encoder and thus needs to be considered
         * when determining overall stream allocation resources.
         * If true, the device requires an encoder from MaxConcurrentEncoders for this combination of ImageCodec,
         * Resolution, and MaxFrameRate.
         * If false, the device can produce this combination without needing a dedicated encoder.
         * This field is only considered if RequiresEncodedPixels is true.
         */
        public Boolean requiresHardwareEncoder; // bool

        public SnapshotCapabilitiesStruct(VideoResolutionStruct resolution, Integer maxFrameRate,
                ImageCodecEnum imageCodec, Boolean requiresEncodedPixels, Boolean requiresHardwareEncoder) {
            this.resolution = resolution;
            this.maxFrameRate = maxFrameRate;
            this.imageCodec = imageCodec;
            this.requiresEncodedPixels = requiresEncodedPixels;
            this.requiresHardwareEncoder = requiresHardwareEncoder;
        }
    }

    /**
     * This struct is used to express the audio capabilities of the camera.
     */
    public static class AudioCapabilitiesStruct {
        /**
         * This field shall indicate the maximum number of channels supported by an audio stream from the camera.
         */
        public Integer maxNumberOfChannels; // uint8
        /**
         * This field shall indicate the list of audio codecs from AudioCodecEnum that are supported by the camera.
         */
        public List<AudioCodecEnum> supportedCodecs; // list
        /**
         * This field shall indicate the list of sample rates that are supported by the audio stream from the camera
         * expressed in Hz, e.g., (48000, 32000, 16000).
         */
        public List<Integer> supportedSampleRates; // list
        /**
         * This field shall indicate the list of bit depths that are supported by the audio stream, e.g., (16-bit,
         * 24-bit).
         */
        public List<Integer> supportedBitDepths; // list

        public AudioCapabilitiesStruct(Integer maxNumberOfChannels, List<AudioCodecEnum> supportedCodecs,
                List<Integer> supportedSampleRates, List<Integer> supportedBitDepths) {
            this.maxNumberOfChannels = maxNumberOfChannels;
            this.supportedCodecs = supportedCodecs;
            this.supportedSampleRates = supportedSampleRates;
            this.supportedBitDepths = supportedBitDepths;
        }
    }

    /**
     * This struct is used to capture all constituent parameters of a video stream in order to fully characterize it.
     */
    public static class VideoStreamStruct {
        /**
         * This field shall indicate the uniquely allocated identifier for the video stream.
         */
        public Integer videoStreamId; // VideoStreamID
        /**
         * This field shall indicate the usage of the stream as described in StreamUsageEnum.
         */
        public StreamUsageEnum streamUsage; // StreamUsageEnum
        /**
         * This field shall indicate the type of video codec being used by the corresponding video stream as described
         * in VideoCodecEnum.
         */
        public VideoCodecEnum videoCodec; // VideoCodecEnum
        /**
         * This field shall indicate the minimum frame rate in frames per second for the corresponding video stream.
         */
        public Integer minFrameRate; // uint16
        /**
         * This field shall indicate the maximum frame rate in frames per second for the corresponding video stream.
         */
        public Integer maxFrameRate; // uint16
        /**
         * This field shall indicate the minimum resolution for the corresponding video stream.
         */
        public VideoResolutionStruct minResolution; // VideoResolutionStruct
        /**
         * This field shall indicate the maximum resolution for the corresponding video stream.
         */
        public VideoResolutionStruct maxResolution; // VideoResolutionStruct
        /**
         * This field shall indicate the minimum bitrate for the corresponding video stream in bits per second.
         */
        public Integer minBitRate; // uint32
        /**
         * This field shall indicate the maximum bitrate for the corresponding video stream in bits per second.
         */
        public Integer maxBitRate; // uint32
        /**
         * This field shall indicate the duration in milliseconds before a regular key-frame shall be generated. A value
         * of 0 shall mean that no regular key-frames are generated. When using push transports with a stream, it is
         * recommended to use a value of 4000 (4 seconds). If the value requested does not exactly align with the
         * framerate, then the next frame after the requested value shall be a regular key-frame.
         */
        public Integer keyFrameInterval; // uint16
        /**
         * This field indicates the status of an applied watermark for the specific video stream. An Enabled value of
         * TRUE means that watermarking has been enabled for that stream.
         */
        public Boolean watermarkEnabled; // bool
        /**
         * This field indicates the status of the OSD (On-Screen Display) for the specific video stream. An Enabled
         * value of TRUE means that OSD has been enabled for that stream.
         */
        public Boolean osdEnabled; // bool
        /**
         * This field shall indicate the number of entities currently using this video stream. The node shall recompute
         * this field to reflect the correct value at runtime (e.g., when restored from a persisted value after a
         * reboot).
         */
        public Integer referenceCount; // uint8

        public VideoStreamStruct(Integer videoStreamId, StreamUsageEnum streamUsage, VideoCodecEnum videoCodec,
                Integer minFrameRate, Integer maxFrameRate, VideoResolutionStruct minResolution,
                VideoResolutionStruct maxResolution, Integer minBitRate, Integer maxBitRate, Integer keyFrameInterval,
                Boolean watermarkEnabled, Boolean osdEnabled, Integer referenceCount) {
            this.videoStreamId = videoStreamId;
            this.streamUsage = streamUsage;
            this.videoCodec = videoCodec;
            this.minFrameRate = minFrameRate;
            this.maxFrameRate = maxFrameRate;
            this.minResolution = minResolution;
            this.maxResolution = maxResolution;
            this.minBitRate = minBitRate;
            this.maxBitRate = maxBitRate;
            this.keyFrameInterval = keyFrameInterval;
            this.watermarkEnabled = watermarkEnabled;
            this.osdEnabled = osdEnabled;
            this.referenceCount = referenceCount;
        }
    }

    /**
     * This struct is used to capture all constituent parameters of an audio stream in order to fully characterize it.
     */
    public static class AudioStreamStruct {
        /**
         * This field shall indicate the uniquely allocated identifier for the audio stream.
         */
        public Integer audioStreamId; // AudioStreamID
        /**
         * This field shall indicate the usage of stream as described in StreamUsageEnum.
         */
        public StreamUsageEnum streamUsage; // StreamUsageEnum
        /**
         * This field shall indicate the type of audio codec being used by the corresponding audio stream as described
         * in AudioCodecEnum.
         */
        public AudioCodecEnum audioCodec; // AudioCodecEnum
        /**
         * This field shall indicate the number of independent channels or tracks being used by the corresponding audio
         * stream. e.g., 1 for mono, 2 for stereo
         */
        public Integer channelCount; // uint8
        /**
         * This field shall indicate the audio sample rate, in hertz (Hz).
         */
        public Integer sampleRate; // uint32
        /**
         * This field shall indicate the target bit rate in bits per second of the audio stream.
         */
        public Integer bitRate; // uint32
        /**
         * This field shall indicate the bit depth (8, 16, 24 or 32 bits) of the audio stream. It represents the number
         * of bits of information used to represent each sample of the audio signal, and affects the resolution and
         * dynamic range of the audio.
         */
        public Integer bitDepth; // uint8
        /**
         * This field shall indicate the number of entities currently using this audio stream. The node shall recompute
         * this field to reflect the correct value at runtime (e.g., when restored from a persisted value after a
         * reboot).
         */
        public Integer referenceCount; // uint8

        public AudioStreamStruct(Integer audioStreamId, StreamUsageEnum streamUsage, AudioCodecEnum audioCodec,
                Integer channelCount, Integer sampleRate, Integer bitRate, Integer bitDepth, Integer referenceCount) {
            this.audioStreamId = audioStreamId;
            this.streamUsage = streamUsage;
            this.audioCodec = audioCodec;
            this.channelCount = channelCount;
            this.sampleRate = sampleRate;
            this.bitRate = bitRate;
            this.bitDepth = bitDepth;
            this.referenceCount = referenceCount;
        }
    }

    /**
     * This struct is used to capture all constituent parameters of a snapshot stream in order to fully characterize it.
     */
    public static class SnapshotStreamStruct {
        /**
         * This field shall indicate the uniquely allocated identifier for the snapshot stream.
         */
        public Integer snapshotStreamId; // SnapshotStreamID
        /**
         * This field shall indicate the type of image codec being used by the corresponding snapshot stream as
         * described in ImageCodecEnum.
         */
        public ImageCodecEnum imageCodec; // ImageCodecEnum
        /**
         * This field shall indicate the frame rate as frames per second of the snapshot stream.
         */
        public Integer frameRate; // uint16
        /**
         * This field shall indicate the minimum resolution for the corresponding snapshot stream.
         */
        public VideoResolutionStruct minResolution; // VideoResolutionStruct
        /**
         * This field shall indicate the maximum resolution for the corresponding snapshot stream.
         */
        public VideoResolutionStruct maxResolution; // VideoResolutionStruct
        /**
         * This field shall indicate a generic quality metric (integer between 1 and 100) as an input parameter to the
         * image codec. A lower number indicates lower image quality. A higher value indicates higher image quality but
         * larger file size and higher bit rate.
         */
        public Integer quality; // uint8
        /**
         * This field shall indicate the number of entities currently using this snapshot stream. The node shall
         * recompute this field to reflect the correct value at runtime (e.g., when restored from a persisted value
         * after a reboot).
         */
        public Integer referenceCount; // uint8
        /**
         * This field shall indicate if this entry counts in the system encoded pixel rate calculation.
         * This shall be true if the SnapshotCapabilitiesStruct for the selected ImageCodec and MaxResolution has
         * RequiresEncodedPixels set to true.
         */
        public Boolean encodedPixels; // bool
        /**
         * This field shall indicate if one of the system hardware encoders is used by this snapshot stream.
         * This shall be true if the SnapshotCapabilitiesStruct for the selected ImageCodec and MaxResolution has
         * RequiresHardwareEncoder set to true.
         */
        public Boolean hardwareEncoder; // bool
        /**
         * This field indicates the status of an applied watermark for the specific snapshot stream. A value of TRUE
         * means that watermarking has been enabled for that stream.
         */
        public Boolean watermarkEnabled; // bool
        /**
         * This field indicates the status of the OSD (On-Screen Display) for the specific snapshot stream. A value of
         * TRUE means that OSD has been enabled for that stream.
         */
        public Boolean osdEnabled; // bool

        public SnapshotStreamStruct(Integer snapshotStreamId, ImageCodecEnum imageCodec, Integer frameRate,
                VideoResolutionStruct minResolution, VideoResolutionStruct maxResolution, Integer quality,
                Integer referenceCount, Boolean encodedPixels, Boolean hardwareEncoder, Boolean watermarkEnabled,
                Boolean osdEnabled) {
            this.snapshotStreamId = snapshotStreamId;
            this.imageCodec = imageCodec;
            this.frameRate = frameRate;
            this.minResolution = minResolution;
            this.maxResolution = maxResolution;
            this.quality = quality;
            this.referenceCount = referenceCount;
            this.encodedPixels = encodedPixels;
            this.hardwareEncoder = hardwareEncoder;
            this.watermarkEnabled = watermarkEnabled;
            this.osdEnabled = osdEnabled;
        }
    }

    /**
     * This struct is used to encode the metadata generated by the device and is included by the various transports that
     * handle audio and video streams.
     * When encoded in TLV binary format and placed inside other standards, this shall be represented using the RFC 8141
     * compliant string urn:csa:matter:av-metadata.
     */
    public static class AVMetadataStruct {
        /**
         * This field shall represent the UTC time that this metadata belongs to. The field is sourced from the Time
         * Synchronization cluster's UTCTime attribute.
         * If null, the device has no current source of wall clock time.
         */
        public BigInteger utcTime; // epoch-us
        /**
         * This field shall represent the list of Motion Zones that are currently triggered.
         */
        public List<Integer> motionZonesActive; // list
        /**
         * This field shall indicate if the sensor is currently active in black and white only mode. A value of true
         * means the sensor and video encode process is operating in black and white only mode.
         */
        public Boolean blackAndWhiteActive; // bool
        /**
         * This field shall be an octet string representing arbitrary format user defined metadata attached. The
         * UserDefined field of the ManuallyTriggerTransport command can be used to populate this field. The format and
         * meaning of this field is not defined in this specification and is up to the users, vendors, or ecosystems
         * deploying it.
         */
        public OctetString userDefined; // octstr

        public AVMetadataStruct(BigInteger utcTime, List<Integer> motionZonesActive, Boolean blackAndWhiteActive,
                OctetString userDefined) {
            this.utcTime = utcTime;
            this.motionZonesActive = motionZonesActive;
            this.blackAndWhiteActive = blackAndWhiteActive;
            this.userDefined = userDefined;
        }
    }

    // Enums
    /**
     * This data type provides an enumeration of the video codecs supported by the camera.
     */
    public enum VideoCodecEnum implements MatterEnum {
        H264(0, "H 264"),
        HEVC(1, "Hevc"),
        VVC(2, "Vvc"),
        AV1(3, "Av 1");

        private final Integer value;
        private final String label;

        private VideoCodecEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /**
     * This data type provides an enumeration of the audio codecs supported by the camera.
     */
    public enum AudioCodecEnum implements MatterEnum {
        OPUS(0, "Opus"),
        AAC_LC(1, "Aac Lc");

        private final Integer value;
        private final String label;

        private AudioCodecEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /**
     * This data type provides an enumeration of the image codecs supported by the camera.
     */
    public enum ImageCodecEnum implements MatterEnum {
        JPEG(0, "Jpeg"),
        HEIC(1, "Heic");

        private final Integer value;
        private final String label;

        private ImageCodecEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /**
     * This data type provides an enumeration of the different modes of bi-directional audio communication that are
     * supported by the camera.
     */
    public enum TwoWayTalkSupportTypeEnum implements MatterEnum {
        NOT_SUPPORTED(0, "Not Supported"),
        HALF_DUPLEX(1, "Half Duplex"),
        FULL_DUPLEX(2, "Full Duplex");

        private final Integer value;
        private final String label;

        private TwoWayTalkSupportTypeEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /**
     * This data type is derived from enum8 and is used for tri-state settings on a device, where a setting can be in
     * one of three states, i.e., On, Off, or Automatic.
     */
    public enum TriStateAutoEnum implements MatterEnum {
        OFF(0, "Off"),
        ON(1, "On"),
        AUTO(2, "Auto");

        private final Integer value;
        private final String label;

        private TriStateAutoEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    // Bitmaps
    public static class FeatureMap {
        /**
         * 
         * The Audio feature indicates the ability of the node to support audio streams.
         */
        public boolean audio;
        /**
         * 
         * The Video feature indicates the ability of the node to support video streams. The video streams could be for
         * either live streaming or recording stream transfer, or both.
         */
        public boolean video;
        /**
         * 
         * The Snapshot feature indicates the ability of the node to support snapshot streams.
         */
        public boolean snapshot;
        /**
         * 
         * The Privacy feature indicates the ability of the node to support privacy settings.
         */
        public boolean privacy;
        /**
         * 
         * The Speaker feature indicates the ability of the node to support audio playback via a speaker. The Audio
         * feature shall be supported if the Speaker feature is supported. Nodes which support this feature shall have
         * the ability to perform playback audio mixing in software or hardware.
         */
        public boolean speaker;
        /**
         * 
         * Image control supported
         */
        public boolean imageControl;
        /**
         * 
         * The Watermark feature indicates the ability of the node to apply a manufacturer watermark logo on a video
         * stream.
         */
        public boolean watermark;
        /**
         * 
         * The On Screen Display (OSD) feature indicates the ability of the node to display text such as date, time,
         * timezone, and/or device name, etc. for a video stream.
         */
        public boolean onScreenDisplay;
        /**
         * 
         * The Local Storage feature indicates that this device has the ability to store recordings and/or snapshots on
         * this device itself. While this specification defines the ability to have this feature and enable or disable
         * it only, it does not currently define any way to access or manage this storage.
         */
        public boolean localStorage;
        /**
         * 
         * The High Dynamic Range feature indicates that the sensor on this device supports operating in High Dynamic
         * Range mode, in addition to a normal operating mode.
         */
        public boolean highDynamicRange;
        /**
         * 
         * The Night Vision feature indicates the ability to operate in a low light environment mode, in addition to a
         * normal operating mode.
         */
        public boolean nightVision;

        public FeatureMap(boolean audio, boolean video, boolean snapshot, boolean privacy, boolean speaker,
                boolean imageControl, boolean watermark, boolean onScreenDisplay, boolean localStorage,
                boolean highDynamicRange, boolean nightVision) {
            this.audio = audio;
            this.video = video;
            this.snapshot = snapshot;
            this.privacy = privacy;
            this.speaker = speaker;
            this.imageControl = imageControl;
            this.watermark = watermark;
            this.onScreenDisplay = onScreenDisplay;
            this.localStorage = localStorage;
            this.highDynamicRange = highDynamicRange;
            this.nightVision = nightVision;
        }
    }

    public CameraAvStreamManagementCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 1361, "CameraAvStreamManagement");
    }

    protected CameraAvStreamManagementCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command shall allocate an audio stream on the camera and return an allocated audio stream identifier.
     */
    public static ClusterCommand audioStreamAllocate(StreamUsageEnum streamUsage, AudioCodecEnum audioCodec,
            Integer channelCount, Integer sampleRate, Integer bitRate, Integer bitDepth) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (streamUsage != null) {
            map.put("streamUsage", streamUsage);
        }
        if (audioCodec != null) {
            map.put("audioCodec", audioCodec);
        }
        if (channelCount != null) {
            map.put("channelCount", channelCount);
        }
        if (sampleRate != null) {
            map.put("sampleRate", sampleRate);
        }
        if (bitRate != null) {
            map.put("bitRate", bitRate);
        }
        if (bitDepth != null) {
            map.put("bitDepth", bitDepth);
        }
        return new ClusterCommand("audioStreamAllocate", map);
    }

    /**
     * This command shall deallocate an audio stream on the camera, corresponding to the given audio stream identifier.
     */
    public static ClusterCommand audioStreamDeallocate(Integer audioStreamId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (audioStreamId != null) {
            map.put("audioStreamId", audioStreamId);
        }
        return new ClusterCommand("audioStreamDeallocate", map);
    }

    /**
     * This command shall allocate a video stream on the camera and return an allocated video stream identifier.
     */
    public static ClusterCommand videoStreamAllocate(StreamUsageEnum streamUsage, VideoCodecEnum videoCodec,
            Integer minFrameRate, Integer maxFrameRate, VideoResolutionStruct minResolution,
            VideoResolutionStruct maxResolution, Integer minBitRate, Integer maxBitRate, Integer keyFrameInterval,
            Boolean watermarkEnabled, Boolean osdEnabled) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (streamUsage != null) {
            map.put("streamUsage", streamUsage);
        }
        if (videoCodec != null) {
            map.put("videoCodec", videoCodec);
        }
        if (minFrameRate != null) {
            map.put("minFrameRate", minFrameRate);
        }
        if (maxFrameRate != null) {
            map.put("maxFrameRate", maxFrameRate);
        }
        if (minResolution != null) {
            map.put("minResolution", minResolution);
        }
        if (maxResolution != null) {
            map.put("maxResolution", maxResolution);
        }
        if (minBitRate != null) {
            map.put("minBitRate", minBitRate);
        }
        if (maxBitRate != null) {
            map.put("maxBitRate", maxBitRate);
        }
        if (keyFrameInterval != null) {
            map.put("keyFrameInterval", keyFrameInterval);
        }
        if (watermarkEnabled != null) {
            map.put("watermarkEnabled", watermarkEnabled);
        }
        if (osdEnabled != null) {
            map.put("osdEnabled", osdEnabled);
        }
        return new ClusterCommand("videoStreamAllocate", map);
    }

    /**
     * This command shall be used to modify a stream specified by the VideoStreamID.
     */
    public static ClusterCommand videoStreamModify(Integer videoStreamId, Boolean watermarkEnabled,
            Boolean osdEnabled) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (videoStreamId != null) {
            map.put("videoStreamId", videoStreamId);
        }
        if (watermarkEnabled != null) {
            map.put("watermarkEnabled", watermarkEnabled);
        }
        if (osdEnabled != null) {
            map.put("osdEnabled", osdEnabled);
        }
        return new ClusterCommand("videoStreamModify", map);
    }

    /**
     * This command shall deallocate a video stream on the camera, corresponding to the given video stream identifier.
     */
    public static ClusterCommand videoStreamDeallocate(Integer videoStreamId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (videoStreamId != null) {
            map.put("videoStreamId", videoStreamId);
        }
        return new ClusterCommand("videoStreamDeallocate", map);
    }

    /**
     * This command shall allocate a snapshot stream on the device and return an allocated snapshot stream identifier.
     */
    public static ClusterCommand snapshotStreamAllocate(ImageCodecEnum imageCodec, Integer maxFrameRate,
            VideoResolutionStruct minResolution, VideoResolutionStruct maxResolution, Integer quality,
            Boolean watermarkEnabled, Boolean osdEnabled) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (imageCodec != null) {
            map.put("imageCodec", imageCodec);
        }
        if (maxFrameRate != null) {
            map.put("maxFrameRate", maxFrameRate);
        }
        if (minResolution != null) {
            map.put("minResolution", minResolution);
        }
        if (maxResolution != null) {
            map.put("maxResolution", maxResolution);
        }
        if (quality != null) {
            map.put("quality", quality);
        }
        if (watermarkEnabled != null) {
            map.put("watermarkEnabled", watermarkEnabled);
        }
        if (osdEnabled != null) {
            map.put("osdEnabled", osdEnabled);
        }
        return new ClusterCommand("snapshotStreamAllocate", map);
    }

    /**
     * This command shall be used to modify a stream specified by the VideoStreamID.
     */
    public static ClusterCommand snapshotStreamModify(Integer snapshotStreamId, Boolean watermarkEnabled,
            Boolean osdEnabled) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (snapshotStreamId != null) {
            map.put("snapshotStreamId", snapshotStreamId);
        }
        if (watermarkEnabled != null) {
            map.put("watermarkEnabled", watermarkEnabled);
        }
        if (osdEnabled != null) {
            map.put("osdEnabled", osdEnabled);
        }
        return new ClusterCommand("snapshotStreamModify", map);
    }

    /**
     * This command shall deallocate an snapshot stream on the camera, corresponding to the given snapshot stream
     * identifier.
     */
    public static ClusterCommand snapshotStreamDeallocate(Integer snapshotStreamId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (snapshotStreamId != null) {
            map.put("snapshotStreamId", snapshotStreamId);
        }
        return new ClusterCommand("snapshotStreamDeallocate", map);
    }

    /**
     * This command shall set the relative priorities of the various stream usages on the camera. The camera then bases
     * its allocation of resources for each stream allocation based on the order of these stream priorities. In order to
     * avoid the complexity of dynamically changing the configurations of currently active streams, this command shall
     * NOT be invoked when there are allocated streams. If changes are required while streams are allocated, all
     * existing streams would need to be deallocated before invoking this command.
     */
    public static ClusterCommand setStreamPriorities(List<StreamUsageEnum> streamPriorities) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (streamPriorities != null) {
            map.put("streamPriorities", streamPriorities);
        }
        return new ClusterCommand("setStreamPriorities", map);
    }

    /**
     * This command shall return a Snapshot from the camera.
     */
    public static ClusterCommand captureSnapshot(Integer snapshotStreamId, VideoResolutionStruct requestedResolution) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (snapshotStreamId != null) {
            map.put("snapshotStreamId", snapshotStreamId);
        }
        if (requestedResolution != null) {
            map.put("requestedResolution", requestedResolution);
        }
        return new ClusterCommand("captureSnapshot", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "featureMap : " + featureMap + "\n";
        str += "maxConcurrentEncoders : " + maxConcurrentEncoders + "\n";
        str += "maxEncodedPixelRate : " + maxEncodedPixelRate + "\n";
        str += "videoSensorParams : " + videoSensorParams + "\n";
        str += "nightVisionUsesInfrared : " + nightVisionUsesInfrared + "\n";
        str += "minViewportResolution : " + minViewportResolution + "\n";
        str += "rateDistortionTradeOffPoints : " + rateDistortionTradeOffPoints + "\n";
        str += "maxContentBufferSize : " + maxContentBufferSize + "\n";
        str += "microphoneCapabilities : " + microphoneCapabilities + "\n";
        str += "speakerCapabilities : " + speakerCapabilities + "\n";
        str += "twoWayTalkSupport : " + twoWayTalkSupport + "\n";
        str += "snapshotCapabilities : " + snapshotCapabilities + "\n";
        str += "maxNetworkBandwidth : " + maxNetworkBandwidth + "\n";
        str += "currentFrameRate : " + currentFrameRate + "\n";
        str += "hdrModeEnabled : " + hdrModeEnabled + "\n";
        str += "supportedStreamUsages : " + supportedStreamUsages + "\n";
        str += "allocatedVideoStreams : " + allocatedVideoStreams + "\n";
        str += "allocatedAudioStreams : " + allocatedAudioStreams + "\n";
        str += "allocatedSnapshotStreams : " + allocatedSnapshotStreams + "\n";
        str += "streamUsagePriorities : " + streamUsagePriorities + "\n";
        str += "softRecordingPrivacyModeEnabled : " + softRecordingPrivacyModeEnabled + "\n";
        str += "softLivestreamPrivacyModeEnabled : " + softLivestreamPrivacyModeEnabled + "\n";
        str += "hardPrivacyModeOn : " + hardPrivacyModeOn + "\n";
        str += "nightVision : " + nightVision + "\n";
        str += "nightVisionIllum : " + nightVisionIllum + "\n";
        str += "viewport : " + viewport + "\n";
        str += "speakerMuted : " + speakerMuted + "\n";
        str += "speakerVolumeLevel : " + speakerVolumeLevel + "\n";
        str += "speakerMaxLevel : " + speakerMaxLevel + "\n";
        str += "speakerMinLevel : " + speakerMinLevel + "\n";
        str += "microphoneMuted : " + microphoneMuted + "\n";
        str += "microphoneVolumeLevel : " + microphoneVolumeLevel + "\n";
        str += "microphoneMaxLevel : " + microphoneMaxLevel + "\n";
        str += "microphoneMinLevel : " + microphoneMinLevel + "\n";
        str += "microphoneAgcEnabled : " + microphoneAgcEnabled + "\n";
        str += "imageRotation : " + imageRotation + "\n";
        str += "imageFlipHorizontal : " + imageFlipHorizontal + "\n";
        str += "imageFlipVertical : " + imageFlipVertical + "\n";
        str += "localVideoRecordingEnabled : " + localVideoRecordingEnabled + "\n";
        str += "localSnapshotRecordingEnabled : " + localSnapshotRecordingEnabled + "\n";
        str += "statusLightEnabled : " + statusLightEnabled + "\n";
        str += "statusLightBrightness : " + statusLightBrightness + "\n";
        return str;
    }
}
