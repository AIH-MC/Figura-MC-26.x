package org.figuramc.figura.mixin.render.feature;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.feature.NameTagFeatureRenderer;
import org.figuramc.figura.ducks.GlyphRendererExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.client.renderer.feature.NameTagFeatureRenderer$GlyphRenderer")
public class GlyphRendererMixin implements GlyphRendererExtension {

    @Shadow
    public void prepare(NameTagFeatureRenderer.Submit submit, Font.DisplayMode displayMode) {
        throw new AssertionError();
    }

    @Override
    public void figura$prepare(NameTagFeatureRenderer.Submit submit, Font.DisplayMode displayMode) {
        this.prepare(submit, displayMode);
    }
}
