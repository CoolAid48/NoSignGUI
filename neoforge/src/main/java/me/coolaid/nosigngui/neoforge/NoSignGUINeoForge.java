package me.coolaid.nosigngui.neoforge;

import com.mojang.blaze3d.platform.InputConstants;
import me.coolaid.nosigngui.NoSignGUI;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
//? if >=1.21.11 {
/*import net.minecraft.resources.Identifier;
*///?} else if >=1.21.9 {
/*import net.minecraft.resources.ResourceLocation;
*///?}
//? if >=1.21.6 {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
*///?} else if >=1.20.2 {
/*import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
//? if >=1.20.5 {
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
//?} else {
/^import net.neoforged.neoforge.event.TickEvent;
^///?}
*///?} else {
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
//?}
import org.lwjgl.glfw.GLFW;

@Mod(NoSignGUI.MOD_ID)
public final class NoSignGUINeoForge {
    //? if >=26.1 && <26.2 {
    /*public NoSignGUINeoForge(IEventBus modEventBus) {
        NoSignGUI.init();
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(ClientModEvents::onRegisterKeyMappings);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.addListener(ClientForgeEvents::onClientTick);
    }
    *///?} else if >=1.21.6 {
    /*public NoSignGUINeoForge(IEventBus modEventBus) {
        NoSignGUI.init();
        modEventBus.addListener(ClientModEvents::onRegisterKeyMappings);
        NeoForge.EVENT_BUS.addListener(ClientForgeEvents::onClientTick);
    }
    *///?} else {
    public NoSignGUINeoForge() {
        NoSignGUI.init();
    }
    //?}

    //? if >=1.20.5 && <1.21.6 {
    /*@EventBusSubscriber(modid = NoSignGUI.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    *///?}
    //? if <1.20.5 {
    @Mod.EventBusSubscriber(modid = NoSignGUI.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    //?}
    public static final class ClientModEvents {
        //? if >=26.1 && <26.2 {
        /*private static final KeyMapping.Category CATEGORY =
                KeyMapping.Category.register(Identifier.parse("nosigngui"));
        *///?} else if >=1.21.11 {
        /*private static final KeyMapping.Category CATEGORY =
                new KeyMapping.Category(Identifier.parse("nosigngui"));
        *///?} else if >=1.21.9 {
        /*private static final KeyMapping.Category CATEGORY =
                new KeyMapping.Category(ResourceLocation.parse("nosigngui"));
        *///?}

        //? if >=26.1 && <26.2 {
        /*private static final KeyMapping TOGGLE_GUI_KEY = new KeyMapping(
                "key.nosigngui.toggle",
                GLFW.GLFW_KEY_G,
                CATEGORY
        );
        *///?} else {
        private static final KeyMapping TOGGLE_GUI_KEY = new KeyMapping(
                "key.nosigngui.toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                //? if >=1.21.9 {
                /*CATEGORY
                *///?} else {
                "key.categories.nosigngui"
                //?}
        );
        //?}

        //? if <1.21.6 {
        @SubscribeEvent
        //?}
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            //? if >=1.21.9 && <26.1 {
            /*event.registerCategory(CATEGORY);
            *///?}
            //? if >=26.2 {
            /*event.registerCategory(CATEGORY);
            *///?}
            event.register(TOGGLE_GUI_KEY);
        }
    }

    //? if >=1.20.5 && <1.21.6 {
    /*@EventBusSubscriber(modid = NoSignGUI.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    *///?}
    //? if <1.20.5 {
    @Mod.EventBusSubscriber(modid = NoSignGUI.MOD_ID, value = Dist.CLIENT)
    //?}
    public static final class ClientForgeEvents {
        //? if <1.21.6 {
        @SubscribeEvent
        //?}
        //? if >=1.20.5 {
        /*public static void onClientTick(ClientTickEvent.Post event) {
        *///?} else {
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }
            //?}

            Minecraft minecraft = Minecraft.getInstance();
            while (ClientModEvents.TOGGLE_GUI_KEY.consumeClick()) {
                if (minecraft.player != null) {
                    //? if >=26.1 {
                    /*minecraft.player.sendOverlayMessage(NoSignGUI.toggleSignGuiMessage());
                    *///?} else {
                    minecraft.player.displayClientMessage(NoSignGUI.toggleSignGuiMessage(), true);
                    //?}
                }
            }
        }
    }
}
