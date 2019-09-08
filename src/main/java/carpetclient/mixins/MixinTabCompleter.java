package carpetclient.mixins;

import carpetclient.util.FastTabComplete;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.network.play.server.SPacketTabComplete;
import net.minecraft.util.TabCompleter;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mixin(TabCompleter.class)
public class MixinTabCompleter {

    ICommandManager ch = new FastTabComplete();

    @Shadow protected boolean requestedCompletions;

    @Shadow @Final protected boolean hasTargetBlock;

    @Shadow public BlockPos getTargetBlockPos(){ return null; }

    /**
     * This method was added to fast tab complete instead of sending a request to the server for the
     * the most common tab completions.
     *
     * @param prefix
     * @param ci
     */
    @Inject(method = "requestCompletions", at = @At("HEAD"), cancellable = true)
    public void tabCompleteHelper(String prefix, CallbackInfo ci){
        if(prefix.length() >= 1 && inExistingCommand(prefix)) {
            List<String> list = getTabCompletions(Minecraft.getMinecraft().player, prefix, this.getTargetBlockPos(), this.hasTargetBlock);
            if(list != null && list.size() > 0) {
                this.requestedCompletions = true;
                Minecraft.getMinecraft().getConnection().handleTabComplete(new SPacketTabComplete((String[])list.toArray(new String[list.size()])));
                ci.cancel();
            }
        }
    }

    /**
     * Method to check if tab complete option exists.
     * @return
     * @param prefix
     */
    private boolean inExistingCommand(String prefix) {
        for(String names : ch.getCommands().keySet() ){
            StringBuilder sb = new StringBuilder();
            sb.append('/');
            sb.append(names);
            sb.append(' ');
            if(prefix.startsWith(sb.toString())){
                return true;
            }
        }
        return false;
    }

    // Helper method for faster tab completion.
    private List<String> getTabCompletions(ICommandSender sender, String input, @Nullable BlockPos pos, boolean hasTargetBlock)
    {
        List<String> list = Lists.<String>newArrayList();
        boolean flag = input.startsWith("/");

        if(flag) {
            input = input.substring(1);
            boolean flag1 = !input.contains(" ");
            List<String> list1 = new ArrayList<>();
            try {
                list1 = ch.getTabCompletions(sender, input, pos);
            }catch(Exception e) {
            }
            if (!list1.isEmpty())
            {
                for (String s : list1)
                {
                    if (flag1 && !hasTargetBlock)
                    {
                        list.add("/" + s);
                    }
                    else
                    {
                        list.add(s);
                    }
                }
            }

            return list;
        }
        return null;
    }
}
