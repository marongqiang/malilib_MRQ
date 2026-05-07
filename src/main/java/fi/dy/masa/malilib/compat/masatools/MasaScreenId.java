package fi.dy.masa.malilib.compat.masatools;

import javax.annotation.Nullable;
import net.minecraft.client.gui.screen.Screen;
import fi.dy.masa.malilib.gui.interfaces.IConfigGui;

public class MasaScreenId
{
    @Nullable
    public static MasaModId getModIdForScreen(Screen screen)
    {
        if (screen == null)
        {
            return null;
        }

        if (screen instanceof IConfigGui)
        {
            return MasaModId.fromModId(((IConfigGui) screen).getModId());
        }

        String name = screen.getClass().getName();

        if (name.contains(".litematica."))
        {
            return MasaModId.LITEMATICA;
        }
        if (name.contains(".tweakeroo."))
        {
            return MasaModId.TWEAKEROO;
        }
        if (name.contains(".minihud."))
        {
            return MasaModId.MINIHUD;
        }
        if (name.contains(".itemscroller."))
        {
            return MasaModId.ITEMSCROLLER;
        }

        return null;
    }
}

