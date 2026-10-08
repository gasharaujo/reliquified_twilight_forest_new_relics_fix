package dev.codex.rtfnrf.legacy.data.leveling;

import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;

public final class LevelingData {
    private final int maxLevel;
    private final int initialCost;
    private final int step;
    private final LevelingSourcesData sources;

    private LevelingData(int maxLevel, int initialCost, int step, LevelingSourcesData sources) {
        this.maxLevel = maxLevel;
        this.initialCost = initialCost;
        this.step = step;
        this.sources = sources;
    }

    public static LevelingDataBuilder builder() { return new LevelingDataBuilder(); }

    public LevelingTemplate toTemplate() {
        return LevelingTemplate.builder().initialCost(initialCost).step(step).build();
    }

    public int getMaxLevel() { return maxLevel; }
    public LevelingSourcesData getSources() { return sources; }

    public static final class LevelingDataBuilder {
        private int maxLevel = 10;
        private int initialCost = 100;
        private int step = 100;
        private LevelingSourcesData sources;

        public LevelingDataBuilder maxLevel(int value) { this.maxLevel = value; return this; }
        public LevelingDataBuilder initialCost(int value) { this.initialCost = value; return this; }
        public LevelingDataBuilder step(int value) { this.step = value; return this; }
        public LevelingDataBuilder sources(LevelingSourcesData value) { this.sources = value; return this; }

        public LevelingData build() {
            return new LevelingData(maxLevel, initialCost, step, sources);
        }
    }
}

