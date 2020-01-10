package carpetclient.transformers;

import com.mumfrey.liteloader.transformers.ByteCodeUtilities;
import com.mumfrey.liteloader.util.ObfuscationUtilities;
import java.io.IOException;
import java.util.Iterator;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import net.minecraft.launchwrapper.IClassTransformer;


public abstract class ClassTransformer extends com.mumfrey.liteloader.transformers.ClassTransformer
        implements IClassTransformer {
    protected String ObfHelper;

    protected ClassNode Obf;
    protected Type targetType;
    protected String targetClassName;

    public ClassTransformer(String ObfHelper) {
        this.ObfHelper = ObfHelper;

        try {
            this.Obf = loadClass(ObfHelper);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.targetType = getObfType(this.Obf);
        this.targetClassName = this.targetType.getClassName();
    }

    protected ClassNode loadClass(String helperName) throws IOException {
        if (!ObfuscationUtilities.fmlIsPresent())
            return ByteCodeUtilities.loadClass(helperName, this);

        // Forge uses Searge mappins, and it has DeobfuscationTransformer
        // to help with remapping Notch -> Searge, but it doesn't help on
        // private members of Mixins which we use to get the Notch map.
        // To workaround this, we transform the Obf helper before Forge
        // does its remapping.
        ClassNode Obf = ByteCodeUtilities.loadClass(helperName, false);
        Type ObfType = getObfType(Obf);

        for (MethodNode method : Obf.methods) {
            Iterator<AbstractInsnNode> iter = method.instructions.iterator();
            while (iter.hasNext()) {
                AbstractInsnNode insn = iter.next();
                boolean patch = false;

                // Change: this.attr => this.__TARGET.attr
                if (insn instanceof MethodInsnNode) {
                    MethodInsnNode insn_ = (MethodInsnNode)insn;
                    if (insn_.owner == Obf.name && !insn_.name.equals("__TARGET")) {
                        insn_.owner = ObfType.getInternalName();
                        patch = true;
                    }
                } else if (insn instanceof FieldInsnNode) {
                    FieldInsnNode insn_ = (FieldInsnNode)insn;
                    if (insn_.owner == Obf.name && !insn_.name.equals("__TARGET")) {
                        insn_.owner = ObfType.getInternalName();
                        patch = true;
                    }
                } else {
                    continue;
                }

                if (patch)
                    method.instructions.insert(insn.getPrevious(),
                        new FieldInsnNode(Opcodes.GETSTATIC, Obf.name, "__TARGET", ObfType.getDescriptor()));
            }
        }

        byte[] ObfTransformed = this.writeClass(Obf);
        ObfTransformed = ByteCodeUtilities.applyTransformers(helperName, ObfTransformed, this);

        return ByteCodeUtilities.readClass(ObfTransformed);
    }

    protected static Type getObfType(ClassNode Obf) {
        for (FieldNode field : Obf.fields) {
            if ("__TARGET".equals(field.name)) {
                return Type.getType(field.desc);
            }
        }

        throw new RuntimeException("Can't resolve obfuscated class name");
    }

    protected String getObfMemberName(String name) {
        return getObfMemberName(this.Obf, name);
    }

    // (De-)Obfuscation mechanics:
    // We have seperate helper classes that reference obfuscated classes.
    // During compilation the references will be obfuscated. However, this only
    // works for public members. Private ones are obfuscated by Sponge Mixin
    // magic.
    protected static String getObfMemberName(ClassNode Obf, String name) {
        String nameObf = name + "Obf";

        for (MethodNode method : Obf.methods) {
            if (nameObf.equals(method.name)) {
                // Need the last one
                String result = null;

                Iterator<AbstractInsnNode> iter = method.instructions.iterator();
                while (iter.hasNext()) {
                    AbstractInsnNode insn = iter.next();

                    if (insn instanceof MethodInsnNode) {
                        MethodInsnNode insn_ = (MethodInsnNode)insn;
                        result = insn_.name;
                    } else if (insn instanceof FieldInsnNode) {
                        FieldInsnNode insn_ = (FieldInsnNode)insn;
                        result = insn_.name;
                    }
                }

                if (result == null)
                    throw new RuntimeException("Unknown obfuscated name for " + name);

                return result;
            }
        }

        throw new RuntimeException("Unknown obfuscated name for " + name);
    }

    protected String getObfClassAndMemberName(String helperName, String name) throws IOException {
        ClassNode Obf = loadClass(helperName);
        return getObfType(Obf).getInternalName() + "/" + getObfMemberName(Obf, name);
    }
}
