package net.gobies.enchantica.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import java.util.Optional;

public record RandomArrowSpreadConfig(Optional<LevelBasedValue> arrowCount, Optional<LevelBasedValue> baseSpread) {
    public static final Codec<RandomArrowSpreadConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LevelBasedValue.CODEC.optionalFieldOf("arrow_count").forGetter(RandomArrowSpreadConfig::arrowCount),
            LevelBasedValue.CODEC.optionalFieldOf("base_spread").forGetter(RandomArrowSpreadConfig::baseSpread)
    ).apply(instance, RandomArrowSpreadConfig::new));
}