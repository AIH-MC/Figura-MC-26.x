package org.figuramc.figura.mixin.render.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFeatureRenderer.class)
public class ItemFeatureRendererMixin {

    @Inject(method = "buildGroup", at = @At("HEAD"), cancellable = true)
    private void figura$preRender(net.minecraft.client.renderer.feature.FeatureFrameContext ctx, java.util.List<ItemFeatureRenderer.Submit> submits, CallbackInfo ci) {
        for (ItemFeatureRenderer.Submit submit : submits) {
            FiguraSubmitCallBackExtension callBackExtension = (FiguraSubmitCallBackExtension) (Object) submit;
            PoseStack poseStack = new PoseStack();
            poseStack.last().pose().set(submit.pose().pose());
            poseStack.last().normal().set(submit.pose().normal());

            for (var callback : callBackExtension.figura$getPreRenderingCallbacks()) {
                callback.apply(null, poseStack);
            }
            callBackExtension.figura$getPreRenderingCallbacks().clear();
        }
    }

    @Inject(method = "buildGroup", at = @At("RETURN"))
    private void figura$postRender(net.minecraft.client.renderer.feature.FeatureFrameContext ctx, java.util.List<ItemFeatureRenderer.Submit> submits, CallbackInfo ci) {
        for (ItemFeatureRenderer.Submit submit : submits) {
            FiguraSubmitCallBackExtension callBackExtension = (FiguraSubmitCallBackExtension) (Object) submit;
            for (var callback : callBackExtension.figura$getPostRenderingCallbacks())
                callback.run();
            callBackExtension.figura$getPostRenderingCallbacks().clear();
        }
    }
}
