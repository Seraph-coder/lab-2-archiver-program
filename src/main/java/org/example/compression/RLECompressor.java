package org.example.compression;

import java.util.ArrayList;
import java.util.List;

/**
 * Реализация алгоритма RLE (Run-Length Encoding).
 * Сжимает повторяющиеся последовательности одинаковых байтов.
 * Если подряд идут более 3 одинаковых байта, они заменяются на флажок, байт и количество повторов.
 */
public class RLECompressor implements Compressor {
    private static final byte FLAG = (byte) 0xFF; // Флажок для RLE

    /**
     * Сжимает данные методом RLE.
     * @param data исходные данные
     * @return сжатый массив байт
     */
    @Override
    public byte[] compress(byte[] data) {
        List<Byte> result = new ArrayList<>();
        int i = 0;
        while (i < data.length) {
            int runLength = 1;
            while (i + runLength < data.length && data[i] == data[i + runLength] && runLength < 255) {
                runLength++;
            }
            if (runLength > 3) {
                result.add(FLAG);
                result.add(data[i]);
                result.add((byte) runLength);
                i += runLength;
            } else {
                result.add(data[i]);
                i++;
            }
        }
        byte[] out = new byte[result.size()];
        for (int j = 0; j < result.size(); j++) out[j] = result.get(j);
        return out;
    }

    /**
     * Восстанавливает исходные данные из RLE-сжатого массива.
     * @param data сжатые данные
     * @return восстановленный массив байт
     */
    @Override
    public byte[] decompress(byte[] data) {
        List<Byte> result = new ArrayList<>();
        int i = 0;
        while (i < data.length) {
            if (data[i] == FLAG && i + 2 < data.length) {
                byte value = data[i + 1];
                int count = data[i + 2] & 0xFF;
                for (int j = 0; j < count; j++) result.add(value);
                i += 3;
            } else {
                result.add(data[i]);
                i++;
            }
        }
        byte[] out = new byte[result.size()];
        for (int j = 0; j < result.size(); j++) out[j] = result.get(j);
        return out;
    }
}
