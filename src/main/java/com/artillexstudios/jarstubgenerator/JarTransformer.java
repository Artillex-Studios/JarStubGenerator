package com.artillexstudios.jarstubgenerator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.stream.Stream;

public final class JarTransformer {
    private final CommandLine commandLine;
    private final Path path;

    public JarTransformer(CommandLine commandLine, Path path) {
        this.commandLine = commandLine;
        this.path = path;
    }

    public void run() {
        if (Files.isDirectory(this.path)) {
            try (Stream<Path> stream = Files.list(this.path)) {
                stream.forEach(this::processFile);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            return;
        }

        this.processFile(this.path);
    }

    public void processFile(Path path) {
        System.out.printf("Transforming file %s...", path.getFileName());
        System.out.println();
        if (!path.getFileName().toString().endsWith(".jar")) {
            System.out.printf("Skipped file %s due to not being jar!", path.getFileName());
            System.out.println();
            return;
        }

        if (path.getFileName().toString().endsWith(this.commandLine.getOptionValue("s", "-stub") + ".jar")) {
            System.out.printf("Skipping already stub jar %s!", path.getFileName());
            System.out.println();
            return;
        }

        File file = path.toFile();
        File outFolder;
        try {
            outFolder = this.commandLine.getParsedOptionValue("out", file.getParentFile());
        } catch (ParseException exception) {
            exception.printStackTrace();
            return;
        }

        long start = System.nanoTime();
        File out = new File(outFolder, path.getFileName().toString().replace(".jar", "") + this.commandLine.getOptionValue("suffix", "-stub") + ".jar");
        try (JarFile jar = new JarFile(file); JarOutputStream tempJar = new JarOutputStream(new FileOutputStream(out))) {
            jar.stream().forEach(entry -> this.processEntry(jar, tempJar, entry));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        long took = System.nanoTime() - start;
        System.out.printf("Transformed jar in %sms", took / 1_000_000);
        System.out.println();
    }

    private void processEntry(JarFile jar, JarOutputStream tempJar, JarEntry entry) {
        try {
            if (entry.isDirectory() || !entry.getName().endsWith(".class")) {
                if (!entry.getName().endsWith(".class")) {
                    return;
                }

                tempJar.putNextEntry(new JarEntry(entry.getName()));
                tempJar.write(jar.getInputStream(entry).readAllBytes());
                return;
            }

            ClassReader classReader = new ClassReader(jar.getInputStream(entry));
            ClassWriter classWriter = new ClassWriter(0);
            ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM9, classWriter) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                    MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                    return new MethodVisitor(Opcodes.ASM9, mv) {
                        @Override
                        public void visitCode() {
                            super.visitCode();
                            mv.visitTypeInsn(Opcodes.NEW, "java/lang/RuntimeException");
                            mv.visitInsn(Opcodes.DUP);
                            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "()V", false);
                            mv.visitInsn(Opcodes.ATHROW);
                            mv.visitMaxs(0, 0);
                            mv.visitEnd();
                        }
                    };
                }

                @Override
                public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                    boolean enumConstant = (access & Opcodes.ACC_ENUM) != 0;
                    if (enumConstant) {
                        return super.visitField(access, name, descriptor, signature, value);
                    }

                    return null;
                }
            };

            classReader.accept(classVisitor, 0);
            tempJar.putNextEntry(new JarEntry(entry.getName()));
            tempJar.write(classWriter.toByteArray());
            tempJar.closeEntry();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
