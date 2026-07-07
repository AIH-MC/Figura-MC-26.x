package org.figuramc.figura.mixin.render.feature;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.NameTagFeatureRenderer;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.ducks.GlyphRendererExtension;
import org.figuramc.figura.model.rendering.nameplate.NameTagOutlineQueue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Constructor;
import java.util.List;

@Mixin(NameTagFeatureRenderer.class)
public class NameTagFeatureRendererMixin {

    private static Constructor<?> figura$glyphRendererCtor;

    @Inject(method = "buildGroup", at = @At("HEAD"))
    private void figura$renderOutlines(FeatureFrameContext ctx, List<NameTagFeatureRenderer.Submit> submits, CallbackInfo ci) {
        if (NameTagOutlineQueue.ENTRIES.isEmpty())
            return;

        Object glyphRenderer;
        try {
            if (figura$glyphRendererCtor == null) {
                Class<?> glyphRendererClass = Class.forName("net.minecraft.client.renderer.feature.NameTagFeatureRenderer$GlyphRenderer");
                figura$glyphRendererCtor = glyphRendererClass.getDeclaredConstructor(NameTagFeatureRenderer.class);
                figura$glyphRendererCtor.setAccessible(true);
            }
            glyphRenderer = figura$glyphRendererCtor.newInstance((NameTagFeatureRenderer) (Object) this);
        } catch (ReflectiveOperationException e) {
            FiguraMod.LOGGER.error("Failed to construct NameTagFeatureRenderer.GlyphRenderer for nameplate outline text", e);
            return;
        }

        Font font = ctx.font();
        Font.GlyphVisitor glyphVisitor = (Font.GlyphVisitor) glyphRenderer;
        GlyphRendererExtension extension = (GlyphRendererExtension) glyphRenderer;

        for (NameTagOutlineQueue.Entry entry : NameTagOutlineQueue.ENTRIES) {
            Font.PreparedText preparedText = font.prepare8xTextOutline(entry.text().getVisualOrderText(), entry.x(), entry.y(), entry.outlineColor());
            extension.figura$prepare(new NameTagFeatureRenderer.Submit(entry.pose(), entry.x(), entry.y(), entry.text(), entry.lightCoords(), entry.outlineColor(), 0, Font.DisplayMode.NORMAL), Font.DisplayMode.NORMAL);
            preparedText.visit(glyphVisitor);
        }

        NameTagOutlineQueue.ENTRIES.clear();
    }
}
