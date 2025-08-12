package org.scoula.specialcontractmanager.service.impl;

import lombok.RequiredArgsConstructor;
import org.scoula.specialcontractmanager.domain.SpecialcontractmanagerVO;
import org.scoula.specialcontractmanager.mapper.SpecialcontractmanagerMapper;
import org.scoula.specialcontractmanager.service.SpecialcontractmanagerService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpecialcontractmanagerServiceImpl implements SpecialcontractmanagerService {

    private final SpecialcontractmanagerMapper mapper;

    @Override
    public List<SpecialcontractmanagerVO> getAll() {
        return mapper.findAll();
    }

    @Override
    public SpecialcontractmanagerVO getById(int id) {
        return mapper.findById(id);
    }

    @Override
    public void add(SpecialcontractmanagerVO vo) {
        mapper.insert(vo);
    }

    @Override
    public void update(SpecialcontractmanagerVO vo) {
        mapper.update(vo);
    }

    @Override
    public void delete(int id) {
        mapper.delete(id);
    }
}
