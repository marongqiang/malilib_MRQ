package fi.dy.masa.malilib.compat.masatools;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Enumerates loaded masa mods in a fixed, user-friendly order.
 * This is intentionally conservative (only known modIds) to avoid pulling in
 * other random malilib-based mods into the switcher.
 */
public class MasaModList
{
    private static final MasaModId[] ORDER = new MasaModId[] {
            MasaModId.LITEMATICA,
            MasaModId.TWEAKEROO,
            MasaModId.MINIHUD,
            MasaModId.ITEMSCROLLER
    };

    public static List<MasaModEntry> getLoadedMasaMods()
    {
        EnumSet<MasaModId> loaded = EnumSet.noneOf(MasaModId.class);

        for (MasaModId id : MasaModId.values())
        {
            if (FabricLoader.getInstance().isModLoaded(id.modId))
            {
                loaded.add(id);
            }
        }

        List<MasaModEntry> out = new ArrayList<>();

        for (MasaModId id : ORDER)
        {
            if (loaded.contains(id))
            {
                out.add(new MasaModEntry(id));
            }
        }

        return out;
    }
}

