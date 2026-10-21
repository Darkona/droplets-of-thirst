package com.darkona.dropletsofthirst.gametest;

import com.darkona.dropletsofthirst.content.thirst.PlayerThirst;
import com.darkona.dropletsofthirst.foundation.common.capability.ModAttachment;
import net.minecraft.gametest.framework.GameTestHelper;
import dev.darkona.jdlib.testkit.TestPlayers;
import net.minecraft.server.level.ServerPlayer;

final class TestSupport
{
    private TestSupport() {}

    static ServerPlayer player(GameTestHelper helper)
    {
        return TestPlayers.survival(helper, "droplets-test");
    }

    static PlayerThirst thirst(ServerPlayer player)
    {
        return player.getData(ModAttachment.PLAYER_THIRST);
    }
}
