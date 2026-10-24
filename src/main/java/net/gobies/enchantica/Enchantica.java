package net.gobies.enchantica;

import net.gobies.enchantica.event.EnchantmentEvents;
import net.gobies.enchantica.init.EnchanticaDataComponents;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(Enchantica.MODID)

public class Enchantica {
    public static final String MODID = "enchantica";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Enchantica(IEventBus modBus, ModContainer container) {

        EnchanticaDataComponents.register(modBus);

        EnchantmentEvents.register();
        //container.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

}