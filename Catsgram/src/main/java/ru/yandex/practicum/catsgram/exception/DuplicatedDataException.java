package ru.yandex.practicum.catsgram.exception;

import java.io.IOException;

public class DuplicatedDataException extends IOException {
    public DuplicatedDataException(final String message) {
        super(message);
    }
}
