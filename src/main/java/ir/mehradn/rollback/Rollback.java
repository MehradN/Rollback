package ir.mehradn.rollback;

import ir.mehradn.rollback.event.CommandHandler;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Rollback implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("rollback");

	@Override
	public void onInitialize() {
		CommandHandler.register();
	}
}