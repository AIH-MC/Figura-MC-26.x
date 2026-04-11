package org.figuramc.figura.mixin.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.blockentity.state.SkullBlockRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.figuramc.figura.ducks.NodeCollectorExtension;
import org.figuramc.figura.ducks.PlayerHeadRenderInfoExtension;
import org.figuramc.figura.ducks.SkullBlockRendererAccessor;
import org.figuramc.figura.ducks.SkullBlockRendererHelper;
import org.figuramc.figura.lua.api.entity.EntityAPI;
import org.figuramc.figura.lua.api.world.BlockStateAPI;
import org.figuramc.figura.lua.api.world.ItemStackAPI;
import org.figuramc.figura.permissions.Permissions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SkullBlockRenderer.class)
public abstract class SkullBlockRendererMixin implements BlockEntityRenderer<SkullBlockEntity, SkullBlockRenderState>, PlayerHeadRenderInfoExtension {

    @Unique
    private static Avatar avatar;
    @Unique
    private static SkullBlockRenderState block;

    @Inject(at = @At("HEAD"), method = "submitSkull", cancellable = true)
    private static void renderSkull(Direction direction, float yaw, float animationProgress, PoseStack stack, SubmitNodeCollector submitNodeCollector, int light, SkullModelBase model, RenderType renderLayer, int outlineColor, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, CallbackInfo ci) {
        // parse block and items first, so we can yeet them in case of a missed event
        if (avatar == null) {
            avatar = SkullBlockRendererHelper.getAvatar();
            SkullBlockRendererHelper.setAvatar(null);
        }

        SkullBlockRenderState localBlock = block;
        block = null;

        ItemStack localItem = SkullBlockRendererAccessor.getItem();
        SkullBlockRendererAccessor.setItem(null);

        Entity localEntity = SkullBlockRendererAccessor.getEntity();
        SkullBlockRendererAccessor.setEntity(null);

        SkullBlockRendererAccessor.SkullRenderMode localMode = SkullBlockRendererAccessor.getRenderMode();
        SkullBlockRendererAccessor.setRenderMode(SkullBlockRendererAccessor.SkullRenderMode.OTHER);

        // avatar pointer incase avatar variable is set during render. (unlikely)
        Avatar localAvatar = avatar;
        avatar = null;

        if (localAvatar == null || localAvatar.permissions.get(Permissions.CUSTOM_SKULL) == 0)
            return;

        float tickDelta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);

        // For non-block-entity skulls (hand items, hotbar, GUI), render immediately
        // instead of deferring through the callback pipeline. The deferred pipeline
        // processes callbacks after the current rendering context returns, when
        // RenderSystem's modelViewStack no longer has the correct projection setup,
        // causing Figura's vertices to be drawn with incorrect GL state (e.g. skulls
        // appearing in the sky at screen-coordinate positions for GUI items, or
        // mispositioned for hand-held items).
        if (localMode != SkullBlockRendererAccessor.SkullRenderMode.BLOCK) {
            MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

            // Apply the same transforms that submitSkull would apply
            stack.pushPose();
            if (direction == null) {
                stack.translate(0.5f, 0f, 0.5f);
            } else {
                float f = 0.25f;
                stack.translate(0.5f - direction.getStepX() * f, f, 0.5f - direction.getStepZ() * f);
            }
            stack.scale(-1f, -1f, 1f);

            FiguraMod.pushProfiler(FiguraMod.MOD_ID);
            FiguraMod.pushProfiler(localAvatar);
            FiguraMod.pushProfiler("skullRender");

            BlockStateAPI b = localBlock == null ? null : new BlockStateAPI(localBlock.blockState, localBlock.blockPos);
            ItemStackAPI i = localItem != null ? ItemStackAPI.verify(localItem) : null;
            EntityAPI<?> e = localEntity != null ? EntityAPI.wrap(localEntity) : null;
            String m = localMode.name();

            FiguraMod.pushProfiler(localItem != null ? String.valueOf(localItem) : "hand");
            FiguraMod.pushProfiler("event");
            boolean eventCancelled = localAvatar.skullRenderEvent(tickDelta, b, i, e, m);

            FiguraMod.popPushProfiler("render");
            if (eventCancelled || localAvatar.skullRender(stack, bufferSource, light, direction, yaw, tickDelta)) {
                FiguraMod.popProfiler(5);
                stack.popPose();
                ci.cancel();
                return;
            }

            FiguraMod.popProfiler(5);
            stack.popPose();
            // Don't cancel — let vanilla submitSkull proceed for non-Figura rendering
            return;
        }

        // For block entity skulls, use the deferred callback pipeline
        FiguraSubmitCallBackExtension modelExtension = (FiguraSubmitCallBackExtension) model;
        modelExtension.figura$addPreRenderingCallback((bufferSource, poseStack) -> {

            FiguraMod.pushProfiler(FiguraMod.MOD_ID);
            FiguraMod.pushProfiler(localAvatar);
            FiguraMod.pushProfiler("skullRender");

            // event
            BlockStateAPI b = localBlock == null ? null : new BlockStateAPI(localBlock.blockState, localBlock.blockPos);
            ItemStackAPI i = localItem != null ? ItemStackAPI.verify(localItem) : null;
            EntityAPI<?> e = localEntity != null ? EntityAPI.wrap(localEntity) : null;
            String m = localMode.name();

            FiguraMod.pushProfiler(localBlock != null ? localBlock.blockPos.toString() : String.valueOf(i));

            FiguraMod.pushProfiler("event");
            boolean bool = localAvatar.skullRenderEvent(tickDelta, b, i, e, m);

            // render skull :3
            FiguraMod.popPushProfiler("render");
            if (bool || localAvatar.skullRender(poseStack, bufferSource, light, direction, yaw, tickDelta))
                return false;

            FiguraMod.popProfiler(5);
            return true;
        });
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/SkullBlockRenderer;submitSkull(Lnet/minecraft/core/Direction;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/model/object/skull/SkullModelBase;Lnet/minecraft/client/renderer/rendertype/RenderType;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"), method = "submit(Lnet/minecraft/client/renderer/blockentity/state/SkullBlockRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V")
    public void render(SkullBlockRenderState skullBlockRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
        block = skullBlockRenderState;
        // Read avatar from the render state (stored during extractRenderState)
        // instead of the static field which was set during extraction phase
        avatar = ((PlayerHeadRenderInfoExtension)(Object)skullBlockRenderState).figura$getAvatar();
        SkullBlockRendererAccessor.setRenderMode(SkullBlockRendererAccessor.SkullRenderMode.BLOCK);
    }

    @Override
    public boolean shouldRenderOffScreen() {
        // Always use default behavior for skull blocks.
        // The OFFSCREEN_RENDERING permission is for entity rendering only.
        // Using it here caused Figura skulls to render at unlimited distance
        // and appear giant/always on screen.
        return BlockEntityRenderer.super.shouldRenderOffScreen();
    }

    @Inject(at = @At("HEAD"), method = "resolveSkullRenderType")
    private static void getRenderType(SkullBlock.Type type, SkullBlockEntity skullBlockEntity, CallbackInfoReturnable<RenderType> cir) {
        // No longer set the static avatar here — extractRenderState is called
        // during a separate phase from submit, so the static field would be
        // overwritten by the last extracted skull before any submit runs.
        // Avatar is now stored on the SkullBlockRenderState via extractRenderState.
    }

    @Inject(at = @At("TAIL"), method = "extractRenderState(Lnet/minecraft/world/level/block/entity/SkullBlockEntity;Lnet/minecraft/client/renderer/blockentity/state/SkullBlockRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V")
    public void figura$extractRenderState(SkullBlockEntity skullBlockEntity, SkullBlockRenderState skullBlockRenderState, float tickDelta, Vec3 vec3, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, CallbackInfo ci) {
        Avatar extractedAvatar = null;
        if (skullBlockRenderState.skullType == SkullBlock.Types.PLAYER) {
            ResolvableProfile profile = skullBlockEntity.getOwnerProfile();
            if (profile != null) {
                extractedAvatar = AvatarManager.getAvatarForPlayer(profile.partialProfile().id());
            }
        }
        ((PlayerHeadRenderInfoExtension)(Object)skullBlockRenderState).figura$setAvatar(extractedAvatar);
    }
}
