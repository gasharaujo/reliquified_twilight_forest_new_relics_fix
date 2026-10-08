package dev.codex.rtfnrf.legacy;

import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class LegacyEffectRegistry {
    public static final DeferredHolder<MobEffect, MobEffect> STUN = RelicsMobEffects.STUN;
    public static final DeferredHolder<MobEffect, MobEffect> PARALYSIS = RelicsMobEffects.PARALYSIS;
    public static final DeferredHolder<MobEffect, MobEffect> IMMORTALITY = RelicsMobEffects.IMMORTALITY;
    public static final DeferredHolder<MobEffect, MobEffect> VANISHING = RelicsMobEffects.VANISHING;
    public static final DeferredHolder<MobEffect, MobEffect> ANTI_HEAL = RelicsMobEffects.ANTI_HEAL;

    private LegacyEffectRegistry() {
    }
}

