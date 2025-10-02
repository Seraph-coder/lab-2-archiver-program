package org.example.compression;

/**
 * Интерфейс для алгоритмов сжатия и восстановления данных.
 * Реализует методы compress и decompress для работы с байтовыми массивами.
 */
public interface Compressor {
    /**
     * Сжимает входной массив байт.
     * @param data исходные данные
     * @return сжатые данные
     */
    byte[] compress(byte[] data);

    /**
     * Восстанавливает исходные данные из сжатого массива байт.
     * @param data сжатые данные
     * @return восстановленные данные
     */
    byte[] decompress(byte[] data);
}
