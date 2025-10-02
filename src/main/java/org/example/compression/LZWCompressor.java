package org.example.compression;

import java.util.*;

/**
 * Реализация алгоритма LZW (Lempel-Ziv-Welch).
 * Сжимает повторяющиеся подстроки, используя динамический словарь.
 * Подходит для текстов с повторяющимися фрагментами.
 */
public class LZWCompressor implements Compressor {
    private static final int DICT_SIZE = 256;

    /**
     * Сжимает данные методом LZW.
     * @param input исходные данные
     * @return сжатый массив байт
     */
    @Override
    public byte[] compress(byte[] input) {
        Map<String, Integer> dict = new HashMap<>();
        for (int i = 0; i < DICT_SIZE; i++) dict.put("" + (char) i, i);
        String w = "";
        List<Integer> result = new ArrayList<>();
        for (byte b : input) {
            char c = (char) (b & 0xFF);
            String wc = w + c;
            if (dict.containsKey(wc)) {
                w = wc;
            } else {
                result.add(dict.get(w));
                dict.put(wc, dict.size());
                w = "" + c;
            }
        }
        if (!w.isEmpty()) result.add(dict.get(w));
        // Преобразуем список чисел в байты (2 байта на число)
        byte[] out = new byte[result.size() * 2];
        for (int i = 0; i < result.size(); i++) {
            int code = result.get(i);
            out[i * 2] = (byte) ((code >> 8) & 0xFF);
            out[i * 2 + 1] = (byte) (code & 0xFF);
        }
        return out;
    }

    /**
     * Восстанавливает исходные данные из LZW-сжатого массива.
     * @param input сжатые данные
     * @return восстановленный массив байт
     */
    @Override
    public byte[] decompress(byte[] input) {
        List<Integer> codes = new ArrayList<>();
        for (int i = 0; i < input.length; i += 2) {
            int code = ((input[i] & 0xFF) << 8) | (input[i + 1] & 0xFF);
            codes.add(code);
        }
        Map<Integer, String> dict = new HashMap<>();
        for (int i = 0; i < DICT_SIZE; i++) dict.put(i, "" + (char) i);
        // Исправлено: получаем первый код как int
        int firstCode = codes.get(0);
        String w = "" + (char) firstCode;
        StringBuilder result = new StringBuilder(w);
        for (int i = 1; i < codes.size(); i++) {
            int k = codes.get(i);
            String entry;
            if (dict.containsKey(k)) {
                entry = dict.get(k);
            } else if (k == dict.size()) {
                entry = w + w.charAt(0);
            } else {
                throw new IllegalArgumentException("Bad LZW code: " + k);
            }
            result.append(entry);
            dict.put(dict.size(), w + entry.charAt(0));
            w = entry;
        }
        byte[] out = new byte[result.length()];
        for (int i = 0; i < result.length(); i++) out[i] = (byte) result.charAt(i);
        return out;
    }
}
