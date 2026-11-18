package net.gobies.enchantica.util;

import net.gobies.enchantica.init.EnchanticaDataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentTarget;

public class EUtils {

    public static void applyPreAttackEffects(ItemStack weapon, LivingEntity attacker, LivingEntity victim, ServerLevel level) {
        EnchantedItemInUse itemInUse = new EnchantedItemInUse(weapon, attacker.getEquipmentSlotForItem(weapon), attacker);

        EnchantmentHelper.runIterationOnItem(weapon, (enchantment, enchantmentLevel) -> {
            var preAttackEffects = enchantment.value().effects().get(EnchanticaDataComponents.PRE_ATTACK.get());
            if (preAttackEffects == null) return;

            for (var conditionalEffect : preAttackEffects) {
                Entity target = (conditionalEffect.affected() == EnchantmentTarget.VICTIM) ? victim : attacker;
                conditionalEffect.effect().apply(level, enchantmentLevel, itemInUse, target, target.position());
            }
        });
    }

    public static void applyFreezeTicks(ItemStack weapon, LivingEntity target) {
        EnchantmentHelper.runIterationOnItem(weapon, (enchantment, level) -> {
            var freezeDuration = enchantment.value().effects().get(EnchanticaDataComponents.FREEZE.get());
            if (freezeDuration != null) {
                float seconds = freezeDuration.calculate(level);
                int freezeTicks = Math.round(seconds * 20.0f);
                target.setTicksFrozen(Math.max(target.getTicksFrozen(), freezeTicks));
            }
        });
    }
}
