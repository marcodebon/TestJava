package it.marcodebon.testjava.beans;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class GenericBean<T> {

	public static <T> T fromResultSet(ResultSet rs,Class<T> t) 
	throws SQLException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, 
		   InvocationTargetException, InstantiationException {
	
		T entity = (T)t.newInstance();
		ResultSetMetaData metaData = rs.getMetaData();
		int count = metaData.getColumnCount();
		for (int i = 1; i <= count; i++) {
			Method[] methods = entity.getClass().getMethods();
		    for (Method setter:methods) {
		        if (setter.getName().equalsIgnoreCase("set" + metaData.getColumnLabel(i))) { 
		        	switch(metaData.getColumnType(i)) { 
		        		case java.sql.Types.INTEGER:		setter.invoke(entity, rs.getInt(i)); 	break;
		        		case java.sql.Types.FLOAT:			setter.invoke(entity, rs.getFloat(i)); 	break; 
		        		case java.sql.Types.DECIMAL:		setter.invoke(entity, rs.getDouble(i)); break;
		        		case java.sql.Types.BIGINT:			setter.invoke(entity, rs.getLong(i)); 	break; 
		        		case java.sql.Types.VARBINARY:		setter.invoke(entity, rs.getBytes(i));	break;
		        		case java.sql.Types.LONGVARBINARY:	setter.invoke(entity, rs.getBytes(i));	break;
		        		//STRING
		        		default: 
		        			if(rs.getString(i) == null || rs.getString(i) == "")
		        				setter.invoke(entity, "");
		        			else
		        				setter.invoke(entity, rs.getString(i).trim());
		        	}
		        	break;
		        }
		    }			 
		}
		return entity;
	}

//	public JSONObject toJSONObject() {
//		ObjectMapper mapper = new ObjectMapper();
//		return mapper.convertValue(this, JSONObject.class);
//	}
}
