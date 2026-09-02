package com.example.task01;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HelloWorldTest {

    private static final String PACKAGE_NAME = "com.example.task01";
    private static final String CLASS_NAME = "HelloWorld";
    private static final String EXPECTED_OUTPUT = "Hello, World!";
    private static final String MAIN_SIGNATURE = "public static void main(String[] args)";

    @Test
    @DisplayName("В классе HelloWorld объявлен main метод")
    void mainMethod_isDeclared() {
        final Class<?> clazz = findHelloWorldClass();

        assertThat(findMainMethod(clazz))
            .as("В классе %s должен быть объявлен метод %s", CLASS_NAME, MAIN_SIGNATURE)
            .isPresent();
    }

    @Test
    @DisplayName("main метод выводит в консоль Hello, World!")
    void mainMethod_printsHelloWorld() {
        final Class<?> clazz = findHelloWorldClass();
        final Method main = findMainMethod(clazz)
            .orElseThrow(() -> new AssertionError("В классе " + CLASS_NAME + " не найден метод " + MAIN_SIGNATURE));

        final String printed = invokeAndCaptureOutput(main);

        assertThat(printed)
            .as("main метод должен вывести в консоль \"%s\"", EXPECTED_OUTPUT)
            .isEqualTo(EXPECTED_OUTPUT);
    }

    private static Class<?> findHelloWorldClass() {
        try {
            return Class.forName(PACKAGE_NAME + "." + CLASS_NAME);
        } catch (final ClassNotFoundException e) {
            throw new AssertionError("Класс " + CLASS_NAME + " не найден", e);
        }
    }

    private static Optional<Method> findMainMethod(final Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods())
            .filter(HelloWorldTest::isMainMethod)
            .findFirst();
    }

    private static boolean isMainMethod(final Method method) {
        final int modifiers = method.getModifiers();

        return "main".equals(method.getName())
            && Modifier.isPublic(modifiers)
            && Modifier.isStatic(modifiers)
            && method.getReturnType() == void.class
            && Arrays.equals(method.getParameterTypes(), new Class<?>[]{String[].class});
    }

    private static String invokeAndCaptureOutput(final Method main) {
        final PrintStream originalOut = System.out;
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try {
            main.invoke(null, (Object) new String[0]);
        } catch (final IllegalAccessException e) {
            throw new AssertionError("Не удалось вызвать " + MAIN_SIGNATURE + " в классе " + CLASS_NAME, e);
        } catch (final InvocationTargetException e) {
            throw new AssertionError("main метод завершился с ошибкой", e.getCause());
        } finally {
            System.out.flush();
            System.setOut(originalOut);
        }

        return buffer.toString(StandardCharsets.UTF_8).trim();
    }

}
