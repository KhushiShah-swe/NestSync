package com.nestsync.service;

import com.nestsync.dto.Requests;
import com.nestsync.model.GroceryItem;
import com.nestsync.repository.*;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class GroceryService {
  private final GroceryRepository records;
  private final CurrentUser current;

  @Transactional(readOnly = true)
  public List<GroceryItem> all() {
    return records.findAllByGroupGroupIdOrderByGroceryIdDesc(
        current.get().getHousehold().getGroupId());
  }

  @Transactional(readOnly = true)
  public GroceryItem get(Long id) {
    return records
        .findByGroceryIdAndGroupGroupId(id, current.get().getHousehold().getGroupId())
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grocery record not found."));
  }

  public GroceryItem create(Requests.Grocery request) {
    var entity = new GroceryItem();
    entity.setGroup(current.get().getHousehold());
    apply(entity, request);
    return records.save(entity);
  }

  public GroceryItem update(Long id, Requests.Grocery request) {
    var entity = get(id);
    apply(entity, request);
    return records.save(entity);
  }

  public void delete(Long id) {
    records.delete(get(id));
  }

  private void apply(GroceryItem entity, Requests.Grocery request) {
    entity.setItemName(request.itemName().strip());
    entity.setQuantity(request.quantity());
    entity.setPurchased(request.purchased());
  }
}
