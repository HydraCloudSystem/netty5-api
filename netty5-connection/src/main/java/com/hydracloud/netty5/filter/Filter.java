package com.hydracloud.netty5.filter;

import org.jetbrains.annotations.NotNull;

public abstract class Filter<T> {
    public abstract boolean evaluateFilter(@NotNull T object);
}