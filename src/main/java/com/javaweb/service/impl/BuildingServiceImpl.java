package com.javaweb.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.javaweb.model.BuildingDTO;
import com.javaweb.repository.BuildingRepository;
import com.javaweb.repository.entity.BuildingEntity;
import com.javaweb.service.BuildingService;

@Service
public class BuildingServiceImpl implements BuildingService{
	@Autowired
	private BuildingRepository buildingRepository;

	@Override
	public List<BuildingDTO> findAll(String name, Long districId) {
		List<BuildingEntity> buildingEntities = buildingRepository.findAll(name,districId);
		List<BuildingDTO> result = new ArrayList<>();
		for(BuildingEntity buildingEntity : buildingEntities) {
			BuildingDTO buildingDTO = new BuildingDTO();
			buildingDTO.setName(buildingEntity.getName());
			buildingDTO.setNumberOfBasement(buildingEntity.getNumberOfBasement());
			buildingDTO.setAddress(buildingEntity.getStreet() + "," + buildingEntity.getWard());
		    result.add(buildingDTO);
		}
		return result;
	}

}
