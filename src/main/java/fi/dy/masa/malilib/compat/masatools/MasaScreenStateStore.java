package fi.dy.masa.malilib.compat.masatools;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.client.gui.screen.Screen;
import fi.dy.masa.malilib.gui.GuiConfigsBase;

public class MasaScreenStateStore
{
    private static final Map<MasaModId, StoredState> STATES = new ConcurrentHashMap<>();

    public static void save(Screen screen)
    {
        MasaModId modId = MasaScreenId.getModIdForScreen(screen);

        if (modId == null)
        {
            return;
        }

        StoredState state = new StoredState();
        state.screenClassName = screen.getClass().getName();

        if (screen instanceof GuiConfigsBase)
        {
            state.scrollbar = ((GuiConfigsBase) screen).getConfigListWidget().getScrollbar().getValue();
        }

        STATES.put(modId, state);
    }

    public static void restoreIfPresent(Screen screen)
    {
        MasaModId modId = MasaScreenId.getModIdForScreen(screen);

        if (modId == null)
        {
            return;
        }

        StoredState state = STATES.get(modId);

        if (state == null)
        {
            return;
        }

        if (state.screenClassName != null && state.screenClassName.equals(screen.getClass().getName()) == false)
        {
            return;
        }

        if (screen instanceof GuiConfigsBase)
        {
            ((GuiConfigsBase) screen).getConfigListWidget().getScrollbar().setValue(state.scrollbar);
            ((GuiConfigsBase) screen).getConfigListWidget().refreshEntries();
        }
    }

    private static class StoredState
    {
        @Nullable String screenClassName;
        int scrollbar;
    }
}

