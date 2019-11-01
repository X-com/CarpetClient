package carpetclient.util;

import carpetclient.mixins.AMixinRegistryNamespaced;
import carpetclient.mixins.IMixinRegistryNamespaced;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JsonUtils;

import java.io.IOException;

public class CustomCrafting {

    public static void addCustomRecipes(PacketBuffer data) {
        NBTTagCompound nbt;
        System.out.println("custom recipe packet reciped");
        try {
            nbt = data.readCompoundTag();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        System.out.println("------- " + ((IMixinRegistryNamespaced)CraftingManager.REGISTRY).getUnderlyingIntegerMap().size());
        ((AMixinRegistryNamespaced)CraftingManager.REGISTRY).clear();
        System.out.println("------- " + ((IMixinRegistryNamespaced)CraftingManager.REGISTRY).getUnderlyingIntegerMap().size());
//
        if (!CraftingManager.init()) {
            return;
        }
//        System.out.println("----   --- " + ((IMixinRegistryNamespaced)CraftingManager.REGISTRY).getUnderlyingIntegerMap().size());


        System.out.println("re recipe adding");
        NBTTagList nbttaglist = nbt.getTagList("recipeList", 10);
        for (int i = 0; i < nbttaglist.tagCount(); i++) {
            System.out.println("re recipe adding " + i);
            NBTTagCompound ruleNBT = (NBTTagCompound) nbttaglist.get(i);

            String name = ruleNBT.getString("name");
            String recipe = ruleNBT.getString("recipe");
            JsonObject json = (new JsonParser()).parse(recipe).getAsJsonObject();
            System.out.println("123 " + i);
            try {
                CraftingManager.register(name, parseRecipeJson(json));
            } catch (Exception e) {
                System.out.println("something went wrong");
                System.out.println(e);
                return;
            }
            System.out.println("456 " + i);
        }

        System.out.println("--   --   --- " + ((IMixinRegistryNamespaced)CraftingManager.REGISTRY).getUnderlyingIntegerMap().size());
    }

//    public static boolean registerCustomRecipes() throws IOException {
//
//        Gson gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
//        File carpetDirectory = new File(CARPET_DIRECTORY_RECIPES);
//        if (!carpetDirectory.exists()) {
//            carpetDirectory.mkdir();
//        }
//
//        Path path = Paths.get(CARPET_DIRECTORY_RECIPES);
//        Iterator<Path> iterator = Files.walk(path).iterator();
//
//        while (iterator.hasNext())
//        {
//            Path path1 = iterator.next();
//
//            if ("json".equals(FilenameUtils.getExtension(path1.toString())))
//            {
//                Path path2 = path.relativize(path1);
//                String s = FilenameUtils.removeExtension(path2.toString()).replaceAll("\\\\", "/");
//                ResourceLocation resourcelocation = new ResourceLocation(s);
//                BufferedReader bufferedreader = null;
//
//                try
//                {
//                    boolean flag;
//
//                    try
//                    {
//                        bufferedreader = Files.newBufferedReader(path1);
//                        CraftingManager.register(s, parseRecipeJson((JsonObject) JsonUtils.fromJson(gson, bufferedreader, JsonObject.class)));
//                    }
//                    catch (JsonParseException jsonparseexception)
//                    {
//                        //CraftingManager.LOGGER.error("Parsing error loading recipe " + resourcelocation, (Throwable)jsonparseexception);
//                        flag = false;
//                        return flag;
//                    }
//                    catch (IOException ioexception)
//                    {
//                        //CraftingManager.LOGGER.error("Couldn't read recipe " + resourcelocation + " from " + path1, (Throwable)ioexception);
//                        flag = false;
//                        return flag;
//                    }
//                }
//                finally
//                {
//                    IOUtils.closeQuietly((Reader)bufferedreader);
//                }
//            }
//        }
//
//        return true;
//    }

    private static IRecipe parseRecipeJson(JsonObject json)
    {
        String s = JsonUtils.getString(json, "type");

        if ("crafting_shaped".equals(s))
        {
            return ShapedRecipes.deserialize(json);
        }
        else if ("crafting_shapeless".equals(s))
        {
            return ShapelessRecipes.deserialize(json);
        }
        else
        {
            throw new JsonSyntaxException("Invalid or unsupported recipe type '" + s + "'");
        }
    }
}

