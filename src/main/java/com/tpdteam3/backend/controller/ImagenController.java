package com.tpdteam3.backend.controller;

import com.tpdteam3.backend.entity.Producto;
import com.tpdteam3.backend.service.DFSClientService;
import com.tpdteam3.backend.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/imagenes")
public class ImagenController {

    @Autowired
    private DFSClientService dfsClientService;

    @Autowired
    private ProductoService productoService;

    /**
     * Subir imagen para un producto
     */
    @PostMapping("/upload/{productoId}")
    public ResponseEntity<Map<String, String>> uploadImagen(
            @PathVariable Integer productoId,
            @RequestParam("file") MultipartFile file) {

        try {
            // Validar que el archivo sea una imagen
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                Map<String, String> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "El archivo debe ser una imagen");
                return ResponseEntity.badRequest().body(error);
            }

            // Obtener producto
            Producto producto = productoService.obtener(productoId);

            // Si ya tiene imagen, eliminar la anterior
            if (producto.getImagenId() != null) {
                try {
                    dfsClientService.deleteImagen(producto.getImagenId());
                } catch (Exception e) {
                    // Log error pero continuar
                    System.err.println("Error eliminando imagen anterior: " + e.getMessage());
                }
            }

            // Subir nueva imagen al sistema distribuido
            String imagenId = dfsClientService.uploadImagen(file);

            // Actualizar producto con referencia a la imagen
            producto.setImagenId(imagenId);
            producto.setImagenNombre(file.getOriginalFilename());
            productoService.actualizar(producto);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Imagen subida correctamente");
            response.put("imagenId", imagenId);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Producto no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Error al subir imagen: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Descargar imagen de un producto
     */
    @GetMapping("/download/{productoId}")
    public ResponseEntity<byte[]> downloadImagen(@PathVariable Integer productoId) {
        try {
            // Obtener producto
            Producto producto = productoService.obtener(productoId);

            if (producto.getImagenId() == null) {
                return ResponseEntity.notFound().build();
            }

            // Descargar imagen del sistema distribuido
            byte[] imageData = dfsClientService.downloadImagen(producto.getImagenId());

            // Determinar tipo de contenido basado en extensión
            String contentType = MediaType.IMAGE_JPEG_VALUE;
            if (producto.getImagenNombre() != null) {
                if (producto.getImagenNombre().toLowerCase().endsWith(".png")) {
                    contentType = MediaType.IMAGE_PNG_VALUE;
                } else if (producto.getImagenNombre().toLowerCase().endsWith(".gif")) {
                    contentType = MediaType.IMAGE_GIF_VALUE;
                }
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(imageData.length);

            return new ResponseEntity<>(imageData, headers, HttpStatus.OK);

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Eliminar imagen de un producto
     */
    @DeleteMapping("/{productoId}")
    public ResponseEntity<Map<String, String>> deleteImagen(@PathVariable Integer productoId) {
        try {
            Producto producto = productoService.obtener(productoId);

            if (producto.getImagenId() == null) {
                Map<String, String> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "El producto no tiene imagen");
                return ResponseEntity.badRequest().body(error);
            }

            // Eliminar del sistema distribuido
            dfsClientService.deleteImagen(producto.getImagenId());

            // Actualizar producto
            producto.setImagenId(null);
            producto.setImagenNombre(null);
            productoService.actualizar(producto);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Imagen eliminada correctamente");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Producto no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Error al eliminar imagen: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}