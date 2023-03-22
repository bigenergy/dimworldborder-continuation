package uk.me.joeclack.dimensionalworldborder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;

public class CompoundTagWithFallback extends CompoundTag {

    public static Double getDouble(CompoundTag readFromTag, String key, Double fallback) {
        if (!readFromTag.contains(key, 99)) {
            return fallback;
        }

        Tag tag = readFromTag.get(key);

        if (tag == null) {
            return fallback;
        }

        try {
            return ((NumericTag) tag).getAsDouble();
        }
        catch (ClassCastException classCastException) {
            return fallback;
        }
    }

    public static Long getLong(CompoundTag readFromTag, String key, Long fallback) {
        if (!readFromTag.contains(key, 99)) {
            return fallback;
        }

        Tag tag = readFromTag.get(key);

        if (tag == null) {
            return fallback;
        }

        try {
            return ((NumericTag) tag).getAsLong();
        }
        catch (ClassCastException classCastException) {
            return fallback;
        }
    }

    public static int getInt(CompoundTag readFromTag, String key, int fallback) {
        if (!readFromTag.contains(key, 99)) {
            return fallback;
        }

        Tag tag = readFromTag.get(key);

        if (tag == null) {
            return fallback;
        }

        try {
            return ((NumericTag) tag).getAsInt();
        }
        catch (ClassCastException classCastException) {
            return fallback;
        }
    }

}
