package org.openhab.binding.gmailparadoxparser.model;

public class ParadoxPartition implements Comparable<ParadoxPartition> {
    String state;
    String partition;
    String activatedBy;
    String time;

    public ParadoxPartition(String state, String partition, String activatedBy, String time) {
        this.state = state;
        this.partition = partition;
        this.activatedBy = activatedBy;
        this.time = time;
    }

    public String getState() {
        return state;
    }

    public void setState(String message) {
        this.state = message;
    }

    @Override
    public String toString() {
        return "ParadoxPartition [partition= \"" + partition + "\", state=" + state + ", activatedBy=" + activatedBy
                + ", time=" + time + "]";
    }

    public String getPartition() {
        return partition;
    }

    public void setPartition(String partition) {
        this.partition = partition;
    }

    public String getActivatedBy() {
        return activatedBy;
    }

    public void setActivatedBy(String activatedBy) {
        this.activatedBy = activatedBy;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((partition == null) ? 0 : partition.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ParadoxPartition other = (ParadoxPartition) obj;
        if (partition == null) {
            if (other.partition != null) {
                return false;
            }
        } else if (!partition.equals(other.partition)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(ParadoxPartition o) {
        // TODO Auto-generated method stub
        return 0;
    }
}
