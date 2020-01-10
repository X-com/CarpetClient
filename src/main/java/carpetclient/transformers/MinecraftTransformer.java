package carpetclient.transformers;

import carpetclient.transformers.ClassTransformer;
import com.mumfrey.liteloader.transformers.ByteCodeUtilities;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import net.minecraft.launchwrapper.IClassTransformer;


public class MinecraftTransformer extends ClassTransformer implements IClassTransformer {
    public MinecraftTransformer() {
        super("carpetclient.transformers.MinecraftObf");
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) return basicClass;

        if (this.targetClassName.equals(transformedName)) {
            return this.transformClass(basicClass);
        }

        return basicClass;
    }

    private byte[] transformClass(byte[] basicClass) {
        ClassNode targetClass = this.readClass(basicClass, true);

        String runTickObf = this.getObfMemberName("runTick");
        for (MethodNode method : targetClass.methods) {
            if (runTickObf.equals(method.name)) {
                transformRunTick(targetClass, method);
            }
        }

        return this.writeClass(targetClass);
    }

    private void transformRunTick(ClassNode targetClass, MethodNode method) {
        int numargs = ByteCodeUtilities.getFirstNonArgLocalIndex(method);

        // We need to create two local variables at the start. Increase
        // the index of all local variables by 2.
        {
            Iterator<AbstractInsnNode> iter = method.instructions.iterator();
            while (iter.hasNext()) {
                AbstractInsnNode insn = iter.next();

                if (insn instanceof VarInsnNode) {
                    VarInsnNode insn_ = (VarInsnNode)insn;
                    if (insn_.var >= numargs)
                        insn_.var += 2;
                }
            }
        }

        int var_tickWorld = numargs;
        int var_tickPlayer = numargs + 1;

        LabelNode startLabel = new LabelNode();
        LabelNode endLabel = new LabelNode();

        method.maxLocals += 2;
        method.localVariables.add(var_tickWorld, new LocalVariableNode("tickWorld", Type.INT_TYPE.getDescriptor(), null, startLabel, endLabel, var_tickWorld));
        method.localVariables.add(var_tickPlayer, new LocalVariableNode("tickPlayer", Type.INT_TYPE.getDescriptor(), null, startLabel, endLabel, var_tickPlayer));

        String timerObf = this.getObfMemberName("timer");
        Type timerType = null;
        for (FieldNode field : targetClass.fields) {
            if (timerObf.equals(field.name)) {
                timerType = Type.getType(field.desc);
            }
        }

        // Construct the initial and final instructions.
        InsnList init_lst = new InsnList();
        InsnList final_lst = new InsnList();
        {
            LabelNode label_tmp;

            /* int tickWorld = 0; */
            init_lst.add(new FrameNode(Opcodes.F_APPEND, 1, new Object[] {Opcodes.INTEGER}, 0, null));
            init_lst.add(new InsnNode(Opcodes.ICONST_0));
            init_lst.add(new VarInsnNode(Opcodes.ISTORE, var_tickWorld));

            /* if (this.timer.elapsedTicksWorld > 0) */
            init_lst.add(new VarInsnNode(Opcodes.ALOAD, 0));
            init_lst.add(new FieldInsnNode(Opcodes.GETFIELD, targetClass.name, timerObf, timerType.getDescriptor()));
            init_lst.add(new FieldInsnNode(Opcodes.GETFIELD, timerType.getInternalName(), "elapsedTicksWorld", Type.INT_TYPE.getDescriptor()));

            /* { */
            label_tmp = new LabelNode();
            init_lst.add(new JumpInsnNode(Opcodes.IFLE, label_tmp));

            /* tickWorld = 1; */
            init_lst.add(new InsnNode(Opcodes.ICONST_1));
            init_lst.add(new VarInsnNode(Opcodes.ISTORE, var_tickWorld));

            /* } */
            init_lst.add(label_tmp);
            init_lst.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));

            /* int tickPlayer = 0; */
            init_lst.add(new FrameNode(Opcodes.F_APPEND, 1, new Object[] {Opcodes.INTEGER}, 0, null));
            init_lst.add(new InsnNode(Opcodes.ICONST_0));
            init_lst.add(new VarInsnNode(Opcodes.ISTORE, var_tickPlayer));

            /* if (this.timer.elapsedTicksPlayer > 0) */
            init_lst.add(new VarInsnNode(Opcodes.ALOAD, 0));
            init_lst.add(new FieldInsnNode(Opcodes.GETFIELD, targetClass.name, timerObf, timerType.getDescriptor()));
            init_lst.add(new FieldInsnNode(Opcodes.GETFIELD, timerType.getInternalName(), "elapsedTicksPlayer", Type.INT_TYPE.getDescriptor()));

            /* { */
            label_tmp = new LabelNode();
            init_lst.add(new JumpInsnNode(Opcodes.IFLE, label_tmp));

            /* tickPlayer = 1; */
            init_lst.add(new InsnNode(Opcodes.ICONST_1));
            init_lst.add(new VarInsnNode(Opcodes.ISTORE, var_tickPlayer));

            /* } */
            init_lst.add(label_tmp);
            init_lst.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));

            /* if (var_tickWorld != 0) { */
            final_lst.add(new VarInsnNode(Opcodes.ILOAD, var_tickWorld));
            label_tmp = new LabelNode();
            final_lst.add(new JumpInsnNode(Opcodes.IFEQ, label_tmp));

            /* this.timer.elapsedTicksWorld--; */
            final_lst.add(new VarInsnNode(Opcodes.ALOAD, 0));
            final_lst.add(new FieldInsnNode(Opcodes.GETFIELD, targetClass.name, timerObf, timerType.getDescriptor()));
            final_lst.add(new InsnNode(Opcodes.DUP));
            final_lst.add(new FieldInsnNode(Opcodes.GETFIELD, timerType.getInternalName(), "elapsedTicksWorld", Type.INT_TYPE.getDescriptor()));
            final_lst.add(new InsnNode(Opcodes.ICONST_1));
            final_lst.add(new InsnNode(Opcodes.ISUB));
            final_lst.add(new FieldInsnNode(Opcodes.PUTFIELD, timerType.getInternalName(), "elapsedTicksWorld", Type.INT_TYPE.getDescriptor()));

            /* } */
            final_lst.add(label_tmp);
            final_lst.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));

            /* if (var_tickPlayer != 0) { */
            final_lst.add(new VarInsnNode(Opcodes.ILOAD, var_tickPlayer));
            label_tmp = new LabelNode();
            final_lst.add(new JumpInsnNode(Opcodes.IFEQ, label_tmp));

            /* this.timer.elapsedTicksPlayer--; */
            final_lst.add(new VarInsnNode(Opcodes.ALOAD, 0));
            final_lst.add(new FieldInsnNode(Opcodes.GETFIELD, targetClass.name, timerObf, timerType.getDescriptor()));
            final_lst.add(new InsnNode(Opcodes.DUP));
            final_lst.add(new FieldInsnNode(Opcodes.GETFIELD, timerType.getInternalName(), "elapsedTicksPlayer", Type.INT_TYPE.getDescriptor()));
            final_lst.add(new InsnNode(Opcodes.ICONST_1));
            final_lst.add(new InsnNode(Opcodes.ISUB));
            final_lst.add(new FieldInsnNode(Opcodes.PUTFIELD, timerType.getInternalName(), "elapsedTicksPlayer", Type.INT_TYPE.getDescriptor()));

            /* } */
            final_lst.add(label_tmp);
            final_lst.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        }

        // Assume no jumps will jump with a non-empty stack, analyze statements.
        List<List<AbstractInsnNode>> statements = new ArrayList<List<AbstractInsnNode>>();
        try {
            Analyzer<BasicValue> analyzer = new Analyzer<BasicValue>(new BasicInterpreter());
            analyzer.analyze(targetClass.name, method);
            Frame<BasicValue>[] frames = analyzer.getFrames();

            List<AbstractInsnNode> currentStatement = null;

            Iterator<AbstractInsnNode> iter = method.instructions.iterator();
            int i = 0;
            while (iter.hasNext()) {
                AbstractInsnNode insn = iter.next();

                if (currentStatement == null)
                    currentStatement = new ArrayList<AbstractInsnNode>();

                currentStatement.add(insn);

                if (
                    (insn.getOpcode() >= 0 || insn instanceof LabelNode) &&
                    (i + 1 >= frames.length || frames[i + 1] == null || frames[i + 1].getStackSize() == 0)
                ) {
                    statements.add(currentStatement);
                    currentStatement = null;
                }

                i++;
            }

            if (currentStatement != null)
                statements.add(currentStatement);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Transform statements
        try {
            List<String> memberWorld = Arrays.asList(new String[] {
                this.getObfMemberName("joinPlayerCounter"),
                this.getObfMemberName("world"),
                this.getObfClassAndMemberName("carpetclient.transformers.EntityRendererObf", "updateRenderer"),
                this.getObfMemberName("renderGlobal"),
                this.getObfClassAndMemberName("carpetclient.transformers.EntityRendererObf", "stopUseShader"),
                this.getObfMemberName("musicTicker"),
                this.getObfMemberName("soundHandler"),
                this.getObfMemberName("effectRenderer"),
                this.getObfMemberName("networkManager"),
            });
            List<String> memberPlayer = Arrays.asList(new String[] {
                this.getObfMemberName("rightClickDelayTimer"),
                this.getObfMemberName("ingameGUI"),
                this.getObfClassAndMemberName("carpetclient.transformers.EntityRendererObf", "getMouseOver"),
                this.getObfMemberName("playerController"),
                this.getObfMemberName("renderEngine"),
                this.getObfMemberName("displayGuiScreen"),
                this.getObfMemberName("leftClickCounter"),
                this.getObfMemberName("currentScreen"),
                this.getObfMemberName("runTickMouse"),
                this.getObfMemberName("runTickKeyboard"),
            });

            for (List<AbstractInsnNode> statement : statements) {
                StatementType type = StatementType.UNKNOWN;

                // Determine the type of this statement
                for (AbstractInsnNode insn : statement) {
                    String owner, name;

                    if (insn instanceof MethodInsnNode) {
                        MethodInsnNode insn_ = (MethodInsnNode)insn;
                        owner = insn_.owner;
                        name = insn_.name;
                    } else if (insn instanceof FieldInsnNode) {
                        FieldInsnNode insn_ = (FieldInsnNode)insn;
                        owner = insn_.owner;
                        name = insn_.name;
                    } else if (insn instanceof JumpInsnNode) {
                        type = StatementType.DO_NOT_ALTER;
                        continue;
                    } else {
                        continue;
                    }

                    String identifier;

                    if (owner.equals(targetType.getInternalName())) {
                        identifier = name;
                    } else {
                        identifier = owner + "/" + name;
                    }

                    if (memberWorld.contains(identifier)) {
                        if (type != StatementType.UNKNOWN && type != StatementType.WORLD)
                            throw new RuntimeException("Multiple possible statement type match");
                        type = StatementType.WORLD;
                    }

                    if (memberPlayer.contains(identifier)) {
                        if (type != StatementType.UNKNOWN && type != StatementType.PLAYER)
                            throw new RuntimeException("Multiple possible statement type match");
                        type = StatementType.PLAYER;
                    }
                }

                // Perform the patch
                {
                    int condvar = 0;
                    if (type == StatementType.UNKNOWN || type == StatementType.DO_NOT_ALTER) {
                        continue;
                    } else if (type == StatementType.WORLD) {
                        condvar = var_tickWorld;
                    } else if (type == StatementType.PLAYER) {
                        condvar = var_tickPlayer;
                    }

                    InsnList prepend = new InsnList();
                    InsnList append = new InsnList();

                    LabelNode label_tmp;

                    /* if (condvar != 0) { */
                    prepend.add(new VarInsnNode(Opcodes.ILOAD, condvar));
                    label_tmp = new LabelNode();
                    prepend.add(new JumpInsnNode(Opcodes.IFEQ, label_tmp));

                    /* } */
                    append.add(label_tmp);
                    append.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));

                    if (statement.get(0) == method.instructions.getFirst()) {
                        method.instructions.insert(prepend);
                    } else {
                        method.instructions.insert(statement.get(0).getPrevious(), prepend);
                    }

                    method.instructions.insert(statement.get(statement.size() - 1), append);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Find the return and insert the final_lst
        {
            Iterator<AbstractInsnNode> iter = method.instructions.iterator();
            while (iter.hasNext()) {
                AbstractInsnNode insn = iter.next();
                if (insn instanceof InsnNode && insn.getOpcode() == Opcodes.RETURN)
                    method.instructions.insertBefore(insn, final_lst);
            }
        }

        method.instructions.insert(init_lst);

        method.instructions.insert(startLabel);
        method.instructions.add(endLabel);
    }

    private static enum StatementType {
        UNKNOWN,
        DO_NOT_ALTER,
        WORLD,
        PLAYER,
    }
}
