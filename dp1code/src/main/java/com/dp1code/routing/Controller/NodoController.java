package com.dp1code.routing.Controller;

import com.dp1code.routing.dto.NodoDTO;
import com.dp1code.routing.Model.Nodo;
import com.dp1code.routing.Service.NodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = {
        "https://h982equipo7g.duckdns.org",
        "http://localhost:3000"
})
@RestController
@RequestMapping("/api/nodos")
public class NodoController {

    @Autowired
    private NodoService nodoService;

    @GetMapping("/{posX}/{posY}")
    public ResponseEntity<NodoDTO> getNodo(@PathVariable int posX, @PathVariable int posY) {
        return nodoService.getNodoPorCoordenadas(posX, posY)
                .map(nodo -> {
                    NodoDTO dto = new NodoDTO(nodo.getPosX(), nodo.getPosY(), nodo.isBloqueado());
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/registrar")
    public ResponseEntity<String> registrarNodo(@RequestBody NodoDTO dto) {
        Nodo nodo = new Nodo(dto.getPosX(), dto.getPosY());
        nodo.setBloqueado(dto.isBloqueado());
        nodoService.registrarNodo(nodo);
        return ResponseEntity.ok("Nodo registrado correctamente.");
    }

    @GetMapping("/obtenerNodo/{id}")
    public ResponseEntity<Nodo> obtenerNodo(@PathVariable int id) {
        try {
            System.out.println(id);
            Nodo nodo = nodoService.getNodoPorId(id);
            return ResponseEntity.ok(nodo);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
