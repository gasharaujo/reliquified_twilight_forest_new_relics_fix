package dev.codex.rtfnrf.mixin;

import it.hurts.octostudios.reliquified_twilight_forest.item.relic.DeerAntlerItem;
import it.hurts.sskirillss.relics.api.relic_containers.AbilityReference;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.abilities.activation.AbilityActivationStage;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Restores the client callback used to select Ride Along's target and send its own packet. */
@Mixin(targets = "it.hurts.sskirillss.relics.client.handlers.ActiveAbilitiesClientHandler", remap = false)
public abstract class ClientAbilityActivationMixin {
    @Inject(
        method = "sendActivation(Lit/hurts/sskirillss/relics/api/relic_containers/AbilityReference;"
            + "Lit/hurts/sskirillss/relics/api/relics/abilities/activation/AbilityActivationStage;)V",
        at = @At("HEAD")
    )
    private static void rtfnrf$activateRideAlongOnClient(
        AbilityReference reference, AbilityActivationStage stage, CallbackInfo ci
    ) {
        // Relics 0.12 only sends C2SActivateAbility. Deer Antler deliberately
        // ignores server callbacks and performs its targeting on client END.
        if (stage != AbilityActivationStage.END || reference.isSynergy()
            || !"ride_along".equals(reference.ability())) return;

        var player = Minecraft.getInstance().player;
        if (player == null) return;
        var stack = reference.stackReference().getStack(player);
        if (!(stack.getItem() instanceof DeerAntlerItem item)) return;

        var ability = ((IRelicItem) (Object) item).getRelicData(player, stack)
            .getAbilitiesData().getAbilityData(reference.ability());
        if (ability != null) {
            // Retain Relics' unlock, cooldown and predicate checks before the old callback.
            ability.activate(player, stage);
        }
    }
}
