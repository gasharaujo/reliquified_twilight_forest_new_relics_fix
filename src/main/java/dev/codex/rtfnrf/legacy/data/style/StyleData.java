package dev.codex.rtfnrf.legacy.data.style;

import java.util.function.BiFunction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class StyleData {
    private StyleData() {
    }

    public static StyleDataBuilder builder() { return new StyleDataBuilder(); }

    public static final class StyleDataBuilder {
        public StyleDataBuilder tooltip(TooltipData value) { return this; }
        public StyleDataBuilder tooltip(BiFunction<Player, ItemStack, TooltipData> value) { return this; }
        public StyleDataBuilder beams(BeamsData value) { return this; }
        public StyleDataBuilder beams(BiFunction<Player, ItemStack, BeamsData> value) { return this; }
        public StyleData build() { return new StyleData(); }
    }
}

