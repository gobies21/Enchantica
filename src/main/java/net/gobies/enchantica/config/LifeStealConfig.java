package net.gobies.enchantica.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.Optional;

public record LifeStealConfig(Optional<LevelBasedValue> percentage, Optional<LevelBasedValue> flat) {
    public static final Codec<LifeStealConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LevelBasedValue.CODEC.optionalFieldOf("percentage").forGetter(LifeStealConfig::percentage),
            LevelBasedValue.CODEC.optionalFieldOf("flat").forGetter(LifeStealConfig::flat)
    ).apply(instance, LifeStealConfig::new));
}