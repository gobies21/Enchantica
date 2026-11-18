package net.gobies.enchantica.event;

import net.gobies.enchantica.Enchantica;
import net.gobies.enchantica.config.CancelDamageConfig;
import net.gobies.enchantica.init.EnchanticaDataComponents;
import net.gobies.enchantica.config.LifeStealConfig;
import net.gobies.enchantica.util.EUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

import java.util.List;

public class EnchantmentEvents {

    public static void register() {
        NeoForge.EVENT_BUS.register(new EnchantmentEvents());
    }

    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent.Pre event) {
        LivingEntity target = event.getEntity();

        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            ItemStack weapon = attacker.getMainHandItem();
            if (weapon.isEmpty() || !weapon.isEnchanted()) return;

            float totalArmor = target.getArmorValue();
            float toughness = (float) target.getAttributeValue(Attributes.ARMOR_TOUGHNESS);

            if (totalArmor <= 0.0F && toughness <= 0.0F) return;
            float initialDamage = event.getOriginalDamage();

            EnchantmentHelper.runIterationOnItem(weapon, (enchantment, level) -> {
                LevelBasedValue piercingValue = enchantment.value().effects().get(EnchanticaDataComponents.ARMOR_PIERCING.get());

                if (piercingValue != null) {
                    float ignorePercentage = piercingValue.calculate(level);
                    if (ignorePercentage > 0.0F) {
                        float standardArmorDamage = CombatRules.getDamageAfterAbsorb(target, initialDamage, event.getSource(), totalArmor, toughness);
                        float piercedArmorDamage = CombatRules.getDamageAfterAbsorb(target, initialDamage, event.getSource(), totalArmor * (1.0F - Math.min(ignorePercentage, 1.0F)), toughness);

                        event.setNewDamage(event.getNewDamage() + (piercedArmorDamage - standardArmorDamage));
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public void onLivingDamagePost(LivingDamageEvent.Post event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            ItemStack weapon = attacker.getMainHandItem();
            if (weapon.isEmpty() || !weapon.isEnchanted()) return;

            float damageDealt = event.getNewDamage();
            if (damageDealt <= 0.0F) return;

            EnchantmentHelper.runIterationOnItem(weapon, (enchantment, level) -> {
                LifeStealConfig config = enchantment.value().effects().get(EnchanticaDataComponents.LIFESTEAL.get());

                if (config != null) {
                    float healAmount = 0.0F;

                    if (config.percentage().isPresent()) {
                        healAmount += damageDealt * config.percentage().get().calculate(level);
                    }

                    if (config.flat().isPresent() && attacker.getRandom().nextFloat() < 0.4F) {
                        healAmount += config.flat().get().calculate(level);
                    }

                    if (healAmount > 0.0F) {
                        attacker.heal(healAmount);
                    }
                }
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack equipmentItem = target.getItemBySlot(slot);
            if (!equipmentItem.isEmpty() && equipmentItem.isEnchanted()) {

                EnchantmentHelper.runIterationOnItem(equipmentItem, (enchantment, level) -> {
                    CancelDamageConfig cancelConfig = enchantment.value().effects().get(EnchanticaDataComponents.CANCEL_DAMAGE.get());

                    if (cancelConfig != null && cancelConfig.chance().isPresent()) {
                        float chance = cancelConfig.chance().get().calculate(level);
                        if (target.getRandom().nextFloat() < chance) {
                            event.setCanceled(true);

                            float volume = cancelConfig.volume().orElse(0.5F);
                            float pitch = cancelConfig.pitch().orElse(1.0F);
                            target.level().playSound(null, target.getX(), target.getY(), target.getZ(), cancelConfig.sound().value(), SoundSource.PLAYERS, volume, pitch);
                            if (cancelConfig.damageItem()) {
                                float damageDealt = event.getOriginalAmount();
                                int durabilityLoss = Math.max(1, Math.round(damageDealt));
                                if (target instanceof ServerPlayer serverPlayer) {
                                    equipmentItem.hurtAndBreak(durabilityLoss, serverPlayer, slot);
                                }
                            }
                            if (cancelConfig.dodge()) {
                                float side = target.getRandom().nextBoolean() ? 1.0F : -1.0F;

                                Vec3 sideVector = Vec3.directionFromRotation(0, target.getViewYRot(1.0F) + 90.0F);

                                double dashX = sideVector.x * side * 0.8D;
                                double dashY = 0.35D;
                                double dashZ = sideVector.z * side * 0.8D;

                                target.setDeltaMovement(target.getDeltaMovement().add(dashX, dashY, dashZ));
                                target.hurtMarked = true;
                            }
                        }
                    }
                });
                if (event.isCanceled()) return;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerAttackMelee(AttackEntityEvent event) {
        LivingEntity attacker = event.getEntity();
        Entity target = event.getTarget();

        if (target instanceof LivingEntity victim && victim.level() instanceof ServerLevel serverLevel) {
            ItemStack weapon = attacker.getMainHandItem();
            if (weapon.isEmpty() || weapon.getItem() instanceof ProjectileWeaponItem) return;

            EUtils.applyPreAttackEffects(weapon, attacker, victim, serverLevel);
            EUtils.applyFreezeTicks(weapon, victim);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onProjectileImpactRanged(ProjectileImpactEvent event) {
        if (event.getProjectile() instanceof Projectile projectile && event.getRayTraceResult() instanceof EntityHitResult hitResult && hitResult.getEntity() instanceof LivingEntity victim) {
            if (projectile.getOwner() instanceof LivingEntity shooter && victim.level() instanceof ServerLevel serverLevel) {
                ItemStack weapon = projectile.getWeaponItem();
                if (weapon == null || weapon.isEmpty()) return;

                EUtils.applyPreAttackEffects(weapon, shooter, victim, serverLevel);
                EUtils.applyFreezeTicks(weapon, victim);
            }
        }
    }

    private static final ThreadLocal<Boolean> IS_SPAWNING = ThreadLocal.withInitial(() -> false);

    @SubscribeEvent
    public void onArrowSpawn(EntityJoinLevelEvent event) {
        if (event.loadedFromDisk() || event.getLevel().isClientSide() || IS_SPAWNING.get()) return;
        if (!(event.getEntity() instanceof AbstractArrow originalArrow) || !(originalArrow.getOwner() instanceof LivingEntity shooter))
            return;
        if (originalArrow.getPersistentData().contains("SplitshotArrow")) return;

        ItemStack weapon = shooter.getMainHandItem();
        EquipmentSlot slot = EquipmentSlot.MAINHAND;
        if (weapon.isEmpty() || !weapon.isEnchanted()) {
            weapon = shooter.getOffhandItem();
            slot = EquipmentSlot.OFFHAND;
        }
        if (weapon.isEmpty()) return;

        final ItemStack finalWeapon = weapon;
        final EquipmentSlot finalSlot = slot;

        EnchantmentHelper.runIterationOnItem(finalWeapon, (enchantment, level) -> {
            var spreadConfig = enchantment.value().effects().get(EnchanticaDataComponents.RANDOM_ARROW_SPREAD.get());
            if (spreadConfig != null && spreadConfig.baseSpread().isPresent() && spreadConfig.arrowCount().isPresent()) {
                try {
                    IS_SPAWNING.set(true);
                    float spreadIntensity = spreadConfig.baseSpread().get().calculate(level);
                    int totalExtraArrows = Math.max(0, Math.round(spreadConfig.arrowCount().get().calculate(level)));

                    Vec3 velocity = originalArrow.getDeltaMovement();
                    float speed = (float) velocity.length();

                    for (int i = 0; i < totalExtraArrows; i++) {
                        if (originalArrow.getType().create(event.getLevel()) instanceof AbstractArrow extraArrow) {
                            extraArrow.setOwner(shooter);
                            extraArrow.setPos(originalArrow.getX(), originalArrow.getY(), originalArrow.getZ());
                            extraArrow.setBaseDamage(originalArrow.getBaseDamage());
                            extraArrow.setCritArrow(originalArrow.isCritArrow());
                            extraArrow.pickup = originalArrow.pickup;

                            if (originalArrow.isOnFire()) {
                                extraArrow.setRemainingFireTicks(originalArrow.getRemainingFireTicks());
                            }

                            try {
                                List<SynchedEntityData.DataValue<?>> syncedValues = originalArrow.getEntityData().getNonDefaultValues();
                                if (syncedValues != null) {
                                    extraArrow.getEntityData().assignValues(syncedValues);
                                }
                            } catch (Exception ignored) {
                            }

                            extraArrow.getPersistentData().putBoolean("SplitshotArrow", true);

                            float yawOffset = (shooter.getRandom().nextFloat() - 0.5F) * spreadIntensity;

                            float pitchOffset = (shooter.getRandom().nextFloat() - 0.5F) * (spreadIntensity * 0.7F);

                            float adjustedYaw = shooter.getYRot() + yawOffset;
                            float adjustedPitch = shooter.getXRot() + pitchOffset;

                            float inaccuracy = spreadIntensity * 0.1F;

                            extraArrow.shootFromRotation(shooter, adjustedPitch, adjustedYaw, 0.0F, speed, inaccuracy);

                            event.getLevel().addFreshEntity(extraArrow);
                        }
                    }

                    if (totalExtraArrows > 0 && shooter instanceof ServerPlayer serverPlayer) {
                        finalWeapon.hurtAndBreak(totalExtraArrows, serverPlayer, finalSlot);
                    }
                } finally {
                    IS_SPAWNING.set(false);
                }
            }
        });
    }

    @SubscribeEvent
    public void onLivingItemUseTick(LivingEntityUseItemEvent.Tick event) {
        LivingEntity livingEntity = event.getEntity();
        ItemStack stack = event.getItem();

        if (stack.isEmpty() || !stack.isEnchanted() || !(stack.getItem() instanceof ProjectileWeaponItem)) return;

        EnchantmentHelper.runIterationOnItem(stack, (enchantment, level) -> {
            var drawSpeedValue = enchantment.value().effects().get(EnchanticaDataComponents.DRAW_SPEED.get());

            if (drawSpeedValue != null) {
                double speedModifier = drawSpeedValue.calculate(level);

                if (speedModifier > 0.0) {
                    int currentTick = (int) Math.floor(livingEntity.tickCount * speedModifier);
                    int previousTick = (int) Math.floor((livingEntity.tickCount - 1) * speedModifier);
                    int skipTicks = currentTick - previousTick;

                    if (skipTicks != 0) {
                        event.setDuration(event.getDuration() - skipTicks);
                    }
                }
            }
        });
    }

    private static final ResourceKey<Enchantment> UPGRADE_POTENTIALS = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(Enchantica.MODID, "upgrade_potentials"));

    @SubscribeEvent
    public void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();

        if (left.isEmpty() || right.isEmpty() || !right.is(Items.ENCHANTED_BOOK)) {
            return;
        }

        var lookup = event.getPlayer().level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var enchantHolderOptional = lookup.get(UPGRADE_POTENTIALS);
        if (enchantHolderOptional.isEmpty()) {
            return;
        }
        var enchantHolder = enchantHolderOptional.get();

        if (EnchantmentHelper.getEnchantmentsForCrafting(right).getLevel(enchantHolder) <= 0) {
            return;
        }

        if (EnchantmentHelper.getEnchantmentsForCrafting(left).getLevel(enchantHolder) > 0) {
            return;
        }

        ItemStack output = left.copy();
        EnchantmentHelper.updateEnchantments(output, mutableContents -> mutableContents.set(enchantHolder, 1));
        output.set(DataComponents.REPAIR_COST, 0);

        event.setOutput(output);
        event.setCost(10);
        event.setMaterialCost(1);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLivingHeal(LivingHealEvent event) {
        EnchantmentHelper.runIterationOnEquipment(event.getEntity(), (enchantment, level, slot) -> {
            LevelBasedValue healingValue = enchantment.value().effects().get(EnchanticaDataComponents.HEALING_RECEIVED.get());
            float amount = event.getAmount();

            if (healingValue != null) {
                float multiplier = healingValue.calculate(level);
                if (multiplier > 0.0F) {
                    event.setAmount(amount * (1.0F + multiplier));
                }
            }
        });
    }
}