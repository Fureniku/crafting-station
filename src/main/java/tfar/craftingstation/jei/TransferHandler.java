package tfar.craftingstation.jei;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import tfar.craftingstation.CraftingStation;
import tfar.craftingstation.CraftingStationMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransferHandler implements IRecipeTransferInfo {

    @Override
    public Class getContainerClass() {
        return CraftingStationMenu.class;
    }

    @Override
    public Optional<MenuType> getMenuType() {
        return Optional.empty();
    }

    @Override
    public RecipeType getRecipeType() {
        return RecipeTypes.CRAFTING;
    }

    @Override
    public boolean canHandle(AbstractContainerMenu container, Object recipe) {
        return true;
    }

    @Override
    public List<Slot> getRecipeSlots(AbstractContainerMenu container, Object recipe) {
        List<Slot> slots = new ArrayList<>();
        for (int i = 1; i < 1 + 9; i++) {
            Slot slot = container.getSlot(i);
            slots.add(slot);
        }
        return slots;
    }

    @Override
    public List<Slot> getInventorySlots(AbstractContainerMenu container, Object recipe) {
        if (container instanceof CraftingStationMenu) {
            CraftingStationMenu menu = (CraftingStationMenu) container;
            List<Slot> slots = new ArrayList<>();
            for (int i = 10; i < 10 + menu.subContainerSize + 36; i++) {
                Slot slot = container.getSlot(i);
                if (slot.hasItem()) {
                    slots.add(slot);
                }
            }
            return slots;
        } else {
            CraftingStation.LOGGER.error("Trying to get recipe slots on an object that isn't a crafting station.");
            return null;
        }

    }
}
