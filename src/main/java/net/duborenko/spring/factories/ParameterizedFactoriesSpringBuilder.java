package net.duborenko.spring.factories;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;

@Configuration
public class ParameterizedFactoriesSpringBuilder implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private Environment environment;

    private BeanNameGenerator beanNameGenerator = new AnnotationBeanNameGenerator();

    @Bean
    public Object parameterizedFactoryBuilder(ApplicationContext applicationContext) {
        return new Object() {

            public <T> T createFactory(Class<T> type) {
                return type.cast(Proxy.newProxyInstance(
                        this.getClass().getClassLoader(),
                        new Class<?>[]{type},
                        (proxy, method, args) -> {
                            Class<?> returnType = method.getReturnType();
                            Constructor<?> constructor = returnType.getDeclaredConstructors()[0];
                            Class<?>[] constructorParameterTypes = constructor.getParameterTypes();
                            Annotation[][] constructorParameterAnnotations = constructor.getParameterAnnotations();
                            Object[] constructorArgs = new Object[constructorParameterTypes.length];
                            for (int i = 0, j = 0; i < constructorParameterTypes.length; i++) {
                                boolean isParameter = hasAnnotation(constructorParameterAnnotations[i], Parameter.class);
                                constructorArgs[i] = isParameter
                                        ? args[j++]
                                        : getBean(constructorParameterTypes[i]);
                            }
                            return constructor.newInstance(constructorArgs);
                        })
                );
            }

            private boolean hasAnnotation(Annotation[] annotations, Class<? extends Annotation> annotation) {
                return Arrays.stream(annotations)
                        .filter(a -> annotation.isInstance(a))
                        .findAny()
                        .isPresent();
            }

            private Object getBean(Class<?> type) {
                if(type.isInstance(applicationContext)) {
                    return applicationContext;
                }
                return applicationContext.getBean(type);
            }

        };
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        ClassPathScanningCandidateComponentProvider provider =
                new InterfaceCandidateComponentProvider(ComponentFactory.class, environment);

        for (String basePackage : getBasePackages(metadata)) {
            for (BeanDefinition beanDefinition : provider.findCandidateComponents(basePackage)) {
                beanDefinition.setFactoryBeanName("parameterizedFactoryBuilder");
                beanDefinition.setFactoryMethodName("createFactory");
                // todo validate bean class
                beanDefinition.getConstructorArgumentValues()
                        .addGenericArgumentValue(getBeanClass(beanDefinition));
                registry.registerBeanDefinition(
                        beanNameGenerator.generateBeanName(beanDefinition, registry),
                        beanDefinition);
            }
        }
    }

    private Class<?> getBeanClass(BeanDefinition beanDefinition) {
        String className = beanDefinition.getBeanClassName();
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(String.format("Cannot find a class for name '%s'.", className), e);
        }
    }

    private String[] getBasePackages(AnnotationMetadata metadata) {
        String[] basePackages;

        Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(
                EnableParameterizedFactories.class.getCanonicalName());


        if (annotationAttributes != null) {
            basePackages = (String[]) annotationAttributes.get("value");

            if (basePackages.length != 0) {
                return basePackages;
            }

        }

        annotationAttributes = metadata.getAnnotationAttributes(ComponentScan.class.getCanonicalName());

        if (annotationAttributes != null) {
            basePackages = (String[]) annotationAttributes.get("value");

            if (basePackages.length != 0) {
                return basePackages;
            }

        }

        return new String[]{((StandardAnnotationMetadata) metadata).getIntrospectedClass().getPackage().getName()};
    }

    private class InterfaceCandidateComponentProvider extends ClassPathScanningCandidateComponentProvider {

        public InterfaceCandidateComponentProvider(
                Class<? extends Annotation> componentFactoryClass,
                Environment environment) {
            super(false, environment);
            addIncludeFilter(new AnnotationTypeFilter(componentFactoryClass));
        }

        @Override
        protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
            AnnotationMetadata metadata = beanDefinition.getMetadata();
            return metadata.isIndependent() && metadata.isInterface();
        }

    }

}
