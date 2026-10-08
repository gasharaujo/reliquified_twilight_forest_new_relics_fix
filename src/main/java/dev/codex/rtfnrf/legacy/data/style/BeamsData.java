package dev.codex.rtfnrf.legacy.data.style;

public final class BeamsData {
    private final int startColor;
    private final int endColor;

    private BeamsData(int startColor, int endColor) {
        this.startColor = startColor;
        this.endColor = endColor;
    }

    public static BeamsDataBuilder builder() { return new BeamsDataBuilder(); }
    public int getStartColor() { return startColor; }
    public int getEndColor() { return endColor; }

    public static final class BeamsDataBuilder {
        private int startColor = 0xFFFFFFFF;
        private int endColor = 0xFFFFFFFF;
        public BeamsDataBuilder startColor(int value) { this.startColor = value; return this; }
        public BeamsDataBuilder endColor(int value) { this.endColor = value; return this; }
        public BeamsData build() { return new BeamsData(startColor, endColor); }
    }
}

