package com.brynnperit.aoc2023.week3.solver182;

public class Coord2D implements Comparable<Coord2D> {
        private long x;
        private long y;

        public Coord2D(long x, long y) {
            this.x = x;
            this.y = y;
        }

        public Coord2D(Coord2D other) {
            this.x = other.x;
            this.y = other.y;
        }

        public long x() {
            return x;
        }

        public long y() {
            return y;
        }

        public void go(Direction toGo, long length) {
            x = toGo.goX(x, length);
            y = toGo.goY(y, length);
        }

        @Override
        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (!(o instanceof Coord2D))
                return false;
            Coord2D other = (Coord2D) o;
            return x == other.x && y == other.y;
        }

        public long distanceTo(Coord2D other){
            return Math.abs(x-other.x)+Math.abs(y-other.y);
        }

        public void addX(long x) {
            this.x += x;
        }

        public void addY(long y) {
            this.y += y;
        }

        public void setX(long x) {
            this.x = x;
        }

        public void setY(long y) {
            this.y = y;
        }

        public boolean isInBounds(long maxX, long maxY) {
            return x >= 0 && x < maxX && y >= 0 && y < maxY;
        }

        @Override
        public int compareTo(Coord2D other) {
            int value = Long.compare(this.x, other.x);
            if (value == 0) {
                value = Long.compare(this.y, other.y);
            }
            return value;
        }

        public int compareToY(Coord2D other) {
            int value = Long.compare(this.y, other.y);
            if (value == 0) {
                value = Long.compare(this.x, other.x);
            }
            return value;
        }
    }