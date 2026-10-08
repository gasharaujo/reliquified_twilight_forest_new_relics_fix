package dev.codex.rtfnrf.legacy.data.leveling;

import com.mojang.datafixers.util.Function3;
import dev.codex.rtfnrf.legacy.data.cast.CastData;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class AbilityData {
    private final String id;
    private final Map<String, StatData> stats;
    private final int maxLevel;
    private final int requiredLevel;
    private final int requiredPoints;
    private final CastData activation;
    private final Function3<Player, ItemStack, String, String> icon;

    private AbilityData(
        String id, Map<String, StatData> stats, int maxLevel,
        int requiredLevel, int requiredPoints, CastData activation,
        Function3<Player, ItemStack, String, String> icon
    ) {
        this.id = id;
        this.stats = Map.copyOf(stats);
        this.maxLevel = maxLevel;
        this.requiredLevel = requiredLevel;
        this.requiredPoints = requiredPoints;
        this.activation = activation;
        this.icon = icon;
    }

    public static AbilityDataBuilder builder(String id) {
        return new AbilityDataBuilder(id);
    }

    public String getId() {
        return id;
    }

    public Map<String, StatData> getStats() {
        return stats;
    }

    public AbilityTemplate toTemplate() {
        var builder = AbilityTemplate.builder(id)
            .initialMaxLevel(Math.max(0, maxLevel))
            .requiredLevel(Math.max(0, requiredLevel))
            .requiredPoints(Math.max(1, requiredPoints));
        for (StatData stat : stats.values()) {
            builder.stat(stat.toTemplate(maxLevel));
        }
        if (icon != null) builder.icon(icon);
        if (activation != null) builder.active(activation.toTemplate());
        else builder.passive();
        return builder.build();
    }

    public static final class AbilityDataBuilder {
        private final String id;
        private final Map<String, StatData> stats = new LinkedHashMap<>();
        private int maxLevel = 10;
        private int requiredLevel;
        private int requiredPoints = 1;
        private CastData activation;
        private Function3<Player, ItemStack, String, String> icon;

        private AbilityDataBuilder(String id) {
            this.id = id;
        }

        public AbilityDataBuilder stat(StatData stat) {
            if (stat != null) stats.put(stat.getId(), stat);
            return this;
        }

        public AbilityDataBuilder maxLevel(int value) {
            this.maxLevel = value;
            return this;
        }

        public AbilityDataBuilder requiredLevel(int value) {
            this.requiredLevel = value;
            return this;
        }

        public AbilityDataBuilder requiredPoints(int value) {
            this.requiredPoints = value;
            return this;
        }

        public AbilityDataBuilder active(CastData value) {
            this.activation = value;
            return this;
        }

        public AbilityDataBuilder icon(Function3<Player, ItemStack, String, String> value) {
            this.icon = value;
            return this;
        }

        public AbilityData build() {
            return new AbilityData(id, stats, maxLevel, requiredLevel, requiredPoints, activation, icon);
        }
    }
}

