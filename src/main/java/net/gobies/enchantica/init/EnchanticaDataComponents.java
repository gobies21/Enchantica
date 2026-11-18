package net.gobies.enchantica.init;

import com.mojang.serialization.Codec;
import net.gobies.enchantica.Enchantica;
import net.gobies.enchantica.config.*;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.TargetedConditionalEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class EnchanticaDataComponents {
    public static final DeferredRegister<DataComponentType<?>> ENCHANTMENT_EFFECTS;
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<TargetedConditionalEffect<EnchantmentEntityEffect>>>> PRE_ATTACK;
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<LevelBasedValue>> ARMOR_PIERCING;
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<LevelBasedValue>> HEALING_RECEIVED;
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<LifeStealConfig>> LIFESTEAL;
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CancelDamageConfig>> CANCEL_DAMAGE;
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RandomArrowSpreadConfig>> RANDOM_ARROW_SPREAD;
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<LevelBasedValue>> DRAW_SPEED;
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<LevelBasedValue>> FREEZE;

    public static void register(IEventBus eventBus) {
        ENCHANTMENT_EFFECTS.register(eventBus);
    }

    static {
        ENCHANTMENT_EFFECTS = DeferredRegister.create(Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, Enchantica.MODID);
        PRE_ATTACK = ENCHANTMENT_EFFECTS.register("pre_attack", () -> DataComponentType.<List<TargetedConditionalEffect<EnchantmentEntityEffect>>>builder().persistent(Codec.list(TargetedConditionalEffect.codec(EnchantmentEntityEffect.CODEC, LootContextParamSets.ENCHANTED_DAMAGE))).build());
        ARMOR_PIERCING = ENCHANTMENT_EFFECTS.register("armor_piercing", () -> DataComponentType.<LevelBasedValue>builder().persistent(LevelBasedValue.CODEC).build());
        HEALING_RECEIVED = ENCHANTMENT_EFFECTS.register("healing_received", () -> DataComponentType.<LevelBasedValue>builder().persistent(LevelBasedValue.CODEC).build());
        LIFESTEAL = ENCHANTMENT_EFFECTS.register("lifesteal", () -> DataComponentType.<LifeStealConfig>builder().persistent(LifeStealConfig.CODEC).build());
        CANCEL_DAMAGE = ENCHANTMENT_EFFECTS.register("cancel_damage", () -> DataComponentType.<CancelDamageConfig>builder().persistent(CancelDamageConfig.CODEC).build());
        RANDOM_ARROW_SPREAD = ENCHANTMENT_EFFECTS.register("random_arrow_spread", () -> DataComponentType.<RandomArrowSpreadConfig>builder().persistent(RandomArrowSpreadConfig.CODEC).build());
        DRAW_SPEED = ENCHANTMENT_EFFECTS.register("draw_speed", () -> DataComponentType.<LevelBasedValue>builder().persistent(LevelBasedValue.CODEC).build());
        FREEZE = ENCHANTMENT_EFFECTS.register("freeze", () -> DataComponentType.<LevelBasedValue>builder().persistent(LevelBasedValue.CODEC).build());
    }
}