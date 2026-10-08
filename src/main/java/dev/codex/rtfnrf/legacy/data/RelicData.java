package dev.codex.rtfnrf.legacy.data;

import dev.codex.rtfnrf.legacy.data.leveling.AbilitiesData;
import dev.codex.rtfnrf.legacy.data.leveling.LevelingData;
import dev.codex.rtfnrf.legacy.data.loot.LootData;
import dev.codex.rtfnrf.legacy.data.style.StyleData;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;

public final class RelicData {
    private final AbilitiesData abilities;
    private final LevelingData leveling;
    private final StyleData style;
    private final LootData loot;

    private RelicData(AbilitiesData abilities, LevelingData leveling, StyleData style, LootData loot) {
        this.abilities = abilities;
        this.leveling = leveling;
        this.style = style;
        this.loot = loot;
    }

    public static RelicDataBuilder builder() { return new RelicDataBuilder(); }
    public AbilitiesData getAbilities() { return abilities; }
    public LevelingData getLeveling() { return leveling; }
    public StyleData getStyle() { return style; }
    public LootData getLoot() { return loot; }

    public RelicTemplate toTemplate() {
        var builder = RelicTemplate.builder();
        if (abilities != null) builder.abilities(abilities.toTemplate());
        if (leveling != null) builder.leveling(leveling.toTemplate());
        if (loot != null) builder.loot(loot.toTemplate());
        return builder.build();
    }

    public static final class RelicDataBuilder {
        private AbilitiesData abilities;
        private LevelingData leveling;
        private StyleData style;
        private LootData loot;
        public RelicDataBuilder abilities(AbilitiesData value) { this.abilities = value; return this; }
        public RelicDataBuilder leveling(LevelingData value) { this.leveling = value; return this; }
        public RelicDataBuilder style(StyleData value) { this.style = value; return this; }
        public RelicDataBuilder loot(LootData value) { this.loot = value; return this; }
        public RelicData build() { return new RelicData(abilities, leveling, style, loot); }
    }
}
