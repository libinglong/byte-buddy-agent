package com.lbl;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;

/**
 * @author binglongli217932
 * <a href="mailto:libinglong9@gmail.com">libinglong:libinglong9@gmail.com</a>
 * @since 2021/1/4
 */
public class SweetAgent {


    public static void premain(String agentArgs, Instrumentation inst) throws IOException {



        final ByteBuddy byteBuddy = new ByteBuddy()
                .with(TypeValidation.of(true));

        AgentBuilder agentBuilder = new AgentBuilder.Default(byteBuddy)
                .with(AgentBuilder.InjectionStrategy.UsingUnsafe.INSTANCE)
                .ignore(nameStartsWith("net.bytebuddy.").or(ElementMatchers.isSynthetic()));


        File temp = Files.createTempDirectory("tmp").toFile();
        Map<TypeDescription.ForLoadedType, byte[]> forLoadedTypeMap = new HashMap<>();
        forLoadedTypeMap.put(new TypeDescription.ForLoadedType(InterceptTemplate.class), ClassFileLocator.ForClassLoader.read(InterceptTemplate.class));
        forLoadedTypeMap.put(new TypeDescription.ForLoadedType(OverrideCallable.class), ClassFileLocator.ForClassLoader.read(OverrideCallable.class));
        ClassInjector.UsingInstrumentation.of(temp, ClassInjector.UsingInstrumentation.Target.BOOTSTRAP, inst)
                .inject(forLoadedTypeMap);


        agentBuilder.type(target -> target.getName().equals("java.util.concurrent.ThreadPoolExecutor"))
                .transform(new Transformer())
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .with(new Listener())
                .disableClassFormatChanges()
                .installOn(inst);


    }

    private static class Transformer implements AgentBuilder.Transformer {


        @Override
        public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) {
            return builder
                    .method(target -> target.getName().equals("submit"))
                    .intercept(MethodDelegation.withDefaultConfiguration()
                            .withBinders(Morph.Binder.install(OverrideCallable.class))
                            .to(InterceptTemplate.class));
        }
    }

    private static class Listener implements AgentBuilder.Listener {


        @Override
        public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
//            System.out.println("dis " + typeName);
        }

        @Override
        public void onTransformation(final TypeDescription typeDescription,
                                     final ClassLoader classLoader,
                                     final JavaModule module,
                                     final boolean loaded,
                                     final DynamicType dynamicType) {
            System.out.println("On Transformation class " + typeDescription.getName() + " loaded:" + loaded);
        }

        @Override
        public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded) {
//            System.out.println("ignore " + typeDescription.getName() + " loaded:" + loaded);
        }

        @Override
        public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded, Throwable throwable) {
            throwable.printStackTrace();
            System.out.println("error " + typeName);
        }

        @Override
        public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
//            System.out.println("com " + typeName);
        }


    }

}
