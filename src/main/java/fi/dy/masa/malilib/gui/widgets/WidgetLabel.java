package fi.dy.masa.malilib.gui.widgets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.StringUtils;

public class WidgetLabel extends WidgetBase
{
    protected final List<String> labels = new ArrayList<>();
    protected final int textColor;
    protected boolean visible = true;
    protected boolean centered;
    protected boolean backgroundEnabled;
    protected int backgroundColor;
    protected int borderULColor;
    protected int borderBRColor;
    protected int borderSize;

    public WidgetLabel(int x, int y, int width, int height, int textColor, String... text)
    {
        this(x, y, width, height, textColor, Arrays.asList(text));
    }

    public WidgetLabel(int x, int y, int width, int height, int textColor, List<String> lines)
    {
        super(x, y, width, height);

        this.textColor = textColor;

        for (String str : lines)
        {
            this.addLine(str);
        }
    }

    public void addLine(String key, Object... args)
    {
        this.labels.add(StringUtils.translate(key, args));
    }

    public void setCentered(boolean centered)
    {
        this.centered = centered;
    }

    public void setBackgroundProperties(int borderSize, int backgroundColor, int borderULColor, int borderBRColor)
    {
        this.borderSize = borderSize;
        this.backgroundColor = backgroundColor;
        this.borderULColor = borderULColor;
        this.borderBRColor = borderBRColor;
        this.backgroundEnabled = true;
    }

    @Override
    public void render(int mouseX, int mouseY, boolean selected, DrawContext drawContext)
    {
        if (this.visible)
        {
            RenderUtils.setupBlend();
            this.drawLabelBackground();

            int fontHeight = this.fontHeight;
            int yCenter = this.y + this.height / 2 + this.borderSize / 2;
            int yTextStart = yCenter - 1 - this.labels.size() * fontHeight / 2;

            for (int i = 0; i < this.labels.size(); ++i)
            {
                String text = this.labels.get(i);

                if (this.centered)
                {
                    this.drawCenteredStringWithShadow(this.x + this.width / 2, yTextStart + i * fontHeight, this.textColor, text, drawContext);
                }
                else
                {
                    this.drawClippedStringWithShadow(this.x, yTextStart + i * fontHeight, this.textColor, text, drawContext);
                }
            }
        }
    }

    /**
     * Clip left-aligned text to this widget's width, without changing the string (no "..." generation).
     */
    protected void drawClippedStringWithShadow(int x, int y, int color, String text, DrawContext drawContext)
    {
        if (this.width <= 0)
        {
            return;
        }

        boolean clip = this.getStringWidth(text) > this.width;

        if (clip)
        {
            this.enableTextClip();
        }

        try
        {
            this.drawStringWithShadow(x, y, color, text, drawContext);
        }
        finally
        {
            if (clip)
            {
                this.disableTextClip();
            }
        }
    }

    private void enableTextClip()
    {
        MinecraftClient client = MinecraftClient.getInstance();
        int sh = (int) ((double) client.getWindow().getFramebufferHeight() / client.getWindow().getScaleFactor());
        int sx1 = (int) this.x;
        int sy1 = (int) (sh - (this.y + this.height));
        int swBox = (int) this.width;
        int shBox = (int) this.height;
        if (swBox < 1)
        {
            swBox = 1;
        }
        if (shBox < 1)
        {
            shBox = 1;
        }
        if (sx1 < 0)
        {
            swBox += sx1;
            sx1 = 0;
        }
        if (sy1 < 0)
        {
            shBox += sy1;
            sy1 = 0;
        }
        if (swBox < 1 || shBox < 1)
        {
            return;
        }
        RenderSystem.enableScissor(sx1, sy1, swBox, shBox);
    }

    private void disableTextClip()
    {
        RenderSystem.disableScissor();
    }

    protected void drawLabelBackground()
    {
        if (this.backgroundEnabled)
        {
            int bgWidth = this.width + this.borderSize * 2;
            int bgHeight = this.height + this.borderSize * 2;
            int xStart = this.x - this.borderSize;
            int yStart = this.y - this.borderSize;

            RenderUtils.drawRect(xStart, yStart, bgWidth, bgHeight, this.backgroundColor);

            RenderUtils.drawHorizontalLine(xStart, yStart           , bgWidth, this.borderULColor);
            RenderUtils.drawHorizontalLine(xStart, yStart + bgHeight, bgWidth, this.borderBRColor);
            RenderUtils.drawVerticalLine(xStart          , yStart, bgHeight, this.borderULColor);
            RenderUtils.drawVerticalLine(xStart + bgWidth, yStart, bgHeight, this.borderBRColor);
        }
    }
}
