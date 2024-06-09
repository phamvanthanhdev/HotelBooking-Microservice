package com.microservice.inventoryservice.controller;

import com.microservice.inventoryservice.dto.InventoryResponse;
import com.microservice.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    /*private static final Logger LOGGER
            = LoggerFactory.getLogger(InventoryController.class);*/

    //http://localhost:8082/api/inventory/?roomId=1
    @GetMapping
    public ResponseEntity<InventoryResponse> isAvailable(@RequestParam Long roomId){
        //LOGGER.info("Check room is available");
        return ResponseEntity.ok(inventoryService.isAvailable(roomId));
    }
}
