package ru.yandex.practicum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.PrintWriter;

public class WordleGame {

    public static final int MAX_ATTEMPTS = 6;

    private final WordleDictionary dictionary;
    private final PrintWriter log;
    private final String answer;

    private int attemptsLeft;
    private boolean won;
    private boolean finished;

    private final List<String> guesses = new ArrayList<>();
    private final List<String> hints = new ArrayList<>();

    private List<String> candidates;

    private final List<String> usedHints = new ArrayList<>();

    public WordleGame(WordleDictionary dictionary, PrintWriter log) {
        this.dictionary = dictionary;
        this.log = log;
        this.answer = dictionary.getRandomWord();
        this.attemptsLeft = MAX_ATTEMPTS;
        this.won = false;
        this.finished = false;
        this.candidates = new ArrayList<>(dictionary.getWords());

        log.println("Загадано слово: " + answer);
        log.flush();
    }

    public WordleGame(WordleDictionary dictionary, PrintWriter log, String fixedAnswer) {
        this.dictionary = dictionary;
        this.log = log;
        String normalized = WordleDictionary.normalize(fixedAnswer);
        if (!dictionary.contains(normalized)) {
            throw new RuntimeException("Слово-ответ отсутствует в словаре: " + fixedAnswer);
        }
        this.answer = normalized;
        this.attemptsLeft = MAX_ATTEMPTS;
        this.won = false;
        this.finished = false;
        this.candidates = new ArrayList<>(dictionary.getWords());

        log.println("Загадано слово (фиксировано): " + answer);
        log.flush();
    }


    public String makeGuess(String rawInput)
            throws InvalidInputException, WordNotFoundInDictionaryException {

        if (finished) {
            throw new RuntimeException("Игра уже завершена");
        }

        String word = validateInput(rawInput);

        if (!dictionary.contains(word)) {
            throw new WordNotFoundInDictionaryException(word);
        }

        attemptsLeft--;
        String hint = WordleDictionary.computeHint(word, answer);
        guesses.add(word);
        hints.add(hint);

        candidates = dictionary.filterCandidates(guesses, hints);

        candidates.removeAll(usedHints);

        log.println("Ход: " + word + " → " + hint + " | осталось попыток: " + attemptsLeft
                + " | кандидатов: " + candidates.size());
        log.flush();

        if (word.equals(answer)) {
            won = true;
            finished = true;
        } else if (attemptsLeft == 0) {
            finished = true;
        }

        return hint;
    }

    public String getSuggestion() {
        List<String> available = new ArrayList<>(candidates);
        available.removeAll(guesses);
        available.remove(answer);

        if (available.isEmpty()) {
            available = new ArrayList<>(dictionary.getWords());
            available.removeAll(guesses);
            available.removeAll(usedHints);
        }

        if (available.isEmpty()) return null;

        Collections.shuffle(available);
        String suggestion = available.getFirst();
        usedHints.add(suggestion);
        return suggestion;
    }

    private String validateInput(String rawInput)
            throws InvalidInputException {

        if (rawInput == null || rawInput.isBlank()) {
            throw new InvalidInputException("Введите слово из 5 русских букв");
        }

        String word = WordleDictionary.normalize(rawInput.trim());

        if (word.length() != WordleDictionary.WORD_LENGTH) {
            throw new InvalidInputException(
                    "Слово должно состоять из " + WordleDictionary.WORD_LENGTH + " букв, введено: " + word.length());
        }

        for (char c : word.toCharArray()) {
            if (c < 'а' || c > 'я') {
                throw new InvalidInputException("Слово должно содержать только русские буквы");
            }
        }

        return word;
    }

    public boolean isWon() {
        return won;
    }

    public boolean isFinished() {
        return finished;
    }

    public int getAttemptsLeft() {
        return attemptsLeft;
    }

    public String getAnswer() {
        return answer;
    }

    public List<String> getGuesses() {
        return Collections.unmodifiableList(guesses);
    }

    public List<String> getHints() {
        return Collections.unmodifiableList(hints);
    }
}
