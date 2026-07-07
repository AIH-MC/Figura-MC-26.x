package org.figuramc.figura.mixin.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;
import java.util.function.BiFunction;

@Mixin(Model.Simple.class)
public class ModelSimpleMixin implements FiguraSubmitCallBackExtension {

    @Override
    public void figura$addPreRenderingCallback(BiFunction<SubmitNodeCollector, PoseStack, Boolean> callback) {
        ((FiguraSubmitCallBackExtension) (Object) ((Model<?>) (Object) this).root()).figura$addPreRenderingCallback(callback);
    }

    @Override
    public void figura$addPostRenderingCallback(Runnable callback) {
        ((FiguraSubmitCallBackExtension) (Object) ((Model<?>) (Object) this).root()).figura$addPostRenderingCallback(callback);
    }

    @Override
    public List<BiFunction<SubmitNodeCollector, PoseStack, Boolean>> figura$getPreRenderingCallbacks() {
        return ((FiguraSubmitCallBackExtension) (Object) ((Model<?>) (Object) this).root()).figura$getPreRenderingCallbacks();
    }

    @Override
    public List<Runnable> figura$getPostRenderingCallbacks() {
        return ((FiguraSubmitCallBackExtension) (Object) ((Model<?>) (Object) this).root()).figura$getPostRenderingCallbacks();
    }
}
