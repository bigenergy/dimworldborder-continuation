package uk.me.joeclack.dimensionalworldborder;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec2;

import java.util.Locale;
import java.util.function.Function;

public class DimensionalWBCommand {
    private static final DynamicCommandExceptionType ERROR_SAME_CENTER = new DynamicCommandExceptionType(dimensionBorderException("commands.dimensionalworldborder.dimworldborder.center.failed"));
    private static final DynamicCommandExceptionType ERROR_SAME_SIZE = new DynamicCommandExceptionType(dimensionBorderException("commands.dimensionalworldborder.dimworldborder.set.failed.nochange"));
    private static final SimpleCommandExceptionType ERROR_TOO_SMALL = new SimpleCommandExceptionType(Component.translatable("commands.dimensionalworldborder.dimworldborder.set.failed.small"));
    private static final SimpleCommandExceptionType ERROR_TOO_BIG = new SimpleCommandExceptionType(Component.translatable("commands.dimensionalworldborder.dimworldborder.set.failed.big", 5.9999968E7D));
    private static final SimpleCommandExceptionType ERROR_TOO_FAR_OUT = new SimpleCommandExceptionType(Component.translatable("commands.dimensionalworldborder.dimworldborder.set.failed.far", 2.9999984E7D));
    private static final DynamicCommandExceptionType ERROR_SAME_WARNING_TIME = new DynamicCommandExceptionType(dimensionBorderException("commands.dimensionalworldborder.dimworldborder.warning.time.failed"));
    private static final DynamicCommandExceptionType ERROR_SAME_WARNING_DISTANCE = new DynamicCommandExceptionType(dimensionBorderException("commands.dimensionalworldborder.dimworldborder.warning.distance.failed"));
    private static final DynamicCommandExceptionType ERROR_SAME_DAMAGE_BUFFER = new DynamicCommandExceptionType(dimensionBorderException("commands.dimensionalworldborder.dimworldborder.damage.buffer.failed"));
    private static final DynamicCommandExceptionType ERROR_SAME_DAMAGE_AMOUNT = new DynamicCommandExceptionType(dimensionBorderException("commands.dimensionalworldborder.dimworldborder.damage.amount.failed"));

    private static Function<Object, Message> dimensionBorderException(String translatableComponentKey) {
        return (dimensionName) -> {
            return Component.translatable(translatableComponentKey, dimensionName);
        };
    }

    public static void register(CommandDispatcher<CommandSourceStack> p_139247_) {
        p_139247_.register(Commands.literal("dimworldborder").requires((p_139268_) -> {
            return p_139268_.hasPermission(2);
        }).then(Commands.argument("dimension", DimensionArgument.dimension()
        ).then(Commands.literal("add").then(Commands.argument("distance", DoubleArgumentType.doubleArg(-5.9999968E7D, 5.9999968E7D)).executes((p_139290_) -> {
            return setSize(p_139290_.getSource(), DimensionArgument.getDimension(p_139290_, "dimension"), p_139290_.getSource().getLevel().getWorldBorder().getSize() + DoubleArgumentType.getDouble(p_139290_, "distance"), 0L);
        }).then(Commands.argument("time", IntegerArgumentType.integer(0)).executes((p_139288_) -> {
            return setSize(p_139288_.getSource(), DimensionArgument.getDimension(p_139288_, "dimension"), p_139288_.getSource().getLevel().getWorldBorder().getSize() + DoubleArgumentType.getDouble(p_139288_, "distance"), p_139288_.getSource().getLevel().getWorldBorder().getLerpRemainingTime() + (long)IntegerArgumentType.getInteger(p_139288_, "time") * 1000L);
        })))).then(Commands.literal("set").then(Commands.argument("distance", DoubleArgumentType.doubleArg(-5.9999968E7D, 5.9999968E7D)).executes((p_139286_) -> {
            return setSize(p_139286_.getSource(), DimensionArgument.getDimension(p_139286_, "dimension"), DoubleArgumentType.getDouble(p_139286_, "distance"), 0L);
        }).then(Commands.argument("time", IntegerArgumentType.integer(0)).executes((p_139284_) -> {
            return setSize(p_139284_.getSource(), DimensionArgument.getDimension(p_139284_, "dimension"), DoubleArgumentType.getDouble(p_139284_, "distance"), (long)IntegerArgumentType.getInteger(p_139284_, "time") * 1000L);
        })))).then(Commands.literal("center").then(Commands.argument("pos", Vec2Argument.vec2()).executes((p_139282_) -> {
            return setCenter(p_139282_.getSource(), DimensionArgument.getDimension(p_139282_, "dimension"), Vec2Argument.getVec2(p_139282_, "pos"));
        }))).then(Commands.literal("damage").then(Commands.literal("amount").then(Commands.argument("damagePerBlock", FloatArgumentType.floatArg(0.0F)).executes((p_139280_) -> {
            return setDamageAmount(p_139280_.getSource(), DimensionArgument.getDimension(p_139280_, "dimension"), FloatArgumentType.getFloat(p_139280_, "damagePerBlock"));
        }))).then(Commands.literal("buffer").then(Commands.argument("distance", FloatArgumentType.floatArg(0.0F)).executes((p_139278_) -> {
            return setDamageBuffer(p_139278_.getSource(), DimensionArgument.getDimension(p_139278_, "dimension"), FloatArgumentType.getFloat(p_139278_, "distance"));
        })))).then(Commands.literal("get").executes((p_139276_) -> {
            return getSize(p_139276_.getSource(), DimensionArgument.getDimension(p_139276_, "dimension"));
        })).then(Commands.literal("warning").then(Commands.literal("distance").then(Commands.argument("distance", IntegerArgumentType.integer(0)).executes((p_139266_) -> {
            return setWarningDistance(p_139266_.getSource(), DimensionArgument.getDimension(p_139266_, "dimension"), IntegerArgumentType.getInteger(p_139266_, "distance"));
        }))).then(Commands.literal("time").then(Commands.argument("time", IntegerArgumentType.integer(0)).executes((p_139249_) -> {
            return setWarningTime(p_139249_.getSource(), DimensionArgument.getDimension(p_139249_, "dimension"), IntegerArgumentType.getInteger(p_139249_, "time"));
        }))))));
    }

    private static int setDamageBuffer(CommandSourceStack p_139257_, ServerLevel dimension, float p_139258_) throws CommandSyntaxException {
        WorldBorder worldborder = dimension.getWorldBorder();
        String dimensionName = dimension.dimension().location().toString();
        if (worldborder.getDamageSafeZone() == (double)p_139258_) {
            throw ERROR_SAME_DAMAGE_BUFFER.create(dimensionName);
        } else {
            worldborder.setDamageSafeZone((double)p_139258_);
            p_139257_.sendSuccess(Component.translatable("commands.dimensionalworldborder.dimworldborder.damage.buffer.success", dimensionName, String.format(Locale.ROOT, "%.2f", p_139258_)), true);
            return (int)p_139258_;
        }
    }

    private static int setDamageAmount(CommandSourceStack p_139270_, ServerLevel dimension, float p_139271_) throws CommandSyntaxException {
        WorldBorder worldborder = dimension.getWorldBorder();
        String dimensionName = dimension.dimension().location().toString();
        if (worldborder.getDamagePerBlock() == (double)p_139271_) {
            throw ERROR_SAME_DAMAGE_AMOUNT.create(dimensionName);
        } else {
            worldborder.setDamagePerBlock((double)p_139271_);
            p_139270_.sendSuccess(Component.translatable("commands.dimensionalworldborder.dimworldborder.damage.amount.success", dimensionName, String.format(Locale.ROOT, "%.2f", p_139271_)), true);
            return (int)p_139271_;
        }
    }

    private static int setWarningTime(CommandSourceStack p_139260_, ServerLevel dimension, int p_139261_) throws CommandSyntaxException {
        WorldBorder worldborder = dimension.getWorldBorder();
        String dimensionName = dimension.dimension().location().toString();
        if (worldborder.getWarningTime() == p_139261_) {
            throw ERROR_SAME_WARNING_TIME.create(dimensionName);
        } else {
            worldborder.setWarningTime(p_139261_);
            p_139260_.sendSuccess(Component.translatable("commands.dimensionalworldborder.dimworldborder.warning.time.success", dimensionName, p_139261_), true);
            return p_139261_;
        }
    }

    private static int setWarningDistance(CommandSourceStack p_139273_, ServerLevel dimension, int p_139274_) throws CommandSyntaxException {
        WorldBorder worldborder = dimension.getWorldBorder();
        String dimensionName = dimension.dimension().location().toString();
        if (worldborder.getWarningBlocks() == p_139274_) {
            throw ERROR_SAME_WARNING_DISTANCE.create(dimensionName);
        } else {
            worldborder.setWarningBlocks(p_139274_);
            p_139273_.sendSuccess(Component.translatable("commands.dimensionalworldborder.dimworldborder.warning.distance.success", dimensionName, p_139274_), true);
            return p_139274_;
        }
    }

    private static int getSize(CommandSourceStack p_139251_, ServerLevel dimension) {
        double d0 = dimension.getWorldBorder().getSize();
        String dimensionName = dimension.dimension().location().toString();
        p_139251_.sendSuccess(Component.translatable("commands.dimensionalworldborder.dimworldborder.get", dimensionName, String.format(Locale.ROOT, "%.0f", d0)), false);
        return Mth.floor(d0 + 0.5D);
    }

    private static int setCenter(CommandSourceStack p_139263_, ServerLevel dimension, Vec2 p_139264_) throws CommandSyntaxException {
        WorldBorder worldborder = dimension.getWorldBorder();
        String dimensionName = dimension.dimension().location().toString();
        if (worldborder.getCenterX() == (double)p_139264_.x && worldborder.getCenterZ() == (double)p_139264_.y) {
            throw ERROR_SAME_CENTER.create(dimensionName);
        } else if (!((double)Math.abs(p_139264_.x) > 2.9999984E7D) && !((double)Math.abs(p_139264_.y) > 2.9999984E7D)) {
            worldborder.setCenter((double)p_139264_.x, (double)p_139264_.y);
            p_139263_.sendSuccess(Component.translatable("commands.dimensionalworldborder.dimworldborder.center.success", dimensionName, String.format(Locale.ROOT, "%.2f", p_139264_.x), String.format("%.2f", p_139264_.y)), true);
            return 0;
        } else {
            throw ERROR_TOO_FAR_OUT.create();
        }
    }

    private static int setSize(CommandSourceStack p_139253_, ServerLevel dimension, double p_139254_, long p_139255_) throws CommandSyntaxException {
        WorldBorder worldborder = dimension.getWorldBorder();
        String dimensionName = dimension.dimension().location().toString();
        double d0 = worldborder.getSize();
        if (d0 == p_139254_) {
            throw ERROR_SAME_SIZE.create(dimensionName);
        } else if (p_139254_ < 1.0D) {
            throw ERROR_TOO_SMALL.create();
        } else if (p_139254_ > 5.9999968E7D) {
            throw ERROR_TOO_BIG.create();
        } else {
            if (p_139255_ > 0L) {
                worldborder.lerpSizeBetween(d0, p_139254_, p_139255_);
                if (p_139254_ > d0) {
                    p_139253_.sendSuccess(Component.translatable("commands.dimensionalworldborder.dimworldborder.set.grow", dimensionName, String.format(Locale.ROOT, "%.1f", p_139254_), Long.toString(p_139255_ / 1000L)), true);
                } else {
                    p_139253_.sendSuccess(Component.translatable("commands.dimensionalworldborder.dimworldborder.set.shrink", dimensionName, String.format(Locale.ROOT, "%.1f", p_139254_), Long.toString(p_139255_ / 1000L)), true);
                }
            } else {
                worldborder.setSize(p_139254_);
                p_139253_.sendSuccess(Component.translatable("commands.dimensionalworldborder.dimworldborder.set.immediate", dimensionName, String.format(Locale.ROOT, "%.1f", p_139254_)), true);
            }

            return (int)(p_139254_ - d0);
        }
    }
}
