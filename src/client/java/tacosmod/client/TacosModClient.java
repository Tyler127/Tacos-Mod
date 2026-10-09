package tacosmod.client;

import net.fabricmc.api.ClientModInitializer;
import tacosmod.client.tooltip.PotionTooltips;

public class TacosModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.

		// Register tooltips
		PotionTooltips.register();

	}
}
