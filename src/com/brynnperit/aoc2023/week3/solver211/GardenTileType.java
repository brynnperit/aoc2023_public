package com.brynnperit.aoc2023.week3.solver211;

public enum GardenTileType {
        garden('.'),
        rock('#'),
        start('S');

        private final char symbol;

        private GardenTileType(char symbol) {
            this.symbol = symbol;
        }

        public char symbol() {
            return symbol;
        }

        public static GardenTileType fromSymbol(char symbol){
            for (GardenTileType type:GardenTileType.values()){
                if (type.symbol == symbol){
                    return type;
                }
            }
            return null;
        }
    }