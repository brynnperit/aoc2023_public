package com.brynnperit.aoc2023.week4.solver241;

import java.math.*;
import java.util.function.*;

public record Vector3D(BigDecimal x, BigDecimal y, BigDecimal z) {
    public Vector3D(long x, long y, long z) {
        this(new BigDecimal(x), new BigDecimal(y), new BigDecimal(z));
    }

    public static Function<Vector3D, BigDecimal> xGetter = v -> v.x();
    public static Function<Vector3D, BigDecimal> yGetter = v -> v.y();
    public static Function<Vector3D, BigDecimal> zGetter = v -> v.z();

    public Vector3D asUnitVector2D() {
        BigDecimal length = length2D();
        return new Vector3D(x.divide(length), y.divide(length), BigDecimal.ZERO);
    }

    public BigDecimal length2D() {
        return x.pow(2).add(y.pow(2)).sqrt(MathContext.DECIMAL128);
    }

    public BigDecimal length() {
        return x.pow(2).add(y.pow(2)).add(z.pow(2)).sqrt(MathContext.DECIMAL128);
    }

    public Vector3D subtract(Vector3D other) {
        return new Vector3D(x.subtract(other.x), y.subtract(other.y), z.subtract(other.z));
    }

    public BigDecimal dot2D(Vector3D other) {
        return x.multiply(other.x).add(y.multiply(other.y));
    }

    public BigDecimal dot(Vector3D other) {
        return x.multiply(other.x).add(y.multiply(other.y)).add(z.multiply(other.z));
    }

    public Vector3D multiply(long factor) {
        return multiply(new BigDecimal(factor));
    }

    public Vector3D multiply(BigDecimal factor) {
        return new Vector3D(x.multiply(factor), y.multiply(factor), z.multiply(factor));
    }

    public Vector3D divide(long factor) {
        BigDecimal bigFactor = new BigDecimal(factor);
        return divide(bigFactor);
    }

    public Vector3D divide(BigDecimal factor) {
        return new Vector3D(x.divide(factor, MathContext.DECIMAL128), y.divide(factor, MathContext.DECIMAL128), z.divide(factor, MathContext.DECIMAL128));
    }

    public Vector3D add(Vector3D other) {
        return new Vector3D(x.add(other.x), y.add(other.y), z.add(other.z));
    }

    public Vector3D cross(Vector3D other) {
        BigDecimal newX = y.multiply(other.z).subtract(z.multiply(other.y));
        BigDecimal newY = z.multiply(other.x).subtract(x.multiply(other.z));
        BigDecimal newZ = x.multiply(other.y).subtract(y.multiply(other.x));
        return new Vector3D(newX, newY, newZ);
    }

    @Override
    public String toString() {
        return String.format("%.1f, %.1f, %.1f", x, y, z);
    }

    public boolean containsFractions() {
        return (x.stripTrailingZeros().scale() > 0 || y.stripTrailingZeros().scale() > 0 || z.stripTrailingZeros().scale() > 0);
    }
}
