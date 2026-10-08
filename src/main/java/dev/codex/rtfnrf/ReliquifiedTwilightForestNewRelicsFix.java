package dev.codex.rtfnrf;

import dev.codex.rtfnrf.legacy.LegacyDataComponentRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(ReliquifiedTwilightForestNewRelicsFix.MOD_ID)
public final class ReliquifiedTwilightForestNewRelicsFix {
    public static final String MOD_ID = "reliquified_twilight_forest_new_relics_fix";

    public ReliquifiedTwilightForestNewRelicsFix(IEventBus modBus) {
        LegacyDataComponentRegistry.register(modBus);
    }
}

