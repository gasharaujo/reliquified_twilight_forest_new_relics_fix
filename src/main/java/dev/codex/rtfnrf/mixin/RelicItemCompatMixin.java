package dev.codex.rtfnrf.mixin;

import dev.codex.rtfnrf.legacy.LegacyRelicItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(
    targets = {
        "it.hurts.octostudios.reliquified_twilight_forest.item.relic.CharmBackpackItem",
        "it.hurts.octostudios.reliquified_twilight_forest.item.relic.ChromaticCloakItem",
        "it.hurts.octostudios.reliquified_twilight_forest.item.relic.CicadaBottleItem",
        "it.hurts.octostudios.reliquified_twilight_forest.item.relic.DeerAntlerItem",
        "it.hurts.octostudios.reliquified_twilight_forest.item.relic.FireflyQueenItem",
        "it.hurts.octostudios.reliquified_twilight_forest.item.relic.GiantGloveItem",
        "it.hurts.octostudios.reliquified_twilight_forest.item.relic.GoblinNoseItem",
        "it.hurts.octostudios.reliquified_twilight_forest.item.relic.HydraHeartItem",
        "it.hurts.octostudios.reliquified_twilight_forest.item.relic.InvisibilityCloakItem",
        "it.hurts.octostudios.reliquified_twilight_forest.item.relic.LichCrownItem",
        "it.hurts.octostudios.reliquified_twilight_forest.item.relic.MapleSyrupBottleItem",
        "it.hurts.octostudios.reliquified_twilight_forest.item.relic.MinotaurHoofItem",
        "it.hurts.octostudios.reliquified_twilight_forest.item.relic.Parasite115Item",
        "it.hurts.octostudios.reliquified_twilight_forest.item.relic.Parasite116Item",
        "it.hurts.octostudios.reliquified_twilight_forest.item.relic.ScaledCloakItem",
        "it.hurts.octostudios.reliquified_twilight_forest.item.relic.SteelCapeItem",
        "it.hurts.octostudios.reliquified_twilight_forest.item.relic.ThornCrownItem",
        "it.hurts.octostudios.reliquified_twilight_forest.item.relic.TwilightFeatherItem"
    },
    remap = false
)
public abstract class RelicItemCompatMixin implements LegacyRelicItem {
}
