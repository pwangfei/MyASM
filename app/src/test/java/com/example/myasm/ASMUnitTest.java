package com.example.myasm;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class ASMUnitTest {

    @Test
    public void test() throws Exception {
        FileInputStream fis=  new FileInputStream(new File("src/test/java/com/example/myasm/InjectTest.class"));

        /*** class分析器*/
        ClassReader cr=new ClassReader(fis);

        /*** 栈帧！ class 自动计算栈帧和局部变量表的大小*/
        ClassWriter cw=new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        /*** 执行分析器，修改*/
        cr.accept(new MyClassVistor(Opcodes.ASM7,cw),ClassReader.EXPAND_FRAMES);

        /*** 执行了插桩之后的字节码数据*/
        byte[] bytes=cw.toByteArray();
        FileOutputStream fos=new FileOutputStream("src/test/java/com/example/myasm/InjectTest2.class");
        fos.write(bytes);
        fos.close();


    }


    /*** 处理class文件的分析回调结果*/
    static class MyClassVistor extends ClassVisitor{
        public MyClassVistor(int api, ClassVisitor classVisitor) {
            super(api, classVisitor);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodVisitor myMethodVisitor=super.visitMethod(access, name, descriptor, signature, exceptions);
            System.out.println(name);
            return new MyMethodVisitor(api,myMethodVisitor,access,name,descriptor);
        }
    }

    /*** 正在的插桩处理业务*/
    static class MyMethodVisitor extends AdviceAdapter {
        int s;
        protected MyMethodVisitor(int api, MethodVisitor methodVisitor, int access, String name, String descriptor) {
            super(api, methodVisitor, access, name, descriptor);
        }

        /*** 进入方法*/
        @Override
        protected void onMethodEnter() {
            super.onMethodEnter();
            // 插入 long l=System.currentTimeMillis();
            invokeStatic(Type.getType("Ljava/lang/System;"),new Method("currentTimeMillis","()J"));

            //索引
            s = newLocal(Type.LONG_TYPE);
            //用一个本地变量接受上一步的执行结果
            storeLocal(s);


        }

        /*** 退出方法*/
        @Override
        protected void onMethodExit(int opcode) {
            super.onMethodExit(opcode);
//            long e=System.currentTimeMillis();
//            System.out.println("execute"+(e-l)+"ms");

            // 插入 long l=System.currentTimeMillis();
            invokeStatic(Type.getType("Ljava/lang/System;"),new Method("currentTimeMillis","()J"));
            //索引
            int e = newLocal(Type.LONG_TYPE);
            //用一个本地变量接受上一步的执行结果
            storeLocal(e);
            getStatic(Type.getType("Ljava/lang/System;"),"out",Type.getType("Ljava/io/PrintStream;"));
            newInstance(Type.getType("Ljava/lang/StringBuilder;"));
            dup();
            invokeConstructor(Type.getType("Ljava/lang/StringBuilder;"),new Method("<init>","()V"));
            visitLdcInsn("execute");
            invokeVirtual(Type.getType("Ljava/lang/StringBuilder;"),new Method("append","(Ljava/lang/String;)Ljava/lang/StringBuilder;"));
            //减法 ,方法参数用的是索引
            loadLocal(e);
            loadLocal(s);
            math(SUB,Type.LONG_TYPE);
            invokeVirtual(Type.getType("Ljava/lang/StringBuilder;"),new Method("append","(J)Ljava/lang/StringBuilder;"));
            visitLdcInsn("ms.");
            invokeVirtual(Type.getType("Ljava/lang/StringBuilder;"),new Method("append","(Ljava/lang/String;)Ljava/lang/StringBuilder;"));
            invokeVirtual(Type.getType("Ljava/lang/StringBuilder;"),new Method("toString","()Ljava/lang/String;"));
            invokeVirtual(Type.getType("Ljava/io/PrintStream;"),new Method("println","(Ljava/lang/String;)V"));


        }

    }

}
