package org.figuramc.figura.ducks;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.feature.NameTagFeatureRenderer;

public interface GlyphRendererExtension {
    void figura$prepare(NameTagFeatureRenderer.Submit submit, Font.DisplayMode displayMode);
}
