package com.gzhou.eventing.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/*
    Object representing file changes summary;
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Change {

    public Change(int addtions, int deletions, int changes, String status, String patch) {
        this.addtions = addtions;
        this.deletions = deletions;
        this.changes = changes;
        this.status = status;
        // specific changes represented in patch format
        this.patch = patch;
    }

    public Change() {
    }

    private int addtions;
    private int deletions;
    private int changes;
    private String status;
    private String patch;

    public int getAddtions() {
        return addtions;
    }

    public void setAddtions(int addtions) {
        this.addtions = addtions;
    }

    public int getDeletions() {
        return deletions;
    }

    public void setDeletions(int deletions) {
        this.deletions = deletions;
    }

    public int getChanges() {
        return changes;
    }

    public void setChanges(int changes) {
        this.changes = changes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPatch() {
        return patch;
    }

    public void setPatch(String patch) {
        this.patch = patch;
    }
}
