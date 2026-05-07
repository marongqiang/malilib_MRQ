package fi.dy.masa.malilib.compat.masatools;

import javax.annotation.Nullable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import fi.dy.masa.malilib.gui.GuiBase;

public class MasaModRouter
{
    @Nullable
    public static Screen createTargetScreen(MasaModId target)
    {
        try
        {
            switch (target)
            {
                case LITEMATICA:
                    // Prefer the main menu if present
                    return (Screen) Class.forName("fi.dy.masa.litematica.gui.GuiMainMenu")
                            .getDeclaredConstructor().newInstance();
                case TWEAKEROO:
                    return (Screen) Class.forName("fi.dy.masa.tweakeroo.gui.GuiConfigs")
                            .getDeclaredConstructor().newInstance();
                case MINIHUD:
                    return (Screen) Class.forName("fi.dy.masa.minihud.gui.GuiConfigs")
                            .getDeclaredConstructor().newInstance();
                case ITEMSCROLLER:
                    return (Screen) Class.forName("fi.dy.masa.itemscroller.gui.GuiConfigs")
                            .getDeclaredConstructor().newInstance();
            }
        }
        catch (Exception e)
        {
            return null;
        }

        return null;
    }

    public static void switchTo(MasaModEntry entry)
    {
        MinecraftClient mc = MinecraftClient.getInstance();
        Screen current = mc.currentScreen;

        if (entry == null || entry.id == null)
        {
            return;
        }

        if (current != null)
        {
            MasaModId currentId = MasaScreenId.getModIdForScreen(current);

            if (currentId == entry.id)
            {
                return;
            }

            MasaScreenStateStore.save(current);
        }

        Screen target = createTargetScreen(entry.id);

        if (target == null)
        {
            if (current instanceof GuiBase)
            {
                ((GuiBase) current).addGuiMessage(fi.dy.masa.malilib.gui.Message.MessageType.WARNING, 3000,
                        "malilib.masa_switcher.error.target_not_loaded");
            }

            return;
        }

        GuiBase.openGui(target);
    }
}

