package com.javaweb.repository.custom.impl;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import com.javaweb.builder.BuildingSearchBuilder;

import com.javaweb.repository.BuildingRepository;

import com.javaweb.repository.entity.BuildingEntity;
import com.javaweb.utils.NumberUtil;
import com.javaweb.utils.StringUtil;

@Repository
public class JDBCBuildingRepositoryImpl  {
	static final String DB_URL = "jdbc:mysql://localhost:3306/estatebasic";
	static final String USER = "root";
	static final String PASS = "12345678";
    
	@PersistenceContext
	private EntityManager entityManager;
	
	public static void joinTable(BuildingSearchBuilder buildingSearchBuilder, StringBuilder where) {
		String staffId = buildingSearchBuilder.getStaffId().toString();
	    if (StringUtil.checkString(staffId)) {
	        where.append(" JOIN assignmentbuilding a ON a.buildingid = b.id " + staffId);
	    }
	    
	    String rentAreaTo = buildingSearchBuilder.getRentAreaTo().toString();
	    String rentAreaFrom = buildingSearchBuilder.getRentAreaFrom().toString();
	    if (StringUtil.checkString(rentAreaFrom) == true || StringUtil.checkString(rentAreaTo) == true) {
	        where.append(" JOIN rentarea r ON r.buildingid = b.id ");	        
	    }
	    
	    List<String> typeCode = buildingSearchBuilder.getTypeCode();
	    if (typeCode != null && typeCode.size() != 0) {
	        where.append(" JOIN buildingrenttype r ON br.buildingid = b.id"
	        		+ " JOIN renttype rt ON br.renttypeid = rt.id");	        
	    }
	    
	}
	public static void queryNomal(BuildingSearchBuilder buildingSearchBuilder, StringBuilder where) {
	    try {
	        Field[] fields = BuildingSearchBuilder.class.getDeclaredFields();
	        for (Field item : fields) {
	            item.setAccessible(true);
	            String fieldName = item.getName();
	            if (!fieldName.equals("staffId") && !fieldName.equals("typeCode") && !fieldName.startsWith("area")
	                    && !fieldName.startsWith("rentPrice")) {
	                String value = item.get(buildingSearchBuilder).toString();
	                if (StringUtil.checkString(value)) {
	                    if (NumberUtil.isNumber(value) == true) {
	                        where.append(" AND b." + fieldName + " = " + value);
	                    } else {
	                        where.append(" AND b." + fieldName + " LIKE '%" + value + "%' ");
	                    }
	                }
	            }
	        }
	    } catch (Exception ex) {
	        ex.printStackTrace();
	    }
	}
	
	public static void querySpecial(BuildingSearchBuilder buildingSearchBuilder, StringBuilder where) {
	    String staffId = buildingSearchBuilder.getStaffId().toString();
	    if (StringUtil.checkString(staffId)) {
	        where.append(" AND assignmentbuilding.staffid = " + staffId);
	    }

	    String rentAreaTo = buildingSearchBuilder.getRentAreaTo().toString();
	    String rentAreaFrom = buildingSearchBuilder.getRentAreaFrom().toString();
	    if (StringUtil.checkString(rentAreaFrom) == true || StringUtil.checkString(rentAreaTo) == true) {
	        where.append(" AND EXISTS (SELECT * FROM rentarea r WHERE b.id = r.buildingid ");
	        if (StringUtil.checkString(rentAreaFrom)) {
	            where.append(" AND r.value >= " + rentAreaFrom);
	        }
	        if (StringUtil.checkString(rentAreaTo)) {
	            where.append(" AND r.value <= " + rentAreaTo);
	        }
	        where.append(")");
	    }

	    String rentPriceTo = buildingSearchBuilder.getRentPriceTo().toString();
	    String rentPriceFrom = buildingSearchBuilder.getRentPriceFrom().toString();
	    if (StringUtil.checkString(rentPriceTo) == true || StringUtil.checkString(rentPriceFrom) == true) {
	        if (StringUtil.checkString(rentAreaFrom)) {
	            where.append(" AND b.rentprice >= " + rentPriceFrom);
	        }
	        if (StringUtil.checkString(rentAreaTo)) {
	            where.append(" AND b.rentprice <= " + rentPriceTo);
	        }
	    }

	    List<String> typeCode = buildingSearchBuilder.getTypeCode();
	    if (typeCode != null && typeCode.size() != 0) {
	        where.append(" AND");
	        String sql = typeCode.stream().map(it -> " renttype.code like " + "'" + it + "%'").collect(Collectors.joining(" OR "));
	        where.append(sql);
	        where.append(" ");
	    }
	    
	    if (typeCode != null && !typeCode.isEmpty()) {
	        where.append(" AND renttype.code IN (" + String.join("', '", typeCode) + ")");
	    }
	}
	
	public List<BuildingEntity> findAll(BuildingSearchBuilder buildingSearchBuilder) {
		StringBuilder sql = new StringBuilder("SELECT b.* FROM building b");
        joinTable(buildingSearchBuilder, sql);
        StringBuilder where = new StringBuilder("where 1=1 ");
        queryNomal(buildingSearchBuilder, where);
        querySpecial(buildingSearchBuilder, where);
        where.append("GROUP BY b.id;");
        sql.append(where);
        Query query = entityManager.createNativeQuery(sql.toString(),BuildingEntity.class);
		return query.getResultList();
	}

}
