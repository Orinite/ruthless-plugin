package com.ruthless.api;

public interface Validator<T> {
    public boolean valid(T t);
}
