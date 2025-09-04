package com.javaweb.api;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.javaweb.customexception.FieldRequiredException;
import com.javaweb.model.BuildingDTO;
import com.javaweb.model.ErrorResponseDTO;
import com.javaweb.service.BuildingService;

@RestController
public class BuildingAPI {
	@Autowired
	private BuildingService buildingService;
	
    @GetMapping(value="/api/building/")
    public List<BuildingDTO> getBuilding(@RequestParam(value="name", required = false) String name,
    		                            @RequestParam(value="districtid", required = false) Long distric,
    		                            @RequestParam(value="typeCpde", required = false) List<String> typeCode) {
    	List<BuildingDTO> result = buildingService.findAll(name,distric);
    	return result;
    }
    
    @PostMapping(value="/api/building/")
    public Object getBuilding2(@RequestBody BuildingDTO buildingDTO) {
    	
    	valiDate(buildingDTO);
    	return buildingDTO;
    }
    
     public void valiDate(BuildingDTO buildingDTO){
    	 if(buildingDTO.getName() == null || buildingDTO.getName().equals("") || buildingDTO.getNumberOfBasement() == null) {
    		 throw new FieldRequiredException("name or numberofbasement is null");
    	 }
     }
}
