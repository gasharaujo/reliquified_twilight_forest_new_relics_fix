package dev.codex.rtfnrf.core;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TargetType;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

/** Rewrites the Relics 0.10 symbols before Mixin reads the add-on's mixin classes. */
public final class LegacyRelicsApiTransformer implements ITransformer<ClassNode> {
    private static final String PREFIX = "it.hurts.octostudios.reliquified_twilight_forest.";
    private static final String RELIC_ITEM = "it/hurts/sskirillss/relics/items/relics/base/RelicItem";
    private static final String LEGACY_RELIC_ITEM = "dev/codex/rtfnrf/legacy/LegacyRelicItem";

    private static final Set<String> LEGACY_RELIC_ITEM_SUPER_METHODS = Set.of(
        "curioTick(Ltop/theillusivec4/curios/api/SlotContext;Lnet/minecraft/world/item/ItemStack;)V",
        "onUnequip(Ltop/theillusivec4/curios/api/SlotContext;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V",
        "canEquip(Ltop/theillusivec4/curios/api/SlotContext;Lnet/minecraft/world/item/ItemStack;)Z",
        "castActiveAbility(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Ljava/lang/String;Lit/hurts/sskirillss/relics/items/relics/base/data/cast/misc/CastType;Lit/hurts/sskirillss/relics/items/relics/base/data/cast/misc/CastStage;)V",
        "getSlotModifiers(Lnet/minecraft/world/item/ItemStack;)Lit/hurts/sskirillss/relics/items/relics/base/data/RelicSlotModifier;"
    );

    private static final Set<String> TARGET_CLASSES = Set.of(
        PREFIX + "client.gui.layer.RedVignetteLayer",
        PREFIX + "handler.BundleLikeEventHandler",
        PREFIX + "item.ability.LichCrownAbilities",
        PREFIX + "item.ability.LichCrownAbilities$CommonEvents",
        PREFIX + "item.BundleLikeRelicItem",
        PREFIX + "item.GemItem",
        PREFIX + "item.relic.CharmBackpackItem",
        PREFIX + "item.relic.ChromaticCloakItem",
        PREFIX + "item.relic.CicadaBottleItem",
        PREFIX + "item.relic.DeerAntlerItem",
        PREFIX + "item.relic.FireflyQueenItem",
        PREFIX + "item.relic.GiantGloveItem",
        PREFIX + "item.relic.GoblinNoseItem",
        PREFIX + "item.relic.HydraHeartItem",
        PREFIX + "item.relic.InvisibilityCloakItem",
        PREFIX + "item.relic.LichCrownItem",
        PREFIX + "item.relic.MapleSyrupBottleItem",
        PREFIX + "item.relic.MinotaurHoofItem",
        PREFIX + "item.relic.Parasite115Item",
        PREFIX + "item.relic.Parasite116Item",
        PREFIX + "item.relic.ScaledCloakItem",
        PREFIX + "item.relic.SteelCapeItem",
        PREFIX + "item.relic.ThornCrownItem",
        PREFIX + "item.relic.TwilightFeatherItem",
        PREFIX + "mixin.ChainBlockMixin",
        PREFIX + "network.ScaledCloakWallClimbPacket"
    );

    private static final Map<String, String> TYPE_MAP = Map.ofEntries(
        entry("api/events/common/ContainerSlotClickEvent", "it/hurts/sskirillss/relics/api/events/utility/ContainerSlotClickEvent"),
        entry("client/models/items/CurioModel", "dev/codex/rtfnrf/legacy/client/LegacyCurioModel"),
        entry("client/models/items/SidedCurioModel", "dev/codex/rtfnrf/legacy/client/LegacySidedCurioModel"),
        entry("components/AbilitiesComponent", "dev/codex/rtfnrf/legacy/component/LegacyAbilitiesComponent"),
        entry("components/AbilityComponent", "dev/codex/rtfnrf/legacy/component/LegacyAbilityComponent"),
        entry("components/LevelingComponent", "dev/codex/rtfnrf/legacy/component/LegacyLevelingComponent"),
        entry("init/CreativeTabRegistry", "it/hurts/sskirillss/relics/init/RelicsCreativeTabs"),
        entry("init/DataComponentRegistry", "dev/codex/rtfnrf/legacy/LegacyDataComponentRegistry"),
        entry("init/EffectRegistry", "dev/codex/rtfnrf/legacy/LegacyEffectRegistry"),
        entry("items/relics/base/IRelicItem", "dev/codex/rtfnrf/legacy/LegacyRelicItem"),
        entry("items/relics/base/IRenderableCurio", "dev/codex/rtfnrf/legacy/client/LegacyRenderableCurio"),
        entry("items/relics/base/data/RelicData", "dev/codex/rtfnrf/legacy/data/RelicData"),
        entry("items/relics/base/data/cast/CastData", "dev/codex/rtfnrf/legacy/data/cast/CastData"),
        entry("items/relics/base/data/cast/misc/CastStage", "dev/codex/rtfnrf/legacy/data/cast/misc/CastStage"),
        entry("items/relics/base/data/cast/misc/CastType", "dev/codex/rtfnrf/legacy/data/cast/misc/CastType"),
        entry("items/relics/base/data/cast/misc/PredicateType", "dev/codex/rtfnrf/legacy/data/cast/misc/PredicateType"),
        entry("items/relics/base/data/leveling/AbilitiesData", "dev/codex/rtfnrf/legacy/data/leveling/AbilitiesData"),
        entry("items/relics/base/data/leveling/AbilityData", "dev/codex/rtfnrf/legacy/data/leveling/AbilityData"),
        entry("items/relics/base/data/leveling/LevelingData", "dev/codex/rtfnrf/legacy/data/leveling/LevelingData"),
        entry("items/relics/base/data/leveling/LevelingSourceData", "dev/codex/rtfnrf/legacy/data/leveling/LevelingSourceData"),
        entry("items/relics/base/data/leveling/LevelingSourcesData", "dev/codex/rtfnrf/legacy/data/leveling/LevelingSourcesData"),
        entry("items/relics/base/data/leveling/StatData", "dev/codex/rtfnrf/legacy/data/leveling/StatData"),
        entry("items/relics/base/data/leveling/misc/UpgradeOperation", "dev/codex/rtfnrf/legacy/data/leveling/misc/UpgradeOperation"),
        entry("items/relics/base/data/loot/LootData", "dev/codex/rtfnrf/legacy/data/loot/LootData"),
        entry("items/relics/base/data/style/BeamsData", "dev/codex/rtfnrf/legacy/data/style/BeamsData"),
        entry("items/relics/base/data/style/StyleData", "dev/codex/rtfnrf/legacy/data/style/StyleData"),
        entry("items/relics/base/data/style/TooltipData", "dev/codex/rtfnrf/legacy/data/style/TooltipData")
    );

    private static Map.Entry<String, String> entry(String legacySuffix, String current) {
        return Map.entry("it/hurts/sskirillss/relics/" + legacySuffix, current);
    }

    @Override
    public ClassNode transform(ClassNode input, ITransformerVotingContext context) {
        remapClass(input);
        return input;
    }

    @Override
    public TransformerVoteResult castVote(ITransformerVotingContext context) {
        return TransformerVoteResult.YES;
    }

    @Override
    public Set<Target<ClassNode>> targets() {
        return TARGET_CLASSES.stream()
            .map(Target::targetClass)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public TargetType<ClassNode> getTargetType() {
        return TargetType.CLASS;
    }

    private static void remapClass(ClassNode classNode) {
        classNode.superName = mapInternalName(classNode.superName);
        classNode.interfaces.replaceAll(LegacyRelicsApiTransformer::mapInternalName);
        classNode.signature = mapSignature(classNode.signature);
        classNode.outerClass = mapInternalName(classNode.outerClass);
        classNode.outerMethodDesc = mapDescriptor(classNode.outerMethodDesc);
        classNode.nestHostClass = mapInternalName(classNode.nestHostClass);
        if (classNode.nestMembers != null) {
            classNode.nestMembers.replaceAll(LegacyRelicsApiTransformer::mapInternalName);
        }
        if (classNode.permittedSubclasses != null) {
            classNode.permittedSubclasses.replaceAll(LegacyRelicsApiTransformer::mapInternalName);
        }
        if (classNode.innerClasses != null) {
            for (var inner : classNode.innerClasses) {
                inner.name = mapInternalName(inner.name);
                inner.outerName = mapInternalName(inner.outerName);
            }
        }
        if (classNode.recordComponents != null) {
            for (var component : classNode.recordComponents) {
                component.descriptor = mapDescriptor(component.descriptor);
                component.signature = mapSignature(component.signature);
            }
        }

        for (var field : classNode.fields) {
            field.desc = mapDescriptor(field.desc);
            field.signature = mapSignature(field.signature);
            field.value = mapConstant(field.value);
        }

        for (var method : classNode.methods) {
            method.desc = mapDescriptor(method.desc);
            method.signature = mapSignature(method.signature);
            if (method.exceptions != null) {
                method.exceptions.replaceAll(LegacyRelicsApiTransformer::mapInternalName);
            }

            for (AbstractInsnNode instruction : method.instructions) {
                if (instruction instanceof TypeInsnNode node) {
                    node.desc = node.desc.startsWith("[") ? mapDescriptor(node.desc) : mapInternalName(node.desc);
                } else if (instruction instanceof FieldInsnNode node) {
                    node.owner = mapInternalName(node.owner);
                    node.desc = mapDescriptor(node.desc);
                } else if (instruction instanceof MethodInsnNode node) {
                    if (isLegacyRelicItemSuperCall(node)) {
                        node.owner = LEGACY_RELIC_ITEM;
                        node.itf = true;
                    } else {
                        node.owner = mapInternalName(node.owner);
                    }
                    node.desc = mapDescriptor(node.desc);
                } else if (instruction instanceof InvokeDynamicInsnNode node) {
                    node.desc = mapDescriptor(node.desc);
                    node.bsm = mapHandle(node.bsm);
                    for (int i = 0; i < node.bsmArgs.length; i++) {
                        node.bsmArgs[i] = mapConstant(node.bsmArgs[i]);
                    }
                } else if (instruction instanceof LdcInsnNode node) {
                    node.cst = mapConstant(node.cst);
                } else if (instruction instanceof MultiANewArrayInsnNode node) {
                    node.desc = mapDescriptor(node.desc);
                } else if (instruction instanceof FrameNode node) {
                    remapFrameEntries(node.local);
                    remapFrameEntries(node.stack);
                }
            }

            if (method.tryCatchBlocks != null) {
                for (var block : method.tryCatchBlocks) {
                    block.type = mapInternalName(block.type);
                }
            }
            if (method.localVariables != null) {
                for (var local : method.localVariables) {
                    local.desc = mapDescriptor(local.desc);
                    local.signature = mapSignature(local.signature);
                }
            }
        }
    }

    private static boolean isLegacyRelicItemSuperCall(MethodInsnNode node) {
        return node.getOpcode() == Opcodes.INVOKESPECIAL
            && node.owner.equals(RELIC_ITEM)
            && LEGACY_RELIC_ITEM_SUPER_METHODS.contains(node.name + node.desc);
    }

    private static void remapFrameEntries(List<Object> entries) {
        if (entries == null) return;
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i) instanceof String name) {
                entries.set(i, mapInternalName(name));
            }
        }
    }

    private static Object mapConstant(Object value) {
        if (value instanceof Type type) return mapType(type);
        if (value instanceof Handle handle) return mapHandle(handle);
        if (value instanceof ConstantDynamic dynamic) {
            Object[] arguments = new Object[dynamic.getBootstrapMethodArgumentCount()];
            for (int i = 0; i < arguments.length; i++) {
                arguments[i] = mapConstant(dynamic.getBootstrapMethodArgument(i));
            }
            return new ConstantDynamic(
                dynamic.getName(), mapDescriptor(dynamic.getDescriptor()),
                mapHandle(dynamic.getBootstrapMethod()), arguments
            );
        }
        return value;
    }

    private static Handle mapHandle(Handle handle) {
        return new Handle(
            handle.getTag(), mapInternalName(handle.getOwner()), handle.getName(),
            mapDescriptor(handle.getDesc()), handle.isInterface()
        );
    }

    private static String mapDescriptor(String descriptor) {
        return descriptor == null ? null : mapType(Type.getType(descriptor)).getDescriptor();
    }

    private static Type mapType(Type type) {
        return switch (type.getSort()) {
            case Type.OBJECT -> Type.getObjectType(mapInternalName(type.getInternalName()));
            case Type.ARRAY -> Type.getType("[".repeat(type.getDimensions()) + mapType(type.getElementType()).getDescriptor());
            case Type.METHOD -> Type.getMethodType(
                mapType(type.getReturnType()),
                Arrays.stream(type.getArgumentTypes()).map(LegacyRelicsApiTransformer::mapType).toArray(Type[]::new)
            );
            default -> type;
        };
    }

    private static String mapSignature(String signature) {
        if (signature == null) return null;
        String mapped = signature;
        for (var entry : TYPE_MAP.entrySet()) {
            mapped = mapped.replace(entry.getKey(), entry.getValue());
        }
        return mapped;
    }

    private static String mapInternalName(String internalName) {
        if (internalName == null) return null;
        for (var entry : TYPE_MAP.entrySet()) {
            String legacy = entry.getKey();
            if (legacy.equals(internalName)) return entry.getValue();
            if (internalName.startsWith(legacy + "$")) {
                return entry.getValue() + internalName.substring(legacy.length());
            }
        }
        return internalName;
    }
}
