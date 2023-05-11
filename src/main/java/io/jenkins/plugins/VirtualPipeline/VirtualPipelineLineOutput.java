package io.jenkins.plugins.VirtualPipeline;

//possibly make advanced class from this
public class VirtualPipelineLineOutput {
    private String regex;

    private String line;

    private int index;

    private Boolean deleteMark;

    private LineType type;

    private Boolean display;

    private long lineStartOffset;

    public VirtualPipelineLineOutput(String regex, String line, int index, Boolean deleteMark, LineType type, long lineStartOffset, Boolean display) {
        this.regex = regex;
        this.line = line;
        this.index = index;
        this.deleteMark = deleteMark;
        this.type = type;
        this.lineStartOffset = lineStartOffset;
        this.display = display;
    }

    // just for the purpose of good deserialization
    public VirtualPipelineLineOutput() {
        super();
    }

    public LineType getType() {
        return type;
    }

    public long getLineStartOffset() {
        return lineStartOffset;
    }

    public Boolean getDisplay() {
        return display;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getRegex() {
        return regex;
    }

    public int getIndex() {
        return index;
    }


    public Boolean getDeleteMark() {
        return deleteMark;
    }


}
