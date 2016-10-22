/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.internal.api;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.openhab.binding.zoneminder.ZoneMinderConstants;

/**
 * The Class MonitorData Wraps JSON data from ZoneMinder API call.
 *
 * @author Martin S. Eskildsen
 */

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({ "Id", "Name", "ServerId", "Type", "Function", "Enabled", "LinkedMonitors", "Triggers", "Device",
        "Channel", "Format", "V4LMultiBuffer", "V4LCapturesPerFrame", "Protocol", "Method", "Host", "Port", "SubPath",
        "Path", "Options", "User", "Pass", "Width", "Height", "Colours", "Palette", "Orientation", "Deinterlacing",
        "RTSPDescribe", "Brightness", "Contrast", "Hue", "Colour", "EventPrefix", "LabelFormat", "LabelX", "LabelY",
        "LabelSize", "ImageBufferCount", "WarmupCount", "PreEventCount", "PostEventCount", "StreamReplayBuffer",
        "AlarmFrameCount", "SectionLength", "FrameSkip", "MotionFrameSkip", "AnalysisFPS", "AnalysisUpdateDelay",
        "MaxFPS", "AlarmMaxFPS", "FPSReportInterval", "RefBlendPerc", "AlarmRefBlendPerc", "Controllable", "ControlId",
        "ControlDevice", "ControlAddress", "AutoStopTimeout", "TrackMotion", "TrackDelay", "ReturnLocation",
        "ReturnDelay", "DefaultView", "DefaultRate", "DefaultScale", "SignalCheckColour", "WebColour", "Exif",
        "Sequence" })
public class MonitorData extends ZoneMinderApiData {

    @JsonProperty("Id")
    private String Id;

    @JsonProperty("Name")
    private String Name;

    @JsonProperty("ServerId")
    private String ServerId;

    @JsonProperty("Type")
    private String Type;

    @JsonProperty("Function")
    private String Function;

    @JsonProperty("Enabled")
    private String Enabled;

    @JsonProperty("LinkedMonitors")
    private String LinkedMonitors;

    @JsonProperty("Triggers")
    private String Triggers;

    @JsonProperty("Device")
    private String Device;

    @JsonProperty("Channel")
    private String Channel;

    @JsonProperty("Format")
    private String Format;

    @JsonProperty("V4LMultiBuffer")
    private Boolean V4LMultiBuffer;

    @JsonProperty("V4LCapturesPerFrame")
    private String V4LCapturesPerFrame;

    @JsonProperty("Protocol")
    private String Protocol;

    @JsonProperty("Method")
    private String Method;

    @JsonProperty("Host")
    private String Host;

    @JsonProperty("Port")
    private String Port;

    @JsonProperty("SubPath")
    private String SubPath;

    @JsonProperty("Path")
    private String Path;

    @JsonProperty("Options")
    private String Options;

    @JsonProperty("User")
    private String User;

    @JsonProperty("Pass")
    private String Pass;

    @JsonProperty("Width")
    private String Width;

    @JsonProperty("Height")
    private String Height;

    @JsonProperty("Colours")
    private String Colours;

    @JsonProperty("Palette")
    private String Palette;

    @JsonProperty("Orientation")
    private String Orientation;

    @JsonProperty("Deinterlacing")
    private String Deinterlacing;

    @JsonProperty("RTSPDescribe")
    private Boolean RTSPDescribe;

    @JsonProperty("Brightness")
    private String Brightness;

    @JsonProperty("Contrast")
    private String Contrast;

    @JsonProperty("Hue")
    private String Hue;

    @JsonProperty("Colour")
    private String Colour;

    @JsonProperty("EventPrefix")
    private String EventPrefix;

    @JsonProperty("LabelFormat")
    private String LabelFormat;

    @JsonProperty("LabelX")
    private String LabelX;

    @JsonProperty("LabelY")
    private String LabelY;

    @JsonProperty("LabelSize")
    private String LabelSize;
    @JsonProperty("ImageBufferCount")
    private String ImageBufferCount;
    @JsonProperty("WarmupCount")
    private String WarmupCount;
    @JsonProperty("PreEventCount")
    private String PreEventCount;
    @JsonProperty("PostEventCount")
    private String PostEventCount;
    @JsonProperty("StreamReplayBuffer")
    private String StreamReplayBuffer;
    @JsonProperty("AlarmFrameCount")
    private String AlarmFrameCount;
    @JsonProperty("SectionLength")
    private String SectionLength;
    @JsonProperty("FrameSkip")
    private String FrameSkip;
    @JsonProperty("MotionFrameSkip")
    private String MotionFrameSkip;
    @JsonProperty("AnalysisFPS")
    private String AnalysisFPS;
    @JsonProperty("AnalysisUpdateDelay")
    private String AnalysisUpdateDelay;
    @JsonProperty("MaxFPS")
    private String MaxFPS;
    @JsonProperty("AlarmMaxFPS")
    private String AlarmMaxFPS;
    @JsonProperty("FPSReportInterval")
    private String FPSReportInterval;
    @JsonProperty("RefBlendPerc")
    private String RefBlendPerc;
    @JsonProperty("AlarmRefBlendPerc")
    private String AlarmRefBlendPerc;
    @JsonProperty("Controllable")
    private String Controllable;
    @JsonProperty("ControlId")
    private String ControlId;
    @JsonProperty("ControlDevice")
    private Object ControlDevice;
    @JsonProperty("ControlAddress")
    private Object ControlAddress;
    @JsonProperty("AutoStopTimeout")
    private Object AutoStopTimeout;
    @JsonProperty("TrackMotion")
    private String TrackMotion;
    @JsonProperty("TrackDelay")
    private String TrackDelay;
    @JsonProperty("ReturnLocation")
    private String ReturnLocation;
    @JsonProperty("ReturnDelay")
    private String ReturnDelay;
    @JsonProperty("DefaultView")
    private String DefaultView;
    @JsonProperty("DefaultRate")
    private String DefaultRate;
    @JsonProperty("DefaultScale")
    private String DefaultScale;
    @JsonProperty("SignalCheckColour")
    private String SignalCheckColour;
    @JsonProperty("WebColour")
    private String WebColour;
    @JsonProperty("Exif")
    private Boolean Exif;
    @JsonProperty("Sequence")
    private String Sequence;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public String getDisplayName() {

        return String.format("%s [%s-%s]: %s", ZoneMinderConstants.ZONEMINDER_MONITOR_NAME,
                ZoneMinderConstants.THING_ZONEMINDER_MONITOR, getId(), getName());
    }

    public String getOpenHABId() {
        return ZoneMinderConstants.THING_ZONEMINDER_MONITOR + "-" + getId();
    }

    /**
     *
     * @return
     *         The Id
     */
    @JsonProperty("Id")
    public String getId() {
        return Id;
    }

    /**
     *
     * @param Id
     *            The Id
     */
    @JsonProperty("Id")
    public void setId(String Id) {
        this.Id = Id;
    }

    /**
     *
     * @return
     *         The Name
     */
    @JsonProperty("Name")
    public String getName() {
        return Name;
    }

    /**
     *
     * @param Name
     *            The Name
     */
    @JsonProperty("Name")
    public void setName(String Name) {
        this.Name = Name;
    }

    /**
     *
     * @return
     *         The ServerId
     */
    @JsonProperty("ServerId")
    public String getServerId() {
        return ServerId;
    }

    /**
     *
     * @param ServerId
     *            The ServerId
     */
    @JsonProperty("ServerId")
    public void setServerId(String ServerId) {
        this.ServerId = ServerId;
    }

    /**
     *
     * @return
     *         The Type
     */
    @JsonProperty("Type")
    public String getType() {
        return Type;
    }

    /**
     *
     * @param Type
     *            The Type
     */
    @JsonProperty("Type")
    public void setType(String Type) {
        this.Type = Type;
    }

    /**
     *
     * @return
     *         The Function
     */
    @JsonProperty("Function")
    public String getFunction() {
        return Function;
    }

    /**
     *
     * @param Function
     *            The Function
     */
    @JsonProperty("Function")
    public void setFunction(String Function) {
        this.Function = Function;
    }

    /**
     *
     * @return
     *         The Enabled
     */
    @JsonProperty("Enabled")
    public String getEnabled() {
        return Enabled;
    }

    /**
     *
     * @param Enabled
     *            The Enabled
     */
    @JsonProperty("Enabled")
    public void setEnabled(String Enabled) {
        this.Enabled = Enabled;
    }

    /**
     *
     * @return
     *         The LinkedMonitors
     */
    @JsonProperty("LinkedMonitors")
    public String getLinkedMonitors() {
        return LinkedMonitors;
    }

    /**
     *
     * @param LinkedMonitors
     *            The LinkedMonitors
     */
    @JsonProperty("LinkedMonitors")
    public void setLinkedMonitors(String LinkedMonitors) {
        this.LinkedMonitors = LinkedMonitors;
    }

    /**
     *
     * @return
     *         The Triggers
     */
    @JsonProperty("Triggers")
    public String getTriggers() {
        return Triggers;
    }

    /**
     *
     * @param Triggers
     *            The Triggers
     */
    @JsonProperty("Triggers")
    public void setTriggers(String Triggers) {
        this.Triggers = Triggers;
    }

    /**
     *
     * @return
     *         The Device
     */
    @JsonProperty("Device")
    public String getDevice() {
        return Device;
    }

    /**
     *
     * @param Device
     *            The Device
     */
    @JsonProperty("Device")
    public void setDevice(String Device) {
        this.Device = Device;
    }

    /**
     *
     * @return
     *         The Channel
     */
    @JsonProperty("Channel")
    public String getChannel() {
        return Channel;
    }

    /**
     *
     * @param Channel
     *            The Channel
     */
    @JsonProperty("Channel")
    public void setChannel(String Channel) {
        this.Channel = Channel;
    }

    /**
     *
     * @return
     *         The Format
     */
    @JsonProperty("Format")
    public String getFormat() {
        return Format;
    }

    /**
     *
     * @param Format
     *            The Format
     */
    @JsonProperty("Format")
    public void setFormat(String Format) {
        this.Format = Format;
    }

    /**
     *
     * @return
     *         The V4LMultiBuffer
     */
    @JsonProperty("V4LMultiBuffer")
    public Boolean getV4LMultiBuffer() {
        return V4LMultiBuffer;
    }

    /**
     *
     * @param V4LMultiBuffer
     *            The V4LMultiBuffer
     */
    @JsonProperty("V4LMultiBuffer")
    public void setV4LMultiBuffer(Boolean V4LMultiBuffer) {
        this.V4LMultiBuffer = V4LMultiBuffer;
    }

    /**
     *
     * @return
     *         The V4LCapturesPerFrame
     */
    @JsonProperty("V4LCapturesPerFrame")
    public String getV4LCapturesPerFrame() {
        return V4LCapturesPerFrame;
    }

    /**
     *
     * @param V4LCapturesPerFrame
     *            The V4LCapturesPerFrame
     */
    @JsonProperty("V4LCapturesPerFrame")
    public void setV4LCapturesPerFrame(String V4LCapturesPerFrame) {
        this.V4LCapturesPerFrame = V4LCapturesPerFrame;
    }

    /**
     *
     * @return
     *         The Protocol
     */
    @JsonProperty("Protocol")
    public String getProtocol() {
        return Protocol;
    }

    /**
     *
     * @param Protocol
     *            The Protocol
     */
    @JsonProperty("Protocol")
    public void setProtocol(String Protocol) {
        this.Protocol = Protocol;
    }

    /**
     *
     * @return
     *         The Method
     */
    @JsonProperty("Method")
    public String getMethod() {
        return Method;
    }

    /**
     *
     * @param Method
     *            The Method
     */
    @JsonProperty("Method")
    public void setMethod(String Method) {
        this.Method = Method;
    }

    /**
     *
     * @return
     *         The Host
     */
    @JsonProperty("Host")
    public String getHost() {
        return Host;
    }

    /**
     *
     * @param Host
     *            The Host
     */
    @JsonProperty("Host")
    public void setHost(String Host) {
        this.Host = Host;
    }

    /**
     *
     * @return
     *         The Port
     */
    @JsonProperty("Port")
    public String getPort() {
        return Port;
    }

    /**
     *
     * @param Port
     *            The Port
     */
    @JsonProperty("Port")
    public void setPort(String Port) {
        this.Port = Port;
    }

    /**
     *
     * @return
     *         The SubPath
     */
    @JsonProperty("SubPath")
    public String getSubPath() {
        return SubPath;
    }

    /**
     *
     * @param SubPath
     *            The SubPath
     */
    @JsonProperty("SubPath")
    public void setSubPath(String SubPath) {
        this.SubPath = SubPath;
    }

    /**
     *
     * @return
     *         The Path
     */
    @JsonProperty("Path")
    public String getPath() {
        return Path;
    }

    /**
     *
     * @param Path
     *            The Path
     */
    @JsonProperty("Path")
    public void setPath(String Path) {
        this.Path = Path;
    }

    /**
     *
     * @return
     *         The Options
     */
    @JsonProperty("Options")
    public String getOptions() {
        return Options;
    }

    /**
     *
     * @param Options
     *            The Options
     */
    @JsonProperty("Options")
    public void setOptions(String Options) {
        this.Options = Options;
    }

    /**
     *
     * @return
     *         The User
     */
    @JsonProperty("User")
    public String getUser() {
        return User;
    }

    /**
     *
     * @param User
     *            The User
     */
    @JsonProperty("User")
    public void setUser(String User) {
        this.User = User;
    }

    /**
     *
     * @return
     *         The Pass
     */
    @JsonProperty("Pass")
    public String getPass() {
        return Pass;
    }

    /**
     *
     * @param Pass
     *            The Pass
     */
    @JsonProperty("Pass")
    public void setPass(String Pass) {
        this.Pass = Pass;
    }

    /**
     *
     * @return
     *         The Width
     */
    @JsonProperty("Width")
    public String getWidth() {
        return Width;
    }

    /**
     *
     * @param Width
     *            The Width
     */
    @JsonProperty("Width")
    public void setWidth(String Width) {
        this.Width = Width;
    }

    /**
     *
     * @return
     *         The Height
     */
    @JsonProperty("Height")
    public String getHeight() {
        return Height;
    }

    /**
     *
     * @param Height
     *            The Height
     */
    @JsonProperty("Height")
    public void setHeight(String Height) {
        this.Height = Height;
    }

    /**
     *
     * @return
     *         The Colours
     */
    @JsonProperty("Colours")
    public String getColours() {
        return Colours;
    }

    /**
     *
     * @param Colours
     *            The Colours
     */
    @JsonProperty("Colours")
    public void setColours(String Colours) {
        this.Colours = Colours;
    }

    /**
     *
     * @return
     *         The Palette
     */
    @JsonProperty("Palette")
    public String getPalette() {
        return Palette;
    }

    /**
     *
     * @param Palette
     *            The Palette
     */
    @JsonProperty("Palette")
    public void setPalette(String Palette) {
        this.Palette = Palette;
    }

    /**
     *
     * @return
     *         The Orientation
     */
    @JsonProperty("Orientation")
    public String getOrientation() {
        return Orientation;
    }

    /**
     *
     * @param Orientation
     *            The Orientation
     */
    @JsonProperty("Orientation")
    public void setOrientation(String Orientation) {
        this.Orientation = Orientation;
    }

    /**
     *
     * @return
     *         The Deinterlacing
     */
    @JsonProperty("Deinterlacing")
    public String getDeinterlacing() {
        return Deinterlacing;
    }

    /**
     *
     * @param Deinterlacing
     *            The Deinterlacing
     */
    @JsonProperty("Deinterlacing")
    public void setDeinterlacing(String Deinterlacing) {
        this.Deinterlacing = Deinterlacing;
    }

    /**
     *
     * @return
     *         The RTSPDescribe
     */
    @JsonProperty("RTSPDescribe")
    public Boolean getRTSPDescribe() {
        return RTSPDescribe;
    }

    /**
     *
     * @param RTSPDescribe
     *            The RTSPDescribe
     */
    @JsonProperty("RTSPDescribe")
    public void setRTSPDescribe(Boolean RTSPDescribe) {
        this.RTSPDescribe = RTSPDescribe;
    }

    /**
     *
     * @return
     *         The Brightness
     */
    @JsonProperty("Brightness")
    public String getBrightness() {
        return Brightness;
    }

    /**
     *
     * @param Brightness
     *            The Brightness
     */
    @JsonProperty("Brightness")
    public void setBrightness(String Brightness) {
        this.Brightness = Brightness;
    }

    /**
     *
     * @return
     *         The Contrast
     */
    @JsonProperty("Contrast")
    public String getContrast() {
        return Contrast;
    }

    /**
     *
     * @param Contrast
     *            The Contrast
     */
    @JsonProperty("Contrast")
    public void setContrast(String Contrast) {
        this.Contrast = Contrast;
    }

    /**
     *
     * @return
     *         The Hue
     */
    @JsonProperty("Hue")
    public String getHue() {
        return Hue;
    }

    /**
     *
     * @param Hue
     *            The Hue
     */
    @JsonProperty("Hue")
    public void setHue(String Hue) {
        this.Hue = Hue;
    }

    /**
     *
     * @return
     *         The Colour
     */
    @JsonProperty("Colour")
    public String getColour() {
        return Colour;
    }

    /**
     *
     * @param Colour
     *            The Colour
     */
    @JsonProperty("Colour")
    public void setColour(String Colour) {
        this.Colour = Colour;
    }

    /**
     *
     * @return
     *         The EventPrefix
     */
    @JsonProperty("EventPrefix")
    public String getEventPrefix() {
        return EventPrefix;
    }

    /**
     *
     * @param EventPrefix
     *            The EventPrefix
     */
    @JsonProperty("EventPrefix")
    public void setEventPrefix(String EventPrefix) {
        this.EventPrefix = EventPrefix;
    }

    /**
     *
     * @return
     *         The LabelFormat
     */
    @JsonProperty("LabelFormat")
    public String getLabelFormat() {
        return LabelFormat;
    }

    /**
     *
     * @param LabelFormat
     *            The LabelFormat
     */
    @JsonProperty("LabelFormat")
    public void setLabelFormat(String LabelFormat) {
        this.LabelFormat = LabelFormat;
    }

    /**
     *
     * @return
     *         The LabelX
     */
    @JsonProperty("LabelX")
    public String getLabelX() {
        return LabelX;
    }

    /**
     *
     * @param LabelX
     *            The LabelX
     */
    @JsonProperty("LabelX")
    public void setLabelX(String LabelX) {
        this.LabelX = LabelX;
    }

    /**
     *
     * @return
     *         The LabelY
     */
    @JsonProperty("LabelY")
    public String getLabelY() {
        return LabelY;
    }

    /**
     *
     * @param LabelY
     *            The LabelY
     */
    @JsonProperty("LabelY")
    public void setLabelY(String LabelY) {
        this.LabelY = LabelY;
    }

    /**
     *
     * @return
     *         The LabelSize
     */
    @JsonProperty("LabelSize")
    public String getLabelSize() {
        return LabelSize;
    }

    /**
     *
     * @param LabelSize
     *            The LabelSize
     */
    @JsonProperty("LabelSize")
    public void setLabelSize(String LabelSize) {
        this.LabelSize = LabelSize;
    }

    /**
     *
     * @return
     *         The ImageBufferCount
     */
    @JsonProperty("ImageBufferCount")
    public String getImageBufferCount() {
        return ImageBufferCount;
    }

    /**
     *
     * @param ImageBufferCount
     *            The ImageBufferCount
     */
    @JsonProperty("ImageBufferCount")
    public void setImageBufferCount(String ImageBufferCount) {
        this.ImageBufferCount = ImageBufferCount;
    }

    /**
     *
     * @return
     *         The WarmupCount
     */
    @JsonProperty("WarmupCount")
    public String getWarmupCount() {
        return WarmupCount;
    }

    /**
     *
     * @param WarmupCount
     *            The WarmupCount
     */
    @JsonProperty("WarmupCount")
    public void setWarmupCount(String WarmupCount) {
        this.WarmupCount = WarmupCount;
    }

    /**
     *
     * @return
     *         The PreEventCount
     */
    @JsonProperty("PreEventCount")
    public String getPreEventCount() {
        return PreEventCount;
    }

    /**
     *
     * @param PreEventCount
     *            The PreEventCount
     */
    @JsonProperty("PreEventCount")
    public void setPreEventCount(String PreEventCount) {
        this.PreEventCount = PreEventCount;
    }

    /**
     *
     * @return
     *         The PostEventCount
     */
    @JsonProperty("PostEventCount")
    public String getPostEventCount() {
        return PostEventCount;
    }

    /**
     *
     * @param PostEventCount
     *            The PostEventCount
     */
    @JsonProperty("PostEventCount")
    public void setPostEventCount(String PostEventCount) {
        this.PostEventCount = PostEventCount;
    }

    /**
     *
     * @return
     *         The StreamReplayBuffer
     */
    @JsonProperty("StreamReplayBuffer")
    public String getStreamReplayBuffer() {
        return StreamReplayBuffer;
    }

    /**
     *
     * @param StreamReplayBuffer
     *            The StreamReplayBuffer
     */
    @JsonProperty("StreamReplayBuffer")
    public void setStreamReplayBuffer(String StreamReplayBuffer) {
        this.StreamReplayBuffer = StreamReplayBuffer;
    }

    /**
     *
     * @return
     *         The AlarmFrameCount
     */
    @JsonProperty("AlarmFrameCount")
    public String getAlarmFrameCount() {
        return AlarmFrameCount;
    }

    /**
     *
     * @param AlarmFrameCount
     *            The AlarmFrameCount
     */
    @JsonProperty("AlarmFrameCount")
    public void setAlarmFrameCount(String AlarmFrameCount) {
        this.AlarmFrameCount = AlarmFrameCount;
    }

    /**
     *
     * @return
     *         The SectionLength
     */
    @JsonProperty("SectionLength")
    public String getSectionLength() {
        return SectionLength;
    }

    /**
     *
     * @param SectionLength
     *            The SectionLength
     */
    @JsonProperty("SectionLength")
    public void setSectionLength(String SectionLength) {
        this.SectionLength = SectionLength;
    }

    /**
     *
     * @return
     *         The FrameSkip
     */
    @JsonProperty("FrameSkip")
    public String getFrameSkip() {
        return FrameSkip;
    }

    /**
     *
     * @param FrameSkip
     *            The FrameSkip
     */
    @JsonProperty("FrameSkip")
    public void setFrameSkip(String FrameSkip) {
        this.FrameSkip = FrameSkip;
    }

    /**
     *
     * @return
     *         The MotionFrameSkip
     */
    @JsonProperty("MotionFrameSkip")
    public String getMotionFrameSkip() {
        return MotionFrameSkip;
    }

    /**
     *
     * @param MotionFrameSkip
     *            The MotionFrameSkip
     */
    @JsonProperty("MotionFrameSkip")
    public void setMotionFrameSkip(String MotionFrameSkip) {
        this.MotionFrameSkip = MotionFrameSkip;
    }

    /**
     *
     * @return
     *         The AnalysisFPS
     */
    @JsonProperty("AnalysisFPS")
    public String getAnalysisFPS() {
        return AnalysisFPS;
    }

    /**
     *
     * @param AnalysisFPS
     *            The AnalysisFPS
     */
    @JsonProperty("AnalysisFPS")
    public void setAnalysisFPS(String AnalysisFPS) {
        this.AnalysisFPS = AnalysisFPS;
    }

    /**
     *
     * @return
     *         The AnalysisUpdateDelay
     */
    @JsonProperty("AnalysisUpdateDelay")
    public String getAnalysisUpdateDelay() {
        return AnalysisUpdateDelay;
    }

    /**
     *
     * @param AnalysisUpdateDelay
     *            The AnalysisUpdateDelay
     */
    @JsonProperty("AnalysisUpdateDelay")
    public void setAnalysisUpdateDelay(String AnalysisUpdateDelay) {
        this.AnalysisUpdateDelay = AnalysisUpdateDelay;
    }

    /**
     *
     * @return
     *         The MaxFPS
     */
    @JsonProperty("MaxFPS")
    public String getMaxFPS() {
        return MaxFPS;
    }

    /**
     *
     * @param MaxFPS
     *            The MaxFPS
     */
    @JsonProperty("MaxFPS")
    public void setMaxFPS(String MaxFPS) {
        this.MaxFPS = MaxFPS;
    }

    /**
     *
     * @return
     *         The AlarmMaxFPS
     */
    @JsonProperty("AlarmMaxFPS")
    public String getAlarmMaxFPS() {
        return AlarmMaxFPS;
    }

    /**
     *
     * @param AlarmMaxFPS
     *            The AlarmMaxFPS
     */
    @JsonProperty("AlarmMaxFPS")
    public void setAlarmMaxFPS(String AlarmMaxFPS) {
        this.AlarmMaxFPS = AlarmMaxFPS;
    }

    /**
     *
     * @return
     *         The FPSReportInterval
     */
    @JsonProperty("FPSReportInterval")
    public String getFPSReportInterval() {
        return FPSReportInterval;
    }

    /**
     *
     * @param FPSReportInterval
     *            The FPSReportInterval
     */
    @JsonProperty("FPSReportInterval")
    public void setFPSReportInterval(String FPSReportInterval) {
        this.FPSReportInterval = FPSReportInterval;
    }

    /**
     *
     * @return
     *         The RefBlendPerc
     */
    @JsonProperty("RefBlendPerc")
    public String getRefBlendPerc() {
        return RefBlendPerc;
    }

    /**
     *
     * @param RefBlendPerc
     *            The RefBlendPerc
     */
    @JsonProperty("RefBlendPerc")
    public void setRefBlendPerc(String RefBlendPerc) {
        this.RefBlendPerc = RefBlendPerc;
    }

    /**
     *
     * @return
     *         The AlarmRefBlendPerc
     */
    @JsonProperty("AlarmRefBlendPerc")
    public String getAlarmRefBlendPerc() {
        return AlarmRefBlendPerc;
    }

    /**
     *
     * @param AlarmRefBlendPerc
     *            The AlarmRefBlendPerc
     */
    @JsonProperty("AlarmRefBlendPerc")
    public void setAlarmRefBlendPerc(String AlarmRefBlendPerc) {
        this.AlarmRefBlendPerc = AlarmRefBlendPerc;
    }

    /**
     *
     * @return
     *         The Controllable
     */
    @JsonProperty("Controllable")
    public String getControllable() {
        return Controllable;
    }

    /**
     *
     * @param Controllable
     *            The Controllable
     */
    @JsonProperty("Controllable")
    public void setControllable(String Controllable) {
        this.Controllable = Controllable;
    }

    /**
     *
     * @return
     *         The ControlId
     */
    @JsonProperty("ControlId")
    public String getControlId() {
        return ControlId;
    }

    /**
     *
     * @param ControlId
     *            The ControlId
     */
    @JsonProperty("ControlId")
    public void setControlId(String ControlId) {
        this.ControlId = ControlId;
    }

    /**
     *
     * @return
     *         The ControlDevice
     */
    @JsonProperty("ControlDevice")
    public Object getControlDevice() {
        return ControlDevice;
    }

    /**
     *
     * @param ControlDevice
     *            The ControlDevice
     */
    @JsonProperty("ControlDevice")
    public void setControlDevice(Object ControlDevice) {
        this.ControlDevice = ControlDevice;
    }

    /**
     *
     * @return
     *         The ControlAddress
     */
    @JsonProperty("ControlAddress")
    public Object getControlAddress() {
        return ControlAddress;
    }

    /**
     *
     * @param ControlAddress
     *            The ControlAddress
     */
    @JsonProperty("ControlAddress")
    public void setControlAddress(Object ControlAddress) {
        this.ControlAddress = ControlAddress;
    }

    /**
     *
     * @return
     *         The AutoStopTimeout
     */
    @JsonProperty("AutoStopTimeout")
    public Object getAutoStopTimeout() {
        return AutoStopTimeout;
    }

    /**
     *
     * @param AutoStopTimeout
     *            The AutoStopTimeout
     */
    @JsonProperty("AutoStopTimeout")
    public void setAutoStopTimeout(Object AutoStopTimeout) {
        this.AutoStopTimeout = AutoStopTimeout;
    }

    /**
     *
     * @return
     *         The TrackMotion
     */
    @JsonProperty("TrackMotion")
    public String getTrackMotion() {
        return TrackMotion;
    }

    /**
     *
     * @param TrackMotion
     *            The TrackMotion
     */
    @JsonProperty("TrackMotion")
    public void setTrackMotion(String TrackMotion) {
        this.TrackMotion = TrackMotion;
    }

    /**
     *
     * @return
     *         The TrackDelay
     */
    @JsonProperty("TrackDelay")
    public String getTrackDelay() {
        return TrackDelay;
    }

    /**
     *
     * @param TrackDelay
     *            The TrackDelay
     */
    @JsonProperty("TrackDelay")
    public void setTrackDelay(String TrackDelay) {
        this.TrackDelay = TrackDelay;
    }

    /**
     *
     * @return
     *         The ReturnLocation
     */
    @JsonProperty("ReturnLocation")
    public String getReturnLocation() {
        return ReturnLocation;
    }

    /**
     *
     * @param ReturnLocation
     *            The ReturnLocation
     */
    @JsonProperty("ReturnLocation")
    public void setReturnLocation(String ReturnLocation) {
        this.ReturnLocation = ReturnLocation;
    }

    /**
     *
     * @return
     *         The ReturnDelay
     */
    @JsonProperty("ReturnDelay")
    public String getReturnDelay() {
        return ReturnDelay;
    }

    /**
     *
     * @param ReturnDelay
     *            The ReturnDelay
     */
    @JsonProperty("ReturnDelay")
    public void setReturnDelay(String ReturnDelay) {
        this.ReturnDelay = ReturnDelay;
    }

    /**
     *
     * @return
     *         The DefaultView
     */
    @JsonProperty("DefaultView")
    public String getDefaultView() {
        return DefaultView;
    }

    /**
     *
     * @param DefaultView
     *            The DefaultView
     */
    @JsonProperty("DefaultView")
    public void setDefaultView(String DefaultView) {
        this.DefaultView = DefaultView;
    }

    /**
     *
     * @return
     *         The DefaultRate
     */
    @JsonProperty("DefaultRate")
    public String getDefaultRate() {
        return DefaultRate;
    }

    /**
     *
     * @param DefaultRate
     *            The DefaultRate
     */
    @JsonProperty("DefaultRate")
    public void setDefaultRate(String DefaultRate) {
        this.DefaultRate = DefaultRate;
    }

    /**
     *
     * @return
     *         The DefaultScale
     */
    @JsonProperty("DefaultScale")
    public String getDefaultScale() {
        return DefaultScale;
    }

    /**
     *
     * @param DefaultScale
     *            The DefaultScale
     */
    @JsonProperty("DefaultScale")
    public void setDefaultScale(String DefaultScale) {
        this.DefaultScale = DefaultScale;
    }

    /**
     *
     * @return
     *         The SignalCheckColour
     */
    @JsonProperty("SignalCheckColour")
    public String getSignalCheckColour() {
        return SignalCheckColour;
    }

    /**
     *
     * @param SignalCheckColour
     *            The SignalCheckColour
     */
    @JsonProperty("SignalCheckColour")
    public void setSignalCheckColour(String SignalCheckColour) {
        this.SignalCheckColour = SignalCheckColour;
    }

    /**
     *
     * @return
     *         The WebColour
     */
    @JsonProperty("WebColour")
    public String getWebColour() {
        return WebColour;
    }

    /**
     *
     * @param WebColour
     *            The WebColour
     */
    @JsonProperty("WebColour")
    public void setWebColour(String WebColour) {
        this.WebColour = WebColour;
    }

    /**
     *
     * @return
     *         The Exif
     */
    @JsonProperty("Exif")
    public Boolean getExif() {
        return Exif;
    }

    /**
     *
     * @param Exif
     *            The Exif
     */
    @JsonProperty("Exif")
    public void setExif(Boolean Exif) {
        this.Exif = Exif;
    }

    /**
     *
     * @return
     *         The Sequence
     */
    @JsonProperty("Sequence")
    public String getSequence() {
        return Sequence;
    }

    /**
     *
     * @param Sequence
     *            The Sequence
     */
    @JsonProperty("Sequence")
    public void setSequence(String Sequence) {
        this.Sequence = Sequence;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
