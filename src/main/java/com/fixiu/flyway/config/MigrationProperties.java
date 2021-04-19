package com.fixiu.flyway.config;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.fixiu.flyway.injection.AbstractValueInjector;

@ConfigurationProperties(prefix = MigrationProperties.MIG_PREFIX)
public class MigrationProperties {
	protected static final String MIG_PREFIX = "migration";
	
	private Map<String, ColumnInjection> columnInjection;
	
	public Map<String, ColumnInjection> getColumnInjection() {
		return columnInjection;
	}

	public void setColumnInjection(Map<String, ColumnInjection> columnInjection) {
		this.columnInjection = columnInjection;
	}

	public static class ColumnInjection{
		private List<String> tables;
		private List<OperationstType> operationsSupport;
		private String columnName;
		private Integer columnIndex = -1;
		private Boolean addIfMissing = false;
		private Class<? extends AbstractValueInjector> injectClass;
		private String injectValue;
		private ColumnValueType columnType;
		
		public List<String> getTables() {
			return tables;
		}
		public void setTables(List<String> tables) {
			this.tables = tables;
		}
		public List<OperationstType> getOperationsSupport() {
			return operationsSupport;
		}
		public void setOperationsSupport(List<OperationstType> operationsSupport) {
			this.operationsSupport = operationsSupport;
		}
		public String getColumnName() {
			return columnName;
		}
		public void setColumnName(String columnName) {
			this.columnName = columnName;
		}
		public Integer getColumnIndex() {
			return columnIndex;
		}
		public void setColumnIndex(Integer columnIndex) {
			this.columnIndex = columnIndex;
		}
		public Boolean getAddIfMissing() {
			return addIfMissing;
		}
		public void setAddIfMissing(Boolean addIfMissing) {
			this.addIfMissing = addIfMissing;
		}
		public Class<? extends AbstractValueInjector> getInjectClass() {
			return injectClass;
		}
		public void setInjectClass(Class<? extends AbstractValueInjector> injectClass) {
			this.injectClass = injectClass;
		}
		public String getInjectValue() {
			return injectValue;
		}
		public void setInjectValue(String injectValue) {
			this.injectValue = injectValue;
		}
		public ColumnValueType getColumnType() {
			return columnType;
		}
		public void setColumnType(ColumnValueType columnType) {
			this.columnType = columnType;
		}
	}
	
	public static enum OperationstType{
		INSERT, UPDATE
	}
	public static enum ColumnValueType{
		STRING, LONG, DOUBLE, 
		/**yyyy-mm-dd**/
		DATE, 
		/**hh:mm:ss**/
		TIME, 
		/**yyyy-mm-dd hh:mm:ss.f**/
		TIMESTAMP, HEX, NULL
	}
}
