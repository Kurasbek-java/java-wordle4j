package ru.yandex.practicum;

public class WordNotFoundInDictionaryException extends Exception {

    public WordNotFoundInDictionaryException(String word) {
        super("Слово «" + word + "» не найдено в словаре");
    }
}
