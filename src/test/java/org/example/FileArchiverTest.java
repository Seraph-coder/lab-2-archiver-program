package org.example;

import org.example.compression.*;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

public class FileArchiverTest {
    @Test
    void testFileArchiverRLE() throws IOException {
        String text = "aaaaabbbbccccccdddddd";
        File tempIn = File.createTempFile("testRLE", ".txt");
        File tempOut = File.createTempFile("testRLE", ".rle");
        File tempRestored = File.createTempFile("testRLE", ".restored.txt");
        try (FileOutputStream fos = new FileOutputStream(tempIn)) {
            fos.write(text.getBytes());
        }
        FileArchiver.compressFile(tempIn.getAbsolutePath(), tempOut.getAbsolutePath(), FileArchiver.Method.RLE);
        FileArchiver.decompressFile(tempOut.getAbsolutePath(), tempRestored.getAbsolutePath(), FileArchiver.Method.RLE);
        byte[] restored = new FileInputStream(tempRestored).readAllBytes();
        assertEquals(text, new String(restored));
    }

    @Test
    void testFileArchiverLZW() throws IOException {
        String text = "TOBEORNOTTOBEORTOBEORNOT";
        File tempIn = File.createTempFile("testLZW", ".txt");
        File tempOut = File.createTempFile("testLZW", ".lzw");
        File tempRestored = File.createTempFile("testLZW", ".restored.txt");
        try (FileOutputStream fos = new FileOutputStream(tempIn)) {
            fos.write(text.getBytes());
        }
        FileArchiver.compressFile(tempIn.getAbsolutePath(), tempOut.getAbsolutePath(), FileArchiver.Method.LZW);
        FileArchiver.decompressFile(tempOut.getAbsolutePath(), tempRestored.getAbsolutePath(), FileArchiver.Method.LZW);
        byte[] restored = new FileInputStream(tempRestored).readAllBytes();
        assertEquals(text, new String(restored));
    }

    @Test
    void testFileArchiverHuffman() throws IOException {
        String text = "ABBCCCDDDDEEEEE";
        File tempIn = File.createTempFile("testHuffman", ".txt");
        File tempOut = File.createTempFile("testHuffman", ".huff");
        File tempRestored = File.createTempFile("testHuffman", ".restored.txt");
        try (FileOutputStream fos = new FileOutputStream(tempIn)) {
            fos.write(text.getBytes());
        }
        FileArchiver.compressFile(tempIn.getAbsolutePath(), tempOut.getAbsolutePath(), FileArchiver.Method.HUFFMAN);
        FileArchiver.decompressFile(tempOut.getAbsolutePath(), tempRestored.getAbsolutePath(), FileArchiver.Method.HUFFMAN);
        byte[] restored = new FileInputStream(tempRestored).readAllBytes();
        assertEquals(text, new String(restored));
    }

    @Test
    void testAutoRLE() throws IOException {
        String text = "aaaaaaaaaabbbbbbbbbbccccccccccdddddddddd";
        File tempIn = File.createTempFile("testAutoRLE", ".txt");
        File tempOut = File.createTempFile("testAutoRLE", ".arc");
        File tempRestored = File.createTempFile("testAutoRLE", ".restored.txt");
        try (FileOutputStream fos = new FileOutputStream(tempIn)) {
            fos.write(text.getBytes());
        }
        FileArchiver.compressFileAuto(tempIn.getAbsolutePath(), tempOut.getAbsolutePath());
        FileArchiver.decompressFileAuto(tempOut.getAbsolutePath(), tempRestored.getAbsolutePath());
        byte[] restored = new FileInputStream(tempRestored).readAllBytes();
        assertEquals(text, new String(restored));
    }

    @Test
    void testAutoLZW() throws IOException {
        String text = "word word word phrase phrase phrase word word phrase";
        File tempIn = File.createTempFile("testAutoLZW", ".txt");
        File tempOut = File.createTempFile("testAutoLZW", ".arc");
        File tempRestored = File.createTempFile("testAutoLZW", ".restored.txt");
        try (FileOutputStream fos = new FileOutputStream(tempIn)) {
            fos.write(text.getBytes());
        }
        FileArchiver.compressFileAuto(tempIn.getAbsolutePath(), tempOut.getAbsolutePath());
        FileArchiver.decompressFileAuto(tempOut.getAbsolutePath(), tempRestored.getAbsolutePath());
        byte[] restored = new FileInputStream(tempRestored).readAllBytes();
        assertEquals(text, new String(restored));
    }

    @Test
    void testAutoHuffman() throws IOException {
        String text = "abcdefgABCDEFG1234567";
        File tempIn = File.createTempFile("testAutoHuffman", ".txt");
        File tempOut = File.createTempFile("testAutoHuffman", ".arc");
        File tempRestored = File.createTempFile("testAutoHuffman", ".restored.txt");
        try (FileOutputStream fos = new FileOutputStream(tempIn)) {
            fos.write(text.getBytes());
        }
        FileArchiver.compressFileAuto(tempIn.getAbsolutePath(), tempOut.getAbsolutePath());
        FileArchiver.decompressFileAuto(tempOut.getAbsolutePath(), tempRestored.getAbsolutePath());
        byte[] restored = new FileInputStream(tempRestored).readAllBytes();
        assertEquals(text, new String(restored));
    }

    @Test
    void testAutoBinary() throws IOException {
        byte[] data = new byte[100];
        new Random(42).nextBytes(data);
        File tempIn = File.createTempFile("testAutoBin", ".bin");
        File tempOut = File.createTempFile("testAutoBin", ".arc");
        File tempRestored = File.createTempFile("testAutoBin", ".restored.bin");
        try (FileOutputStream fos = new FileOutputStream(tempIn)) {
            fos.write(data);
        }
        FileArchiver.compressFileAuto(tempIn.getAbsolutePath(), tempOut.getAbsolutePath());
        FileArchiver.decompressFileAuto(tempOut.getAbsolutePath(), tempRestored.getAbsolutePath());
        byte[] restored = new FileInputStream(tempRestored).readAllBytes();
        assertArrayEquals(data, restored);
    }
}
