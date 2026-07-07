package org.figuramc.figura.mixin.render.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.feature.NameTagFeatureRenderer;
import net.minecraft.client.renderer.feature.phase.SimpleFeatureRenderPhase;
import net.minecraft.client.renderer.feature.phase.TranslucentFeatureRenderPhase;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.ducks.CameraRenderStateExtension;
import org.figuramc.figura.lua.api.nameplate.EntityNameplateCustomization;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.model.rendering.nameplate.NameTagOutlineQueue;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.TextUtils;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SubmitNodeCollection.class)
public class SubmitNodeCollectionNameTagMixin {

    @Shadow @Final public SimpleFeatureRenderPhase nameTags;
    @Shadow @Final public TranslucentFeatureRenderPhase seeThroughNameTags;

    @Inject(method = "submitNameTag", at = @At("HEAD"), cancellable = true)
    private void figura$submitCustomNameTag(PoseStack poseStack, Vec3 vec3, int lightCoords, Component component, boolean isSeeThroughCapable, int rawLight, CameraRenderState cameraRenderState, CallbackInfo ci) {
        if (vec3 == null)
            return;

        CameraRenderStateExtension stateExtension = (CameraRenderStateExtension) cameraRenderState;
        Avatar avatar = stateExtension.figura$getAvatar();
        boolean isRenderingName = stateExtension.figura$isRenderingNameTag();
        stateExtension.figura$setAvatar(null);
        stateExtension.figura$setRenderingNameTag(false);

        if (avatar == null)
            return;

        EntityNameplateCustomization custom = avatar.luaRuntime == null ? null : avatar.luaRuntime.nameplate.ENTITY;
        boolean hasCustom = custom != null && avatar.permissions.get(Permissions.NAMEPLATE_EDIT) == 1;
        boolean enabled = hasCustom && Configs.ENTITY_NAMEPLATE.value > 0 && !AvatarManager.panic;
        if (!enabled)
            return;

        ci.cancel();

        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        poseStack.pushPose();

        FiguraVec3 pivot = custom.getPivot() != null ? custom.getPivot() : FiguraVec3.of(vec3.x, vec3.y + 0.5, vec3.z);
        poseStack.translate(pivot.x, pivot.y, pivot.z);
        poseStack.mulPose(cameraRenderState.orientation);

        if (custom.getPos() != null) {
            FiguraVec3 pos = custom.getPos();
            poseStack.translate(pos.x, pos.y, pos.z);
        }

        FiguraVec3 scale = FiguraVec3.of(0.025, -0.025, 0.025);
        if (custom.getScale() != null)
            scale.multiply(custom.getScale());
        poseStack.scale((float) scale.x, (float) scale.y, (float) scale.z);

        Matrix4f pose = new Matrix4f(poseStack.last().pose());

        int backgroundColorDefault = ARGB.color(mc.gameRenderer.gameRenderState().optionsRenderState.getBackgroundOpacity(0.25f), -16777216);
        int backgroundColor = custom.background != null ? custom.background : backgroundColorDefault;
        int light = custom.light != null ? custom.light : rawLight;
        int outlineColor = (custom.outlineColor != null ? custom.outlineColor : 0x202020) | 0xFF000000;

        boolean deadmau = component.getString().equals("deadmau5");
        List<Component> lines = isRenderingName ? TextUtils.splitText(component, "\n") : List.of(component);

        for (int i = 0; i < lines.size(); i++) {
            Component line = lines.get(i);
            if (line.getString().isEmpty())
                continue;

            int lineFromBottom = i - lines.size() + 1;
            float x = -font.width(line) / 2f;
            float y = (deadmau ? -10f : 0f) + (font.lineHeight + 1) * lineFromBottom;

            if (custom.outline)
                NameTagOutlineQueue.ENTRIES.add(new NameTagOutlineQueue.Entry(pose, x, y, line, light, outlineColor));

            Font.DisplayMode mainDisplayMode = custom.outline ? Font.DisplayMode.POLYGON_OFFSET : Font.DisplayMode.NORMAL;

            if (isSeeThroughCapable) {
                nameTags.submit(new NameTagFeatureRenderer.Submit(pose, x, y, line, LightCoordsUtil.lightCoordsWithEmission(light, 2), -1, 0, mainDisplayMode));
                seeThroughNameTags.submit(new NameTagFeatureRenderer.Submit(pose, x, y, line, light, -2130706433, backgroundColor, Font.DisplayMode.SEE_THROUGH));
            } else {
                nameTags.submit(new NameTagFeatureRenderer.Submit(pose, x, y, line, light, -2130706433, backgroundColor, mainDisplayMode));
            }
        }

        poseStack.popPose();
    }
}
