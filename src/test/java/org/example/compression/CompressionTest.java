package org.example.compression;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CompressionTest {
    @Test
    void testRLE() {
        Compressor c = new RLECompressor();
        byte[] data = "aaaabbbccdddddd".getBytes();
        byte[] compressed = c.compress(data);
        byte[] decompressed = c.decompress(compressed);
        assertArrayEquals(data, decompressed);
    }

    @Test
    void testLZW() {
        Compressor c = new LZWCompressor();
        byte[] data = "TOBEORNOTTOBEORTOBEORNOT".getBytes();
        byte[] compressed = c.compress(data);
        byte[] decompressed = c.decompress(compressed);
        assertArrayEquals(data, decompressed);
    }

    @Test
    void testHuffman() {
        Compressor c = new HuffmanCompressor();
        byte[] data = "ABBCCCDDDDEEEEE".getBytes();
        byte[] compressed = c.compress(data);
        byte[] decompressed = c.decompress(compressed);
        assertArrayEquals(data, decompressed);
    }
}

