package ru.yandex.practicum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WordleTest {

    private static final List<String> WORDS = Arrays.asList(
            "герой", "гонец", "ответ", "слово", "книга", "земля", "город", "парус", "отвес"
    );

    private WordleDictionary dictionary;
    private PrintWriter log;

    @BeforeEach
    void setUp() {
        dictionary = new WordleDictionary(WORDS);
        // В тестах пишем лог в System.out
        log = new PrintWriter(System.out, true);
    }

    private WordleGame gameWith(String answer) {
        return new WordleGame(dictionary, log, answer);
    }

    // ---- Базовое состояние ----

    @Test
    void initialState_6AttemptsLeft() {
        WordleGame game = gameWith("герой");
        assertEquals(WordleGame.MAX_ATTEMPTS, game.getAttemptsLeft());
        assertFalse(game.isFinished());
        assertFalse(game.isWon());
    }

    // ---- Корректный ход ----

    @Test
    void makeGuess_correctWord_returnsHint()
            throws InvalidInputException, WordNotFoundInDictionaryException {
        WordleGame game = gameWith("герой");
        String hint = game.makeGuess("гонец");
        assertEquals("+^-^-", hint);
        assertEquals(5, game.getAttemptsLeft());
    }

    @Test
    void makeGuess_rightAnswer_setsWon()
            throws InvalidInputException, WordNotFoundInDictionaryException {
        WordleGame game = gameWith("герой");
        String hint = game.makeGuess("герой");
        assertEquals("+++++", hint);
        assertTrue(game.isWon());
        assertTrue(game.isFinished());
    }

    @Test
    void makeGuess_decreasesAttempts()
            throws InvalidInputException, WordNotFoundInDictionaryException {
        WordleGame game = gameWith("герой");
        game.makeGuess("гонец");
        assertEquals(5, game.getAttemptsLeft());
    }

    // ---- Исчерпание попыток ----

    @Test
    void makeGuess_sixWrongGuesses_gameOver()
            throws InvalidInputException, WordNotFoundInDictionaryException {
        WordleGame game = gameWith("герой");
        String[] wrongWords = {"гонец", "ответ", "слово", "книга", "земля", "город"};
        for (String w : wrongWords) {
            game.makeGuess(w);
        }
        assertTrue(game.isFinished());
        assertFalse(game.isWon());
        assertEquals(0, game.getAttemptsLeft());
    }

    // ---- Валидация ввода ----

    @Test
    void makeGuess_emptyString_throwsInvalidInput() {
        WordleGame game = gameWith("герой");
        assertThrows(InvalidInputException.class, () -> game.makeGuess(""));
    }

    @Test
    void makeGuess_tooShortWord_throwsInvalidInput() {
        WordleGame game = gameWith("герой");
        assertThrows(InvalidInputException.class, () -> game.makeGuess("год"));
    }

    @Test
    void makeGuess_latinLetters_throwsInvalidInput() {
        WordleGame game = gameWith("герой");
        assertThrows(InvalidInputException.class, () -> game.makeGuess("hello"));
    }

    @Test
    void makeGuess_wordNotInDictionary_throwsWordNotFound() {
        WordleGame game = gameWith("герой");
        assertThrows(WordNotFoundInDictionaryException.class,
                () -> game.makeGuess("ааааа"));
    }

    // ---- Нормализация ввода ----

    @Test
    void makeGuess_uppercaseInput_normalizedAndAccepted()
            throws InvalidInputException, WordNotFoundInDictionaryException {
        WordleGame game = gameWith("герой");
        String hint = game.makeGuess("ГЕРОЙ");
        assertEquals("+++++", hint);
        assertTrue(game.isWon());
    }

    // ---- Подсказки ----

    @Test
    void getSuggestion_atStart_returnsValidWord() {
        WordleGame game = gameWith("герой");
        String suggestion = game.getSuggestion();
        assertNotNull(suggestion);
        assertTrue(dictionary.contains(suggestion));
    }

    @Test
    void getSuggestion_noRepetition() throws InvalidInputException, WordNotFoundInDictionaryException {
        WordleGame game = gameWith("герой");
        game.makeGuess("гонец");

        // Получаем несколько подсказок и проверяем, что они не повторяются
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (int i = 0; i < 5; i++) {
            String s = game.getSuggestion();
            if (s != null) {
                assertFalse(seen.contains(s), "Подсказка повторилась: " + s);
                seen.add(s);
            }
        }
    }

    // ---- Состояние после хода ----

    @Test
    void getGuessesAndHints_afterGuess_recordedCorrectly()
            throws InvalidInputException, WordNotFoundInDictionaryException {
        WordleGame game = gameWith("герой");
        game.makeGuess("гонец");
        assertEquals(List.of("гонец"), game.getGuesses());
        assertEquals(List.of("+^-^-"), game.getHints());
    }

    // ---- Запрет хода после окончания игры ----

    @Test
    void makeGuess_afterGameFinished_throwsRuntimeException()
            throws InvalidInputException, WordNotFoundInDictionaryException {
        WordleGame game = gameWith("герой");
        game.makeGuess("герой"); // выигрыш
        assertThrows(RuntimeException.class, () -> game.makeGuess("гонец"));
    }
}
