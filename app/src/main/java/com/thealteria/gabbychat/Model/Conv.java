package com.thealteria.gabbychat.Model;

public class Conv {

    private boolean seen, typing;
    private long timestamp;

    public Conv(){

    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Conv(boolean seen, boolean typing, long timestamp) {
        this.seen = seen;
        this.timestamp = timestamp;
        this.typing = typing;
    }

    public boolean isTyping() {
        return typing;
    }

    public void setTyping(boolean typing) {
        this.typing = typing;
    }
}
