package fi.dy.masa.malilib.gui.interfaces;

/**
 * Optional interface for config GUIs that want collapsible group headers.
 * Implementations can expose a group expanded/collapsed state and toggle it.
 */
public interface IConfigGroupHandler
{
    boolean isGroupExpanded(String groupId);

    void toggleGroupExpanded(String groupId);
}

