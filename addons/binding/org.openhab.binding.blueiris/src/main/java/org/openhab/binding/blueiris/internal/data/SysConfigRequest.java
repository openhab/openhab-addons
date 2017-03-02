package org.openhab.binding.blueiris.internal.data;

import com.google.gson.annotations.Expose;

/**
 * The request to the blue iris system to ask for sysconfig stuff, or set
 * sysconfig stuff.
 *
 * @author David Bennett - Initial Contribution
 */
public class SysConfigRequest extends BlueIrisCommandRequest<SysConfigReply> {
    @Expose
    private Boolean archive;
    @Expose
    private Boolean schedule;

    public SysConfigRequest() {
        super(SysConfigReply.class, "sysconfig");
    }

    public Boolean getArchive() {
        return archive;
    }

    public void setArchive(Boolean archive) {
        this.archive = archive;
    }

    public Boolean getSchedule() {
        return schedule;
    }

    public void setSchedule(Boolean schedule) {
        this.schedule = schedule;
    }

}
