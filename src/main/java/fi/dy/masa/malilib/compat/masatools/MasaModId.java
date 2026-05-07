package fi.dy.masa.malilib.compat.masatools;

public enum MasaModId
{
    LITEMATICA("litematica"),
    TWEAKEROO("tweakeroo"),
    MINIHUD("minihud"),
    ITEMSCROLLER("itemscroller");

    public final String modId;

    MasaModId(String modId)
    {
        this.modId = modId;
    }

    public static MasaModId fromModId(String modId)
    {
        if (modId == null)
        {
            return null;
        }

        for (MasaModId id : values())
        {
            if (id.modId.equals(modId))
            {
                return id;
            }
        }

        return null;
    }
}

