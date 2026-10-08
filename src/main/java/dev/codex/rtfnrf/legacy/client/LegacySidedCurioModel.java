package dev.codex.rtfnrf.legacy.client;

import java.util.List;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.item.Item;

public class LegacySidedCurioModel extends LegacyCurioModel {
    private int slot;

    public LegacySidedCurioModel(Item item) {
        super(item);
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return filteredParts(true);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return filteredParts(false);
    }

    private Iterable<ModelPart> filteredParts(boolean head) {
        if (!(getItem() instanceof LegacyRenderableCurio renderable)) return List.of();
        String hiddenSide = slot % 2 == 0 ? "left" : "right";
        List<String> ids = head ? renderable.headParts() : renderable.bodyParts();
        return ids.stream().filter(id -> !id.startsWith(hiddenSide)).map(this::getById).toList();
    }

    public int getSlot() { return slot; }
    public void setSlot(int slot) { this.slot = slot; }
}

