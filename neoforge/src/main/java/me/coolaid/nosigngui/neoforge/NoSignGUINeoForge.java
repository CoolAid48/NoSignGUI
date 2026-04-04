package me.coolaid.nosigngui.neoforge;

import me.coolaid.nosigngui.NoSignGUI;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

@Mod(NoSignGUI.MOD_ID)
public final class NoSignGUINeoForge {
    private static final KeyMapping TOGGLE_GUI_KEY = new KeyMapping(
            "key.nosigngui.toggle",
            GLFW.GLFW_KEY_G,
            "key.categories." + NoSignGUI.MOD_ID
    );

    public NoSignGUINeoForge(IEventBus modEventBus) {
        NoSignGUI.init();
        modEventBus.addListener(this::onRegisterKeyMappings);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
    }

    private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
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