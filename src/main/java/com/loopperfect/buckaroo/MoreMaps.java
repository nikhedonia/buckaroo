package com.loopperfect.buckaroo;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

public final class MoreMaps {

    public static <K, V> ImmutableMap<K, V> with(final ImmutableMap<K, V> x, final K k, final V v) {
        Preconditions.checkNotNull(x);
        return new ImmutableMap.Builder<K, V>()
            .putAll(x)
            .put(k, v)
            .build();
    }

    public static <K, V> ImmutableMap<K, V> merge(final ImmutableMap<K, V> x, final ImmutableMap<K, V> y) {
        Preconditions.checkNotNull(x);
        Preconditions.checkNotNull(y);
        return new ImmutableMap.Builder<K, V>()
            .putAll(x)
            .putAll(y)
            .build();
    }
}
