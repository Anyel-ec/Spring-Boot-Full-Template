package com.luidmidev.template.spring.services.quizz.validations;

import com.luidmidev.template.spring.services.quizz.models.Answer;
import com.luidmidev.template.spring.validation.CiConstraintValidator;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public interface QValidator extends Function<Answer, String> {

    @Override
    String apply(Answer answer);

    String INITIAL_MESSAGE = "La respuesta de la pregunta ";

    static QValidator get(String type, String... args) {
        return switch (type) {
            case "required" -> required();
            case "maxLength" -> maxLength(Integer.parseInt(args[0]));
            case "minLength" -> minLength(Integer.parseInt(args[0]));
            case "email" -> email();
            case "number" -> number();
            case "ci" -> ciValidator();
            case "positive" -> isPositive();
            case "negative" -> isNegative();
            case "positiveOrZero" -> isPositiveOrZero();
            case "negativeOrZero" -> isNegativeOrZero();
            case "greaterThan" -> greaterThan(Integer.parseInt(args[0]));
            case "lessThan" -> lessThan(Integer.parseInt(args[0]));
            case "greaterThanOrEqual" -> greaterThanOrEqual(Integer.parseInt(args[0]));
            case "lessThanOrEqual" -> lessThanOrEqual(Integer.parseInt(args[0]));
            case "inRange" -> inRange(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
            case "inList" -> inList(List.of(args));
            default -> throw new IllegalArgumentException("Invalid type: " + type + " for QValidator");
        };
    }

    static QValidator required() {

        return answer -> {

            if (answer.getValue() == null) {
                return INITIAL_MESSAGE + answer.getQuestionId() + " es requerida";
            }

            if (listOperation(answer.getValue(), List::isEmpty)) {
                return "Debe seleccionar al menos una opción en la pregunta " + answer.getQuestionId();
            }

            if (stringOperation(answer.getValue(), String::isEmpty)) {
                return INITIAL_MESSAGE + answer.getQuestionId() + " no puede estar vacía";
            }

            return null;

        };
    }

    static QValidator maxLength(int length) {

        return answer -> {
            if (stringOperation(answer.getValue(), value -> value.length() > length)) {
                return INITIAL_MESSAGE + answer.getQuestionId() + " no puede tener más de " + length + " caracteres";
            }
            return null;
        };
    }


    static QValidator minLength(int length) {

        return answer -> {
            if (stringOperation(answer.getValue(), value -> value.length() < length)) {
                return INITIAL_MESSAGE + answer.getQuestionId() + " no puede tener menos de " + length + " caracteres";
            }
            return null;
        };

    }


    static QValidator email() {
        return answer -> {
            if (stringOperation(answer.getValue(), value -> value.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$"))) {
                return null;
            }
            return INITIAL_MESSAGE + answer.getQuestionId() + " debe ser un correo electrónico válido";
        };
    }


    static QValidator number() {

        return answer -> {
            if (isNotNumber(answer.getValue())) {
                return INITIAL_MESSAGE + answer.getQuestionId() + " debe ser un número";
            }

            return null;
        };
    }

    static QValidator ciValidator() {

        return answer -> {
            if (!existsValue(answer) || stringOperation(answer.getValue(), value -> !CiConstraintValidator.validateCi(value))) {
                return INITIAL_MESSAGE + answer.getQuestionId() + " no es un número de cédula válido";
            }

            return null;
        };

    }

    static QValidator isPositive() {
        return answer -> {
            if (isNotNumber(answer.getValue()) || numberOperation(answer.getValue(), number -> number.intValue() <= 0)) {
                return INITIAL_MESSAGE + answer.getQuestionId() + " debe ser mayor a 0";
            }
            return null;
        };

    }

    static QValidator isNegative() {
        return answer -> {
            if (isNotNumber(answer.getValue()) || numberOperation(answer.getValue(), number -> number.intValue() >= 0)) {
                return INITIAL_MESSAGE + answer.getQuestionId() + " debe ser menor a 0";
            }
            return null;
        };
    }

    static QValidator isPositiveOrZero() {
        return answer -> {
            if (isNotNumber(answer.getValue()) || numberOperation(answer.getValue(), number -> number.intValue() < 0)) {
                return INITIAL_MESSAGE + answer.getQuestionId() + " debe ser mayor o igual a 0";
            }
            return null;
        };
    }

    static QValidator isNegativeOrZero() {
        return answer -> {
            if (isNotNumber(answer.getValue()) || numberOperation(answer.getValue(), number -> number.intValue() <= 0)) {
                return INITIAL_MESSAGE + answer.getQuestionId() + " debe ser menor o igual a 0";
            }
            return null;
        };
    }

    static QValidator greaterThan(int min) {
        return answer -> {
            if (isNotNumber(answer.getValue()) || numberOperation(answer.getValue(), number -> number.intValue() <= min)) {
                return INITIAL_MESSAGE + answer.getQuestionId() + " debe ser mayor a " + min;
            }
            return null;
        };
    }

    static QValidator lessThan(int max) {
        return answer -> {
            if (isNotNumber(answer.getValue()) || numberOperation(answer.getValue(), number -> number.intValue() >= max)) {
                return INITIAL_MESSAGE + answer.getQuestionId() + " debe ser menor a " + max;
            }
            return null;
        };
    }

    static QValidator greaterThanOrEqual(int min) {
        return answer -> {
            if (isNotNumber(answer.getValue()) || numberOperation(answer.getValue(), number -> number.intValue() < min)) {
                return INITIAL_MESSAGE + answer.getQuestionId() + " debe ser mayor o igual a " + min;
            }
            return null;
        };
    }

    static QValidator lessThanOrEqual(int max) {
        return answer -> {
            if (isNotNumber(answer.getValue()) || numberOperation(answer.getValue(), number -> number.intValue() > max)) {
                return INITIAL_MESSAGE + answer.getQuestionId() + " debe ser menor o igual a " + max;
            }
            return null;
        };
    }

    static QValidator inRange(int min, int max) {
        return answer -> {
            if (isNotNumber(answer.getValue()) || numberOperation(answer.getValue(), number -> number.intValue() < min || number.intValue() > max)) {
                return INITIAL_MESSAGE + answer.getQuestionId() + " debe estar entre " + min + " y " + max;
            }
            return null;
        };
    }

    static QValidator inList(List<?> list) {
        return answer -> {
            if (listOperation(answer.getValue(), value -> list.stream().anyMatch(value::contains))) {
                return INITIAL_MESSAGE + answer.getQuestionId() + " debe ser una de las opciones: " + list;
            }
            return null;
        };
    }

    static boolean numberOperation(Object value, Predicate<Number> operation) {
        if (value instanceof Number number) {
            return operation.test(number);
        }
        if (value instanceof String stringValue && stringValue.matches("\\d+")) {
            return operation.test(Integer.parseInt(stringValue));
        }
        return false;
    }

    static boolean stringOperation(Object value, Predicate<String> operation) {
        if (value instanceof String stringValue) {
            return operation.test(stringValue);
        }
        return operation.test(value.toString());
    }

    static boolean listOperation(Object value, Predicate<List<?>> operation) {
        if (value instanceof List<?> list) {
            return operation.test(list);
        }
        return false;
    }

    static boolean isNotNumber(Object value) {
        return !(value instanceof Number) && (!(value instanceof String stringValue) || !stringValue.matches("\\d+"));
    }

    static boolean existsValue(Answer value) {
        return required().apply(value) == null;
    }

}
