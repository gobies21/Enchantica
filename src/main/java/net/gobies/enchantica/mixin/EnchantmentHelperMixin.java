package net.gobies.enchantica.mixin;

import net.gobies.enchantica.Enchantica;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    @Unique
    private static final ResourceKey<Enchantment> ADVANCED_LOOTING_KEY = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(Enchantica.MODID, "advanced_looting"));

    @Inject(
            method = "getEnchantmentLevel(Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/LivingEntity;)I",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void setAdvancedLootingLevel(Holder<Enchantment> enchantment, LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        if (entity == null) return;

        var enchantmentRegistry = entity.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        var lootingHolder = enchantmentRegistry.getHolderOrThrow(Enchantments.LOOTING);

        if (enchantment.equals(lootingHolder)) {
            var advancedLootingHolder = enchantmentRegistry.getHolderOrThrow(ADVANCED_LOOTING_KEY);

            int advancedLootingLevel = EnchantmentHelper.getEnchantmentLevel(advancedLootingHolder, entity);

            if (advancedLootingLevel > 0) {
                cir.setReturnValue(advancedLootingLevel * 2);
            }
        }
    }
}