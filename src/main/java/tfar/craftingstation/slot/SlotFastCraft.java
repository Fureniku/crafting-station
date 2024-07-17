package tfar.craftingstation.slot;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import tfar.craftingstation.CraftingInventoryPersistant;
import tfar.craftingstation.CraftingStationMenu;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;

/**
 * SlotCraftingSucks from FastWorkbench adapted for the Crafting Station container
 * See: https://github.com/Shadows-of-Fire/FastWorkbench/blob/master/src/main/java/shadows/fastbench/gui/SlotCraftingSucks.java
 * <p>
 * Basically it makes crafting less laggy.
 * There is one minor change to allow crafting station to prioritise inventory slots first.
 * -
 */
public class SlotFastCraft extends ResultSlot {
  private final Inventory playerInventory;
  private final NonNullList<Slot> sideSlots;
  private final CraftingStationMenu container;
  protected CraftingInventoryPersistant craftingInventoryPersistant;

  public SlotFastCraft(CraftingStationMenu container, CraftingInventoryPersistant craftingInventoryPersistant, Container resultInventory, int slotIndex, int xPosition, int yPosition, Player player, NonNullList<Slot> slots) {
    super(player, craftingInventoryPersistant, resultInventory, slotIndex, xPosition, yPosition);
    this.playerInventory = player.getInventory();
    this.sideSlots = slots;
    this.container = container;
    this.craftingInventoryPersistant = craftingInventoryPersistant;
  }

  @Override
  public ItemStack remove(int amount) {
    if (this.hasItem()) {
      this.removeCount += Math.min(amount, getItem().getCount());
    }
    return getItem();
  }

  /**
   * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood.
   */
  @Override
  protected void checkTakeAchievements(ItemStack stack) {
    if (this.removeCount > 0) {
      stack.onCraftedBy(this.player.level(), this.player, this.removeCount);
      net.minecraftforge.event.ForgeEventFactory.firePlayerCraftingEvent(this.player, stack, craftSlots);
    }

    this.removeCount = 0;
  }

  @Override
  public void onTake(Player thePlayer, ItemStack craftingResult) {
    this.checkTakeAchievements(craftingResult);
    net.minecraftforge.common.ForgeHooks.setCraftingPlayer(thePlayer);

    NonNullList<ItemStack> nonnulllist = container.getRemainingItems();

    net.minecraftforge.common.ForgeHooks.setCraftingPlayer(null);

    // note: craftMatrixPersistent and this.craftSlots are the same object!
    craftingInventoryPersistant.setDoNotCallUpdates(true);

    for (int i = 0; i < nonnulllist.size(); ++i) {
      ItemStack stackInSlot = this.craftSlots.getItem(i);
      ItemStack stack1 = nonnulllist.get(i);

      if (!stackInSlot.isEmpty()) {
        if (container.tileEntity.getUseConnectedResources()) {
          int sideInvMatchingIndex = -1;
          for (int j = 10; j < sideSlots.size(); j++) {
            if (sideSlots.get(j).hasItem() && ItemStack.isSameItemSameTags(sideSlots.get(j).getItem(), stackInSlot)) {
              sideInvMatchingIndex = j;
              break;
            }
          }
          int playerInvMatchingIndex = playerInventory.findSlotMatchingItem(stackInSlot);

          if(stackInSlot.getCount() > 1) {
            this.craftSlots.removeItem(i, 1);
          } else if(playerInvMatchingIndex > 0) {
            this.playerInventory.removeItem(playerInvMatchingIndex, 1);
          } else if(sideInvMatchingIndex > 0) {
            this.sideSlots.get(sideInvMatchingIndex).getItem().shrink(1);
            this.sideSlots.get(sideInvMatchingIndex).container.setChanged();
          } else {
            this.craftSlots.removeItem(i, 1);
          }
        } else {
          this.craftSlots.removeItem(i, 1);
        }
        stackInSlot = this.craftSlots.getItem(i);
      }

      if (!stack1.isEmpty()) {
        if (stackInSlot.isEmpty()) {
          this.craftSlots.setItem(i, stack1);
        } else if (ItemStack.isSameItemSameTags(stackInSlot,stack1)) {
          stack1.grow(stackInSlot.getCount());
          this.craftSlots.setItem(i, stack1);
        } else if (!this.player.getInventory().add(stack1)) {
          this.player.drop(stack1, false);
        }
      }
    }

    craftingInventoryPersistant.setDoNotCallUpdates(false);
    container.slotsChanged(craftingInventoryPersistant);
    container.tileEntity.setChanged();
    //return craftingResult;
  }
}
