package geni.witherutils.core.common.helper;

import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class NullHelper {

    private NullHelper() {
    }

    @Nonnull
    public final static <P> P notnull(@Nullable P o, @Nonnull String message) {
        return notnull(o, (Object) message);
    }

    @Nonnull
    public final static <P> P notnullJ(@Nullable P o, @Nonnull String message) {
        return notnullJ(o, (Object) message);
    }

    @Nonnull
    public final static <P> P notnullM(@Nullable P o, @Nonnull String message) {
        return notnullM(o, (Object) message);
    }

    @Nonnull
    public final static <P> P notnullF(@Nullable P o, @Nonnull String message) {
        return notnullF(o, (Object) message);
    }

    @Nonnull
    public final static <P> P untrusted(@Nonnull P o, @Nonnull String message) {
        return untrusted(o, (Object) message);
    }

    @Nonnull
    public final static <P> P notnull(@Nullable P o, @Nonnull Object... message) {
        if (o == null) {
            throw new NullPointerException("Houston we have a problem: '" + join(message) + "'. "
                    + "Please report that on our bugtracker unless you are using some old version. Thank you.");
        }
        return o;
    }

    @Nonnull
    public final static <P> P notnullJ(@Nullable P o, @Nonnull Object... message) {
        if (o == null) {
            throw new NullPointerException("There was a problem with Java: The call '" + join(message)
                    + "' returned null even though it should not be able to do that. Is your Java broken?");
        }
        return o;
    }

    @Nonnull
    public final static <P> P notnullM(@Nullable P o, @Nonnull Object... message) {
        if (o == null) {
            throw new NullPointerException("There was a problem with Minecraft: The call '" + join(message)
                    + "' returned null even though it should not be able to do that. Is your Minecraft broken? Did some other mod break it?");
        }
        return o;
    }

    @Nonnull
    public final static <P> P notnullF(@Nullable P o, @Nonnull Object... message) {
        if (o == null) {
            throw new NullPointerException("There was a problem with Forge: The call '" + join(message)
                    + "' returned null even though it should not be able to do that. Is your Forge broken? Did some other mod break it?");
        }
        return o;
    }

    @Nonnull
    public final static <P> P untrusted(@Nonnull P o, @Nonnull Object... message) {
        if (o == null) {
            throw new NullPointerException("There was a problem with Minecraft: The call '" + join(message)
                    + "' returned null even though it says it is not be able to do that. " //
                    + "Your Minecraft is broken. This mod is NOT(!) the cause of this crash!");
        }
        return o;
    }

    public final static @Nullable <P> P untrust(@Nonnull P o) {
        return o;
    }

    @SafeVarargs
    public final static @Nonnull <P> P first(@Nullable P... o) {
        for (P on : notnull(o, (Object) "... param is null")) {
            if (on != null) {
                return on;
            }
        }
        throw new NullPointerException(
                "Houston we have a problem. Please report that on our bugtracker unless you are using some old version. Thank you.");
    }

    @SafeVarargs
    public final static @Nonnull <P> P first(@Nonnull Supplier<P>... o) {
        for (Supplier<P> on : notnull(o, (Object) "... param is null")) {
            P p = notnull(on, (Object) "... param value is null").get();
            if (p != null) {
                return p;
            }
        }
        throw new NullPointerException(
                "Houston we have a problem. Please report that on our bugtracker unless you are using some old version. Thank you.");
    }

    @SafeVarargs
    public final static @Nullable <P> P firstOrNull(@Nonnull Supplier<P>... o) {
        for (Supplier<P> on : notnull(o, (Object) "... param is null")) {
            P p = notnull(on, (Object) "... param value is null").get();
            if (p != null) {
                return p;
            }
        }
        return null;
    }

    @SafeVarargs
    public final static @Nonnull <P> P first(@Nullable P value, @Nonnull Supplier<P>... o) {
        if (value != null) {
            return value;
        }
        for (Supplier<P> on : notnull(o, (Object) "... param is null")) {
            P p = notnull(on, (Object) "... param value is null").get();
            if (p != null) {
                return p;
            }
        }
        throw new NullPointerException(
                "Houston we have a problem. Please report that on our bugtracker unless you are using some old version. Thank you.");
    }

    public final static @Nullable <P> P firstWithDefault(@Nullable P value, @Nonnull Supplier<P> on, @Nullable P d) {
        if (value != null) {
            return value;
        }
        P p = notnull(on, (Object) "... param value is null").get();
        if (p != null) {
            return p;
        }
        return d;
    }

    @SafeVarargs
    public final static @Nullable <P> P firstWithDefault(@Nullable P d, @Nonnull Supplier<P>... o) {
        for (Supplier<P> on : notnull(o, (Object) "... param is null")) {
            P p = notnull(on, (Object) "... param value is null").get();
            if (p != null) {
                return p;
            }
        }
        return d;
    }

    private static String join(@Nonnull Object... data) {
        StringBuilder b = new StringBuilder();
        for (Object object : data) {
            b.append(object);
        }
        return b.toString();
    }
}
