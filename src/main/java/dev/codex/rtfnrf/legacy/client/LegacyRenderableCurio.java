package dev.codex.rtfnrf.legacy.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.hurts.sskirillss.relics.client.renderer.items.base.IRelicRenderer;
import java.util.List;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public interface LegacyRenderableCurio extends IRelicRenderer {
    default LegacyCurioModel getModel(ItemStack stack) {
        return new LegacyCurioModel(stack.getItem());
    }

    default ResourceLocation getTexture(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "textures/item/model/" + id.getPath() + ".png");
    }

    default List<String> headParts() { return List.of(); }
    default List<String> bodyParts() { return List.of(); }
    LayerDefinition constructLayerDefinition();

    @Override
    default <T extends LivingEntity, M extends EntityModel<T>> void render(
        ItemStack stack, SlotContext slotContext, PoseStack poseStack,
        RenderLayerParent<T, M> renderLayerParent, MultiBufferSource buffers,
        int light, float limbSwing, float limbSwingAmount, float partialTicks,
        float ageInTicks, float netHeadYaw, float headPitch
    ) {
        LegacyCurioModel model = getModel(stack);
        poseStack.pushPose();
        LivingEntity entity = slotContext.entity();
        model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
        model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        ICurioRenderer.followBodyRotations(entity, model);
        VertexConsumer consumer = ItemRenderer.getArmorFoilBuffer(
            buffers, RenderType.armorCutoutNoCull(getTexture(stack)), stack.hasFoil()
        );
        model.renderToBuffer(poseStack, consumer, light, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}

