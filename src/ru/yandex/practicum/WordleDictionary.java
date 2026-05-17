package ru.yandex.practicum;

import java.util.*;

public class WordleDictionary {

    public static final int WORD_LENGTH = 5;

    private final List<String> words;

    public WordleDictionary(List<String> words) {
        this.words = new ArrayList<>(words);
    }

    public List<String> getWords() {
        return Collections.unmodifiableList(words);
    }

    public boolean contains(String word) {
        return words.contains(normalize(word));
    }

    public boolean isEmpty() {
        return words.isEmpty();
    }

    public int size() {
        return words.size();
    }

    public String getRandomWord() {
        if (words.isEmpty()) {
            throw new RuntimeException("Словарь пуст — невозможно выбрать слово");
        }
        return words.get(new Random().nextInt(words.size()));
    }

    public static String normalize(String word) {
        if (word == null) return null;
        return word.toLowerCase().replace('ё', 'е');
    }

    public static String computeHint(String guess, String answer) {
        if (guess.length() != WORD_LENGTH || answer.length() != WORD_LENGTH) {
            throw new IllegalArgumentException("Слова должны быть длиной " + WORD_LENGTH);
        }

        char[] hint = new char[WORD_LENGTH];
        int[] answerCharCount = new int[Character.MAX_VALUE + 1];

        for (int i = 0; i < WORD_LENGTH; i++) {
            if (guess.charAt(i) == answer.charAt(i)) {
                hint[i] = '+';
            } else {
                answerCharCount[answer.charAt(i)]++;
            }
        }

        for (int i = 0; i < WORD_LENGTH; i++) {
            if (hint[i] == '+') continue;
            char c = guess.charAt(i);
            if (answerCharCount[c] > 0) {
                hint[i] = '^';
                answerCharCount[c]--;
            } else {
                hint[i] = '-';
            }
        }

        return new String(hint);
    }

    /**
     * Фильтрует словарь по всем введённым словам и их подсказкам.
     */
    public List<String> filterCandidates(List<String> guesses, List<String> hints) {
        List<String> candidates = new ArrayList<>(words);

        for (int g = 0; g < guesses.size(); g++) {
            String guess = guesses.get(g);
            String hint = hints.get(g);
            candidates.removeIf(word -> !isCompatible(word, guess, hint));
        }

        return candidates;
    }

    public static boolean isCompatible(String candidate, String guess, String hint) {
        int[] candidateCount = new int[Character.MAX_VALUE + 1];
        for (int i = 0; i < WORD_LENGTH; i++) {
            if (hint.charAt(i) != '+') {
                candidateCount[candidate.charAt(i)]++;
            }
        }

        for (int i = 0; i < WORD_LENGTH; i++) {
            char g = guess.charAt(i);
            char c = candidate.charAt(i);
            char h = hint.charAt(i);

            switch (h) {
                case '+':
                    if (c != g) return false;
                    break;
                case '^':
                    if (c == g) return false;
                    if (candidateCount[g] <= 0) return false;
                    candidateCount[g]--;
                    break;
                case '-':
                    if (candidateCount[g] > 0) return false;
                    break;
                default:
                    throw new IllegalArgumentException("Неизвестный символ подсказки: " + h);
            }
        }
        return true;
    }
}
