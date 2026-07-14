package me.coolaid.nosigngui.forge;

import com.mojang.blaze3d.platform.InputConstants;
import me.coolaid.nosigngui.NoSignGUI;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
//? if >=1.21.11 {
/*import net.minecraft.resources.Identifier;
*///?} else if >=1.21.9 {
/*import net.minecraft.resources.ResourceLocation;
*///?}
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
//? if >=1.21.6 {
/*import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
*///?} else {
import net.minecraftforge.eventbus.api.SubscribeEvent;
//?}
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod(NoSignGUI.MOD_ID)
public final class NoSignGUIForge {
    public NoSignGUIForge() {
        NoSignGUI.init();
    }

    @Mod.EventBusSubscriber(modid = NoSignGUI.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ClientModEvents {
        //? if >=1.21.11 {
        /*private static final KeyMapping.Category CATEGORY =
                KeyMapping.Category.register(Identifier.parse("nosigngui"));
        *///?} else if >=1.21.9 {
        /*private static final KeyMapping.Category CATEGORY =
                KeyMapping.Category.register(ResourceLocation.parse("nosigngui"));
        *///?}

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

        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(TOGGLE_GUI_KEY);
        }
    }

    @Mod.EventBusSubscriber(modid = NoSignGUI.MOD_ID, value = Dist.CLIENT)
    public static final class ClientForgeEvents {
        @SubscribeEvent
        //? if >=1.20.3 {
        /*public static void onClientTick(TickEvent.ClientTickEvent.Post event) {
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
