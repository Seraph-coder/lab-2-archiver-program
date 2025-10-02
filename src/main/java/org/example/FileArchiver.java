package org.example;

import org.example.compression.*;
import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Основной класс для работы с файлами: архивация и разархивация.
 * Позволяет выбрать алгоритм сжатия, читать и записывать файлы.
 * Пример использования:
 *   compress RLE input.txt output.rle
 *   decompress RLE output.rle restored.txt
 */
public class FileArchiver {
    public enum Method { RLE, LZW, HUFFMAN }

    /**
     * Получить байт-метку для алгоритма.
     */
    private static byte methodToByte(Method method) {
        return switch (method) {
            case RLE -> 0;
            case LZW -> 1;
            case HUFFMAN -> 2;
        };
    }
    /**
     * Получить алгоритм по байту-метке.
     */
    private static Method byteToMethod(byte b) {
        return switch (b) {
            case 0 -> Method.RLE;
            case 1 -> Method.LZW;
            case 2 -> Method.HUFFMAN;
            default -> throw new IllegalArgumentException("Unknown method byte: " + b);
        };
    }

    /**
     * Сжимает файл выбранным методом, добавляя байт-метку алгоритма.
     * @param inputPath путь к исходному файлу
     * @param outputPath путь к архиву
     * @param method выбранный алгоритм
     */
    public static void compressFile(String inputPath, String outputPath, Method method) throws IOException {
        Compressor compressor = getCompressor(method);
        byte[] input = readFile(inputPath);
        byte[] output = compressor.compress(input);
        byte[] archive = new byte[output.length + 1];
        archive[0] = methodToByte(method);
        System.arraycopy(output, 0, archive, 1, output.length);
        writeFile(outputPath, archive);
    }

    /**
     * Автоматически выбирает оптимальный алгоритм сжатия по анализу входных данных.
     * RLE — если много длинных повторов;
     * LZW — если много повторяющихся подстрок;
     * Huffman — если много уникальных символов.
     * @param data исходные данные
     * @return выбранный метод
     */
    public static Method autoSelectMethod(byte[] data) {
        // Оценка для RLE: максимальная длина повторов
        int maxRun = 1, curRun = 1;
        for (int i = 1; i < data.length; i++) {
            if (data[i] == data[i-1]) curRun++;
            else curRun = 1;
            if (curRun > maxRun) maxRun = curRun;
        }
        // Оценка для Huffman: количество уникальных символов
        Set<Byte> unique = new HashSet<>();
        for (byte b : data) unique.add(b);
        // Оценка для LZW: количество уникальных подстрок длины 3
        Set<String> substrings = new HashSet<>();
        for (int i = 0; i < data.length - 2; i++) {
            substrings.add(new String(data, i, 3));
        }
        // Простая эвристика
        if (maxRun > 10) return Method.RLE;
        if (substrings.size() < data.length / 2) return Method.LZW;
        return Method.HUFFMAN;
    }

    /**
     * Сжимает файл с автоматическим выбором алгоритма, добавляя байт-метку.
     * @param inputPath путь к исходному файлу
     * @param outputPath путь к архиву
     * @return выбранный алгоритм
     */
    public static Method compressFileAuto(String inputPath, String outputPath) throws IOException {
        byte[] input = readFile(inputPath);
        Method method = autoSelectMethod(input);
        Compressor compressor = getCompressor(method);
        byte[] output = compressor.compress(input);
        byte[] archive = new byte[output.length + 1];
        archive[0] = methodToByte(method);
        System.arraycopy(output, 0, archive, 1, output.length);
        writeFile(outputPath, archive);
        return method;
    }

    /**
     * Восстанавливает файл из архива, определяя алгоритм по байту-метке.
     * @param inputPath путь к архиву
     * @param outputPath путь к восстановленному файлу
     */
    public static void decompressFileAuto(String inputPath, String outputPath) throws IOException {
        byte[] archive = readFile(inputPath);
        Method method = byteToMethod(archive[0]);
        Compressor compressor = getCompressor(method);
        byte[] data = Arrays.copyOfRange(archive, 1, archive.length);
        byte[] restored = compressor.decompress(data);
        writeFile(outputPath, restored);
    }

    /**
     * Восстанавливает файл из архива выбранным методом (для совместимости).
     * @param inputPath путь к архиву
     * @param outputPath путь к восстановленному файлу
     * @param method выбранный алгоритм
     */
    public static void decompressFile(String inputPath, String outputPath, Method method) throws IOException {
        byte[] archive = readFile(inputPath);
        Compressor compressor = getCompressor(method);
        byte[] data = Arrays.copyOfRange(archive, 1, archive.length);
        byte[] restored = compressor.decompress(data);
        writeFile(outputPath, restored);
    }

    private static Compressor getCompressor(Method method) {
        return switch (method) {
            case RLE -> new RLECompressor();
            case LZW -> new LZWCompressor();
            case HUFFMAN -> new HuffmanCompressor();
        };
    }

    private static byte[] readFile(String path) throws IOException {
        try (FileInputStream fis = new FileInputStream(path)) {
            return fis.readAllBytes();
        }
    }

    private static void writeFile(String path, byte[] data) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(path)) {
            fos.write(data);
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("Usage: java FileArchiver <compress|decompress|auto> <method|auto> <input> <output>");
            System.out.println("Methods: RLE, LZW, HUFFMAN, auto");
            return;
        }
        String action = args[0];
        String methodArg = args[1];
        String input = args[2];
        String output = args[3];
        if (action.equalsIgnoreCase("auto") || methodArg.equalsIgnoreCase("auto")) {
            Method selected = compressFileAuto(input, output);
            System.out.println("Auto-selected method: " + selected);
        } else if (action.equalsIgnoreCase("compress")) {
            Method method = Method.valueOf(methodArg.toUpperCase());
            compressFile(input, output, method);
            System.out.println("Compressed " + input + " to " + output + " using " + method);
        } else if (action.equalsIgnoreCase("decompress")) {
            // Если метод не указан, используем автоматическую разархивацию
            if (methodArg.equalsIgnoreCase("auto")) {
                decompressFileAuto(input, output);
                System.out.println("Decompressed " + input + " to " + output + " (auto method)");
            } else {
                Method method = Method.valueOf(methodArg.toUpperCase());
                decompressFile(input, output, method);
                System.out.println("Decompressed " + input + " to " + output + " using " + method);
            }
        } else {
            System.out.println("Unknown action: " + action);
        }
    }
}
