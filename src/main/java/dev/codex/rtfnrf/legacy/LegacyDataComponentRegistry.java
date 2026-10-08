package dev.codex.rtfnrf.legacy;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.UUIDUtil;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.item.ItemStack;

public final class LegacyDataComponentRegistry {
    private static final DeferredRegister<DataComponentType<?>> COMPONENTS =
        DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, "reliquified_twilight_forest_new_relics_fix");

    // These names mirror the add-on's old DataComponentRegistry.  The coremod
    // rewrites references to that registry to this class, so every referenced
    // component must exist here as well as the simple integer components below.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ItemStack>>> BUNDLE_LIKE_CONTENTS =
        register("gems", ItemStack.CODEC.listOf());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ZOMBIE_TIME = registerInt("zombie_time");
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TWILIGHT_TIME = registerInt("twilight_time");
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> LIFEDRAIN_TIME = registerInt("absorption_time");
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> FORTIFICATION_TIME = registerInt("fortification_time");
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<UUID>>> ZOMBIES =
        register("zombies", UUIDUtil.CODEC.listOf());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<UUID>>> ENTITIES =
        register("entities", UUIDUtil.CODEC.listOf());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Float>> MULTIPLIER = registerFloat("multiplier");
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TIME = registerInt("time");
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> DONT_EAT = registerBoolean("dont_eat");
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> REGENERATION_TICKS = registerInt("regeneration_ticks");

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> COUNT = registerInt("legacy_count");
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CHARGE = registerInt("legacy_charge");

    private LegacyDataComponentRegistry() {
    }

    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String id, Codec<T> codec) {
        return COMPONENTS.register(id, () -> DataComponentType.<T>builder().persistent(codec).build());
    }

    private static DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> registerInt(String id) {
        return register(id, Codec.INT);
    }

    private static DeferredHolder<DataComponentType<?>, DataComponentType<Float>> registerFloat(String id) {
        return register(id, Codec.FLOAT);
    }

    private static DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> registerBoolean(String id) {
        return register(id, Codec.BOOL);
    }

    public static void register(IEventBus bus) {
        COMPONENTS.register(bus);
    }
}
