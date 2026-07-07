package org.figuramc.figura.model.rendering.nodeRenderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.CustomFeatureRenderer;
import org.figuramc.figura.ducks.NodeCollectorExtension;
import org.figuramc.figura.utils.ui.UIHelper;

import java.util.List;
import java.util.Map;

public class FiguraFeatureRenderer {
    public void render(SubmitNodeCollection submitNodeCollection, SubmitNodeStorage submitNodeStorage) {
        List<FiguraSubmission> storedSubmissions = ((NodeCollectorExtension) submitNodeCollection).getFiguraSubmissions();
        List<FiguraSubmission> figuraSubmissions = new java.util.ArrayList<>(storedSubmissions);
        storedSubmissions.clear();

        for (FiguraSubmission figuraSubmission : figuraSubmissions) {
            if (figuraSubmission.avatar() == null)
                continue;

            figuraSubmission.renderer().apply(
                    figuraSubmission.avatar(),
                    figuraSubmission.renderState(),
                    submitNodeStorage
            );
        }
    }
}
