package fr.openmc.core.features.corporation.manager;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import fr.openmc.core.bootstrap.integration.OMCLogger;
import fr.openmc.core.features.corporation.models.Shop;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ShopDatabaseManager {

	private static Dao<Shop, UUID> shopDao;
	
	public static void initDB(ConnectionSource connectionSource) throws SQLException {
		TableUtils.createTableIfNotExists(connectionSource, Shop.class);
		shopDao = DaoManager.createDao(connectionSource, Shop.class);
	}
	
	public static @NonNull Map<Location, Shop> loadDBShops() throws SQLException {
		Map<Location, Shop> shopsByLocation = new HashMap<>();
		
		List<Shop> shops = shopDao.queryForAll();
		for (Shop shop : shops) {
			Location loc = new Location(Bukkit.getWorld("world"), shop.getX(), shop.getY(), shop.getZ());
			if (shop.getMultiblock() == null) {
				if (!shop.setMultiblock(new Shop.Multiblock(loc, loc.clone().add(0, 1, 0)))) {
					OMCLogger.error("Cannot set multiblock for {}, but shop is registered", shop.getName());
				}
			}
			shopsByLocation.put(loc, shop);
		}
		return shopsByLocation;
	}
	
	public static @Nullable Shop loadShopFor(UUID ownerUUID) {
		try {
			return shopDao.queryForId(ownerUUID);
		} catch (SQLException e) {
			OMCLogger.error("Failed to load shop for owner UUID: {}\nCause: {}", ownerUUID, e.getCause());
			return null;
		}
	}

	public static boolean saveShop(Shop shop) {
		try {
			shopDao.createOrUpdate(shop);
			return true;
		} catch (SQLException e) {
			OMCLogger.error("Failed to save shop for owner UUID: {}\nCause: {}", shop.getOwnerUUID(), e.getCause());
			return false;
		}
	}
	
	public static boolean deleteShop(Shop shop) {
		try {
			shopDao.delete(shop);
			return true;
		} catch (SQLException e) {
			OMCLogger.error("Failed to delete shop for owner UUID: {}\nCause: {}", shop.getOwnerUUID(), e.getCause());
			return false;
		}
	}
}
