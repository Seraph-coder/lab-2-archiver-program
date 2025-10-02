package org.example.compression;

import java.util.*;

/**
 * Реализация алгоритма Хаффмена для сжатия данных.
 * Строит оптимальное префиксное дерево на основе частот символов.
 * Дерево сериализуется вместе с данными для восстановления.
 */
public class HuffmanCompressor implements Compressor {
    private static class Node implements Comparable<Node> {
        final byte symbol;
        final int freq;
        final Node left, right;
        Node(byte symbol, int freq, Node left, Node right) {
            this.symbol = symbol;
            this.freq = freq;
            this.left = left;
            this.right = right;
        }
        boolean isLeaf() { return left == null && right == null; }
        @Override public int compareTo(Node o) { return Integer.compare(freq, o.freq); }
    }

    private static Map<Byte, String> buildCode(Node root) {
        Map<Byte, String> code = new HashMap<>();
        buildCodeRec(root, "", code);
        return code;
    }
    private static void buildCodeRec(Node node, String s, Map<Byte, String> code) {
        if (node.isLeaf()) code.put(node.symbol, s);
        else {
            buildCodeRec(node.left, s + "0", code);
            buildCodeRec(node.right, s + "1", code);
        }
    }

    /**
     * Сжимает данные методом Хаффмена.
     * @param data исходные данные
     * @return сжатый массив байт с сериализованным деревом
     */
    @Override
    public byte[] compress(byte[] data) {
        // Подсчет частот
        Map<Byte, Integer> freq = new HashMap<>();
        for (byte b : data) freq.put(b, freq.getOrDefault(b, 0) + 1);
        // Построение дерева
        PriorityQueue<Node> pq = new PriorityQueue<>();
        for (Map.Entry<Byte, Integer> e : freq.entrySet())
            pq.add(new Node(e.getKey(), e.getValue(), null, null));
        while (pq.size() > 1) {
            Node l = pq.poll(), r = pq.poll();
            pq.add(new Node((byte)0, l.freq + r.freq, l, r));
        }
        Node root = pq.poll();
        Map<Byte, String> code = buildCode(root);
        // Кодирование
        StringBuilder encoded = new StringBuilder();
        for (byte b : data) encoded.append(code.get(b));
        // Сохраняем дерево (префиксный код)
        List<Byte> treeBytes = new ArrayList<>();
        serializeTree(root, treeBytes);
        byte[] treeArr = new byte[treeBytes.size()];
        for (int i = 0; i < treeBytes.size(); i++) treeArr[i] = treeBytes.get(i);
        // Кодируем строку в байты
        int len = (encoded.length() + 7) / 8;
        byte[] encodedArr = new byte[len];
        for (int i = 0; i < encoded.length(); i++) {
            int idx = i / 8;
            int bit = encoded.charAt(i) == '1' ? 1 : 0;
            encodedArr[idx] |= bit << (7 - (i % 8));
        }
        // Формируем итоговый массив: [длина дерева][дерево][длина данных][данные][длина исходных данных]
        byte[] out = new byte[4 + treeArr.length + 4 + encodedArr.length + 4];
        out[0] = (byte) ((treeArr.length >> 24) & 0xFF);
        out[1] = (byte) ((treeArr.length >> 16) & 0xFF);
        out[2] = (byte) ((treeArr.length >> 8) & 0xFF);
        out[3] = (byte) (treeArr.length & 0xFF);
        System.arraycopy(treeArr, 0, out, 4, treeArr.length);
        int offset = 4 + treeArr.length;
        out[offset] = (byte) ((encodedArr.length >> 24) & 0xFF);
        out[offset+1] = (byte) ((encodedArr.length >> 16) & 0xFF);
        out[offset+2] = (byte) ((encodedArr.length >> 8) & 0xFF);
        out[offset+3] = (byte) (encodedArr.length & 0xFF);
        System.arraycopy(encodedArr, 0, out, offset+4, encodedArr.length);
        // Сохраняем длину исходных данных
        int dataLenOffset = offset + 4 + encodedArr.length;
        int originalLen = data.length;
        out[dataLenOffset] = (byte) ((originalLen >> 24) & 0xFF);
        out[dataLenOffset+1] = (byte) ((originalLen >> 16) & 0xFF);
        out[dataLenOffset+2] = (byte) ((originalLen >> 8) & 0xFF);
        out[dataLenOffset+3] = (byte) (originalLen & 0xFF);
        return out;
    }

    private static void serializeTree(Node node, List<Byte> out) {
        if (node.isLeaf()) {
            out.add((byte)1);
            out.add(node.symbol);
        } else {
            out.add((byte)0);
            serializeTree(node.left, out);
            serializeTree(node.right, out);
        }
    }

    /**
     * Восстанавливает исходные данные из Хаффмен-сжатого массива.
     * @param data сжатые данные
     * @return восстановленный массив байт
     */
    @Override
    public byte[] decompress(byte[] data) {
        int treeLen = ((data[0] & 0xFF) << 24) | ((data[1] & 0xFF) << 16) | ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);
        int offset = 4;
        Node root = deserializeTree(data, offset, new int[]{0}, treeLen);
        offset += treeLen;
        int encodedLen = ((data[offset] & 0xFF) << 24) | ((data[offset+1] & 0xFF) << 16) | ((data[offset+2] & 0xFF) << 8) | (data[offset+3] & 0xFF);
        offset += 4;
        byte[] encodedArr = Arrays.copyOfRange(data, offset, offset + encodedLen);
        offset += encodedLen;
        // Читаем длину исходных данных
        int originalLen = ((data[offset] & 0xFF) << 24) | ((data[offset+1] & 0xFF) << 16) | ((data[offset+2] & 0xFF) << 8) | (data[offset+3] & 0xFF);
        List<Byte> result = new ArrayList<>();
        Node node = root;
        int decodedSymbols = 0;
        for (int i = 0; i < encodedArr.length * 8 && decodedSymbols < originalLen; i++) {
            int idx = i / 8;
            int bit = (encodedArr[idx] >> (7 - (i % 8))) & 1;
            node = bit == 0 ? node.left : node.right;
            if (node.isLeaf()) {
                result.add(node.symbol);
                node = root;
                decodedSymbols++;
            }
        }
        byte[] out = new byte[result.size()];
        for (int i = 0; i < result.size(); i++) out[i] = result.get(i);
        return out;
    }

    private static Node deserializeTree(byte[] data, int offset, int[] pos, int treeLen) {
        if (pos[0] >= treeLen) return null;
        byte flag = data[offset + pos[0]];
        pos[0]++;
        if (flag == 1) {
            byte symbol = data[offset + pos[0]];
            pos[0]++;
            return new Node(symbol, 0, null, null);
        } else {
            Node left = deserializeTree(data, offset, pos, treeLen);
            Node right = deserializeTree(data, offset, pos, treeLen);
            return new Node((byte)0, 0, left, right);
        }
    }

    /**
     * Подсчитывает количество символов в дереве Хаффмена.
     * Используется для ограничения декодирования только исходным количеством символов.
     */
    private static int countSymbols(Node node) {
        if (node == null) return 0;
        if (node.isLeaf()) return node.freq;
        return countSymbols(node.left) + countSymbols(node.right);
    }
}
