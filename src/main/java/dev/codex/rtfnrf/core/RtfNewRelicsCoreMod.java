package dev.codex.rtfnrf.core;

import cpw.mods.modlauncher.api.ITransformer;
import java.util.List;
import net.neoforged.neoforgespi.coremod.ICoreMod;

public final class RtfNewRelicsCoreMod implements ICoreMod {
    @Override
    public Iterable<? extends ITransformer<?>> getTransformers() {
        return List.of(new LegacyRelicsApiTransformer());
    }
}

