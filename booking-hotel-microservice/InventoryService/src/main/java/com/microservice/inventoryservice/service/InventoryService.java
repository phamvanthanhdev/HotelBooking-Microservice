package com.microservice.inventoryservice.service;

import com.microservice.inventoryservice.dto.InventoryQuantityResponse;
import com.microservice.inventoryservice.dto.InventoryResponse;
import com.microservice.inventoryservice.model.Inventory;
import com.microservice.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    @Transactional(readOnly = true)
    @SneakyThrows
    public InventoryResponse isAvailable(Long roomId) {
//        log.info("Wait Started");
//        Thread.sleep(10000);
//        log.info("Wait Ended");
        Inventory inventory = inventoryRepository.findByRoomId(roomId);
         return  InventoryResponse.builder()
                .roomId(inventory.getRoomId())
                .isAvailable(inventory.getQuantity()>0)
                .build();
    }

    public List<InventoryQuantityResponse> getQuantityInventoryByIds(List<Long> roomIds) {
        List<InventoryQuantityResponse> quantityResponses = new ArrayList<>();
        for (Long idRoom:roomIds) {
            Inventory inventory = inventoryRepository.findByRoomId(idRoom);
            if(inventory == null){
                inventory = new Inventory();
                inventory.setRoomId(idRoom);
                inventory.setQuantity(0);
            }
            InventoryQuantityResponse quantityResponse = convertInventoryToInventoryQuantity(inventory);
            quantityResponses.add(quantityResponse);
        }

        return quantityResponses;
    }

    public InventoryQuantityResponse convertInventoryToInventoryQuantity(Inventory inventory){
        return new InventoryQuantityResponse(inventory.getRoomId(), inventory.getQuantity());
    }
}
