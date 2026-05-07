package fi.dy.masa.malilib.config;

import java.util.List;
import javax.annotation.Nullable;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.screen.Screen;
import fi.dy.masa.malilib.gui.interfaces.IConfigGui;
import fi.dy.masa.malilib.gui.interfaces.IDialogHandler;

public interface IConfigStringList extends IConfigBase
{
    List<String> getStrings();

    ImmutableList<String> getDefaultStrings();

    void setStrings(List<String> strings);

    /**
     * If true, the config GUI will call {@link #openCustomStringListEditor} instead of the default string-list editor.
     * Allows mods to supply their own picker UI (e.g. the hotkey wheel whitelist).
     */
    default boolean hasCustomStringListEditor()
    {
        return false;
    }

    default void openCustomStringListEditor(IConfigGui configGui, @Nullable IDialogHandler dialogHandler, @Nullable Screen parent)
    {
    }

    /**
     * @return non-empty to override the right-hand button text; empty string to use the default list preview.
     */
    default String getStringListButtonDisplayString(int buttonWidth)
    {
        return "";
    }
}
