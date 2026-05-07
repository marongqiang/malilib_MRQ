package fi.dy.masa.malilib.gui.widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import fi.dy.masa.malilib.config.ConfigType;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigResettable;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase.ConfigOptionWrapper;
import fi.dy.masa.malilib.gui.LeftRight;
import fi.dy.masa.malilib.gui.MaLiLibIcons;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.util.AlphaNumComparator;

public class WidgetListConfigOptions extends WidgetListConfigOptionsBase<ConfigOptionWrapper, WidgetConfigOption>
{
    protected final GuiConfigsBase parent;
    protected final WidgetSearchBarConfigs widgetSearchConfigs;

    public WidgetListConfigOptions(int x, int y, int width, int height, int configWidth, float zLevel, boolean useKeybindSearch, GuiConfigsBase parent)
    {
        super(x, y, width, height, configWidth);

        this.parent = parent;
        // The config GUI layout (especially with collapsible groups) is order-sensitive.
        // Sorting would separate headers from their children and break collapse/expand.
        this.shouldSortList = false;

        if (useKeybindSearch)
        {
            this.widgetSearchConfigs = new WidgetSearchBarConfigs(x + 2, y + 4, width - 14, 20, 0, MaLiLibIcons.SEARCH, LeftRight.LEFT);
            this.widgetSearchBar = this.widgetSearchConfigs;
            this.browserEntriesOffsetY = 23;
        }
        else
        {
            this.widgetSearchConfigs = null;
            this.widgetSearchBar = new WidgetSearchBar(x + 2, y + 4, width - 14, 14, 0, MaLiLibIcons.SEARCH, LeftRight.LEFT);
            this.browserEntriesOffsetY = 17;
        }
    }

    @Override
    protected Collection<ConfigOptionWrapper> getAllEntries()
    {
        return this.parent.getConfigs();
    }

    @Override
    protected void reCreateListEntryWidgets()
    {
        this.maxLabelWidth = this.getMaxNameLengthWrapped(this.listContents);
        super.reCreateListEntryWidgets();
    }

    @Override
    protected List<String> getEntryStringsForFilter(ConfigOptionWrapper entry)
    {
        IConfigBase config = entry.getConfig();

        if (config != null)
        {
            ArrayList<String> list = new ArrayList<>();
            String name = config.getName();
            String translated = config.getConfigGuiDisplayName();

            list.add(name.toLowerCase());

            if (name.equals(translated) == false)
            {
                list.add(translated.toLowerCase());
            }

            if (config instanceof IConfigResettable && ((IConfigResettable) config).isModified())
            {
                list.add("modified");
            }

            return list;
        }

        return Collections.emptyList();
    }

    @Override
    protected void addFilteredContents(Collection<ConfigOptionWrapper> entries)
    {
        if (this.widgetSearchConfigs != null)
        {
            String filterText = this.widgetSearchConfigs.getFilter();
            IKeybind filterKeys = this.widgetSearchConfigs.getKeybind();

            // While the search *text* is empty, show the full list (so group headers are never
            // accidentally filtered out) and let IConfigGroupHandler control collapsed/expanded
            // visibility. (If we also require filterKeys to be empty, a non-empty "key filter"
            // alone can produce zero visible rows when nothing matches, including all labels.)
            if (filterText.isEmpty() &&
                this.parent instanceof fi.dy.masa.malilib.gui.interfaces.IConfigGroupHandler handler)
            {
                this.addNonFilteredContents(entries);
                return;
            }

            // When filtering, also include group headers for matched configs (auto-expand path)
            Set<String> groupsToShow = new HashSet<>();
            List<ConfigOptionWrapper> matched = new ArrayList<>();
            List<String> groupStack = new ArrayList<>();

            // Pass 1: find matches and record their group path
            for (ConfigOptionWrapper entry : entries)
            {
                if (entry.getType() == ConfigOptionWrapper.Type.LABEL)
                {
                    String label = entry.getLabel();
                    int indent = entry.getIndent();

                    while (groupStack.size() > indent)
                    {
                        groupStack.remove(groupStack.size() - 1);
                    }

                    if (label != null && label.startsWith("\u0001group:"))
                    {
                        String rest = label.substring("\u0001group:".length());
                        String[] parts = rest.split("\\|", 3);
                        String groupId = parts.length >= 1 ? parts[0] : rest;

                        if (groupStack.size() == indent)
                        {
                            groupStack.add(groupId);
                        }
                        else if (groupStack.size() > indent)
                        {
                            groupStack.set(indent, groupId);
                        }
                    }

                    continue;
                }

                if (entry.getType() != ConfigOptionWrapper.Type.CONFIG)
                {
                    continue;
                }

                IConfigBase config = entry.getConfig();
                if (config == null)
                {
                    continue;
                }

                boolean textOk = filterText.isEmpty() || this.entryMatchesFilter(entry, filterText);
                boolean keyOk = config.getType() != ConfigType.HOTKEY ||
                        filterKeys.getKeys().size() == 0 ||
                        ((IHotkey) config).getKeybind().overlaps(filterKeys);

                if (textOk && keyOk)
                {
                    matched.add(entry);
                    groupsToShow.addAll(groupStack);
                }
            }

            // Pass 2: output headers on the path + matched configs, preserving original order
            groupStack.clear();
            for (ConfigOptionWrapper entry : entries)
            {
                if (entry.getType() == ConfigOptionWrapper.Type.LABEL)
                {
                    String label = entry.getLabel();
                    int indent = entry.getIndent();

                    while (groupStack.size() > indent)
                    {
                        groupStack.remove(groupStack.size() - 1);
                    }

                    if (label != null && label.startsWith("\u0001group:"))
                    {
                        String rest = label.substring("\u0001group:".length());
                        String[] parts = rest.split("\\|", 3);
                        String groupId = parts.length >= 1 ? parts[0] : rest;

                        if (groupsToShow.contains(groupId))
                        {
                            this.listContents.add(entry);
                        }

                        if (groupStack.size() == indent)
                        {
                            groupStack.add(groupId);
                        }
                        else if (groupStack.size() > indent)
                        {
                            groupStack.set(indent, groupId);
                        }
                    }
                    continue;
                }

                if (entry.getType() == ConfigOptionWrapper.Type.CONFIG && matched.contains(entry))
                {
                    this.listContents.add(entry);
                }
            }
        }
        else
        {
            super.addFilteredContents(entries);
        }
    }

    @Override
    protected void addNonFilteredContents(Collection<ConfigOptionWrapper> entries)
    {
        if (this.parent instanceof fi.dy.masa.malilib.gui.interfaces.IConfigGroupHandler handler)
        {
            List<String> groupStack = new ArrayList<>();
            List<Boolean> expandedStack = new ArrayList<>();

            for (ConfigOptionWrapper entry : entries)
            {
                if (entry.getType() == ConfigOptionWrapper.Type.LABEL)
                {
                    String label = entry.getLabel();
                    int indent = entry.getIndent();

                    while (groupStack.size() > indent)
                    {
                        groupStack.remove(groupStack.size() - 1);
                        expandedStack.remove(expandedStack.size() - 1);
                    }

                    boolean ancestorsExpanded = true;
                    for (Boolean expanded : expandedStack)
                    {
                        if (expanded == false)
                        {
                            ancestorsExpanded = false;
                            break;
                        }
                    }

                    if (label != null && label.startsWith("\u0001group:"))
                    {
                        String rest = label.substring("\u0001group:".length());
                        String[] parts = rest.split("\\|", 3);
                        String groupId = parts.length >= 1 ? parts[0] : rest;

                        boolean expanded = handler.isGroupExpanded(groupId);

                        // Only keep nested group headers visible if all ancestors are expanded.
                        // Otherwise a collapsed parent would still show its sub-group headers.
                        if (ancestorsExpanded)
                        {
                            if (groupStack.size() == indent)
                            {
                                groupStack.add(groupId);
                                expandedStack.add(expanded);
                            }
                            else if (groupStack.size() > indent)
                            {
                                groupStack.set(indent, groupId);
                                expandedStack.set(indent, expanded);
                            }

                            this.listContents.add(entry);
                        }
                        else
                        {
                            // Don't add the header, and don't push its state; the parent collapse already hides everything.
                        }

                        continue;
                    }

                    continue;
                }

                if (entry.getType() == ConfigOptionWrapper.Type.CONFIG)
                {
                    boolean ancestorsExpanded = true;
                    for (Boolean expanded : expandedStack)
                    {
                        if (expanded == false)
                        {
                            ancestorsExpanded = false;
                            break;
                        }
                    }

                    if (ancestorsExpanded)
                    {
                        this.listContents.add(entry);
                    }
                }
            }

            return;
        }

        super.addNonFilteredContents(entries);
    }

    @Override
    protected Comparator<ConfigOptionWrapper> getComparator()
    {
        return new ConfigComparator();
    }

    @Override
    protected WidgetConfigOption createListEntryWidget(int x, int y, int listIndex, boolean isOdd, ConfigOptionWrapper wrapper)
    {
        return new WidgetConfigOption(x, y, this.browserEntryWidth, this.browserEntryHeight,
                this.maxLabelWidth, this.configWidth, wrapper, listIndex, this.parent, this);
    }

    public int getMaxNameLengthWrapped(List<ConfigOptionWrapper> wrappers)
    {
        int width = 0;

        for (ConfigOptionWrapper wrapper : wrappers)
        {
            if (wrapper.getType() == ConfigOptionWrapper.Type.CONFIG)
            {
                // Must include indent (same 14px per level as in WidgetConfigOption#addConfigOption):
                // maxLabelWidth is later reduced by indent per row, so a global max of plain string
                // width alone makes indented long labels overlap the right-side controls.
                int w = this.getStringWidth(wrapper.getConfig().getConfigGuiDisplayName());
                w += Math.max(0, wrapper.getIndent()) * 14;
                width = Math.max(width, w);
            }
        }

        return width;
    }

    protected static class ConfigComparator extends AlphaNumComparator implements Comparator<ConfigOptionWrapper>
    {
        @Override
        public int compare(ConfigOptionWrapper config1, ConfigOptionWrapper config2)
        {
            if (config1.getType() != ConfigOptionWrapper.Type.CONFIG && config2.getType() != ConfigOptionWrapper.Type.CONFIG)
            {
                return 0;
            }
            else if (config1.getType() != ConfigOptionWrapper.Type.CONFIG)
            {
                return -1;
            }
            else if (config2.getType() != ConfigOptionWrapper.Type.CONFIG)
            {
                return 1;
            }

            return this.compare(config1.getConfig().getName(), config2.getConfig().getName());
        }
    }
}
