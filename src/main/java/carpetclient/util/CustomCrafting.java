package carpetclient.util;

import carpetclient.mixinInterface.AMixinRegistryNamespaced;
import carpetclient.mixins.IMixinCraftingManager;
import carpetclient.mixins.IMixinRecipeBookClient;
import carpetclient.pluginchannel.CarpetPluginChannel;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.netty.buffer.Unpooled;
import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.client.util.RecipeBookClient;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JsonUtils;

import java.io.IOException;

/**
 * Recipe bridge class for Carpet servers synching custom recipes with carpet servers.
 */
public class CustomCrafting {

    /**
     * Main custom recipe method created to recieve custom recipes from carpet servers.
     *
     * @param data
     */
    public static void addCustomRecipes(PacketBuffer data) {
        NBTTagCompound nbt;
        try {
            nbt = data.readCompoundTag();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        resetCraftingRecipes();

        NBTTagList nbttaglist = nbt.getTagList("recipeList", 10);
        for (int i = 0; i < nbttaglist.tagCount(); i++) {
            NBTTagCompound ruleNBT = (NBTTagCompound) nbttaglist.get(i);

            String name = ruleNBT.getString("name");
            String recipe = ruleNBT.getString("recipe");
            JsonObject json = (new JsonParser()).parse(recipe).getAsJsonObject();
            try {
                CraftingManager.register(name, parseRecipeJson(json));
            } catch (Exception e) {
                System.out.println("something went wrong");
                System.out.println(e);
                return;
            }
        }
        resetRecipeBook();
        sendConfirmationPacketThatUpdatesCanBeReceived();
    }

    /**
     * Confirmation method that recipes where recieved requesting an update from the server.
     * Packets can't be sent at the same time or they will create issues in the packet reader, this system is in place to create artificial delay.
     */
    private static void sendConfirmationPacketThatUpdatesCanBeReceived() {
        PacketBuffer sender = new PacketBuffer(Unpooled.buffer());
        sender.writeInt(CarpetPluginChannel.CUSTOM_RECIPES);
        CarpetPluginChannel.packatSender(sender);
    }

    /**
     * Resets recipe book to vanilla as it might be cluttered with custom recipes.
     */
    public static void resetCraftingRecipes() {
        ((AMixinRegistryNamespaced) CraftingManager.REGISTRY).clear();
        IMixinCraftingManager.setNextAvailableId(0);
        if (!CraftingManager.init()) {
            return;
        }
        resetRecipeBook();
    }

    /**
     * Private reset method for reseting to vanilla recipes.
     */
    private static void resetRecipeBook() {
        RecipeBookClient.ALL_RECIPES.clear();
        RecipeBookClient.RECIPES_BY_TAB.clear();

        Table<CreativeTabs, String, RecipeList> table = HashBasedTable.<CreativeTabs, String, RecipeList>create();

        for (IRecipe irecipe : CraftingManager.REGISTRY) {
            if (!irecipe.isDynamic()) {
                CreativeTabs creativetabs = IMixinRecipeBookClient.callGetItemStackTab(irecipe.getRecipeOutput());
                String s = irecipe.getGroup();
                RecipeList recipelist1;

                if (s.isEmpty()) {
                    recipelist1 = IMixinRecipeBookClient.callNewRecipeList(creativetabs);
                } else {
                    recipelist1 = table.get(creativetabs, s);

                    if (recipelist1 == null) {
                        recipelist1 = IMixinRecipeBookClient.callNewRecipeList(creativetabs);
                        table.put(creativetabs, s, recipelist1);
                    }
                }

                recipelist1.add(irecipe);
            }
        }
    }

    /**
     * Helper method grabed from recipe parser.
     *
     * @param json
     * @return
     */
    private static IRecipe parseRecipeJson(JsonObject json) {
        String s = JsonUtils.getString(json, "type");

        if ("crafting_shaped".equals(s)) {
            return ShapedRecipes.deserialize(json);
        } else if ("crafting_shapeless".equals(s)) {
            return ShapelessRecipes.deserialize(json);
        } else {
            throw new JsonSyntaxException("Invalid or unsupported recipe type '" + s + "'");
        }
    }
}

