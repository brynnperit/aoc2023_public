package com.brynnperit.aoc2023.week3.solver162;

import java.util.*;

public record Contraption(EnumMap<Direction, Contraption> orthogonalConnectedContraptions, Coord2D position,
            ContraptionType type) implements Comparable<Contraption> {
        public Contraption(Coord2D position, ContraptionType type) {
            this(new EnumMap<>(Direction.class), position, type);
        }

        public Optional<Contraption> get(Direction direction) {
            return Optional.ofNullable(orthogonalConnectedContraptions.get(direction));
        }

        public List<Ray> getRays(Direction encounterDirection) {
            Map<Contraption, EnumSet<Direction>> completedContraptions = new TreeMap<>();
            Deque<Direction> allDirections = new ArrayDeque<>();
            Deque<Contraption> allContraptions = new ArrayDeque<>();
            List<Ray> rays = new ArrayList<>();

            allDirections.push(encounterDirection);
            allContraptions.push(this);
            completedContraptions.computeIfAbsent(this, i -> EnumSet.noneOf(Direction.class))
                    .add(encounterDirection);

            while (!allDirections.isEmpty()) {
                // TODO: The iterator doesn't work as advertised, just do int iteration.
                Direction currentDirection = allDirections.pop();
                Contraption currentContraption = allContraptions.pop();

                EnumSet<Direction> resultDirections = currentContraption.type.GetDirections(currentDirection);
                for (Direction resultDirection : resultDirections) {
                    Optional<Contraption> resultContraption = currentContraption.get(resultDirection);
                    if (resultContraption.isPresent()) {
                        if (!completedContraptions
                                .computeIfAbsent(resultContraption.get(), i -> EnumSet.noneOf(Direction.class))
                                .contains(resultDirection)) {
                            Contraption nextContraption = resultContraption.get();
                            // System.out.printf("Current contraption: %d %d %s %s, next contraption: %d %d
                            // %s %s%n",
                            // currentContraption.position.x, currentContraption.position.y,
                            // currentDirection.toString(), currentContraption.type.symbol,
                            // nextContraption.position.x, nextContraption.position.y,
                            // resultDirection.toString(),
                            // nextContraption.type.symbol);
                            completedContraptions.get(nextContraption).add(resultDirection);
                            allDirections.push(resultDirection);
                            allContraptions.push(nextContraption);
                        }
                    }
                }
            }

            for (Contraption completeContraption : completedContraptions.keySet()) {
                for (Direction completedDirection : completedContraptions.get(completeContraption)) {
                    Optional<Contraption> optionalContraption = completeContraption.get(completedDirection.opposite());
                    if (optionalContraption.isPresent()) {
                        rays.add(new Ray(completeContraption.position(),
                                optionalContraption.get().position()));
                    }
                }
            }
            return rays;
        }

        @Override
        public int compareTo(Contraption other) {
            int result = position.compareTo(other.position);
            if (result == 0) {
                result = type.compareTo(other.type);
            }
            return result;
        }
    }