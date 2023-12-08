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

public class solver071 {
    private static Pattern numberPattern = Pattern.compile("([0-9]+)$");
    private static Pattern handPattern = Pattern.compile("[23456789TJQKA]{5}");
    private static Pattern cardPattern = Pattern.compile("[23456789TJQKA]");
    private static Set<CardHand> hands = new TreeSet<>();

    private enum Card {
        c2,
        c3,
        c4,
        c5,
        c6,
        c7,
        c8,
        c9,
        cT,
        cJ,
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
                return cardCounts.keySet().size() == 5;
            }
        },
        OnePair {
            protected boolean handMatchesType(Map<Card, Integer> cardCounts) {
                return cardCounts.keySet().size() == 4;
            }
        },
        TwoPair {
            protected boolean handMatchesType(Map<Card, Integer> cardCounts) {
                return cardCounts.values().stream().mapToInt(i -> i).max().getAsInt() == 2
                        && cardCounts.keySet().size() == 3;
            }
        },
        ThreeOfAKind {
            protected boolean handMatchesType(Map<Card, Integer> cardCounts) {
                return cardCounts.values().stream().mapToInt(i -> i).max().getAsInt() == 3
                        && cardCounts.keySet().size() == 3;
            }
        },
        FullHouse {
            protected boolean handMatchesType(Map<Card, Integer> cardCounts) {
                return cardCounts.values().stream().mapToInt(i -> i).max().getAsInt() == 3
                        && cardCounts.keySet().size() == 2;
            }
        },
        FourOfAKind {
            protected boolean handMatchesType(Map<Card, Integer> cardCounts) {
                return cardCounts.values().stream().mapToInt(i -> i).max().getAsInt() == 4;
            }
        },
        FiveOfAKind {
            protected boolean handMatchesType(Map<Card, Integer> cardCounts) {
                return cardCounts.keySet().size() == 1;
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
            inputLines.forEach(solver071::processInputLine);
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
