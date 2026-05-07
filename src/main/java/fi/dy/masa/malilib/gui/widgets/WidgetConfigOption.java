package fi.dy.masa.malilib.gui.widgets;

import javax.annotation.Nullable;
import com.google.common.collect.ImmutableList;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

import fi.dy.masa.malilib.config.ConfigType;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigBoolean;
import fi.dy.masa.malilib.config.IConfigColorList;
import fi.dy.masa.malilib.config.IConfigDouble;
import fi.dy.masa.malilib.config.IConfigInteger;
import fi.dy.masa.malilib.config.IConfigOptionList;
import fi.dy.masa.malilib.config.IConfigResettable;
import fi.dy.masa.malilib.config.IConfigSlider;
import fi.dy.masa.malilib.config.IConfigStringList;
import fi.dy.masa.malilib.config.IConfigValue;
import fi.dy.masa.malilib.config.IStringRepresentable;
import fi.dy.masa.malilib.config.gui.ConfigOptionChangeListenerButton;
import fi.dy.masa.malilib.config.gui.ConfigOptionChangeListenerKeybind;
import fi.dy.masa.malilib.config.gui.ConfigOptionChangeListenerTextField;
import fi.dy.masa.malilib.config.gui.ConfigOptionListenerResetConfig;
import fi.dy.masa.malilib.config.gui.ConfigOptionListenerResetConfig.ConfigResetterButton;
import fi.dy.masa.malilib.config.gui.ConfigOptionListenerResetConfig.ConfigResetterTextField;
import fi.dy.masa.malilib.config.gui.SliderCallbackDouble;
import fi.dy.masa.malilib.config.gui.SliderCallbackInteger;
import fi.dy.masa.malilib.config.options.BooleanHotkeyGuiWrapper;
import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase.ConfigOptionWrapper;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.MaLiLibIcons;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.ConfigButtonBoolean;
import fi.dy.masa.malilib.gui.button.ConfigButtonColorList;
import fi.dy.masa.malilib.gui.button.ConfigButtonKeybind;
import fi.dy.masa.malilib.gui.button.ConfigButtonOptionList;
import fi.dy.masa.malilib.gui.button.ConfigButtonStringList;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.interfaces.IConfigGroupHandler;
import fi.dy.masa.malilib.gui.interfaces.IConfigInfoProvider;
import fi.dy.masa.malilib.gui.interfaces.IGuiIcon;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.gui.interfaces.ISliderCallback;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.GuiUtils;

public class WidgetConfigOption extends WidgetConfigOptionBase<ConfigOptionWrapper>
{
    // Encoded as "\u0001group:<id>|<indent>|<label>"
    private static final String GROUP_LABEL_PREFIX = "\u0001group:";

    protected final ConfigOptionWrapper wrapper;
    protected final IKeybindConfigGui host;
    @Nullable protected final KeybindSettings initialKeybindSettings;
    @Nullable protected ImmutableList<String> initialStringList;
    protected int colorDisplayPosX;
    private boolean initialBoolean;

    public WidgetConfigOption(int x, int y, int width, int height, int labelWidth, int configWidth,
            ConfigOptionWrapper wrapper, int listIndex, IKeybindConfigGui host, WidgetListConfigOptionsBase<?, ?> parent)
    {
        super(x, y, width, height, parent, wrapper, listIndex);

        this.host = host;
        this.wrapper = wrapper;

        if (wrapper.getType() == ConfigOptionWrapper.Type.CONFIG)
        {
            IConfigBase config = wrapper.getConfig();

            if (config instanceof BooleanHotkeyGuiWrapper booleanHotkey)
            {
                this.initialBoolean = booleanHotkey.getBooleanValue();
                this.initialStringValue = booleanHotkey.getKeybind().getStringValue();
                this.initialKeybindSettings = booleanHotkey.getKeybind().getSettings();
            }
            else if (config instanceof ConfigBooleanHotkeyed booleanHotkey)
            {
                this.initialBoolean = booleanHotkey.getBooleanValue();
                this.initialStringValue = booleanHotkey.getKeybind().getStringValue();
                this.initialKeybindSettings = booleanHotkey.getKeybind().getSettings();
            }
            else if (config instanceof IStringRepresentable configStr)
            {
                this.initialStringValue = configStr.getStringValue();
                this.lastAppliedValue = configStr.getStringValue();
                this.initialKeybindSettings = config.getType() == ConfigType.HOTKEY ? ((IHotkey) config).getKeybind().getSettings() : null;
            }
            else
            {
                this.initialStringValue = null;
                this.lastAppliedValue = null;
                this.initialKeybindSettings = null;

                if (config instanceof IConfigStringList)
                {
                    this.initialStringList = ImmutableList.copyOf(((IConfigStringList) config).getStrings());
                }
            }

            this.addConfigOption(x, y, this.zLevel, labelWidth, configWidth, config);
        }
        else
        {
            this.initialStringValue = null;
            this.lastAppliedValue = null;
            this.initialKeybindSettings = null;

            this.addGroupOrLabel(x, y, width, labelWidth, wrapper.getLabel(), wrapper.getIndent());
        }
    }

    /**
     * Smallest width (in px) the right-hand control strip needs for this config, so the label column
     * can be capped and never run under the buttons when the list row is narrow or heavily indented.
     */
    private static int getMinimumControlStripPixels(ConfigType type, IConfigBase config, int configWidth)
    {
        // "重置" 等长文案、窗口缩放、圆角误差：宁可多留几像素
        final int resetApprox = 72;
        if (config instanceof BooleanHotkeyGuiWrapper || config instanceof ConfigBooleanHotkeyed)
        {
            int keybindW = Math.max(24, configWidth - 60 - 2 - 22);
            return 60 + 2 + keybindW + 2 + 22 + resetApprox + 8;
        }
        switch (type)
        {
            case BOOLEAN:
            case OPTION_LIST:
            case STRING_LIST:
            case COLOR_LIST:
            case STRING:
                return configWidth + 2 + resetApprox;
            case HOTKEY:
            {
                int keybindW = configWidth - 22;
                if (keybindW < 20)
                {
                    keybindW = 20;
                }
                return keybindW + 2 + 22 + 22 + resetApprox;
            }
            case COLOR:
            case INTEGER:
            case DOUBLE:
                return configWidth + 2 + 22 + 2 + 22 + resetApprox;
            default:
                return configWidth + 2 + resetApprox;
        }
    }

    private void addGroupOrLabel(int x, int y, int rowWidth, int labelWidth, @Nullable String label, int indentFromWrapper)
    {
        if (label == null)
        {
            return;
        }

        if (label.startsWith(GROUP_LABEL_PREFIX) && this.host instanceof IConfigGroupHandler handler)
        {
            String rest = label.substring(GROUP_LABEL_PREFIX.length());
            String[] parts = rest.split("\\|", 3);
            String groupId = parts.length >= 1 ? parts[0] : rest;
            int indent = indentFromWrapper;
            String groupLabel = parts.length == 3 ? parts[2] : (parts.length == 2 ? parts[1] : groupId);

            if (parts.length >= 2)
            {
                try
                {
                    indent = Integer.parseInt(parts[1]);
                }
                catch (Exception ignore) {}
            }

            boolean expanded = handler.isGroupExpanded(groupId);
            String arrow = expanded ? "▾" : "▸";

            // Arrow button should appear BEFORE the group title; both must be left-aligned
            // (ButtonGeneric defaults to centered text, which looks wrong for headers).
            final int indentPx = Math.max(0, indent) * 14;
            final int arrowW = 14;
            final int gap = 2;
            final int rightPad = 14; // match list scrollbar / margin

            ButtonGeneric arrowBtn = new ButtonGeneric(x + indentPx, y + 1, arrowW, 20, "§6" + arrow + "§r");
            arrowBtn.setTextCentered(false);
            ButtonGeneric titleBtn = new ButtonGeneric(x + indentPx + arrowW + gap, y + 1,
                    Math.max(20, rowWidth - rightPad - indentPx - arrowW - gap), 20, "§6" + groupLabel + "§r");
            titleBtn.setTextCentered(false);

            GroupHeaderListener listener = new GroupHeaderListener(groupId, handler, this.parent);
            this.addButton(arrowBtn, listener);
            this.addButton(titleBtn, listener);
        }
        else
        {
            this.addLabel(x, y + 7, labelWidth, 8, 0xFFFFFFFF, label);
        }
    }

    private static class GroupHeaderListener implements IButtonActionListener
    {
        private final String groupId;
        private final IConfigGroupHandler handler;
        private final WidgetListConfigOptionsBase<?, ?> list;

        private GroupHeaderListener(String groupId, IConfigGroupHandler handler, WidgetListConfigOptionsBase<?, ?> list)
        {
            this.groupId = groupId;
            this.handler = handler;
            this.list = list;
        }

        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton)
        {
            this.handler.toggleGroupExpanded(this.groupId);
            // Refresh the list contents immediately
            this.list.refreshEntries();
        }
    }

    protected void addConfigOption(int x, int y, float zLevel, int labelWidth, int configWidth, IConfigBase config)
    {
        ConfigType type = config.getType();

        y += 1;
        int configHeight = 20;

        int indent = this.wrapper.getIndent();
        int ix = Math.max(0, indent) * 14;
        x += ix;
        if (labelWidth > ix + 20)
        {
            labelWidth -= ix;
        }

        // Hard clamp: never let the right-side controls render outside the list row.
        // Prefer compressing the right-side "config" column; keep the label column as wide as
        // the layout would naturally give (and clip visually in WidgetLabel if it still can't fit).
        final int xLabelStart = x;
        final String displayName = config.getConfigGuiDisplayName();
        final int nameW = this.getStringWidth(displayName);
        final int rowWidth = this.width;
        final int gapAfterLabel = 10;
        final int resetAndGap = 22; // 20px reset + ~2px gap

        // Column alignment uses the per-tab max label width; if this row is longer, the text
        // should still start on the same column, but the controls must not be pushed out.
        final int xIdeal = xLabelStart + Math.max(labelWidth, nameW) + gapAfterLabel;
        int cCap = Math.min(configWidth, rowWidth - xIdeal - resetAndGap);
        if (cCap < 1)
        {
            cCap = 1;
        }

        int cTry = cCap;
        int wStrip;
        for (int safety = 0; safety < 256 && cTry > 0; safety++)
        {
            wStrip = this.getRightControlStripWidth(type, config, cTry);
            if (xIdeal + wStrip <= rowWidth)
            {
                break;
            }
            cTry--;
        }
        wStrip = this.getRightControlStripWidth(type, config, cTry);

        // If we still don't fit, move controls left (keeps on-screen) while clipping the name area.
        int xControls = Math.min(xIdeal, rowWidth - wStrip);
        if (xControls < xLabelStart)
        {
            xControls = xLabelStart;
        }

        int effectiveConfigWidth = cTry;
        if (effectiveConfigWidth < 1)
        {
            effectiveConfigWidth = 1;
        }

        int labelClipW = xControls - xLabelStart - gapAfterLabel;
        if (labelClipW < 1)
        {
            labelClipW = 1;
        }

        // Keep the label on a single line (no translation-level "..."). If too long, it is visually clipped.
        this.addLabel(xLabelStart, y + 7, labelClipW, 8, 0xFFFFFFFF, displayName);

        String comment;
        IConfigInfoProvider infoProvider = this.host.getHoverInfoProvider();

        if (infoProvider != null)
        {
            comment = infoProvider.getHoverInfo(config);
        }
        else
        {
            comment = config.getComment();
        }

        if (comment != null)
        {
            this.addConfigComment(xLabelStart, y + 5, labelClipW, 12, comment);
        }

        x = xControls;

        if (config instanceof BooleanHotkeyGuiWrapper wrapper)
        {
            IConfigBoolean booleanConfig = wrapper.getBooleanConfig();
            IKeybind keybind = wrapper.getKeybind();
            this.addBooleanAndHotkeyWidgets(x, y, effectiveConfigWidth, wrapper, booleanConfig, keybind);
        }
        else if (config instanceof ConfigBooleanHotkeyed hotkeyedBoolean)
        {
            IKeybind keybind = hotkeyedBoolean.getKeybind();
            this.addBooleanAndHotkeyWidgets(x, y, effectiveConfigWidth, hotkeyedBoolean, hotkeyedBoolean, keybind);
        }
        else if (type == ConfigType.BOOLEAN)
        {
            ConfigButtonBoolean optionButton = new ConfigButtonBoolean(x, y, effectiveConfigWidth, configHeight, (IConfigBoolean) config);
            this.addConfigButtonEntry(x + effectiveConfigWidth + 2, y, (IConfigResettable) config, optionButton);
        }
        else if (type == ConfigType.OPTION_LIST)
        {
            ConfigButtonOptionList optionButton = new ConfigButtonOptionList(x, y, effectiveConfigWidth, configHeight, (IConfigOptionList) config);
            this.addConfigButtonEntry(x + effectiveConfigWidth + 2, y, (IConfigResettable) config, optionButton);
        }
        else if (type == ConfigType.STRING_LIST)
        {
            ConfigButtonStringList optionButton = new ConfigButtonStringList(x, y, effectiveConfigWidth, configHeight, (IConfigStringList) config, this.host, this.host.getDialogHandler());
            this.addConfigButtonEntry(x + effectiveConfigWidth + 2, y, (IConfigResettable) config, optionButton);
        }
        else if (type == ConfigType.COLOR_LIST)
        {
            ConfigButtonColorList optionButton = new ConfigButtonColorList(x, y, effectiveConfigWidth, configHeight, (IConfigColorList) config, this.host, this.host.getDialogHandler());
            this.addConfigButtonEntry(x + effectiveConfigWidth + 2, y, (IConfigResettable) config, optionButton);
        }
        else if (type == ConfigType.HOTKEY)
        {
            this.addHotkeyConfigElements(x, y, effectiveConfigWidth, config.getName(), (IHotkey) config);
        }
        else if (type == ConfigType.STRING ||
                 type == ConfigType.COLOR ||
                 type == ConfigType.INTEGER ||
                 type == ConfigType.DOUBLE)
        {
            int resetX = x + effectiveConfigWidth + 2;
            int innerWidth = effectiveConfigWidth;

            if (type == ConfigType.COLOR)
            {
                innerWidth -= 22; // adjust the width to match other configs due to the color display
                this.colorDisplayPosX = x + innerWidth + 2;
                this.addWidget(new WidgetColorIndicator(this.colorDisplayPosX, y + 1, 19, 19, (IConfigInteger) config));
            }
            else if (type == ConfigType.INTEGER || type == ConfigType.DOUBLE)
            {
                innerWidth -= 18;
                this.colorDisplayPosX = x + innerWidth + 2;
            }

            if ((type == ConfigType.INTEGER || type == ConfigType.DOUBLE) &&
                 config instanceof IConfigSlider && ((IConfigSlider) config).shouldUseSlider())
            {
                this.addConfigSliderEntry(x, y, resetX, innerWidth, configHeight, (IConfigSlider) config);
            }
            else
            {
                this.addConfigTextFieldEntry(x, y, resetX, innerWidth, configHeight, (IConfigValue) config);
            }

            if (type != ConfigType.COLOR && config instanceof IConfigSlider)
            {
                IGuiIcon icon = ((IConfigSlider) config).shouldUseSlider() ? MaLiLibIcons.BTN_TXTFIELD : MaLiLibIcons.BTN_SLIDER;
                ButtonGeneric toggleBtn = new ButtonGeneric(this.colorDisplayPosX, y + 2, icon);
                this.addButton(toggleBtn, new ListenerSliderToggle((IConfigSlider) config));
            }
        }
    }

    /**
     * Total horizontal space needed from the first right-side control to the end of the row
     * (inclusive of the reset button and its small gap), matching how widgets are actually laid out.
     */
    private int getRightControlStripWidth(ConfigType type, IConfigBase config, int effectiveConfigWidth)
    {
        int c = effectiveConfigWidth;
        if (c < 1)
        {
            c = 1;
        }

        // These widths are derived directly from the layout math in addBooleanAndHotkeyWidgets /
        // addHotkeyConfigElements / addConfigButtonEntry: the passed "config width" already includes
        // any side widgets (icons, keybind settings column, etc.).
        if (config instanceof BooleanHotkeyGuiWrapper || config instanceof ConfigBooleanHotkeyed || type == ConfigType.HOTKEY)
        {
            // resetX = x + effectiveConfigWidth + 2  => total = c + 22 (reset) + 2 (gap) = c + 24
            return c + 24;
        }

        // resetX = x + effectiveConfigWidth + 2  => total = c + 22
        return c + 22;
    }

    @Override
    public boolean wasConfigModified()
    {
        if (this.wrapper.getType() == ConfigOptionWrapper.Type.CONFIG)
        {
            IConfigBase config = this.wrapper.getConfig();
            boolean modified = false;

            if (config instanceof BooleanHotkeyGuiWrapper booleanHotkey)
            {
                IKeybind keybind = booleanHotkey.getKeybind();
                return this.initialBoolean != booleanHotkey.getBooleanValue() ||
                       this.initialStringValue.equals(keybind.getStringValue()) == false ||
                       this.initialKeybindSettings.equals(keybind.getSettings()) == false;
            }
            else if (config instanceof ConfigBooleanHotkeyed booleanHotkey)
            {
                IKeybind keybind = booleanHotkey.getKeybind();
                return this.initialBoolean != booleanHotkey.getBooleanValue() ||
                       this.initialStringValue.equals(keybind.getStringValue()) == false ||
                       this.initialKeybindSettings.equals(keybind.getSettings()) == false;
            }
            else if (config instanceof IStringRepresentable)
            {
                if (this.textField != null)
                {
                    modified |= this.initialStringValue.equals(this.textField.getTextField().getText()) == false;
                }

                if (this.initialKeybindSettings != null && this.initialKeybindSettings.equals(((IHotkey) config).getKeybind().getSettings()) == false)
                {
                    modified = true;
                }

                return modified || this.initialStringValue.equals(((IStringRepresentable) config).getStringValue()) == false;
            }
            else if (this.initialStringList != null && config instanceof IConfigStringList)
            {
                return this.initialStringList.equals(((IConfigStringList) config).getStrings()) == false;
            }
        }

        return false;
    }

    @Override
    public void applyNewValueToConfig()
    {
        if (this.wrapper.getType() == ConfigOptionWrapper.Type.CONFIG &&
            this.wrapper.getConfig() instanceof IStringRepresentable config)
        {
            if (this.textField != null && this.hasPendingModifications())
            {
                config.setValueFromString(this.textField.getTextField().getText());
            }

            this.lastAppliedValue = config.getStringValue();
        }
    }

    protected void addConfigComment(int x, int y, int width, int height, String comment)
    {
        this.addWidget(new WidgetHoverInfo(x, y, width, height, comment));
    }

    protected void addHotkeyConfigElements(int x, int y, int configWidth, String configName, IHotkey hotkey)
    {
        configWidth -= 22; // adjust the width to match other configs due to the settings widget
        IKeybind keybind = hotkey.getKeybind();
        ConfigButtonKeybind keybindButton = new ConfigButtonKeybind(x, y, configWidth, 20, keybind, this.host);
        x += configWidth + 2;

        this.addWidget(new WidgetKeybindSettings(x, y, 20, 20, keybind, configName, this.parent, this.host.getDialogHandler()));
        x += 22;

        this.addButton(keybindButton, this.host.getButtonPressListener());
        this.addKeybindResetButton(x, y, keybind, keybindButton);
    }

    protected void addBooleanAndHotkeyWidgets(int x, int y, int configWidth,
                                              IConfigResettable resettableConfig,
                                              IConfigBoolean booleanConfig,
                                              IKeybind keybind)
    {
        int booleanBtnWidth = 44;
        ConfigButtonBoolean booleanButton = new ConfigButtonBoolean(x, y, booleanBtnWidth, 20, booleanConfig);
        x += booleanBtnWidth + 2;
        configWidth -= booleanBtnWidth + 2 + 22;

        ConfigButtonKeybind keybindButton = new ConfigButtonKeybind(x, y, configWidth, 20, keybind, this.host);
        x += configWidth + 2;

        this.addWidget(new WidgetKeybindSettings(x, y, 20, 20, keybind, booleanConfig.getName(), this.parent, this.host.getDialogHandler()));
        x += 22;

        ButtonGeneric resetButton = this.createResetButton(x, y, resettableConfig);

        ConfigOptionChangeListenerButton booleanChangeListener = new ConfigOptionChangeListenerButton(resettableConfig, resetButton, null);
        HotkeyedBooleanResetListener resetListener = new HotkeyedBooleanResetListener(resettableConfig, booleanButton, keybindButton, resetButton, this.host);

        this.host.addKeybindChangeListener(resetListener::updateButtons);

        this.addButton(booleanButton, booleanChangeListener);
        this.addButton(keybindButton, this.host.getButtonPressListener());
        this.addButton(resetButton, resetListener);
    }

    protected void addConfigButtonEntry(int xReset, int yReset, IConfigResettable config, ButtonBase optionButton)
    {
        ButtonGeneric resetButton = this.createResetButton(xReset, yReset, config);
        ConfigOptionChangeListenerButton listenerChange = new ConfigOptionChangeListenerButton(config, resetButton, null);
        ConfigOptionListenerResetConfig listenerReset = new ConfigOptionListenerResetConfig(config, new ConfigResetterButton(optionButton), resetButton, null);

        this.addButton(optionButton, listenerChange);
        this.addButton(resetButton, listenerReset);
    }

    protected void addConfigTextFieldEntry(int x, int y, int resetX, int configWidth, int configHeight, IConfigValue config)
    {
        GuiTextFieldGeneric field = this.createTextField(x, y + 1, configWidth - 4, configHeight - 3);
        field.setMaxLength(this.maxTextfieldTextLength);
        field.setText(config.getStringValue());

        ButtonGeneric resetButton = this.createResetButton(resetX, y, config);
        ConfigOptionChangeListenerTextField listenerChange = new ConfigOptionChangeListenerTextField(config, field, resetButton);
        ConfigOptionListenerResetConfig listenerReset = new ConfigOptionListenerResetConfig(config, new ConfigResetterTextField(config, field), resetButton, null);

        this.addTextField(field, listenerChange);
        this.addButton(resetButton, listenerReset);
    }

    protected void addConfigSliderEntry(int x, int y, int resetX, int configWidth, int configHeight, IConfigSlider config)
    {
        ButtonGeneric resetButton = this.createResetButton(resetX, y, config);
        ISliderCallback callback;

        if (config instanceof IConfigDouble)
        {
            callback = new SliderCallbackDouble((IConfigDouble) config, resetButton);
        }
        else if (config instanceof IConfigInteger)
        {
            callback = new SliderCallbackInteger((IConfigInteger) config, resetButton);
        }
        else
        {
            return;
        }

        WidgetSlider slider = new WidgetSlider(x, y, configWidth, configHeight, callback);
        ConfigOptionListenerResetConfig listenerReset = new ConfigOptionListenerResetConfig(config, null, resetButton, null);

        this.addWidget(slider);
        this.addButton(resetButton, listenerReset);
    }

    protected void addKeybindResetButton(int x, int y, IKeybind keybind, ConfigButtonKeybind buttonHotkey)
    {
        ButtonGeneric button = this.createResetButton(x, y, keybind);

        ConfigOptionChangeListenerKeybind listener = new ConfigOptionChangeListenerKeybind(keybind, buttonHotkey, button, this.host);
        this.host.addKeybindChangeListener(listener::updateButtons);
        this.addButton(button, listener);
    }

    @Override
    public void render(int mouseX, int mouseY, boolean selected, DrawContext drawContext)
    {
        RenderUtils.color(1f, 1f, 1f, 1f);

        this.drawSubWidgets(mouseX, mouseY, drawContext);

        if (this.wrapper.getType() == ConfigOptionWrapper.Type.CONFIG)
        {
            this.drawTextFields(mouseX, mouseY, drawContext);
            super.render(mouseX, mouseY, selected, drawContext);
        }
    }

    public static class ListenerSliderToggle implements IButtonActionListener
    {
        protected final IConfigSlider config;

        public ListenerSliderToggle(IConfigSlider config)
        {
            this.config = config;
        }

        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton)
        {
            this.config.toggleUseSlider();

            Screen gui = GuiUtils.getCurrentScreen();

            if (gui instanceof GuiBase)
            {
                ((GuiBase) gui).initGui();
            }
        }
    }

    public static class HotkeyedBooleanResetListener implements IButtonActionListener
    {
        private final IConfigResettable config;
        private final ButtonGeneric booleanButton;
        private final ConfigButtonKeybind hotkeyButton;
        private final ButtonGeneric resetButton;
        private final IKeybindConfigGui host;

        public HotkeyedBooleanResetListener(IConfigResettable config,
                                            ButtonGeneric booleanButton,
                                            ConfigButtonKeybind hotkeyButton,
                                            ButtonGeneric resetButton,
                                            IKeybindConfigGui host)
        {
            this.config = config;
            this.booleanButton = booleanButton;
            this.hotkeyButton = hotkeyButton;
            this.resetButton = resetButton;
            this.host = host;
        }

        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton)
        {
            this.config.resetToDefault();
            this.host.getButtonPressListener().actionPerformedWithButton(button, mouseButton);
            this.updateButtons();
        }

        public void updateButtons()
        {
            this.booleanButton.updateDisplayString();
            this.hotkeyButton.updateDisplayString();
            this.resetButton.setEnabled(this.config.isModified());
        }
    }
}
