package ru.yandex.practicum;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Wordle {

    private static final String DICTIONARY_FILE = "words_ru.txt";
    private static final String LOG_FILE = "wordle.log";

    public static void main(String[] args) {
        try (PrintWriter log = openLog(LOG_FILE)) {

            WordleDictionaryLoader loader = new WordleDictionaryLoader(log);
            WordleDictionary dictionary = loader.load(DICTIONARY_FILE);

            WordleGame game = new WordleGame(dictionary, log);
            Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

            playGame(game, scanner, log);

        } catch (WordleException e) {
            System.err.println("Ошибка запуска: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Непредвиденная ошибка: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    static void playGame(WordleGame game, Scanner scanner, PrintWriter log) {
        printWelcome(game);

        while (!game.isFinished()) {
            System.out.printf("%nПопыток осталось: %d%n", game.getAttemptsLeft());
            System.out.print("Введите слово (или Enter для подсказки): ");

            String input = scanner.nextLine();

            // Пустой ввод — запрос подсказки
            if (input.isBlank()) {
                String suggestion = game.getSuggestion();
                if (suggestion == null) {
                    System.out.println("Подсказок больше нет.");
                } else {
                    System.out.println("Подсказка: " + suggestion);
                }
                continue;
            }

            try {
                String hint = game.makeGuess(input);
                System.out.println(WordleDictionary.normalize(input.trim()));
                System.out.println(hint);
            } catch (InvalidInputException e) {
                System.out.println("Некорректный ввод: " + e.getMessage());
            } catch (WordNotFoundInDictionaryException e) {
                System.out.println(e.getMessage());
            }
        }

        System.out.println();
        if (game.isWon()) {
            System.out.println("🎉 Поздравляем! Вы угадали слово за "
                    + (WordleGame.MAX_ATTEMPTS - game.getAttemptsLeft()) + " попыток!");
        } else {
            System.out.println("Попытки закончились. Загаданное слово: " + game.getAnswer());
        }

        log.println("Игра завершена. Победа: " + game.isWon()
                + ", слово: " + game.getAnswer()
                + ", попыток использовано: " + (WordleGame.MAX_ATTEMPTS - game.getAttemptsLeft()));
        log.flush();
    }

    private static void printWelcome(WordleGame game) {
        System.out.println("Угадайте слово из 5 букв за " + WordleGame.MAX_ATTEMPTS + " попыток.");
        System.out.println("Подсказки: + = верная буква и позиция");
        System.out.println("           ^ = буква есть, но не на этом месте");
        System.out.println("           - = буквы нет в слове");
        System.out.println("Нажмите Enter без ввода для получения подсказки.");
    }

    private static PrintWriter openLog(String logFile) throws WordleException {
        try {
            return new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    new FileOutputStream(logFile, true),
                                    StandardCharsets.UTF_8)));
        } catch (IOException e) {
            throw new WordleException("Невозможно создать лог-файл: " + logFile, e);
        }
    }
}
