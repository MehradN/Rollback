package ir.mehradn.rollback.exception;

import java.util.function.Function;

public interface Assertion {
    static <E extends RuntimeException> void general(boolean check, Function<String, E> exceptionBuilder) throws E {
        general(check, "Assertion failed!", exceptionBuilder);
    }

    static <E extends RuntimeException> void general(boolean check, String message, Function<String, E> exceptionBuilder) throws E {
        if (!check)
            throw exceptionBuilder.apply(message);
    }

    static void runtime(boolean check) throws RuntimeException {
        general(check, RuntimeException::new);
    }

    static void runtime(boolean check, String message) throws RuntimeException {
        general(check, message, RuntimeException::new);
    }

    static void argument(boolean check) throws IllegalArgumentException {
        general(check, IllegalArgumentException::new);
    }

    static void argument(boolean check, String message) throws IllegalArgumentException {
        general(check, message, IllegalArgumentException::new);
    }

    static void state(boolean check) throws IllegalStateException {
        general(check, IllegalStateException::new);
    }

    static void state(boolean check, String message) throws IllegalStateException {
        general(check, message, IllegalStateException::new);
    }
}
