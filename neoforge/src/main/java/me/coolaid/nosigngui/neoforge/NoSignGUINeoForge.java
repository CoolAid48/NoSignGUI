package me.coolaid.nosigngui.neoforge;

import com.mojang.blaze3d.platform.InputConstants;
import me.coolaid.nosigngui.NoSignGUI;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

@Mod(NoSignGUI.MOD_ID)
public final class NoSignGUINeoForge {
    private static final KeyMapping.Category CATEGORY = new KeyMapping.Category(ResourceLocation.parse("nosigngui"));

    private static final KeyMapping TOGGLE_GUI_KEY = new KeyMapping(
            "key.nosigngui.toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            CATEGORY
    );

    public NoSignGUINeoForge(IEventBus modEventBus) {
        NoSignGUI.init();
        modEventBus.addListener(this::onRegisterKeyMappings);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
    }

    private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(TOGGLE_GUI_KEY);
    }

    private void onClientTick(ClientTickEvent.Post event) {
        while (TOGGLE_GUI_KEY.consumeClick()) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null) {
                minecraft.player.displayClientMessage(NoSignGUI.toggleSignGuiMessage(), true);
            }
        }
    }
}