package org.openhab.binding.blueiris.internal.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CamConfigRequest extends BlueIrisCommandRequest<CamConfigReply> {
    @Expose
    private Boolean reset;
    @Expose
    private Boolean enable;
    @Expose
    private Integer pause;
    @Expose
    private Boolean motion;
    @Expose
    @SerializedName("camera")
    private String camera;

    public CamConfigRequest() {
        super(CamConfigReply.class, "camconfig");
    }

    public boolean isReset() {
        return reset;
    }

    public void setReset(Boolean reset) {
        this.reset = reset;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public Integer getPause() {
        return pause;
    }

    public void setPause(Integer pause) {
        this.pause = pause;
    }

    public boolean isMotion() {
        return motion;
    }

    public void setMotion(Boolean motion) {
        this.motion = motion;
    }

    public String getCamera() {
        return camera;
    }

    public void setCamera(String camera) {
        this.camera = camera;
    }
}
