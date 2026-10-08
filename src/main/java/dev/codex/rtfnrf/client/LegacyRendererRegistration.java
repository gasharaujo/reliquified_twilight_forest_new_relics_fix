package dev.codex.rtfnrf.client;

import dev.codex.rtfnrf.ReliquifiedTwilightForestNewRelicsFix;
import dev.codex.rtfnrf.legacy.client.LegacyCurioModel;
import dev.codex.rtfnrf.legacy.client.LegacyRenderableCurio;
import it.hurts.sskirillss.relics.init.RelicsRelicRenderers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions;

@EventBusSubscriber(modid = ReliquifiedTwilightForestNewRelicsFix.MOD_ID, value = Dist.CLIENT)
public final class LegacyRendererRegistration {
    private LegacyRendererRegistration() {
    }

    @SubscribeEvent
    public static void setupClient(FMLClientSetupEvent event) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof LegacyRenderableCurio renderable) {
                RelicsRelicRenderers.register(item, () -> renderable);
            }
        }
    }

    @SubscribeEvent
    public static void registerLayers(RegisterLayerDefinitions event) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof LegacyRenderableCurio renderable) {
                event.registerLayerDefinition(LegacyCurioModel.getLayerLocation(item), renderable::constructLayerDefinition);
            }
        }
    }
}
