package dev.codex.rtfnrf;

import dev.codex.rtfnrf.core.LegacyRelicsApiTransformer;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import cpw.mods.modlauncher.api.ITransformer;
import net.neoforged.coremod.CoreModScriptingEngine;
import net.neoforged.coremod.ICoreModScriptSource;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.ClassNode;

public final class BytecodeCompatibilityVerifier {
    private static final String CURRENT_RELIC_ITEM = "it.hurts.sskirillss.relics.items.relics.base.RelicItem";
    private static final String LEGACY_RELIC_ITEM = "dev/codex/rtfnrf/legacy/LegacyRelicItem";
    private static final String RELIC_ITEM_MIXIN = "dev/codex/rtfnrf/mixin/RelicItemCompatMixin";
    private static final String RELIC_DEFINITION_MIXIN = "dev/codex/rtfnrf/mixin/RelicDefinitionCompatMixin";
    private static final List<String> BANNED = List.of(
        "it/hurts/sskirillss/relics/api/events/common/ContainerSlotClickEvent",
        "it/hurts/sskirillss/relics/client/models/items/CurioModel",
        "it/hurts/sskirillss/relics/client/models/items/SidedCurioModel",
        "it/hurts/sskirillss/relics/components/AbilitiesComponent",
        "it/hurts/sskirillss/relics/components/AbilityComponent",
        "it/hurts/sskirillss/relics/components/LevelingComponent",
        "it/hurts/sskirillss/relics/init/CreativeTabRegistry",
        "it/hurts/sskirillss/relics/init/DataComponentRegistry",
        "it/hurts/sskirillss/relics/init/EffectRegistry",
        "it/hurts/sskirillss/relics/items/relics/base/IRelicItem",
        "it/hurts/sskirillss/relics/items/relics/base/IRenderableCurio",
        "it/hurts/sskirillss/relics/items/relics/base/data/RelicData",
        "it/hurts/sskirillss/relics/items/relics/base/data/cast/CastData",
        "it/hurts/sskirillss/relics/items/relics/base/data/cast/misc/CastStage",
        "it/hurts/sskirillss/relics/items/relics/base/data/cast/misc/CastType",
        "it/hurts/sskirillss/relics/items/relics/base/data/cast/misc/PredicateType",
        "it/hurts/sskirillss/relics/items/relics/base/data/leveling/AbilitiesData",
        "it/hurts/sskirillss/relics/items/relics/base/data/leveling/AbilityData",
        "it/hurts/sskirillss/relics/items/relics/base/data/leveling/LevelingData",
        "it/hurts/sskirillss/relics/items/relics/base/data/leveling/LevelingSourceData",
        "it/hurts/sskirillss/relics/items/relics/base/data/leveling/LevelingSourcesData",
        "it/hurts/sskirillss/relics/items/relics/base/data/leveling/StatData",
        "it/hurts/sskirillss/relics/items/relics/base/data/leveling/misc/UpgradeOperation",
        "it/hurts/sskirillss/relics/items/relics/base/data/loot/LootData",
        "it/hurts/sskirillss/relics/items/relics/base/data/style/BeamsData",
        "it/hurts/sskirillss/relics/items/relics/base/data/style/StyleData",
        "it/hurts/sskirillss/relics/items/relics/base/data/style/TooltipData"
    );

    private BytecodeCompatibilityVerifier() {
    }

    private static int verifiedReferences;
    private static int redirectedBaseCalls;
    private static final Set<String> LINKAGE_ERRORS = new LinkedHashSet<>();
    private static final Map<String, ClassNode> CLASS_CACHE = new HashMap<>();

    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("Expected add-on JAR and compatibility JAR");
        Path addon = Path.of(args[0]);
        Path compat = Path.of(args[1]);
        ITransformer<ClassNode> transformer = loadCoreModTransformer(compat);
        Set<String> targets = transformer.targets().stream().map(target -> target.className()).collect(java.util.stream.Collectors.toSet());
        Set<String> javaReferenceTargets = new LegacyRelicsApiTransformer().targets().stream()
            .map(target -> target.className())
            .collect(java.util.stream.Collectors.toSet());
        if (!targets.equals(javaReferenceTargets)) {
            throw new AssertionError("JavaScript coremod targets differ from the reference transformer: " + targets);
        }

        int classCount = 0;
        int transformedCount = 0;
        Set<String> seenTargets = new HashSet<>();
        try (ZipFile archive = new ZipFile(addon.toFile())) {
            var entries = archive.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".class")) continue;
                classCount++;
                byte[] bytes = archive.getInputStream(entry).readAllBytes();
                ClassNode node = new ClassNode();
                new ClassReader(bytes).accept(node, 0);
                String className = node.name.replace('/', '.');
                if (targets.contains(className)) {
                    seenTargets.add(className);
                    transformer.transform(node, null);
                    ClassWriter writer = new ClassWriter(0);
                    node.accept(writer);
                    bytes = writer.toByteArray();
                    transformedCount++;
                }
                assertNoBannedReferences(className, bytes);
                verifyRelicsReferences(node);
            }
        }

        if (!seenTargets.equals(targets)) {
            Set<String> missing = new HashSet<>(targets);
            missing.removeAll(seenTargets);
            throw new AssertionError("Transformer targets absent from add-on JAR: " + missing);
        }
        if (!LINKAGE_ERRORS.isEmpty()) {
            throw new AssertionError(
                "Unresolved Relics compatibility references:\n - "
                    + String.join("\n - ", LINKAGE_ERRORS)
            );
        }
        verifyScopedRelicItemMixin();
        verifyCompatJar(compat);
        System.out.printf(
            "Verified %d add-on classes; transformed %d affected classes; resolved %d Relics/bridge member references; redirected %d removed RelicItem super calls; no removed or unresolved Relics symbols remain.%n",
            classCount, transformedCount, verifiedReferences, redirectedBaseCalls
        );
    }

    @SuppressWarnings("unchecked")
    private static ITransformer<ClassNode> loadCoreModTransformer(Path compat) throws IOException {
        CoreModScriptingEngine engine = new CoreModScriptingEngine();
        engine.loadCoreMod(new JarScriptSource(compat));
        List<ITransformer<?>> transformers = engine.initializeCoreMods();
        if (transformers.size() != 1) {
            throw new AssertionError("Expected one JavaScript coremod transformer, found " + transformers.size());
        }
        ITransformer<?> transformer = transformers.getFirst();
        if (!transformer.getTargetType().equals(cpw.mods.modlauncher.api.TargetType.CLASS)) {
            throw new AssertionError("JavaScript coremod did not create a class transformer");
        }
        return (ITransformer<ClassNode>) transformer;
    }

    private record JarScriptSource(Path jar) implements ICoreModScriptSource {
        @Override
        public String getOwnerId() {
            return "reliquified_twilight_forest_new_relics_fix";
        }

        @Override
        public Reader readCoreMod() throws IOException {
            return readEntry("coremods/legacy_relics_api.js");
        }

        @Override
        public Reader getAdditionalFile(String name) throws IOException {
            return readEntry(name);
        }

        @Override
        public String getDebugSource() {
            return jar + "!/coremods/legacy_relics_api.js";
        }

        private Reader readEntry(String name) throws IOException {
            try (ZipFile archive = new ZipFile(jar.toFile())) {
                ZipEntry entry = archive.getEntry(name);
                if (entry == null) throw new IOException("Missing coremod resource: " + name);
                return new StringReader(new String(archive.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8));
            }
        }
    }

    private static void verifyRelicsReferences(ClassNode node) throws Exception {
        for (var method : node.methods) {
            for (var instruction : method.instructions) {
                if (instruction instanceof MethodInsnNode call && isRelicsOrBridgeOwner(call.owner)) {
                    if (!hasMethod(call.owner, call.name, call.desc, new HashSet<>())) {
                        LINKAGE_ERRORS.add(node.name + " calls missing method " + call.owner + "." + call.name + call.desc);
                    } else {
                        verifiedReferences++;
                    }
                    verifyInterfaceFlag(node.name, call.owner, call.name, call.desc, call.itf);
                    if (call.owner.equals(LEGACY_RELIC_ITEM) && call.getOpcode() == Opcodes.INVOKESPECIAL) {
                        redirectedBaseCalls++;
                    }
                } else if (instruction instanceof FieldInsnNode access && isRelicsOrBridgeOwner(access.owner)) {
                    if (!hasField(access.owner, access.name, access.desc, new HashSet<>())) {
                        LINKAGE_ERRORS.add(node.name + " accesses missing field " + access.owner + "." + access.name + access.desc);
                    } else {
                        verifiedReferences++;
                    }
                } else if (instruction instanceof InvokeDynamicInsnNode dynamic) {
                    verifyHandle(node.name, dynamic.bsm);
                    for (Object argument : dynamic.bsmArgs) verifyConstant(node.name, argument);
                } else if (instruction instanceof LdcInsnNode constant) {
                    verifyConstant(node.name, constant.cst);
                }
            }
        }
    }

    private static void verifyConstant(String source, Object value) throws IOException {
        if (value instanceof Handle handle) {
            verifyHandle(source, handle);
        } else if (value instanceof ConstantDynamic dynamic) {
            verifyHandle(source, dynamic.getBootstrapMethod());
            for (int i = 0; i < dynamic.getBootstrapMethodArgumentCount(); i++) {
                verifyConstant(source, dynamic.getBootstrapMethodArgument(i));
            }
        }
    }

    private static void verifyHandle(String source, Handle handle) throws IOException {
        if (!isRelicsOrBridgeOwner(handle.getOwner())) return;
        int tag = handle.getTag();
        if (tag >= Opcodes.H_GETFIELD && tag <= Opcodes.H_PUTSTATIC) {
            if (!hasField(handle.getOwner(), handle.getName(), handle.getDesc(), new HashSet<>())) {
                LINKAGE_ERRORS.add(source + " has handle to missing field " + handle.getOwner() + "." + handle.getName() + handle.getDesc());
            } else {
                verifiedReferences++;
            }
            return;
        }
        if (!hasMethod(handle.getOwner(), handle.getName(), handle.getDesc(), new HashSet<>())) {
            LINKAGE_ERRORS.add(source + " has handle to missing method " + handle.getOwner() + "." + handle.getName() + handle.getDesc());
        } else {
            verifiedReferences++;
        }
        verifyInterfaceFlag(source, handle.getOwner(), handle.getName(), handle.getDesc(), handle.isInterface());
    }

    private static void verifyInterfaceFlag(String source, String owner, String name, String descriptor, boolean interfaceReference) throws IOException {
        boolean ownerIsInterface = (readClass(owner).access & Opcodes.ACC_INTERFACE) != 0;
        if (interfaceReference != ownerIsInterface) {
            LINKAGE_ERRORS.add(
                source + " uses the wrong constant-pool reference kind for " + owner + "." + name + descriptor
            );
        }
    }

    private static boolean isRelicsOrBridgeOwner(String owner) {
        return owner.startsWith("dev/codex/rtfnrf/legacy/")
            || owner.startsWith("it/hurts/sskirillss/relics/");
    }

    private static boolean hasMethod(String internalName, String name, String descriptor, Set<String> visited) throws IOException {
        if (internalName == null || !visited.add(internalName)) return false;
        ClassNode type = readClass(internalName);
        for (var method : type.methods) {
            if (method.name.equals(name) && method.desc.equals(descriptor)) return true;
        }
        if (name.equals("<init>")) return false;
        if (hasMethod(type.superName, name, descriptor, visited)) return true;
        for (String contract : type.interfaces) {
            if (hasMethod(contract, name, descriptor, visited)) return true;
        }
        return false;
    }

    private static boolean hasField(String internalName, String name, String descriptor, Set<String> visited) throws IOException {
        if (internalName == null || !visited.add(internalName)) return false;
        ClassNode type = readClass(internalName);
        for (var field : type.fields) {
            if (field.name.equals(name) && field.desc.equals(descriptor)) return true;
        }
        if (hasField(type.superName, name, descriptor, visited)) return true;
        for (String contract : type.interfaces) {
            if (hasField(contract, name, descriptor, visited)) return true;
        }
        return false;
    }

    private static ClassNode readClass(String internalName) throws IOException {
        ClassNode cached = CLASS_CACHE.get(internalName);
        if (cached != null) return cached;
        try (InputStream stream = BytecodeCompatibilityVerifier.class.getClassLoader().getResourceAsStream(internalName + ".class")) {
            if (stream == null) throw new AssertionError("Referenced owner class is absent: " + internalName);
            ClassNode node = new ClassNode();
            new ClassReader(stream).accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            CLASS_CACHE.put(internalName, node);
            return node;
        }
    }

    private static void verifyScopedRelicItemMixin() throws IOException {
        ClassNode bridgeMixin = readClass(RELIC_ITEM_MIXIN);
        if (!bridgeMixin.interfaces.contains(LEGACY_RELIC_ITEM)) {
            throw new AssertionError("Twilight Forest item mixin does not add the legacy contract");
        }

        Set<String> bridgeTargets = new HashSet<>();
        readMixinTargets(bridgeMixin.visibleAnnotations, bridgeTargets);
        readMixinTargets(bridgeMixin.invisibleAnnotations, bridgeTargets);

        ClassNode definitionMixin = readClass(RELIC_DEFINITION_MIXIN);
        Set<String> definitionTargets = new HashSet<>();
        readMixinTargets(definitionMixin.visibleAnnotations, definitionTargets);
        readMixinTargets(definitionMixin.invisibleAnnotations, definitionTargets);

        if (bridgeTargets.size() != 18 || !bridgeTargets.equals(definitionTargets)) {
            throw new AssertionError("Legacy bridge must target exactly the 18 translated relic classes: " + bridgeTargets);
        }
        if (bridgeTargets.contains(CURRENT_RELIC_ITEM)) {
            throw new AssertionError("Twilight Forest bridge leaked onto the global RelicItem base");
        }
    }

    private static void readMixinTargets(List<AnnotationNode> annotations, Set<String> targets) {
        if (annotations == null) return;
        for (AnnotationNode annotation : annotations) {
            if (!annotation.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;") || annotation.values == null) continue;
            for (int index = 0; index < annotation.values.size(); index += 2) {
                if (!annotation.values.get(index).equals("targets")) continue;
                Object value = annotation.values.get(index + 1);
                if (value instanceof List<?> values) {
                    for (Object target : values) targets.add(String.valueOf(target));
                }
            }
        }
    }

    private static void assertNoBannedReferences(String className, byte[] bytes) {
        String constants = new String(bytes, StandardCharsets.ISO_8859_1);
        for (String banned : BANNED) {
            if (constants.contains(banned)) {
                throw new AssertionError(className + " still references removed symbol " + banned);
            }
        }
    }

    private static void verifyCompatJar(Path compat) throws IOException {
        try (ZipFile archive = new ZipFile(compat.toFile())) {
            requireEntry(archive, "META-INF/coremods.json");
            requireEntry(archive, "coremods/legacy_relics_api.js");
            requireEntry(archive, "META-INF/neoforge.mods.toml");
            requireEntry(archive, "reliquified_twilight_forest_new_relics_fix.mixins.json");
            requireEntry(archive, "dev/codex/rtfnrf/legacy/LegacyEffectRegistry.class");
            requireEntry(archive, "dev/codex/rtfnrf/legacy/LegacyRelicItem.class");
            requireEntry(archive, "dev/codex/rtfnrf/mixin/RelicDefinitionCompatMixin.class");
            requireEntry(archive, "dev/codex/rtfnrf/mixin/RelicItemCompatMixin.class");
            var entries = archive.entries();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith("it/hurts/sskirillss/relics/")) {
                    throw new AssertionError("Compatibility JAR creates a split Relics package: " + name);
                }
            }
        }
    }

    private static void requireEntry(ZipFile archive, String name) {
        if (archive.getEntry(name) == null) throw new AssertionError("Missing JAR entry: " + name);
    }
}
