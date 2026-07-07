package org.figuramc.figura.mixin.render.feature;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.feature.FlameFeatureRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.ducks.FlameSubmitExtension;
import org.figuramc.figura.utils.RenderUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(FlameFeatureRenderer.class)
public class FlameFeatureRendererMixin {

    @ModifyVariable(method = "prepare", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private TextureAtlasSprite firstFireTexture(TextureAtlasSprite sprite, @Local(argsOnly = true) FlameFeatureRenderer.Submit submit) {
        Avatar avatar = ((FlameSubmitExtension) (Object) submit).figura$getAvatar();
        TextureAtlasSprite s = RenderUtils.firstFireLayer(avatar);
        return s != null ? s : sprite;
    }

    @ModifyVariable(method = "prepare", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private TextureAtlasSprite secondFireTexture(TextureAtlasSprite sprite, @Local(argsOnly = true) FlameFeatureRenderer.Submit submit) {
        Avatar avatar = ((FlameSubmitExtension) (Object) submit).figura$getAvatar();
        TextureAtlasSprite s = RenderUtils.secondFireLayer(avatar);
        return s != null ? s : sprite;
    }
}
