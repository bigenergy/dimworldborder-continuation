package uk.me.joeclack.dimensionalworldborder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.saveddata.SavedData;

public class DimensionalWBSavedData extends SavedData {

    private DimensionalWorldborderSettings worldborderSettings;

    private DimensionalWBSavedData() {
        this.worldborderSettings = new DimensionalWorldborderSettings(WorldBorder.DEFAULT_SETTINGS);
    }

    public static DimensionalWBSavedData create() {
        return new DimensionalWBSavedData();
    }

    public static DimensionalWBSavedData load(CompoundTag tag) {
        DimensionalWBSavedData data = create();

        data.worldborderSettings = DimensionalWorldborderSettings.readFromTag(tag, WorldBorder.DEFAULT_SETTINGS);

        return data;
    }

    public CompoundTag save(CompoundTag tag) {
        this.worldborderSettings.write(tag);
        return tag;
    }

    public static DimensionalWBSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(DimensionalWBSavedData::load, DimensionalWBSavedData::create, "dimensionalwb");
    }

    public void applyTo(WorldBorder border) {
        this.worldborderSettings.applyTo(border);
    }

    public void setWorldborderSettings(WorldBorder.Settings settings) {
        this.worldborderSettings = new DimensionalWorldborderSettings(settings);
        this.setDirty();
    }

    public static class DimensionalWorldborderSettings {
        private final double centerX;
        private final double centerZ;
        private final double damagePerBlock;
        private final double safeZone;
        private final int warningBlocks;
        private final int warningTime;
        private final double size;
        private final long sizeLerpTime;
        private final double sizeLerpTarget;

        DimensionalWorldborderSettings(double p_62011_, double p_62012_, double p_62013_, double p_62014_, int p_62015_, int p_62016_, double p_62017_, long p_62018_, double p_62019_) {
            this.centerX = p_62011_;
            this.centerZ = p_62012_;
            this.damagePerBlock = p_62013_;
            this.safeZone = p_62014_;
            this.warningBlocks = p_62015_;
            this.warningTime = p_62016_;
            this.size = p_62017_;
            this.sizeLerpTime = p_62018_;
            this.sizeLerpTarget = p_62019_;
        }

        DimensionalWorldborderSettings(WorldBorder.Settings p_62032_) {
            this.centerX = p_62032_.getCenterX();
            this.centerZ = p_62032_.getCenterZ();
            this.damagePerBlock = p_62032_.getDamagePerBlock();
            this.safeZone = p_62032_.getSafeZone();
            this.warningBlocks = p_62032_.getWarningBlocks();
            this.warningTime = p_62032_.getWarningTime();
            this.size = p_62032_.getSize();
            this.sizeLerpTime = p_62032_.getSizeLerpTime();
            this.sizeLerpTarget = p_62032_.getSizeLerpTarget();
        }

        public double getCenterX() {
            return this.centerX;
        }

        public double getCenterZ() {
            return this.centerZ;
        }

        public double getDamagePerBlock() {
            return this.damagePerBlock;
        }

        public double getSafeZone() {
            return this.safeZone;
        }

        public int getWarningBlocks() {
            return this.warningBlocks;
        }

        public int getWarningTime() {
            return this.warningTime;
        }

        public double getSize() {
            return this.size;
        }

        public long getSizeLerpTime() {
            return this.sizeLerpTime;
        }

        public double getSizeLerpTarget() {
            return this.sizeLerpTarget;
        }

        public static DimensionalWorldborderSettings readFromTag(CompoundTag tag, WorldBorder.Settings defaultSettings) {
            double d0 = Mth.clamp(CompoundTagWithFallback.getDouble(tag, "BorderCenterX", defaultSettings.getCenterX()), -2.9999984E7D, 2.9999984E7D);
            double d1 = Mth.clamp(CompoundTagWithFallback.getDouble(tag, "BorderCenterZ", defaultSettings.getCenterZ()), -2.9999984E7D, 2.9999984E7D);
            double d2 = CompoundTagWithFallback.getDouble(tag, "BorderSize", defaultSettings.getSize());
            long i = CompoundTagWithFallback.getLong(tag,"BorderSizeLerpTime", defaultSettings.getSizeLerpTime());
            double d3 = CompoundTagWithFallback.getDouble(tag, "BorderSizeLerpTarget", defaultSettings.getSizeLerpTarget());
            double d4 = CompoundTagWithFallback.getDouble(tag, "BorderSafeZone", defaultSettings.getSafeZone());
            double d5 = CompoundTagWithFallback.getDouble(tag, "BorderDamagePerBlock", defaultSettings.getDamagePerBlock());
            int j = CompoundTagWithFallback.getInt(tag, "BorderWarningBlocks", defaultSettings.getWarningBlocks());
            int k = CompoundTagWithFallback.getInt(tag, "BorderWarningTime", defaultSettings.getWarningTime());
            return new DimensionalWorldborderSettings(d0, d1, d5, d4, j, k, d2, i, d3);
        }

        public void write(CompoundTag p_62041_) {
            p_62041_.putDouble("BorderCenterX", this.centerX);
            p_62041_.putDouble("BorderCenterZ", this.centerZ);
            p_62041_.putDouble("BorderSize", this.size);
            p_62041_.putLong("BorderSizeLerpTime", this.sizeLerpTime);
            p_62041_.putDouble("BorderSafeZone", this.safeZone);
            p_62041_.putDouble("BorderDamagePerBlock", this.damagePerBlock);
            p_62041_.putDouble("BorderSizeLerpTarget", this.sizeLerpTarget);
            p_62041_.putDouble("BorderWarningBlocks", (double)this.warningBlocks);
            p_62041_.putDouble("BorderWarningTime", (double)this.warningTime);
        }

        public void applyTo(WorldBorder border) {
            border.setCenter(this.getCenterX(), this.getCenterZ());
            border.setDamagePerBlock(this.getDamagePerBlock());
            border.setDamageSafeZone(this.getSafeZone());
            border.setWarningBlocks(this.getWarningBlocks());
            border.setWarningTime(this.getWarningTime());
            if (this.getSizeLerpTime() > 0L) {
                border.lerpSizeBetween(this.getSize(), this.getSizeLerpTarget(), this.getSizeLerpTime());
            } else {
                border.setSize(this.getSize());
            }
        }
    }
}
