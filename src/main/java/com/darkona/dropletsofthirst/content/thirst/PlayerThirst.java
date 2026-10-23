package com.darkona.dropletsofthirst.content.thirst;

import com.darkona.dropletsofthirst.api.DropletsAPI;
import com.darkona.dropletsofthirst.api.DropletsView;
import com.darkona.dropletsofthirst.api.ThirstValues;
import com.darkona.dropletsofthirst.api.event.DehydrationDamageEvent;
import com.darkona.dropletsofthirst.api.event.DrinkEvent;
import com.darkona.dropletsofthirst.api.event.EatEvent;
import com.darkona.dropletsofthirst.api.event.OverhydrationEvent;
import com.darkona.dropletsofthirst.api.event.ThirstChangeEvent;
import com.darkona.dropletsofthirst.data.DropletsTags;
import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import com.darkona.dropletsofthirst.foundation.config.GameplayConfig;
import com.darkona.dropletsofthirst.content.registry.AttributeInit;
import com.darkona.dropletsofthirst.content.registry.EffectInit;
import com.darkona.dropletsofthirst.foundation.common.capability.IThirst;
import com.darkona.dropletsofthirst.foundation.common.capability.ModAttachment;
import com.darkona.dropletsofthirst.foundation.common.damagesource.ModDamageSource;
import com.darkona.dropletsofthirst.foundation.network.message.PlayerThirstSyncMessage;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.network.PacketDistributor;
import dev.darkona.jdlib.core.stat.FractionalCarry;
import dev.darkona.jdlib.core.stat.ShownFields;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import static com.darkona.dropletsofthirst.core.ThirstConstants.*;

public class PlayerThirst implements IThirst, DropletsView, INBTSerializable<CompoundTag>
{
    public static final int SYNC_ENABLED = 1;
    public static final int SYNC_SPRINT_BLOCKED = 2;
    public static final int SYNC_BOTH_HANDS = 4;
    public static final int SYNC_HAND_DRINKING = 8;
    public static final int SYNC_SPRINT_MIN_SHIFT = 8;
    private static final double MAX_STEP = 10.0;
    private static final int UNCHANGED = 0;
    private static final int CHANGED = 1;
    private static final int CANCELLED = 2;

    int thirst = MAX_THIRST;
    int quenched = RESPAWN_QUENCHED;
    float exhaustion = 0;
    int damageTimer = 0;
    float prevTickExhaustion = 0.0F;
    boolean justHealed = false;
    boolean shouldTickThirst = true;
    boolean exhaustionRecalculate = false;
    float pendingActivity;
    float pendingRegen;
    double lastX;
    double lastY;
    double lastZ;
    boolean hasLastPosition;
    float exhaustionModifier = 1.0F;
    boolean modifierDirty = true;
    boolean effectsDirty = true;
    boolean pausedByEffect;
    boolean stoppedByEffect;
    boolean nauseous;
    private enum Shown { THIRST, QUENCHED, EXHAUSTION_STEP, RULES }
    private final ShownFields<Shown> shown = new ShownFields<>(Shown.class);
    int syncedRules = SYNC_SPRINT_BLOCKED | SYNC_BOTH_HANDS | SPRINT_MIN_THIRST << SYNC_SPRINT_MIN_SHIFT;
    int handDrinkReadyTick = 0;
    int fullHydrationTicks;
    float overflow;
    private final FractionalCarry bloodThirst = new FractionalCarry();
    private final FractionalCarry bloodQuenched = new FractionalCarry();

    public PlayerThirst() {}

    private void readThirstEffects(Player player)
    {
        boolean paused = false;
        boolean stopped = false;
        for (MobEffectInstance effect : player.getActiveEffects())
        {
            Holder<MobEffect> holder = effect.getEffect();
            stopped |= holder.is(DropletsTags.STOPS_THIRST);
            paused |= holder.is(DropletsTags.PAUSES_THIRST);
        }
        pausedByEffect = paused;
        stoppedByEffect = stopped;
        nauseous = player.hasEffect(MobEffects.CONFUSION);
    }

    public int getThirst()
    {
        return thirst;
    }

    public void setThirst(int value)
    {
        thirst = Mth.clamp(value, 0, MAX_THIRST);
        quenched = Math.min(quenched, thirst);
    }

    public int getQuenched()
    {
        return quenched;
    }

    public void setQuenched(int value)
    {
        quenched = Mth.clamp(value, 0, thirst);
    }

    public float getExhaustion()
    {
        return exhaustion;
    }

    public void setExhaustion(float value)
    {
        exhaustion = value;
    }

    @Override
    public int thirst()
    {
        return thirst;
    }

    @Override
    public int quenched()
    {
        return quenched;
    }

    @Override
    public float exhaustion()
    {
        return exhaustion;
    }

    @Override
    public boolean isEnabled()
    {
        return shouldTickThirst;
    }

    @Override
    public float lastModifier()
    {
        return exhaustionModifier;
    }

    public boolean change(Player player, int newThirst, int newQuenched, ThirstChangeEvent.Cause cause)
    {
        return apply(player, newThirst, newQuenched, cause) == CHANGED;
    }

    private int apply(Player player, int newThirst, int newQuenched, ThirstChangeEvent.Cause cause)
    {
        newThirst = Mth.clamp(newThirst, 0, MAX_THIRST);
        newQuenched = Mth.clamp(newQuenched, 0, newThirst);
        if (newThirst == thirst && newQuenched == quenched)
            return UNCHANGED;
        ThirstChangeEvent.Pre pre = NeoForge.EVENT_BUS.post(new ThirstChangeEvent.Pre(player, cause, thirst, quenched, newThirst, newQuenched));
        if (pre.isCanceled())
            return CANCELLED;
        newThirst = Mth.clamp(pre.getNewThirst(), 0, MAX_THIRST);
        newQuenched = Mth.clamp(pre.getNewQuenched(), 0, newThirst);
        if (newThirst == thirst && newQuenched == quenched)
            return UNCHANGED;
        int oldThirst = thirst;
        int oldQuenched = quenched;
        thirst = newThirst;
        quenched = newQuenched;
        NeoForge.EVENT_BUS.post(new ThirstChangeEvent.Post(player, cause, oldThirst, oldQuenched, thirst, quenched));
        return CHANGED;
    }

    @Override
    public void setShouldTickThirst(boolean value){shouldTickThirst = value;}
    @Override
    public boolean getShouldTickThirst(){return shouldTickThirst;}

    public static void consume(ItemStack item, Player player)
    {
        ThirstValues values = ThirstHelper.drinkValuesOf(item);
        if (values == null || !ThirstHelper.playerRestoresThirst(item, player))
            return;
        if (item.getUseAnimation() == UseAnim.DRINK || WaterPurity.isWaterFilledContainer(item))
            drink(player, item, values.thirst(), values.quenched(), WaterPurity.drinkPurity(item));
        else
            eat(player, item, values.thirst(), values.quenched());
    }

    public static boolean drinkBlood(Player player, ItemStack source, float thirst, float quenched)
    {
        if (player.level().isClientSide)
            return false;
        PlayerThirst data = player.getData(ModAttachment.PLAYER_THIRST);
        int wholeThirst = data.bloodThirst.add(thirst);
        int wholeQuenched = data.bloodQuenched.add(quenched);
        return (wholeThirst != 0 || wholeQuenched != 0) && drink(player, source, wholeThirst, wholeQuenched, DropletsAPI.NO_PURITY, false);
    }

    public static boolean drinkWater(Player player, int thirst, int quenched, int purity)
    {
        return drink(player, ItemStack.EMPTY, thirst + WaterPurity.waterThirstBonus(purity), quenched + WaterPurity.waterQuenchedBonus(purity), purity);
    }

    public static boolean eat(Player player, ItemStack item, int thirst, int quenched)
    {
        if (player.level().isClientSide || !VampireThirst.canHydrate(item, player))
            return false;
        EatEvent.Pre pre = NeoForge.EVENT_BUS.post(new EatEvent.Pre(player, item, thirst, quenched));
        if (pre.isCanceled())
            return false;
        boolean hydrated = player.getData(ModAttachment.PLAYER_THIRST).hydrate(player, pre.getThirst(), pre.getQuenched(), true, ThirstChangeEvent.Cause.EAT);
        NeoForge.EVENT_BUS.post(new EatEvent.Post(player, item, pre.getThirst(), pre.getQuenched(), hydrated));
        return hydrated;
    }

    public static boolean drink(Player player, ItemStack item, int thirst, int quenched, int purity)
    {
        return VampireThirst.canHydrate(item, player) && drink(player, item, thirst, quenched, purity, true);
    }

    private static boolean drink(Player player, ItemStack item, int thirst, int quenched, int purity, boolean overflows)
    {
        if (player.level().isClientSide)
            return false;
        if (!WaterPurity.enabled())
            purity = DropletsAPI.NO_PURITY;
        DrinkEvent.Pre pre = NeoForge.EVENT_BUS.post(new DrinkEvent.Pre(player, item, thirst, quenched, purity));
        if (pre.isCanceled())
            return false;
        boolean hydrated = WaterPurity.givePurityEffects(player, pre.getPurity())
                && player.getData(ModAttachment.PLAYER_THIRST).hydrate(player, pre.getThirst(), pre.getQuenched(), overflows, ThirstChangeEvent.Cause.DRINK);
        NeoForge.EVENT_BUS.post(new DrinkEvent.Post(player, item, pre.getThirst(), pre.getQuenched(), pre.getPurity(), hydrated));
        return hydrated;
    }

    public boolean hydrate(Player player, int thirst, int quenched, boolean overflows, ThirstChangeEvent.Cause cause)
    {
        int extra = GameplayConfig.EXTRA_THIRST_TO_QUENCHED.get() ? Math.max(this.thirst + thirst - MAX_THIRST, 0) : 0;
        return absorb(player, thirst, quenched, this.thirst + thirst, this.quenched + quenched + extra, overflows, cause);
    }

    private boolean absorb(Player player, int thirst, int quenched, int newThirst, int newQuenched, boolean overflows, ThirstChangeEvent.Cause cause)
    {
        int wasted = 0;
        if (overflows && thirst >= 0 && quenched >= 0 && GameplayConfig.OVERHYDRATION.get() && shouldTickThirst && !player.getAbilities().invulnerable)
        {
            int fitThirst = Mth.clamp(newThirst, 0, MAX_THIRST);
            wasted = thirst + quenched - (fitThirst - this.thirst) - (Mth.clamp(newQuenched, 0, fitThirst) - this.quenched);
        }
        int result = apply(player, newThirst, newQuenched, cause);
        if (wasted > 0 && result != CANCELLED)
            overflow(player, wasted);
        return result == CHANGED;
    }

    private void overflow(Player player, int wasted)
    {
        overflow += wasted;
        int threshold = GameplayConfig.OVERHYDRATION_THRESHOLD.get();
        if (overflow < threshold)
            return;
        MobEffectInstance current = player.getEffect(EffectInit.OVERHYDRATED);
        int amplifier = Math.min(2, Math.max((int) ((overflow - threshold) * 2 / threshold), current == null ? 0 : current.getAmplifier() + 1));
        OverhydrationEvent event = NeoForge.EVENT_BUS.post(new OverhydrationEvent(player, overflow, GameplayConfig.OVERHYDRATION_DURATION_TICKS.get(), amplifier));
        overflow = 0;
        if (event.isCanceled())
            return;
        player.addEffect(new MobEffectInstance(EffectInit.OVERHYDRATED, event.getDuration(), event.getAmplifier()));
        if (GameplayConfig.OVERHYDRATION_NAUSEA.get())
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, OVERHYDRATION_NAUSEA_TICKS));
    }

    public void tick(Player player)
    {
        if(player.getAbilities().invulnerable || !shouldTickThirst)
            return;

        boolean interval = (player.tickCount + player.getId()) % MODIFIER_INTERVAL_TICKS == 0;
        if(interval || effectsDirty)
        {
            effectsDirty = false;
            readThirstEffects(player);
        }
        if(stoppedByEffect)
            return;

        if(interval)
        {
            modifierDirty = true;
            if(GameplayConfig.FULL_HYDRATION_BONUS.get())
                fullHydrationBonus(player);
            if(overflow > 0)
                overflow = Math.max(0.0F, overflow - GameplayConfig.OVERHYDRATION_DECAY.get().floatValue() * MODIFIER_INTERVAL_TICKS / 20.0F);
        }

        Difficulty difficulty = player.level().getDifficulty();
        boolean paused = pausedByEffect;

        if(nauseous && GameplayConfig.DEPLETES_WHEN_NAUSEOUS.get())
            exhaustion += GameplayConfig.NAUSEA_PER_TICK.get().floatValue() * exhaustionModifier(player);

        float activity;
        if(GameplayConfig.MODE.get() == GameplayConfig.Mode.OWN)
        {
            activity = pendingActivity + movementExhaustion(player);
            pendingActivity = 0;
        }
        else
        {
            activity = mirroredFoodExhaustion(player);
            MobEffectInstance hunger = player.getEffect(MobEffects.HUNGER);
            if(hunger != null)
                activity -= HUNGER_EXHAUSTION_PER_LEVEL * (float)(hunger.getAmplifier() + 1);
        }
        activity += GameplayConfig.BASAL_PER_TICK.get().floatValue();

        float scale = paused ? 0.0F : 1.0F;
        if(player.isPassenger())
            scale *= GameplayConfig.RIDING_MULTIPLIER.get().floatValue();
        if(player.isSleeping())
            scale *= GameplayConfig.SLEEPING_MULTIPLIER.get().floatValue();
        addExhaustion(player, activity * scale);

        if(pendingRegen > 0)
        {
            if(GameplayConfig.REGEN_DEPLETES_THIRST.get())
                exhaustion += pendingRegen * scale * (GameplayConfig.REGEN_CLIMATE_DEPENDENT.get() ? exhaustionModifier(player) : 1.0F);
            pendingRegen = 0;
        }

        float perPoint = GameplayConfig.EXHAUSTION_PER_POINT.get().floatValue();
        if (exhaustion > perPoint)
        {
            exhaustion -= perPoint;
            if (quenched > 0)
                change(player, thirst, quenched - 1, ThirstChangeEvent.Cause.DEPLETION);
            else if (difficulty != Difficulty.PEACEFUL || GameplayConfig.DEPLETES_IN_PEACEFUL.get())
                change(player, thirst - 1, quenched, ThirstChangeEvent.Cause.DEPLETION);
        }

        if(difficulty == Difficulty.PEACEFUL && !GameplayConfig.DEPLETES_IN_PEACEFUL.get() && player.tickCount % GameplayConfig.PEACEFUL_REGEN_INTERVAL_TICKS.get() == 0)
            change(player, thirst + GameplayConfig.PEACEFUL_REGEN_AMOUNT.get(), quenched, ThirstChangeEvent.Cause.PEACEFUL);

        if(GameplayConfig.RAIN_DRINKING.get() && player.tickCount % GameplayConfig.RAIN_INTERVAL_TICKS.get() == 0
                && Mth.wrapDegrees(player.getXRot()) <= GameplayConfig.RAIN_MAX_PITCH.get() && player.level().isRainingAt(player.blockPosition().above())
                && !VampireThirst.isVampire(player))
        {
            int rainThirst = GameplayConfig.RAIN_THIRST.get();
            int rainQuenched = GameplayConfig.RAIN_QUENCHED.get();
            absorb(player, rainThirst, rainQuenched, thirst + rainThirst, quenched + rainQuenched, true, ThirstChangeEvent.Cause.RAIN);
        }

        if (thirst <= 0)
        {
            ++damageTimer;
            if (damageTimer >= GameplayConfig.DAMAGE_INTERVAL_TICKS.get())
            {
                float damage = GameplayConfig.DAMAGE_AMOUNT.get().floatValue();
                float health = player.getHealth();
                if (health > minHealth(difficulty) && (GameplayConfig.DAMAGE_CAN_KILL.get() || health > damage))
                {
                    DehydrationDamageEvent event = NeoForge.EVENT_BUS.post(new DehydrationDamageEvent(player, damage));
                    if (!event.isCanceled() && event.getAmount() > 0)
                        player.hurt(ModDamageSource.getDamageSource(player.level(),ModDamageSource.DIE_OF_THIRST_KEY), event.getAmount());
                }

                damageTimer = 0;
            }
        }
        else
            damageTimer = 0;
    }

    private void fullHydrationBonus(Player player)
    {
        if(thirst < MAX_THIRST || quenched < GameplayConfig.FULL_HYDRATION_MIN_QUENCHED.get())
        {
            fullHydrationTicks = 0;
            return;
        }
        if(fullHydrationTicks < GameplayConfig.FULL_HYDRATION_SECONDS.get() * 20)
        {
            fullHydrationTicks += MODIFIER_INTERVAL_TICKS;
            return;
        }
        MobEffectInstance current = player.getEffect(EffectInit.HYDRATED);
        if(current == null || current.endsWithin(MODIFIER_INTERVAL_TICKS))
            player.addEffect(new MobEffectInstance(EffectInit.HYDRATED, GameplayConfig.FULL_HYDRATION_DURATION_TICKS.get(), 0, true, true));
    }

    private static float minHealth(Difficulty difficulty)
    {
        return (switch (difficulty)
        {
            case PEACEFUL, EASY -> GameplayConfig.DAMAGE_MIN_HEALTH_EASY;
            case NORMAL -> GameplayConfig.DAMAGE_MIN_HEALTH_NORMAL;
            case HARD -> GameplayConfig.DAMAGE_MIN_HEALTH_HARD;
        }).get().floatValue();
    }

    private float mirroredFoodExhaustion(Player player)
    {
        float hungerExhaustion = player.getFoodData().getExhaustionLevel();
        float deltaExhaustion;
        if (hungerExhaustion >= this.prevTickExhaustion)
            deltaExhaustion = hungerExhaustion - this.prevTickExhaustion;
        else if (exhaustionRecalculate)
            deltaExhaustion = hungerExhaustion + 4.0F - this.prevTickExhaustion;
        else
            deltaExhaustion = hungerExhaustion;
        exhaustionRecalculate = false;
        this.prevTickExhaustion = hungerExhaustion;
        return deltaExhaustion;
    }

    private float movementExhaustion(Player player)
    {
        double dx = player.getX() - lastX;
        double dy = player.getY() - lastY;
        double dz = player.getZ() - lastZ;
        boolean first = !hasLastPosition;
        lastX = player.getX();
        lastY = player.getY();
        lastZ = player.getZ();
        hasLastPosition = true;

        double horizontal = dx * dx + dz * dz;
        double total = horizontal + dy * dy;
        if(first || total > MAX_STEP * MAX_STEP || player.isPassenger())
            return 0.0F;
        if(player.isSwimming() || player.isEyeInFluid(FluidTags.WATER))
            return (float) (Math.sqrt(total) * GameplayConfig.SWIM_PER_METER.get());
        if(player.isInWater())
            return (float) (Math.sqrt(horizontal) * GameplayConfig.SWIM_PER_METER.get());
        if(player.onGround() && player.isSprinting())
            return (float) (Math.sqrt(horizontal) * GameplayConfig.SPRINT_PER_METER.get());
        return 0.0F;
    }

    public void addActivity(Player player, float amount)
    {
        if(!player.getAbilities().invulnerable && shouldTickThirst)
            pendingActivity += amount;
    }

    public void onFoodHeal(float amount)
    {
        if(GameplayConfig.MODE.get() == GameplayConfig.Mode.OWN)
            pendingRegen += amount * GameplayConfig.HEAL_PER_HEALTH.get().floatValue();
        else
            justHealed = true;
    }

    public void updateThirstData(Player player)
    {
        shown.resendNext();
    }

    public void syncIfChanged(ServerPlayer player)
    {
        int flags = (shouldTickThirst ? SYNC_ENABLED : 0)
                | (GameplayConfig.SPRINT_BLOCKED_WHEN_THIRSTY.get() ? SYNC_SPRINT_BLOCKED : 0)
                | (GameplayConfig.HAND_DRINKING_BOTH_HANDS.get() ? SYNC_BOTH_HANDS : 0)
                | (GameplayConfig.HAND_DRINKING.get() ? SYNC_HAND_DRINKING : 0)
                | GameplayConfig.SPRINT_MIN_THIRST.get() << SYNC_SPRINT_MIN_SHIFT;
        if(player instanceof FakePlayer || player.connection == null)
            return;
        shown.show(Shown.THIRST, thirst);
        shown.show(Shown.QUENCHED, quenched);
        shown.show(Shown.EXHAUSTION_STEP, (int) (exhaustion * EXHAUSTION_SYNC_STEPS));
        shown.show(Shown.RULES, flags);
        if(!shown.shouldSend())
            return;
        PacketDistributor.sendToPlayer(player, new PlayerThirstSyncMessage(thirst, quenched, exhaustion, flags));
    }

    public void setSyncedRules(int flags)
    {
        syncedRules = flags;
        shouldTickThirst = (flags & SYNC_ENABLED) != 0;
    }

    public boolean isSprintBlocked()
    {
        return (syncedRules & SYNC_SPRINT_BLOCKED) != 0;
    }

    public int sprintMinThirst()
    {
        return syncedRules >>> SYNC_SPRINT_MIN_SHIFT & 0xFF;
    }

    public boolean needsBothHandsToDrink()
    {
        return (syncedRules & SYNC_BOTH_HANDS) != 0;
    }

    public boolean handDrinkingAllowed()
    {
        return (syncedRules & SYNC_HAND_DRINKING) != 0;
    }

    public boolean canDrinkByHand(int serverTick)
    {
        return serverTick >= handDrinkReadyTick;
    }

    public void startHandDrinkCooldown(int serverTick, int cooldownTicks)
    {
        handDrinkReadyTick = serverTick + cooldownTicks;
    }

    @Override
    public void setJustHealed()
    {
        justHealed = true;
    }

    @Override
    public void ExhaustionRecalculate(){exhaustionRecalculate = true;}

    public void addExhaustion(Player player, float amount)
    {
        if(!GameplayConfig.REGEN_DEPLETES_THIRST.get() && justHealed)
            amount = 0;

        if(!GameplayConfig.REGEN_CLIMATE_DEPENDENT.get() && justHealed)
            exhaustion += amount;
        else
            exhaustion += amount * exhaustionModifier(player);

        if(justHealed)
            justHealed = false;
    }

    public void addScaledExhaustion(Player player, float amount)
    {
        if(!player.getAbilities().invulnerable && shouldTickThirst)
            exhaustion = Math.max(0.0F, exhaustion + amount * exhaustionModifier(player));
    }

    public float exhaustionModifier(Player player)
    {
        if(modifierDirty)
        {
            modifierDirty = false;
            exhaustionModifier = ExhaustionFactors.compute(player, null);
        }
        return exhaustionModifier * (float) player.getAttributeValue(AttributeInit.THIRST_DRAIN);
    }

    public void invalidateModifier()
    {
        modifierDirty = true;
        effectsDirty = true;
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("thirst", thirst);
        nbt.putInt("quenched", quenched);
        nbt.putFloat("exhaustion", exhaustion);
        nbt.putBoolean("enable",shouldTickThirst);
        nbt.putFloat("overflow", overflow);
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, CompoundTag nbt) {
        setThirst(nbt.getInt("thirst"));
        setQuenched(nbt.getInt("quenched"));
        exhaustion = nbt.getFloat("exhaustion");
        shouldTickThirst = !nbt.contains("enable") || nbt.getBoolean("enable");
        overflow = nbt.getFloat("overflow");
    }
}
