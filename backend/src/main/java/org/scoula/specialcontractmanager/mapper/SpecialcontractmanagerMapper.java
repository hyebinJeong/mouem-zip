package org.scoula.specialcontractmanager.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.scoula.specialcontractmanager.domain.SpecialcontractmanagerVO;

import java.util.List;

@Mapper
public interface SpecialcontractmanagerMapper {
    List<SpecialcontractmanagerVO> findAll();
    SpecialcontractmanagerVO findById(int id);
    void insert(SpecialcontractmanagerVO vo);
    void update(SpecialcontractmanagerVO vo);
    void delete(int id);
}
