package rw.modden.npc;

import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class NpcRenderer extends BipedEntityRenderer<NpcEntity, PlayerEntityModel<NpcEntity>> {
    public NpcRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new PlayerEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER), false), 0.5F);
    }

    @Override
    public Identifier getTexture(NpcEntity entity) {
        return DefaultSkinHelper.getTexture();
    }

    @Override
    protected void scale(NpcEntity npc, MatrixStack matrices, float amount) {
        matrices.scale(npc.getWidthSize()/0.6F, npc.getHeightSize()/1.8F, npc.getWidthSize()/0.6F);
    }
}
