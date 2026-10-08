package dev.codex.rtfnrf.legacy.data.style;

public final class TooltipData {
    private TooltipData() {
    }

    public static TooltipDataBuilder builder() { return new TooltipDataBuilder(); }

    public static final class TooltipDataBuilder {
        public TooltipDataBuilder borderTop(int value) { return this; }
        public TooltipDataBuilder borderBottom(int value) { return this; }
        public TooltipDataBuilder backgroundTop(int value) { return this; }
        public TooltipDataBuilder backgroundBottom(int value) { return this; }
        public TooltipDataBuilder textured(boolean value) { return this; }
        public TooltipDataBuilder icon(String value) { return this; }
        public TooltipData build() { return new TooltipData(); }
    }
}

