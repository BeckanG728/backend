package com.tpdteam3.backend.controller;

import com.tpdteam3.backend.service.GfsStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/archivos")
public class ArchivoController {

    @Autowired
    private GfsStorageService gfsStorageService;

    @PostMapping("/subir")
    public ResponseEntity<String> subirArchivo(@RequestParam("file") MultipartFile file) {
        try {
            gfsStorageService.guardarArchivo(file);
            return ResponseEntity.ok("Archivo almacenado exitosamente con replicación tipo GFS");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al almacenar el archivo: " + e.getMessage());
        }
    }

    @GetMapping("/descargar/{nombre}")
    public ResponseEntity<byte[]> descargarArchivo(@PathVariable String nombre) {
        try {
            byte[] data = gfsStorageService.leerArchivo(nombre);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nombre)
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
