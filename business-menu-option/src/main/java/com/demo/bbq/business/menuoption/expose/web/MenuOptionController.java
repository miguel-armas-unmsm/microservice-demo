package com.demo.bbq.business.menuoption.expose.web;

import java.net.URI;
import java.util.List;
import java.util.function.Function;
import javax.validation.Valid;
import com.demo.bbq.business.menuoption.service.MenuOptionService;
import com.demo.bbq.business.menuoption.util.model.dto.request.MenuOptionRequest;
import com.demo.bbq.business.menuoption.util.model.dto.response.MenuOptionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * <br/>Clase Controller que implementa los métodos necesarios para exponer mediante REST
 * los servicios del contexto Menu Option.<br/>
 *
 * <b>Class</b>: MenuOptionController<br/>
 *
 * @author Miguel Armas Abt <br/>
 *      <u>Developed by</u>: <br/>
 *      <ul>
 *      <li>Miguel Armas Abt</li>
 *      </ul>
 *      <u>Changes</u>:<br/>
 *      <ul>
 *      <li>Set, 2021 Creación de Clase.</li>
 *      </ul>
 * @version 1.0
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/bbq/business/v1/menu-options")
public class MenuOptionController {

  private final MenuOptionService service;

  @GetMapping(produces = "application/json", value = "/{id}")
  public ResponseEntity<MenuOptionResponse> findById(@PathVariable(name = "id") Long id) {
    return ResponseEntity.ok(service.findById(id));
  }

  @GetMapping(produces = "application/json")
  public ResponseEntity<List<MenuOptionResponse>> findByCategory(
      @RequestParam(value = "category", required = false) String category) {
    return ResponseEntity.ok(service.findByCategory(category));
  }

  @PostMapping
  public ResponseEntity<Void> save(@Valid @RequestBody MenuOptionRequest menuOption) {
    Long id = service.save(menuOption);
    return ResponseEntity.created(buildPostUriLocation.apply(id)).build();
  }

  @PutMapping(value = "/{id}")
  public ResponseEntity<Void> update(@Valid @RequestBody MenuOptionRequest menuOption, @PathVariable("id") Long id) {
    service.update(id, menuOption);
    return ResponseEntity.created(buildPutUriLocation.apply(id)).build();
  }

  @DeleteMapping(value = "/{id}")
  public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
    service.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  private final static Function<Long, URI> buildPostUriLocation = id ->
      ServletUriComponentsBuilder.fromCurrentRequest()
      .path("/{id}")
      .buildAndExpand(id)
      .toUri();

  private final static Function<Long, URI> buildPutUriLocation = id ->
      ServletUriComponentsBuilder.fromCurrentRequest()
      .buildAndExpand()
      .toUri();
}