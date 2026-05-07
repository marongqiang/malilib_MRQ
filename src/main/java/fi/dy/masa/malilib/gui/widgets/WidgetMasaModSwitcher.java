package fi.dy.masa.malilib.gui.widgets;

import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import fi.dy.masa.malilib.compat.masatools.MasaModEntry;
import fi.dy.masa.malilib.util.KeyCodes;

public class WidgetMasaModSwitcher extends WidgetDropDownList<MasaModEntry>
{
    private final Consumer<MasaModEntry> onSelect;
    @Nullable private MasaModEntry lastSelected;

    public WidgetMasaModSwitcher(int x, int y, int width, int height,
            int maxHeight, int maxVisibleEntries,
            List<MasaModEntry> entries,
            Consumer<MasaModEntry> onSelect)
    {
        super(x, y, width, height, maxHeight, maxVisibleEntries, entries, MasaModEntry::getDisplayName);
        this.onSelect = onSelect;
        this.lastSelected = this.getSelectedEntry();
    }

    public boolean isOpen()
    {
        return this.isOpen;
    }

    public void closeDropdown()
    {
        if (this.isOpen)
        {
            this.isOpen = false;
            this.searchBar.getTextField().setText("");
            this.updateFilteredEntries();
        }
    }

    /**
     * Called from the parent screen on any mouse click, to support
     * "click outside closes the dropdown".
     */
    public void handleGlobalMouseClick(int mouseX, int mouseY, int mouseButton)
    {
        if (this.isOpen && this.isMouseOver(mouseX, mouseY) == false)
        {
            this.closeDropdown();
        }
    }

    @Override
    protected boolean onKeyTypedImpl(int keyCode, int scanCode, int modifiers)
    {
        if (this.isOpen && keyCode == KeyCodes.KEY_ESCAPE)
        {
            this.closeDropdown();
            return true;
        }

        return super.onKeyTypedImpl(keyCode, scanCode, modifiers);
    }

    @Override
    protected boolean onMouseClickedImpl(int mouseX, int mouseY, int mouseButton)
    {
        boolean wasOpen = this.isOpen;
        boolean handled = super.onMouseClickedImpl(mouseX, mouseY, mouseButton);

        // If the dropdown was open and the click was in the list area, selection may have changed.
        if (wasOpen && mouseY > this.y + this.height)
        {
            MasaModEntry selected = this.getSelectedEntry();

            if (selected != null && selected != this.lastSelected)
            {
                this.lastSelected = selected;
                // Close immediately to avoid interactions leaking into the new screen
                this.closeDropdown();
                this.onSelect.accept(selected);
                return true;
            }
        }

        return handled;
    }

    @Override
    protected int getRequiredWidth(int width, List<MasaModEntry> entries, net.minecraft.client.MinecraftClient mc)
    {
        width = super.getRequiredWidth(width, entries, mc);

        // Spec: half size variant - min 40px, max 75px (use clamped width)
        width = Math.max(40, Math.min(75, width));

        return width;
    }

    @Override
    public void setPosition(int x, int y)
    {
        super.setPosition(x, y);

        // Keep right aligned when the screen size changes
        // (actual x is computed by the parent screen when added)
    }
}

