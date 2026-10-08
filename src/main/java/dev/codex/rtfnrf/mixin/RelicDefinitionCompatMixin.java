package dev.codex.rtfnrf.mixin;

import dev.codex.rtfnrf.legacy.data.RelicData;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import java.lang.reflect.InvocationTargetException;
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
public abstract class RelicDefinitionCompatMixin {
    public RelicTemplate constructDefaultRelicTemplate() {
        try {
            Object value = getClass().getMethod("constructDefaultRelicData").invoke(this);
            if (value instanceof RelicData data) {
                return data.toTemplate();
            }
            throw new IllegalStateException("Unexpected legacy relic definition: " + value);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Failed to translate relic definition for " + getClass().getName(), exception);
        }
    }
}
