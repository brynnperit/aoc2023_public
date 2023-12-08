package com.brynnperit.aoc2023;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class solver072 {
    private static Pattern numberPattern = Pattern.compile("([0-9]+)$");
    private static Pattern handPattern = Pattern.compile("[23456789TJQKA]{5}");
    private static Pattern cardPattern = Pattern.compile("[23456789TJQKA]");
    private static Set<CardHand> hands = new TreeSet<>();

    private enum Card {
        cJ,
        c2,
        c3,
        c4,
        c5,
        c6,
        c7,
        c8,
        c9,
        cT,
        cQ,
        cK,
        cA;

        private static final Map<String, Card> stringToEnum = Stream.of(values())
                .collect(Collectors.toMap(c -> c.toString().substring(1), c -> c));

        public static Optional<Card> fromString(String symbol) {
            return Optional.ofNullable(stringToEnum.get(symbol));
        }
    }

    private enum CardHandType {
        HighCard {
            protected boolean handMatchesType(Map<Card, Integer> cardCounts) {
                return cardCounts.keySet().size() == 5 && !cardCounts.containsKey(Card.cJ);
            }
        },
        OnePair {
            protected boolean handMatchesType(Map<Card, Integer> cardCounts) {
                boolean onePairNoJs = cardCounts.keySet().size() == 4 && !cardCounts.containsKey(Card.cJ);
                boolean onePairOneJ = cardCounts.keySet().size() == 5 && cardCounts.containsKey(Card.cJ);
                return onePairNoJs || onePairOneJ;
            }
        },
        TwoPair {
            protected boolean handMatchesType(Map<Card, Integer> cardCounts) {
                int maxCardCount = cardCounts.values().stream().mapToInt(i -> i).max().getAsInt();
                // Can't get two pairs with jokers as they'd just become ThreeOfAKind or higher
                // instead
                boolean hasTwoPairsNoJs = maxCardCount == 2 && cardCounts.keySet().size() == 3
                        && !cardCounts.containsKey(Card.cJ);
                return hasTwoPairsNoJs;
            }
        },
        ThreeOfAKind {
            protected boolean handMatchesType(Map<Card, Integer> cardCounts) {
                int maxCardCount = cardCounts.values().stream().mapToInt(i -> i).max().getAsInt();
                boolean threeOfAKindNoJs = maxCardCount == 3
                        && cardCounts.keySet().size() == 3 && !cardCounts.containsKey(Card.cJ);
                // 2234J
                boolean threeOfAKindOneJ = cardCounts.keySet().size() == 4 && cardCounts.containsKey(Card.cJ)
                        && cardCounts.get(Card.cJ) == 1;
                // 234JJ
                boolean threeOfAKindTwoJs = cardCounts.keySet().size() == 4 && cardCounts.containsKey(Card.cJ)
                        && cardCounts.get(Card.cJ) == 2;
                return threeOfAKindNoJs || threeOfAKindOneJ || threeOfAKindTwoJs;
            }
        },
        FullHouse {
            protected boolean handMatchesType(Map<Card, Integer> cardCounts) {
                int maxCardCount = cardCounts.values().stream().mapToInt(i -> i).max().getAsInt();
                boolean fullHouseNoJs = maxCardCount == 3
                        && cardCounts.keySet().size() == 2 && !cardCounts.containsKey(Card.cJ);
                // 2233J
                boolean fullHouseOneJ = maxCardCount == 2 && cardCounts.keySet().size() == 3
                        && cardCounts.containsKey(Card.cJ) && cardCounts.get(Card.cJ) == 1;
                return fullHouseNoJs || fullHouseOneJ;
            }
        },
        FourOfAKind {
            protected boolean handMatchesType(Map<Card, Integer> cardCounts) {
                int maxCardCount = cardCounts.values().stream().mapToInt(i -> i).max().getAsInt();
                boolean fourOfAKindNoJs = maxCardCount == 4 && !cardCounts.containsKey(Card.cJ);
                boolean fourOfAKindOneJ = maxCardCount == 3
                        && cardCounts.keySet().size() == 3 && cardCounts.containsKey(Card.cJ)
                        && cardCounts.get(Card.cJ) == 1;
                boolean fourOfAKindTwoJs = maxCardCount == 2 && cardCounts.keySet().size() == 3
                        && cardCounts.containsKey(Card.cJ) && cardCounts.get(Card.cJ) == 2;
                boolean fourOfAKindThreeJs = maxCardCount == 3
                        && cardCounts.keySet().size() == 3 && cardCounts.containsKey(Card.cJ)
                        && cardCounts.get(Card.cJ) == 3;
                return fourOfAKindNoJs || fourOfAKindOneJ || fourOfAKindTwoJs || fourOfAKindThreeJs;
            }
        },
        FiveOfAKind {
            protected boolean handMatchesType(Map<Card, Integer> cardCounts) {
                int maxCardCount = cardCounts.values().stream().mapToInt(i -> i).max().getAsInt();
                boolean fiveOfAKindJsOrNot = cardCounts.keySet().size() == 1;
                boolean fiveOfAKindOneJ = maxCardCount == 4 && cardCounts.containsKey(Card.cJ)
                        && cardCounts.get(Card.cJ) == 1;
                boolean fiveOfAKindTwoJs = maxCardCount == 3
                        && cardCounts.keySet().size() == 2 && cardCounts.containsKey(Card.cJ)
                        && cardCounts.get(Card.cJ) == 2;
                boolean fiveOfAKindThreeJs = maxCardCount == 3
                        && cardCounts.keySet().size() == 2 && cardCounts.containsKey(Card.cJ)
                        && cardCounts.get(Card.cJ) == 3;
                boolean fiveOfAKindFourJs = maxCardCount == 4 && cardCounts.containsKey(Card.cJ)
                        && cardCounts.get(Card.cJ) == 4;
                return fiveOfAKindJsOrNot || fiveOfAKindOneJ || fiveOfAKindTwoJs || fiveOfAKindThreeJs
                        || fiveOfAKindFourJs;
            }
        };

        public static Optional<CardHandType> getCardHandType(CardHand cardsInHand) {
            CardHandType toReturn = null;
            Map<Card, Integer> cardCounts = cardsInHand.cardCounts();
            for (CardHandType handType : CardHandType.values()) {
                if (handType.handMatchesType(cardCounts)) {
                    toReturn = handType;
                    break;
                }
            }
            return Optional.ofNullable(toReturn);
        }

        protected abstract boolean handMatchesType(Map<Card, Integer> cardCounts);
    }

    private record CardHand(List<Card> cards, long bid, Map<Card, Integer> cardCounts, CardHandType cardHandType)
            implements Comparable<CardHand> {

        public Map<Card, Integer> cardCounts() {
            return Collections.unmodifiableMap(cardCounts);
        }

        private CardHand(List<Card> cards, long bid, Map<Card, Integer> cardCounts, CardHandType cardHandType) {
            this.cards = List.copyOf(cards);
            this.bid = bid;
            this.cardCounts = new EnumMap<>(Card.class);
            for (Card card : cards) {
                if (this.cardCounts.containsKey(card)) {
                    this.cardCounts.put(card, this.cardCounts.get(card) + 1);
                } else {
                    this.cardCounts.put(card, 1);
                }
            }
            this.cardHandType = CardHandType.getCardHandType(this).get();

        }

        public CardHand(List<Card> cards, long bid) {
            this(List.copyOf(cards), bid, null, null);
        }

        @Override
        public int compareTo(CardHand arg0) {
            int compareValue = this.cardHandType.compareTo(arg0.cardHandType);
            if (compareValue == 0) {
                Iterator<Card> ourCardIterator = cards.iterator();
                Iterator<Card> otherCardIterator = arg0.cards.iterator();
                while (compareValue == 0 && ourCardIterator.hasNext() && otherCardIterator.hasNext()) {
                    compareValue = ourCardIterator.next().compareTo(otherCardIterator.next());
                }
                if (compareValue == 0) {
                    compareValue = Long.compare(bid, arg0.bid);
                }
            }
            return compareValue;
        }
    };

    private static void processInputLine(String line) {
        Matcher handPatternMatcher = handPattern.matcher(line);
        Matcher numberPatternMatcher = numberPattern.matcher(line);
        handPatternMatcher.find();
        numberPatternMatcher.find();
        Matcher cardMatcher = cardPattern.matcher(handPatternMatcher.group());
        List<Card> cards = cardMatcher.results().map(i -> i.group()).map(Card::fromString).map(i -> i.get()).toList();
        hands.add(new CardHand(cards, Long.parseLong(numberPatternMatcher.group())));
    }

    private record rankAndBid(int rank, long bid) {
    };

    public static void main(String[] args) {
        long totalWinnings = -1;
        try (Stream<String> inputLines = Files.lines(new File("inputs/input_07").toPath())) {
            inputLines.forEach(solver072::processInputLine);
            List<CardHand> CardHandsInOrder = hands.stream().toList();
            totalWinnings = IntStream.range(1, CardHandsInOrder.size() + 1)
                    .mapToObj(i -> new rankAndBid(i, CardHandsInOrder.get(i - 1).bid())).mapToLong(r -> r.bid * r.rank)
                    .sum();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Total winnings are: " + totalWinnings);
    }
}
