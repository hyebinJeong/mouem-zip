package org.scoula.specialcontractmanager.service;

import org.scoula.specialcontractmanager.domain.SpecialcontractmanagerVO;

import java.util.List;

public interface SpecialcontractmanagerService {
    List<SpecialcontractmanagerVO> getAll();
    SpecialcontractmanagerVO getById(int id);
    void add(SpecialcontractmanagerVO vo);
    void update(SpecialcontractmanagerVO vo);
    void delete(int id);
}
