package com.ruthless.api;

public interface Validator<T> {

    public boolean validate(T t);
}
