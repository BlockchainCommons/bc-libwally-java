package com.bc.libwally.bip32;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.bc.libwally.bip32.Bip32Constant.BIP32_INITIAL_HARDENED_CHILD;
import static com.bc.libwally.bip32.Bip32Error.INVALID_DEPTH;
import static com.bc.libwally.bip32.Bip32Error.INVALID_INDEX;

public class Bip32Path {

    private final Bip32Derivation[] components;

    private final long[] rawPath;

    private final boolean relative;

    public Bip32Path(long[] rawPath, boolean relative) {
        if (rawPath == null || rawPath.length == 0)
            throw new Bip32Exception("invalid raw path");
        Bip32Derivation[] components = new Bip32Derivation[rawPath.length];
        for (int i = 0; i < rawPath.length; i++) {
            if (i < BIP32_INITIAL_HARDENED_CHILD) {
                components[i] = Bip32Derivation.newNormal(i);
            } else {
                components[i] = Bip32Derivation.newHardened(i - BIP32_INITIAL_HARDENED_CHILD);
            }
        }

        this.components = components;
        this.relative = relative;
        this.rawPath = rawPath;
    }

    public Bip32Path(Bip32Derivation[] components, boolean relative) {
        if (components == null || components.length == 0)
            throw new Bip32Exception("invalid components");

        long[] rawPath = new long[components.length];

        for (int i = 0; i < components.length; i++) {
            Bip32Derivation component = components[i];
            long index = component.getIndex();
            if (index >= BIP32_INITIAL_HARDENED_CHILD)
                throw new Bip32Exception(INVALID_INDEX);

            if (component.isHardened()) {
                rawPath[i] = BIP32_INITIAL_HARDENED_CHILD + index;
            } else {
                rawPath[i] = index;
            }
        }

        this.components = components;
        this.relative = relative;
        this.rawPath = rawPath;
    }

    public Bip32Path(Bip32Derivation component, boolean relative) {
        this(new Bip32Derivation[]{component}, relative);
    }

    public Bip32Path(Bip32Derivation component) {
        this(component, true);
    }

    public Bip32Path(long index, boolean relative) {
        this(Bip32Derivation.newNormal(index), relative);
    }

    public Bip32Path(long index) {
        this(index, true);
    }

    public Bip32Path(String path) {
        if (path == null || path.isEmpty())
            throw new Bip32Exception("invalid path");

        boolean relative = !path.startsWith("m/");
        String[] rawComponents = path.split("/");

        List<Bip32Derivation> components = new ArrayList<>();

        for (String rawComponent : rawComponents) {
            if (rawComponent.equals("m"))
                continue;

            Long index = null;
            try {
                index = Long.parseLong(rawComponent);
            } catch (NumberFormatException ignore) {
            }

            if (index != null) {
                components.add(Bip32Derivation.newNormal(index));
            } else if (rawComponent.endsWith("h") || rawComponent.endsWith("'")) {
                String canonicalRawComponent = rawComponent.substring(0, rawComponent.length() - 1);
                try {
                    index = Long.parseLong(canonicalRawComponent);
                } catch (NumberFormatException ignore) {
                }

                if (index != null) {
                    components.add(Bip32Derivation.newHardened(index));
                } else {
                    throw new Bip32Exception("invalid path");
                }

            } else {
                throw new Bip32Exception("invalid path");
            }
        }

        Bip32Path bip32Path = new Bip32Path(components.toArray(new Bip32Derivation[0]), relative);
        this.components = bip32Path.components;
        this.relative = bip32Path.relative;
        this.rawPath = bip32Path.rawPath;
    }

    public Bip32Derivation[] getComponents() {
        return components;
    }

    public long[] getRawPath() {
        return rawPath;
    }

    public boolean isRelative() {
        return relative;
    }

    public String getPath() {
        StringBuilder builder = new StringBuilder();
        if (!relative)
            builder.append("m/");

        for (int i = 0; i < components.length; i++) {
            Bip32Derivation component = components[i];
            if (component.isHardened()) {
                builder.append(component.getIndex()).append("h");
            } else {
                builder.append(component.getIndex());
            }

            if (i < components.length - 1) {
                builder.append("/");
            }
        }

        return builder.toString();
    }

    public Bip32Path chop(int depth) {
        if (depth > components.length)
            throw new Bip32Exception(INVALID_DEPTH);
        Bip32Derivation[] newComponents = Arrays.copyOfRange(components, depth, components.length);
        return new Bip32Path(newComponents, true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Bip32Path path = (Bip32Path) o;
        return relative == path.relative &&
               Arrays.equals(components, path.components) &&
               Arrays.equals(rawPath, path.rawPath);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(relative);
        result = 31 * result + Arrays.hashCode(components);
        result = 31 * result + Arrays.hashCode(rawPath);
        return result;
    }
}
