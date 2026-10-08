package dev.codex.rtfnrf.legacy.data.leveling;

import dev.codex.rtfnrf.legacy.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemColor;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemShape;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class LevelingSourceData {
    private final String id;
    private final int initialValue;

    private LevelingSourceData(String id, int initialValue) {
        this.id = id;
        this.initialValue = initialValue;
    }

    public static LevelingSourceDataBuilder genericBuilder(String id) {
        return new LevelingSourceDataBuilder(id);
    }

    public static LevelingSourceDataBuilder abilityBuilder(String ability) {
        return new LevelingSourceDataBuilder(ability);
    }

    public static LevelingSourceDataBuilder abilityBuilder(String id, String ability) {
        return new LevelingSourceDataBuilder(id);
    }

    public String getId() {
        return id;
    }

    public int getInitialValue() {
        return initialValue;
    }

    public static final class LevelingSourceDataBuilder {
        private final String id;
        private int initialValue;

        private LevelingSourceDataBuilder(String id) {
            this.id = id;
        }

        public LevelingSourceDataBuilder initialValue(int value) { this.initialValue = value; return this; }
        public LevelingSourceDataBuilder upgradeModifier(UpgradeOperation operation, int value) { return this; }
        public LevelingSourceDataBuilder maxLevel(int value) { return this; }
        public LevelingSourceDataBuilder cost(int value) { return this; }
        public LevelingSourceDataBuilder requiredLevel(int value) { return this; }
        public LevelingSourceDataBuilder requiredAbility(String value) { return this; }
        public LevelingSourceDataBuilder manualIcon(Function<ItemStack, ResourceLocation> value) { return this; }
        public LevelingSourceDataBuilder genericIcon(String value) { return this; }
        public LevelingSourceDataBuilder abilityIcon(String value) { return this; }
        public LevelingSourceDataBuilder gem(GemShape shape, GemColor color) { return this; }
        public LevelingSourceDataBuilder translationPath(Function<ItemStack, String> value) { return this; }
        public LevelingSourceDataBuilder shape(GemShape value) { return this; }
        public LevelingSourceDataBuilder color(GemColor value) { return this; }

        public LevelingSourceData build() {
            return new LevelingSourceData(id, initialValue);
        }
    }
}

