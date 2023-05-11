package io.jenkins.plugins.VirtualPipeline;

public class LineWithOffset {
    private final String line;
    private final long offset;

    public String getLine() {
        return line;
    }

    public long getOffset() {
        return offset;
    }

    public LineWithOffset(String line, long offset) {
        this.line = line;
        this.offset = offset;
    }
}
