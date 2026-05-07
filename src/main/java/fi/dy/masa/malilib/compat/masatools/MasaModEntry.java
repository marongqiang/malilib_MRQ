package fi.dy.masa.malilib.compat.masatools;

import fi.dy.masa.malilib.util.StringUtils;

public class MasaModEntry
{
    public final MasaModId id;

    public MasaModEntry(MasaModId id)
    {
        this.id = id;
    }

    public String getDisplayName()
    {
        // Must be stable and match our localization keys
        return StringUtils.translate("malilib.masa_switcher.mod." + this.id.modId);
    }

    @Override
    public String toString()
    {
        return getDisplayName();
    }
}

