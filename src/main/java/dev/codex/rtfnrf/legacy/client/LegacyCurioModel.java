package dev.codex.rtfnrf.legacy.client;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

public class LegacyCurioModel extends HumanoidModel<LivingEntity> {
    private final Item item;
    private final ModelPart root;

    private LegacyCurioModel(Item item, ModelPart root) {
        super(root);
        this.item = item;
        this.root = root;
    }

    public LegacyCurioModel(Item item) {
        this(item, constructRoot(item));
    }

    public static ModelLayerLocation getLayerLocation(Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath()), id.getPath());
    }

    public static ModelPart constructRoot(Item item) {
        return Minecraft.getInstance().getEntityModels().bakeLayer(getLayerLocation(item));
    }

    public ModelPart getById(String id) {
        return switch (id) {
            case "head" -> head;
            case "hat" -> hat;
            case "body" -> body;
            case "right_arm" -> rightArm;
            case "left_arm" -> leftArm;
            case "right_leg" -> rightLeg;
            case "left_leg" -> leftLeg;
            default -> root.getChild(id);
        };
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return item instanceof LegacyRenderableCurio renderable
            ? renderable.headParts().stream().map(this::getById).toList()
            : List.of();
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return item instanceof LegacyRenderableCurio renderable
            ? renderable.bodyParts().stream().map(this::getById).toList()
            : List.of();
    }

    public Item getItem() { return item; }
    public ModelPart getRoot() { return root; }
}

