package dev.codex.rtfnrf.mixin;

import dev.codex.rtfnrf.legacy.LegacyRelicItem;
import it.hurts.sskirillss.relics.api.relics.data.AbilityData;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Applies the legacy Lich Crown gem gating to Relics 0.12's ability state. */
@Mixin(value = AbilityData.class, remap = false)
public abstract class LichCrownAbilityDataMixin {
    @Inject(method = "isEnabled", at = @At("HEAD"), cancellable = true)
    private void rtfnrf$gateEnabledByGems(CallbackInfoReturnable<Boolean> callback) {
        LegacyRelicItem relic = getLichCrown();
        if (relic == null) return;

        String ability = ((AbilityData) (Object) this).getId();
        ItemStack stack = getStack();
        if (LegacyRelicItem.LICH_CROWN_GEM_ABILITIES.contains(ability)) {
            callback.setReturnValue(hasGem(relic, stack, ability));
        } else if (ability.equals("twilight_sovereign")) {
            callback.setReturnValue(LegacyRelicItem.LICH_CROWN_GEM_ABILITIES.stream().allMatch(gem -> hasGem(relic, stack, gem)));
        }
    }

    @Inject(method = "isUnlocked", at = @At("HEAD"), cancellable = true)
    private void rtfnrf$gateUnlockedByGems(CallbackInfoReturnable<Boolean> callback) {
        LegacyRelicItem relic = getLichCrown();
        if (relic == null) return;

        String ability = ((AbilityData) (Object) this).getId();
        ItemStack stack = getStack();
        if (LegacyRelicItem.LICH_CROWN_GEM_ABILITIES.contains(ability)) {
            callback.setReturnValue(hasGem(relic, stack, ability));
        } else if (ability.equals("twilight_sovereign")) {
            callback.setReturnValue(LegacyRelicItem.LICH_CROWN_GEM_ABILITIES.stream().allMatch(gem -> hasGem(relic, stack, gem)));
        }
    }

    private LegacyRelicItem getLichCrown() {
        var relicData = ((AbilityData) (Object) this).getAbilitiesData().getRelicData();
        if (relicData.getRelic() instanceof LegacyRelicItem relic && LegacyRelicItem.isLichCrown(relic)) {
            return relic;
        }
        return null;
    }

    private ItemStack getStack() {
        return ((AbilityData) (Object) this).getAbilitiesData().getRelicData().getStack();
    }

    private static boolean hasGem(LegacyRelicItem relic, ItemStack stack, String ability) {
        return relic.getAbilityLevel(stack, ability) > 0;
    }
}
