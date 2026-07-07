package org.figuramc.figura.model.rendering.nameplate;

import net.minecraft.network.chat.Component;
import org.joml.Matrix4fc;

import java.util.ArrayList;
import java.util.List;

public final class NameTagOutlineQueue {
    private NameTagOutlineQueue() {}

    public record Entry(Matrix4fc pose, float x, float y, Component text, int lightCoords, int outlineColor) {}

    public static final List<Entry> ENTRIES = new ArrayList<>();
}
