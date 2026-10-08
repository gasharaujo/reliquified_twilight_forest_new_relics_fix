package dev.codex.rtfnrf.legacy;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import dev.codex.rtfnrf.legacy.component.LegacyAbilitiesComponent;
import dev.codex.rtfnrf.legacy.component.LegacyAbilityComponent;
import dev.codex.rtfnrf.legacy.component.LegacyLevelingComponent;
import dev.codex.rtfnrf.legacy.data.RelicData;
import dev.codex.rtfnrf.legacy.data.cast.misc.CastStage;
import dev.codex.rtfnrf.legacy.data.cast.misc.CastType;
import dev.codex.rtfnrf.legacy.data.leveling.AbilitiesData;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.abilities.activation.AbilityActivationContext;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicAttributeModifier;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicSlotModifier;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/** Runtime surface of the Relics 0.10 IRelicItem API used by the add-on. */
public interface LegacyRelicItem extends IRelicItem, ICurioItem {
    Set<String> LICH_CROWN_GEM_ABILITIES = Set.of(
        "fortification", "lifedrain", "twilight", "zombie", "frostbite",
        "biome_burn", "vendetta", "ethereal_guard", "mirror_leech", "frenzy"
    );

    default RelicData constructLegacyRelicData() {
        try {
            Object result = getClass().getMethod("constructDefaultRelicData").invoke(this);
            if (result instanceof RelicData data) return data;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Could not read the legacy relic definition from " + getClass().getName(), exception);
        }
        throw new IllegalStateException("Unexpected legacy relic definition type in " + getClass().getName());
    }

    default AbilitiesData getAbilitiesData() {
        return constructLegacyRelicData().getAbilities();
    }

    default void castActiveAbility(ItemStack stack, Player player, String ability, CastType type, CastStage stage) {
    }

    @Override
    default void activateAbility(AbilityActivationContext context) {
        castActiveAbility(
            context.stack(), context.player(), context.abilityData().getId(),
            CastType.valueOf(context.type().name()), CastStage.valueOf(context.stage().name())
        );
    }

    default RelicAttributeModifier getRelicAttributeModifiers(ItemStack stack) {
        return RelicAttributeModifier.builder().build();
    }

    default RelicSlotModifier getSlotModifiers(ItemStack stack) {
        return RelicSlotModifier.builder().build();
    }

    @Override
    default Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(
        SlotContext slotContext, ResourceLocation id, ItemStack stack
    ) {
        Multimap<Holder<Attribute>, AttributeModifier> modifiers = LinkedHashMultimap.create();
        RelicAttributeModifier attributes = getRelicAttributeModifiers(stack);
        if (attributes != null) {
            attributes.getAttributes().forEach(attribute -> modifiers.put(
                attribute.getAttribute(),
                new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath(
                        "reliquified_twilight_forest_new_relics_fix",
                        BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() + "_"
                            + BuiltInRegistries.ATTRIBUTE.getKey(attribute.getAttribute().value()).getPath() + "_"
                            + slotContext.identifier() + "_" + slotContext.index()
                    ),
                    attribute.getMultiplier(), attribute.getOperation()
                )
            ));
        }
        RelicSlotModifier slots = getSlotModifiers(stack);
        if (slots != null) {
            slots.getModifiers().forEach((slot, count) ->
                CuriosApi.addSlotModifier(modifiers, slot, id, count, AttributeModifier.Operation.ADD_VALUE)
            );
        }
        return modifiers;
    }

    default it.hurts.sskirillss.relics.api.relics.data.RelicData current(ItemStack stack) {
        return getRelicData(null, stack);
    }

    default double getStatValue(ItemStack stack, String ability, String stat) {
        return current(stack).getAbilitiesData().getAbilityData(ability).getStatData(stat).getValue();
    }

    default double getStatValue(ItemStack stack, String ability, String stat, int level) {
        return current(stack).getAbilitiesData().getAbilityData(ability).getStatData(stat).getValueForLevel(level);
    }

    default int getStatQuality(ItemStack stack, String ability, String stat) {
        return current(stack).getAbilitiesData().getAbilityData(ability).getStatData(stat).getQuality();
    }

    default double getStatValueByQuality(String ability, String stat, int quality) {
        ItemStack temporary = new ItemStack(getItem());
        return current(temporary).getAbilitiesData().getAbilityData(ability).getStatData(stat).getValueFromQuality(quality);
    }

    default void setStatInitialValue(ItemStack stack, String ability, String stat, double value) {
        current(stack).getAbilitiesData().getAbilityData(ability).getStatData(stat).setOverrideValue(value);
    }

    default int getAbilityLevel(ItemStack stack, String ability) {
        return current(stack).getAbilitiesData().getAbilityData(ability).getLevel();
    }

    default int getAbilityMaxLevel(ItemStack stack, String ability) {
        return current(stack).getAbilitiesData().getAbilityData(ability).getTemplate().getInitialMaxLevel();
    }

    default boolean isAbilityUnlocked(ItemStack stack, String ability) {
        if (isLichCrown(this)) {
            if (LICH_CROWN_GEM_ABILITIES.contains(ability)) {
                return getAbilityLevel(stack, ability) > 0;
            }
            if (ability.equals("twilight_sovereign")) {
                return LICH_CROWN_GEM_ABILITIES.stream().allMatch(gemAbility -> getAbilityLevel(stack, gemAbility) > 0);
            }
        }
        return current(stack).getAbilitiesData().getAbilityData(ability).isUnlocked();
    }

    static boolean isLichCrown(LegacyRelicItem relic) {
        return relic.getClass().getName().equals("it.hurts.octostudios.reliquified_twilight_forest.item.relic.LichCrownItem");
    }

    default boolean isAbilityEnabled(ItemStack stack, String ability) {
        return current(stack).getAbilitiesData().getAbilityData(ability).isEnabled();
    }

    default boolean isAbilityTicking(ItemStack stack, String ability) {
        return current(stack).getAbilitiesData().getAbilityData(ability).isActivationTicking();
    }

    default boolean isAbilityMaxLevel(ItemStack stack, String ability) {
        return current(stack).getAbilitiesData().getAbilityData(ability).isMaxLevel();
    }

    default boolean isAbilityFlawless(ItemStack stack, String ability) {
        var data = current(stack).getAbilitiesData().getAbilityData(ability);
        return data.isMaxLevel() && data.calculateQuality() >= data.getMaxQuality();
    }

    default boolean mayUpgrade(ItemStack stack, String ability) {
        return current(stack).getAbilitiesData().getAbilityData(ability).mayUpgrade();
    }

    default boolean mayReset(ItemStack stack, String ability) {
        return current(stack).getAbilitiesData().getAbilityData(ability).mayReset();
    }

    default boolean isAbilityUpgradeEnabled(ItemStack stack, String ability) {
        return current(stack).getAbilitiesData().getAbilityData(ability).isUpgradeEnabled();
    }

    default boolean isAbilityResetEnabled(ItemStack stack, String ability) {
        return current(stack).getAbilitiesData().getAbilityData(ability).isResetEnabled();
    }

    default LegacyAbilitiesComponent getAbilitiesComponent(ItemStack stack) {
        return new LegacyAbilitiesComponent(current(stack).getAbilitiesData().getComponent());
    }

    default void setAbilitiesComponent(ItemStack stack, LegacyAbilitiesComponent component) {
        current(stack).getAbilitiesData().setComponent(component.unwrap());
    }

    default LegacyAbilityComponent getAbilityComponent(ItemStack stack, String ability) {
        return new LegacyAbilityComponent(current(stack).getAbilitiesData().getAbilityData(ability).getComponent());
    }

    default void setAbilityComponent(ItemStack stack, String ability, LegacyAbilityComponent component) {
        current(stack).getAbilitiesData().getAbilityData(ability).setComponent(component.unwrap());
    }

    default LegacyLevelingComponent getLevelingComponent(ItemStack stack) {
        return new LegacyLevelingComponent(current(stack).getLevelingData().getComponent());
    }

    default void setLevelingComponent(ItemStack stack, LegacyLevelingComponent component) {
        current(stack).getLevelingData().setComponent(component.unwrap());
    }

    default void spreadRelicExperience(LivingEntity entity, ItemStack stack, int amount) {
        getRelicData(entity, stack).getLevelingData().addExperience(amount);
    }

    default void spreadRelicExperience(LivingEntity entity, ItemStack stack, int amount, double multiplier) {
        getRelicData(entity, stack).getLevelingData().addExperience(amount * multiplier);
    }
}
