package io.jenkins.plugins.VirtualPipeline;

public enum LineType {
    ONE_LINE, // for lines generated by single input
    START_MARK, // for lines matching startMark
    CONTENT_LINE, // for lines that are part of the advanced regex content
    END_MARK, // for lines matching startMark
    LIMIT_REACHED_LINE, // same as CONTENT_LINE, but marking reached limit of lines
    DEFAULT // used for lines with no marking meaning
}