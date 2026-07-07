package org.figuramc.figura.mixin.render.feature;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.figuramc.figura.model.rendering.nodeRenderer.FiguraFeatureRenderer;
import org.figuramc.figura.utils.PlatformUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FeatureRenderDispatcher.class)
public class FeatureRendererDispatcherMixin {
    private static final boolean HAS_IRIS = PlatformUtils.isModLoaded("iris") || PlatformUtils.isModLoaded("oculus");

    final FiguraFeatureRenderer figuraFeatureRenderer = new FiguraFeatureRenderer();

    @Inject(method = "renderAllFeatures",
            at = @At("HEAD"))
    private void figura$renderFiguraFeatures(SubmitNodeStorage submitNodeStorage, CallbackInfo ci) {
        if (HAS_IRIS && net.irisshaders.iris.shadows.ShadowRenderer.ACTIVE)
            return;

        SubmitNodeCollection collection = submitNodeStorage.order(0);
        figuraFeatureRenderer.render(collection, submitNodeStorage);
    }
}
