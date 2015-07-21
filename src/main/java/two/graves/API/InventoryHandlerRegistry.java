/*
 *  (c) Two aka Stefan Feldbinder
 */
package two.graves.API;

import cpw.mods.fml.common.FMLLog;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.Level;

/**
 *
 * @author Two
 */
public class InventoryHandlerRegistry {

  /* Internal collection of registered handlers. */
  protected static final ConcurrentHashMap<String, IInventoryHandler> inventoryHandlers = new ConcurrentHashMap<String, IInventoryHandler>();

  /**
   * Register a new IInventoryHandler.
   *
   * @param handler An initialized IInventoryHandler that will receive all requests for inventory insertion for its handlerID.
   * @return true if the handler was added, false if that handler was already registered.
   */
  public static boolean registerHandler(final IInventoryHandler handler) {
    if ((handler.getID() == null) || handler.getID().isEmpty()) {
      throw new IllegalArgumentException("HandlerID cannot be null or empty");
    }
    if (handler == null) {
      throw new IllegalArgumentException("Handler cannot be null");
    }

    if (inventoryHandlers.putIfAbsent(handler.getID(), handler) == null) {
      FMLLog.log("TwoGraves", Level.INFO, "Added Grave handler for %s", handler.getID());
      return true;
    }
    return false;
  }

  /**
   * Returns a list of all registered handlers.
   *
   * @return A list of all registered handlers.
   */
  public static Collection<IInventoryHandler> getAllHandlers() {
    return inventoryHandlers.values();
  }

  /**
   * Returns the handler registered for the given ID or null.
   *
   * @param handlerID the ID to query.
   * @return The handler registered with the given ID or null.
   */
  public static IInventoryHandler getHandlerForID(final String handlerID) {
    return inventoryHandlers.get(handlerID);
  }

  /**
   * Returns true if a handler with a given ID is registered, false otherwise.
   *
   * @param handlerID the ID to query.
   * @return True if a handler with a given ID is registered, false otherwise.
   */
  public static boolean isHandlerRegistered(final String handlerID) {
    return inventoryHandlers.containsKey(handlerID);
  }

  /* For reference. This ID is intentionally blank to minimize network packet size. */
  public static final String INVENTORY_HANDLER_ID_VANILLA = "";

  static {
    /**
     * Don't do this! Use InventoryHandlerRegistry.registerHandler instead.
     * This is done here to bypass usual handlerID checks for the special vanilla handler.
     */
    InventoryHandlerRegistry.inventoryHandlers.put(INVENTORY_HANDLER_ID_VANILLA, new InventoryHandlerVanilla());
  }
}
