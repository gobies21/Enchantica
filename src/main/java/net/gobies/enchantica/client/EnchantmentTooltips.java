package net.gobies.enchantica.client;

import net.gobies.enchantica.Enchantica;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.List;

@EventBusSubscriber(modid = Enchantica.MODID, value = Dist.CLIENT)
public class EnchantmentTooltips {

    private static final TagKey<Enchantment> RUNE_TAG = TagKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Enchantica.MODID, "rune_enchantments")
    );

    private static final TagKey<Enchantment> ANCIENT_TAG = TagKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Enchantica.MODID, "ancient_enchantments")
    );

    private static final TagKey<Enchantment> CELESTIAL_TAG = TagKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Enchantica.MODID, "celestial_enchantments")
    );

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        boolean isBook = stack.is(Items.ENCHANTED_BOOK);

        ItemEnchantments enchants = stack.get(isBook ? DataComponents.STORED_ENCHANTMENTS : DataComponents.ENCHANTMENTS);
        if (enchants == null || enchants.isEmpty()) return;

        List<Component> lines = event.getToolTip();

        for (var entry : enchants.entrySet()) {
            Holder<Enchantment> holder = entry.getKey();
            boolean isRune = holder.is(RUNE_TAG);
            boolean isAncient = holder.is(ANCIENT_TAG);
            boolean isCelestial = holder.is(CELESTIAL_TAG);

            if (isRune || isAncient || isCelestial) {
                String name = Component.translatable(holder.value().description().getString()).getString();
                lines.removeIf(line -> line.getString().contains(name));

                int level = entry.getIntValue();
                Component customLine;
                if (isRune) {
                    customLine = getRuneName(holder, level);
                } else if (isAncient) {
                    customLine = getAncientName(holder, level);
                } else {
                    customLine = getCelestialName(holder, level);
                }

                int index = 1;

                if (isBook) {
                    for (int i = 0; i < lines.size(); i++) {
                        if (lines.get(i).getString().toLowerCase().contains("ingredients")) {
                            index = i + 1;
                            break;
                        }
                    }
                }

                lines.add(index, customLine);
            }
        }
    }

    public static Component getRuneName(Holder<Enchantment> holder, int level) {
        Enchantment enchant = holder.value();
        MutableComponent line = Component.literal("Rune: ").append(Component.translatable(enchant.description().getString()));

        if (level != 1 || enchant.getMaxLevel() != 1) {
            line.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + level));
        }

        return line.withStyle(ChatFormatting.GREEN);
    }

    public static Component getAncientName(Holder<Enchantment> holder, int level) {
        Enchantment enchant = holder.value();
        MutableComponent line = Component.literal("Ancient ").append(Component.translatable(enchant.description().getString()));

        if (level != 1 || enchant.getMaxLevel() != 1) {
            line.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + level));
        }

        return line.withStyle(ChatFormatting.YELLOW);
    }

    public static Component getCelestialName(Holder<Enchantment> holder, int level) {
        Enchantment enchant = holder.value();
        MutableComponent line = Component.literal("Celestial: ").append(Component.translatable(enchant.description().getString()));

        if (level != 1 || enchant.getMaxLevel() != 1) {
            line.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + level));
        }

        return line.withStyle(ChatFormatting.LIGHT_PURPLE);
    }
}