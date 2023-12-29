package com.brynnperit.aoc2023.week3.solver182;

public class Edge implements Comparable<Edge> {
        private Direction direction;
        private final long length;
        private Edge previous;
        private Edge next;
        private Coord2D startCoord;
        private Coord2D endCoord;
        private Coord2D lowerCoord;
        private Coord2D higherCoord;
        private Direction interiorSide;

        public Edge(Direction direction, long length, Edge previous, Edge next, Coord2D startCoord) {
            this.direction = direction;
            this.length = length;
            this.previous = previous;
            this.next = next;
            this.startCoord = startCoord;
            endCoord = new Coord2D(startCoord);
            endCoord.go(direction, length);
            if (startCoord.compareTo(endCoord) < 0) {
                this.lowerCoord = startCoord;
                this.higherCoord = endCoord;
            } else {
                this.lowerCoord = endCoord;
                this.higherCoord = startCoord;
            }
        }

        public Coord2D lowerCoord() {
            return lowerCoord;
        }

        public Coord2D higherCoord() {
            return higherCoord;
        }

        public Direction interiorSide() {
            return interiorSide;
        }

        public Coord2D startCoord() {
            return startCoord;
        }

        public void setInteriorSide(Direction interior) {
            this.interiorSide = interior;
        }

        public Edge previous() {
            return previous;
        }

        public Edge next() {
            return next;
        }

        public void setPrevious(Edge previous) {
            this.previous = previous;
        }

        public void setNext(Edge next) {
            this.next = next;
        }

        public Coord2D endCoord() {
            return endCoord;
        }

        public Direction direction() {
            return direction;
        }

        public long length() {
            return length;
        }

        @Override
        public int compareTo(Edge other) {
            int value = lowerCoord.compareTo(other.lowerCoord);
            if (value == 0) {
                value = higherCoord.compareTo(other.higherCoord);
            }
            return value;
        }

        public int compareToY(Edge other) {
            int value = lowerCoord.compareToY(other.lowerCoord);
            if (value == 0) {
                value = higherCoord.compareToY(other.higherCoord);
            }
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (!(o instanceof Edge))
                return false;
            Edge other = (Edge) o;
            return startCoord.equals(other.startCoord) && endCoord.equals(other.endCoord);
        }
    }