package com.demo.bbq.business.diningroomorder.service.impl;

import com.demo.bbq.business.diningroomorder.repository.database.MenuOrderRepository;
import com.demo.bbq.business.diningroomorder.repository.api.MenuOptionApi;
import com.demo.bbq.business.diningroomorder.service.DiningRoomOrderService;
import com.demo.bbq.business.diningroomorder.repository.database.TableRepository;
import com.demo.bbq.business.diningroomorder.util.exception.ExceptionCatalog;
import com.demo.bbq.business.diningroomorder.util.mapper.DiningRoomOrderMapper;
import com.demo.bbq.business.diningroomorder.util.mapper.MenuOrderMapper;
import com.demo.bbq.business.diningroomorder.util.model.dto.request.MenuOrderRequest;
import com.demo.bbq.business.diningroomorder.util.model.dto.response.DiningRoomOrderResponse;
import com.demo.bbq.business.diningroomorder.util.model.dto.third.MenuOptionThird;
import com.demo.bbq.business.diningroomorder.util.model.entity.DiningRoomTable;
import com.demo.bbq.business.diningroomorder.util.model.entity.MenuOrder;
import java.util.List;
import java.util.function.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * <br/>Clase Service que implementa los métodos necesarios para tramitar la lógica de negocio del contexto Dining Room
 * Order.<br/>
 *
 * @author Miguel Armas Abt <br/>
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class DiningRoomOrderServiceImpl implements DiningRoomOrderService {

  private final TableRepository tableRepository;
  private final MenuOrderRepository menuOrderRepository;

  private final MenuOptionApi menuOptionApi;
  private final DiningRoomOrderMapper diningRoomOrderMapper;

  private final MenuOrderMapper menuOrderMapper;

  @Override
  public Mono<DiningRoomOrderResponse> findByTableNumber(Integer tableNumber) {
    return findDiningRoomTableByTableNumber(tableNumber)
        .map(diningRoomOrderMapper::fromEntityToResponse)
        .switchIfEmpty(Mono.error(ExceptionCatalog.ERROR1000.buildCustomException()));
  }

  @Override
  public Mono<Long> generateTableOrder(List<MenuOrderRequest> menuOrderRequestList, Integer tableNumber) {
    return this.findDiningRoomTableByTableNumber(tableNumber)
        .flatMap(table -> Flux.fromIterable(menuOrderRequestList)
            .map(menuOptionRequest -> this.findMenuOptionById(menuOptionRequest.getMenuOptionId())
                .onErrorResume(ex -> Mono.error(ExceptionCatalog.ERROR1001.buildCustomException("id: " + menuOptionRequest.getMenuOptionId())))
                .ignoreElement()
                .then(this.saveMenuOrder(menuOrderMapper.fromRequestToEntity(menuOptionRequest, table.getId()))))
            .ignoreElements()
            .then(this.saveDiningRoomTable(table)
                .map(DiningRoomTable::getId)));

// si se agrega a la orden más opciones de menu del mismo tipo (mismo menuOptionId) entonces chancar el registro que ya existía en bd
//
//    return this.findDiningRoomTableByTableNumber(tableNumber)
//        .flatMap(diningRoomTable -> Flux.fromIterable(menuOrderRequestList)
//            .flatMap(menuOrderRequest -> this.findMenuOptionById(menuOrderRequest.getMenuOptionId())
//                .onErrorResume(ex -> Mono.error(ExceptionCatalog.ERROR1001.buildCustomException("id: " + menuOrderRequest.getMenuOptionId())))
//                .flatMapMany(menuOptionFound -> Flux.fromIterable(diningRoomTable.getMenuOrderList())
//                    .doOnNext(x -> log.info("diningRoomTable-MenuOrder: " + new com.google.gson.Gson().toJson(x)))
//                    .flatMap(previousMenuOrder -> this.validateAndSaveMenuOrder(previousMenuOrder.getMenuOptionId(), diningRoomTable.getId(), menuOrderRequest))
//                    .switchIfEmpty(this.validateAndSaveMenuOrder(menuOrderMapper.fromRequestToEntity(menuOrderRequest, diningRoomTable.getId())))
//                ))
//            .flatMap(savedMenuOrder -> Mono.just(diningRoomTable))
//            .next())
//        .flatMap(diningRoomTable -> this.saveDiningRoomTable(diningRoomTable)
//            .map(DiningRoomTable::getId));
  }
  private Mono<MenuOrder> validateAndSaveMenuOrder(Long previousMenuOrderId, Long diningRoomTableId, MenuOrderRequest menuOrderRequest) {
    return (!previousMenuOrderId.equals(menuOrderRequest.getMenuOptionId()))
        ? this.addToPreviousOrder.apply(menuOrderRequest).doOnError(ex -> log.info("error addToPreviousOrder"))
        : this.saveMenuOrder(menuOrderMapper.fromRequestToEntity(menuOrderRequest, diningRoomTableId)).doOnError(ex -> log.info("error saveMenuOrder"));
  }

  private Function<MenuOrderRequest, Mono<MenuOrder>> addToPreviousOrder = menuOrderRequest ->
      this.findMenuOrderByMenuOptionId(menuOrderRequest.getMenuOptionId())
          .flatMap(savedMenuOrder -> {
            Integer actualQuantity = savedMenuOrder.getQuantity() + menuOrderRequest.getQuantity();
            savedMenuOrder.setQuantity(actualQuantity);
            return this.saveMenuOrder(savedMenuOrder);
          });

  private Mono<DiningRoomTable> findDiningRoomTableByTableNumber(Integer tableNumber) {
      return tableRepository.findByTableNumber(tableNumber)
          .map(Mono::just)
          .orElse(Mono.empty());
  }

  private Mono<MenuOptionThird> findMenuOptionById(Long menuOptionRequestId) {
    return RxJava2Adapter.singleToMono(menuOptionApi.findById(menuOptionRequestId));
  }

  private Mono<MenuOrder> saveMenuOrder(MenuOrder menuOrder) {
    return Mono.just(menuOrderRepository.save(menuOrder));
  }

  private Mono<DiningRoomTable> saveDiningRoomTable(DiningRoomTable table) {
    return Mono.just(tableRepository.save(table));
  }

  private Mono<MenuOrder> findMenuOrderByMenuOptionId(Long menuOptionId) {
    return menuOrderRepository.findByMenuOptionId(menuOptionId)
        .map(Mono::just)
        .orElse(Mono.empty());
  }

}