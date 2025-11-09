package com.tpdteam3.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class DFSClientService {

    @Value("${dfs.master.url:http://localhost:9000/master}")
    private String masterUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Sube una imagen al sistema distribuido
     */
    public String uploadImagen(MultipartFile file) throws Exception {
        // 1. Generar ID único para la imagen
        String imagenId = UUID.randomUUID().toString();

        // 2. Leer bytes de la imagen
        byte[] imageBytes = file.getBytes();

        // 3. Consultar al Master dónde escribir
        String uploadUrl = masterUrl + "/api/master/upload";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> request = new HashMap<>();
        request.put("imagenId", imagenId);
        request.put("size", imageBytes.length);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(uploadUrl, entity, Map.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Error consultando Master para upload");
        }

        // 4. Obtener ubicaciones de los chunkservers
        Map<String, Object> responseBody = response.getBody();
        List<Map<String, Object>> chunks = (List<Map<String, Object>>) responseBody.get("chunks");

        // 5. Dividir imagen en fragmentos y enviar a chunkservers
        int chunkSize = 512 * 1024; // 512KB por fragmento
        int offset = 0;

        for (Map<String, Object> chunkInfo : chunks) {
            int chunkIndex = (Integer) chunkInfo.get("chunkIndex");
            String chunkserverUrl = (String) chunkInfo.get("chunkserverUrl");

            // Calcular tamaño del fragmento
            int length = Math.min(chunkSize, imageBytes.length - offset);
            byte[] chunkData = Arrays.copyOfRange(imageBytes, offset, offset + length);

            // Enviar fragmento al chunkserver
            String writeUrl = chunkserverUrl + "/api/chunk/write";

            HttpHeaders chunkHeaders = new HttpHeaders();
            chunkHeaders.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> chunkRequest = new HashMap<>();
            chunkRequest.put("imagenId", imagenId);
            chunkRequest.put("chunkIndex", chunkIndex);
            chunkRequest.put("data", Base64.getEncoder().encodeToString(chunkData));

            HttpEntity<Map<String, Object>> chunkEntity = new HttpEntity<>(chunkRequest, chunkHeaders);
            restTemplate.postForEntity(writeUrl, chunkEntity, String.class);

            offset += length;
        }

        return imagenId;
    }

    /**
     * Descarga una imagen del sistema distribuido
     */
    public byte[] downloadImagen(String imagenId) throws Exception {
        // 1. Consultar al Master dónde están los fragmentos
        String metadataUrl = masterUrl + "/api/master/metadata?imagenId=" + imagenId;
        ResponseEntity<Map> response = restTemplate.getForEntity(metadataUrl, Map.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Error consultando Master para download");
        }

        Map<String, Object> metadata = response.getBody();
        List<Map<String, Object>> chunks = (List<Map<String, Object>>) metadata.get("chunks");

        // 2. Descargar cada fragmento
        List<byte[]> chunkDataList = new ArrayList<>();
        int totalSize = 0;

        for (Map<String, Object> chunkInfo : chunks) {
            int chunkIndex = (Integer) chunkInfo.get("chunkIndex");
            String chunkserverUrl = (String) chunkInfo.get("chunkserverUrl");

            // Descargar fragmento
            String readUrl = chunkserverUrl + "/api/chunk/read?imagenId=" + imagenId + "&chunkIndex=" + chunkIndex;
            ResponseEntity<Map> chunkResponse = restTemplate.getForEntity(readUrl, Map.class);

            if (chunkResponse.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> chunkData = chunkResponse.getBody();
                String base64Data = (String) chunkData.get("data");
                byte[] bytes = Base64.getDecoder().decode(base64Data);
                chunkDataList.add(bytes);
                totalSize += bytes.length;
            }
        }

        // 3. Reconstruir imagen completa
        byte[] fullImage = new byte[totalSize];
        int offset = 0;
        for (byte[] chunk : chunkDataList) {
            System.arraycopy(chunk, 0, fullImage, offset, chunk.length);
            offset += chunk.length;
        }

        return fullImage;
    }

    /**
     * Elimina una imagen del sistema distribuido
     */
    public void deleteImagen(String imagenId) throws Exception {
        String deleteUrl = masterUrl + "/api/master/delete?imagenId=" + imagenId;
        restTemplate.delete(deleteUrl);
    }
}