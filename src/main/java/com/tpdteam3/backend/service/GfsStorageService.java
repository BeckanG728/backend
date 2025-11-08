package com.tpdteam3.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

@Service
public class GfsStorageService {

    private static final String CHUNK_DIR = "chunks/";
    private static final int CHUNK_SIZE = 1024 * 1024; // 1 MB por fragmento

    public void guardarArchivo(MultipartFile archivo) throws IOException {
        byte[] data = archivo.getBytes();
        int parte = 0;

        // Crear carpeta si no existe
        Files.createDirectories(Path.of(CHUNK_DIR));

        for (int i = 0; i < data.length; i += CHUNK_SIZE) {
            int fin = Math.min(data.length, i + CHUNK_SIZE);
            byte[] chunk = Arrays.copyOfRange(data, i, fin);
            String chunkName = CHUNK_DIR + archivo.getOriginalFilename() + "_part" + (++parte) + ".chunk";
            Files.write(Path.of(chunkName), chunk);

            // Simular replicación (GFS replica los chunks en otros "nodos")
            Path replicaDir = Path.of(CHUNK_DIR, "replicas");
            Files.createDirectories(replicaDir);
            Files.copy(Path.of(chunkName), replicaDir.resolve(archivo.getOriginalFilename() + "_copy" + parte + ".chunk"), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public byte[] leerArchivo(String nombreArchivo) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int parte = 1;
        while (true) {
            Path chunkPath = Path.of(CHUNK_DIR + nombreArchivo + "_part" + parte + ".chunk");
            if (!Files.exists(chunkPath)) break;
            baos.write(Files.readAllBytes(chunkPath));
            parte++;
        }
        return baos.toByteArray();
    }
}