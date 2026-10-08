package dev.codex.rtfnrf.mixin;

import it.hurts.octostudios.reliquified_twilight_forest.item.BundleLike;
import it.hurts.sskirillss.relics.api.events.utility.ContainerSlotClickEvent;
import net.minecraft.world.inventory.ClickAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Prevents legacy bundle interactions from mutating the same stack on both logical sides. */
@Mixin(targets = "it.hurts.octostudios.reliquified_twilight_forest.handler.BundleLikeEventHandler", remap = false)
public abstract class BundleLikeEventHandlerMixin {
    @Inject(method = "onSlotClick", at = @At("HEAD"), cancellable = true)
    private static void rtfnrf$handleOnlyOnServer(ContainerSlotClickEvent event, CallbackInfo callback) {
        boolean involvesBundle = event.getSlotStack().getItem() instanceof BundleLike
            || event.getHeldStack().getItem() instanceof BundleLike;
        if (event.getEntity().level().isClientSide
            && event.getAction() == ClickAction.SECONDARY
            && involvesBundle) {
            callback.cancel();
        }
    }
}
