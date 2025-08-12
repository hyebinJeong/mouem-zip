package org.scoula.specialcontractmanager.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.specialcontractmanager.domain.SpecialcontractmanagerVO;
import org.scoula.specialcontractmanager.service.SpecialcontractmanagerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/specialcontracts-manager")
@RequiredArgsConstructor
public class SpecialcontractmanagerController {

    private final SpecialcontractmanagerService service;

    @GetMapping
    public List<SpecialcontractmanagerVO> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public SpecialcontractmanagerVO getById(@PathVariable int id) {
        return service.getById(id);
    }

    @PostMapping
    public void add(@RequestBody SpecialcontractmanagerVO vo) {
        service.add(vo);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable int id, @RequestBody SpecialcontractmanagerVO vo) {
        vo.setSpecialClauseId(id);
        service.update(vo);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        service.delete(id);
    }
}
