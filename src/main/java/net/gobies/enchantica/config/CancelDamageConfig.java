package net.gobies.enchantica.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.Optional;

public record CancelDamageConfig(Optional<LevelBasedValue> chance, boolean damageItem, boolean dodge, Holder<SoundEvent> sound, Optional<Float> volume, Optional<Float> pitch) {
    public static final Codec<CancelDamageConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LevelBasedValue.CODEC.optionalFieldOf("chance").forGetter(CancelDamageConfig::chance),
            Codec.BOOL.optionalFieldOf("damage_item", false).forGetter(CancelDamageConfig::damageItem),
            Codec.BOOL.optionalFieldOf("dodge", false).forGetter(CancelDamageConfig::dodge),
            SoundEvent.CODEC.fieldOf("sound").forGetter(CancelDamageConfig::sound),
            Codec.FLOAT.optionalFieldOf("volume").forGetter(CancelDamageConfig::volume),
            Codec.FLOAT.optionalFieldOf("pitch").forGetter(CancelDamageConfig::pitch)
    ).apply(instance, CancelDamageConfig::new));
}