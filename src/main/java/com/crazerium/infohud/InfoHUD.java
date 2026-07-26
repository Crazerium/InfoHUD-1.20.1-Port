package com.crazerium.infohud;

import com.crazerium.infohud.config.ClientConfig;
import com.crazerium.infohud.network.ModNetwork;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(InfoHUD.MOD_ID)
public final class InfoHUD {

    public static final String MOD_ID = "infohud";

    public InfoHUD() {
        ModLoadingContext.get().registerConfig(
                ModConfig.Type.CLIENT,
                ClientConfig.SPEC,
                "infohud-client.toml"
        );

        ModNetwork.register();
    }
}