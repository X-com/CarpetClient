package carpetclient.mixins;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Class to add recipes such as rockets to the crafting recipe book when server sends the recipes unlock.
 */
@Mixin(CraftingManager.class)
public class MixinCraftingManager {

    private static IRecipe rocketOne;
    private static IRecipe rocketTwo;
    private static IRecipe rocketThree;

    /**
     * Injection to the init method to register the rocket recipes before the any other recipes are added. Critical to not add them before other recipes as there is a bug with rockets if added after the Firework recipes are added.
     */
    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/crafting/CraftingManager;register(Ljava/lang/String;Lnet/minecraft/item/crafting/IRecipe;)V", ordinal = 0, shift = At.Shift.BEFORE))
    private static void inectInit(CallbackInfoReturnable<Boolean> cir) {
        createRockets();
        CraftingManager.register("rocket1", rocketOne);
        CraftingManager.register("rocket2", rocketTwo);
        CraftingManager.register("rocket3", rocketThree);
    }

    /**
     * Class to generate a NonNullList or Ingredients based on Item stacks.
     */
    private static NonNullList<Ingredient> getIngridientList(Ingredient[] ingr) {
        NonNullList<Ingredient> nonnulllist = NonNullList.<Ingredient>create();

        for (int i = 0; i < ingr.length; ++i) {
            Ingredient ingredient = ingr[i];

            if (ingredient != Ingredient.EMPTY) {
                nonnulllist.add(ingredient);
            }
        }

        return nonnulllist;
    }

    /**
     * A method to generate the recipes that are going to get added.
     */
    private static void createRockets() {
        ItemStack[] resultItem = new ItemStack[3];

        Ingredient[] ingr1 = {Ingredient.fromStacks(new ItemStack(Items.PAPER, 1)), Ingredient.fromStacks(new ItemStack(Items.GUNPOWDER, 1))};
        Ingredient[] ingr2 = {Ingredient.fromStacks(new ItemStack(Items.PAPER, 1)), Ingredient.fromStacks(new ItemStack(Items.GUNPOWDER, 1)), Ingredient.fromStacks(new ItemStack(Items.GUNPOWDER, 1))};
        Ingredient[] ingr3 = {Ingredient.fromStacks(new ItemStack(Items.PAPER, 1)), Ingredient.fromStacks(new ItemStack(Items.GUNPOWDER, 1)), Ingredient.fromStacks(new ItemStack(Items.GUNPOWDER, 1)), Ingredient.fromStacks(new ItemStack(Items.GUNPOWDER, 1))};

        for (int i = 0; i < 3; i++) {
            resultItem[i] = new ItemStack(Items.FIREWORKS, 3);
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            nbttagcompound1.setByte("Flight", (byte) (i + 1));
            NBTTagCompound nbttagcompound3 = new NBTTagCompound();
            nbttagcompound3.setTag("Fireworks", nbttagcompound1);
            resultItem[i].setTagCompound(nbttagcompound3);
        }

        rocketOne = new ShapelessRecipes("rocket", resultItem[0], getIngridientList(ingr1));
        rocketTwo = new ShapelessRecipes("rocket", resultItem[1], getIngridientList(ingr2));
        rocketThree = new ShapelessRecipes("rocket", resultItem[2], getIngridientList(ingr3));
    }
}
