package org.figuramc.figura.mixin.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Special Biome accessor for 25w14craftmine that doesn't try to access the climateSettings field
 * which does not exist in this version.
 */
@Mixin(Biome.class)
public interface CraftmineBiomeAccessor {
    @Intrinsic
    @Invoker("getTemperature")
    float getTheTemperature(BlockPos blockPos, int i);
}
