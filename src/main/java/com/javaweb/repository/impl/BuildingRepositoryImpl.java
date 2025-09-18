package com.javaweb.repository.impl;

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

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import com.javaweb.builder.BuildingSearchBuilder;

import com.javaweb.repository.BuildingRepository;

import com.javaweb.repository.entity.BuildingEntity;
import com.javaweb.utils.NumberUtil;
import com.javaweb.utils.StringUtil;

@Repository
@Primary
public class BuildingRepositoryImpl implements BuildingRepository {

	@PersistenceContext
	private EntityManager entityManager;
	
	public static void joinTable(BuildingSearchBuilder buildingSearchBuilder, StringBuilder sql) {
		Long staffId = buildingSearchBuilder.getStaffId();
	    if (staffId != null) {
	    	sql.append(" JOIN assignmentbuilding a ON a.buildingid = b.id ");
	    }
	    
	    Long rentAreaTo = buildingSearchBuilder.getRentAreaTo();
	    Long rentAreaFrom = buildingSearchBuilder.getRentAreaFrom();
	    if (rentAreaFrom != null || rentAreaTo != null) {
	    	sql.append(" JOIN rentarea r ON r.buildingid = b.id ");	    
	    }
	    
	    List<String> typeCode = buildingSearchBuilder.getTypeCode();
	    if (typeCode != null && !typeCode.isEmpty()) {
	    	sql.append(" JOIN buildingrenttype br ON br.buildingid = b.id");
	    	sql.append(" JOIN renttype rt ON br.renttypeid = rt.id");	    
	    }
	}
	
	public static void queryNomal(BuildingSearchBuilder buildingSearchBuilder, StringBuilder where) {
	    try {
	        Field[] fields = BuildingSearchBuilder.class.getDeclaredFields();
	        for (Field item : fields) {
	            item.setAccessible(true);
	            String fieldName = item.getName();
	            if (!fieldName.equals("staffId") && !fieldName.equals("typeCode") && !fieldName.startsWith("rentArea")
	                    && !fieldName.startsWith("rentPrice")) {
	                Object value = item.get(buildingSearchBuilder);
	                if (value != null && StringUtil.checkString(value.toString())) {
	                    if (NumberUtil.isNumber(value.toString())) {
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
	    Long staffId = buildingSearchBuilder.getStaffId();
	    if (staffId != null) {
	        where.append(" AND a.staffid = " + staffId);
	    }

	    Long rentAreaTo = buildingSearchBuilder.getRentAreaTo();
	    Long rentAreaFrom = buildingSearchBuilder.getRentAreaFrom();
	    if (rentAreaFrom != null || rentAreaTo != null) {
	        where.append(" AND EXISTS (SELECT * FROM rentarea r WHERE b.id = r.buildingid ");
	        if (rentAreaFrom != null) {
	            where.append(" AND r.value >= " + rentAreaFrom);
	        }
	        if (rentAreaTo != null) {
	            where.append(" AND r.value <= " + rentAreaTo);
	        }
	        where.append(")");
	    }

	    Long rentPriceTo = buildingSearchBuilder.getRentPriceTo();
	    Long rentPriceFrom = buildingSearchBuilder.getRentPriceFrom();
	    if (rentPriceTo != null || rentPriceFrom != null) {
	        if (rentPriceFrom != null) {
	            where.append(" AND b.rentprice >= " + rentPriceFrom);
	        }
	        if (rentPriceTo != null) {
	            where.append(" AND b.rentprice <= " + rentPriceTo);
	        }
	    }

	    List<String> typeCode = buildingSearchBuilder.getTypeCode();
	    if (typeCode != null && !typeCode.isEmpty()) {
	        String sql = typeCode.stream().map(it -> "'" + it + "'").collect(Collectors.joining(","));
	        where.append(" AND rt.code IN (" + sql + ")");
	    }
	}
	
	@Override
	public List<BuildingEntity> findAll(BuildingSearchBuilder buildingSearchBuilder) {
		StringBuilder sql = new StringBuilder("SELECT b.* FROM building b");
	    joinTable(buildingSearchBuilder, sql);
	    StringBuilder where = new StringBuilder(" where 1=1 ");
	    queryNomal(buildingSearchBuilder, where);
	    querySpecial(buildingSearchBuilder, where);
	    sql.append(where);
	    sql.append(" GROUP BY b.id;");
	    Query query = entityManager.createNativeQuery(sql.toString(),BuildingEntity.class);
		return query.getResultList();
	}
}