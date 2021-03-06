package com.module.plugin.fix;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class FixBugClassVisitor extends ClassVisitor {

    public FixBugClassVisitor(int api) {
        super(api);
    }

    public FixBugClassVisitor(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }


    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        System.out.println("FixBugClassVisitor visitMethod "+String.format("%d %s %s %s",access,name,descriptor,signature));

        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        if(name.equals("onCreate") && descriptor.equals("(Landroid/os/Bundle;)V")){
            System.out.println("FixBugClassVisitor visitMethod ------------------");
            return new FixMethod(api,mv);
        }

        //4.asm修改FixNullException.java空指针
        else if(name.equals("isSuccess") && descriptor.equals("(Ljava/lang/Object;)Z")){
            System.out.println("FixBugClassVisitor visitMethod isSuccess ------------------");
            return new FixMethod(api,mv);
        }
        return mv;
    }

    static final class FixMethod extends MethodVisitor {
        //3.替换功能：android.util.Log.v -> System.out.println
        Object android_util_log_param = null;

        public FixMethod(int api) {
            super(api);
        }

        public FixMethod(int api, MethodVisitor methodVisitor) {
            super(api, methodVisitor);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            System.out.println("visitMethodInsn "+String.format("%d %s %s %s %b",opcode,owner,name,descriptor,isInterface));

            //1.禁止方法调用
//            if(name.equals("show")){
//                System.out.println(" --------- visitMethodInsn ---------");
//                return;
//            }

            //3.替换功能：android.util.Log.v -> System.out.println
            if("android/util/Log".equals(owner)){
//                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);

                mv.visitFieldInsn(Opcodes.GETSTATIC,
                        "java/lang/System",
                        "out",
                        "Ljava/io/PrintStream;");
                mv.visitLdcInsn(android_util_log_param + " , I replace code success");
                // invokes the 'println' method (defined in the PrintStream class)
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                        "java/io/PrintStream",
                        "println",
                        "(Ljava/lang/String;)V");
                return;
            }

            //4.asm修改FixNullException.java空指针，单独使用asm插件没有问题，但是同时使用javassist插件(fix2)后就报错
//            if("java/lang/Object".equals(owner)){
//
//                Label label0 = new Label();
//                mv.visitLabel(label0);
//                mv.visitVarInsn(Opcodes.ALOAD, 0);
//
//                Label label1 = new Label();
//                mv.visitJumpInsn(Opcodes.IFNONNULL,label1);
//
//                Label label2 = new Label();
//                mv.visitLabel(label2);
//                mv.visitInsn(Opcodes.ICONST_0);
//                mv.visitInsn(Opcodes.IRETURN);
//
//                mv.visitLabel(label1);
//                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
//                return;
//            }

            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
            System.out.println("visitFieldInsn "+String.format("%d %s %s %s",opcode,owner,name,descriptor));
            super.visitFieldInsn(opcode, owner, name, descriptor);
        }

        @Override
        public void visitLdcInsn(Object value) {
            System.out.println("visitLdcInsn --------- visitLdcInsn ---------" + value);

            //2.参数替换
            if("我是错误的代码".equals(value)){
                super.visitLdcInsn("我是正确的代码，100%正确");
                return;
            }

            //3.替换功能：android.util.Log.v -> System.out.println
            android_util_log_param = value;
            super.visitLdcInsn(value);
        }
    }

//    static final class FixMethod4 extends MethodVisitor {
//        //3.替换功能：android.util.Log.v -> System.out.println
//        Object android_util_log_param = null;
//
//        public FixMethod4(int api) {
//            super(api);
//        }
//
//        public FixMethod4(int api, MethodVisitor methodVisitor) {
//            super(api, methodVisitor);
//        }
//
//        @Override
//        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
//            System.out.println("visitMethodInsn "+String.format("%d %s %s %s %b",opcode,owner,name,descriptor,isInterface));
//
//            //4.asm修改FixNullException.java空指针
//            if("java/lang/Object".equals(owner)){
//
//                Label label0 = new Label();
//                mv.visitLabel(label0);
//                mv.visitVarInsn(Opcodes.ALOAD, 0);
//
//                Label label1 = new Label();
//                mv.visitJumpInsn(Opcodes.IFNONNULL,label1);
//
//                Label label2 = new Label();
//                mv.visitLabel(label2);
//                mv.visitInsn(Opcodes.ICONST_0);
//                mv.visitInsn(Opcodes.IRETURN);
//
//                mv.visitLabel(label1);
//                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
//                return;
//            }
//
//            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
//        }
//
//        @Override
//        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
//            System.out.println("visitFieldInsn "+String.format("%d %s %s %s",opcode,owner,name,descriptor));
//            super.visitFieldInsn(opcode, owner, name, descriptor);
//        }
//
//        @Override
//        public void visitMaxs(int maxStack, int maxLocals) {
//            super.visitMaxs(maxStack + 4, maxLocals);
//        }
//
//    }
}
