package ru.yandex.practicum;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class WordleDictionaryLoader {

    private final PrintWriter log;

    public WordleDictionaryLoader(PrintWriter log) {
        this.log = log;
    }

    public WordleDictionary load(String filePath) throws WordleException {
        List<String> allWords = readAllLines(filePath);
        List<String> gameWords = filterGameWords(allWords);

        if (gameWords.isEmpty()) {
            throw new WordleException("Словарь пуст: не найдено подходящих слов в файле " + filePath);
        }

        log.println("Загружено слов для игры: " + gameWords.size() + " из " + allWords.size());
        log.flush();

        return new WordleDictionary(gameWords);
    }

    private List<String> readAllLines(String filePath) throws WordleException {
        List<String> lines = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            throw new WordleException("Файл словаря не найден: " + filePath);
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line.trim());
            }
        } catch (IOException e) {
            throw new WordleException("Ошибка при чтении файла словаря: " + e.getMessage(), e);
        }

        log.println("Прочитано строк из файла: " + lines.size());
        return lines;
    }

    private List<String> filterGameWords(List<String> allWords) {
        List<String> result = new ArrayList<>();

        for (String raw : allWords) {
            if (raw.isEmpty()) continue;

            String normalized = WordleDictionary.normalize(raw);

            // Только слова ровно в 5 букв, состоящие из кириллических символов
            if (normalized.length() == WordleDictionary.WORD_LENGTH && isCyrillic(normalized)) {
                result.add(normalized);
            }
        }

        return result;
    }

    private boolean isCyrillic(String word) {
        for (char c : word.toCharArray()) {
            if (c < 'а' || c > 'я') {
                return false;
            }
        }
        return true;
    }
}
