package com.fixiu.flyway.parser;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.fixiu.flyway.config.MigrationProperties;
import com.fixiu.flyway.config.MigrationProperties.ColumnInjection;
import com.fixiu.flyway.config.MigrationProperties.ColumnValueType;
import com.fixiu.flyway.config.MigrationProperties.OperationstType;
import com.fixiu.flyway.injection.AbstractValueInjector;
import com.fixiu.flyway.sql.JsqlParserSupport;

import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.HexValue;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.update.Update;

/**
 *	Standard SQL conversion processor, handling custom column values
 */
public class StandardColumnInjectionSupport extends JsqlParserSupport {

	final MigrationProperties migrationProperties;
	
	final static String BACKTICK = "`";
	
	public StandardColumnInjectionSupport(MigrationProperties migrationProperties) {
		this.migrationProperties = migrationProperties;
	}

	@Override
	protected void processInsert(Insert insert, int index, Object obj) {
		processInsertOrUpdate(insert, index, obj);
	}

	@Override
	protected void processUpdate(Update update, int index, Object obj) {
		processInsertOrUpdate(update, index, obj);
	}
	
	private void processInsertOrUpdate(Statement statement, int index, Object obj) {
		Map<String, ColumnInjection> columnInjectionMap = migrationProperties.getColumnInjection();

		if (columnInjectionMap == null || columnInjectionMap.isEmpty()) {
			return;
		}
		
		OperationstType currentType = getOperationstType(statement);
		if(currentType == null) {
			logger.warn(">>> Unsupported statement: {}", statement.toString());
			return;
		}
		
		Table table = getTable(statement);
			
		if (table == null) {
			logger.warn(">>> Table name cannot be resolved: {}", statement.toString());
			return;
		}

		List<Column> columns = getColumnList(statement);

		if (Objects.isNull(columns) || columns.isEmpty()) {
			logger.warn(">>> Unable to parse data column: {}", statement.toString());
			return;
		}

		logger.info(">>> Current sql statement: {}", statement.toString());

		for (Entry<String, ColumnInjection> entry : columnInjectionMap.entrySet()) {
			ColumnInjection injection = entry.getValue();
			// operations-support
			List<OperationstType> operationsSupport = injection.getOperationsSupport();
			if (operationsSupport == null || operationsSupport.isEmpty()) {
				logger.info(">>> Skipped because no value is set for the 'operations-support' attribute");
				continue;
			}
			if(!operationsSupport.contains(currentType)) {
				logger.info(">>> Skipped because 'operations-support' does not contain the '{}'", currentType);
				continue;
			}
			
			// tables
			if (!injection.getTables().contains(removeBacktick(table.getName()))) {
				logger.info(">>> Skipped because current table name '{}' mismatch '{}'", table.getName(), StringUtils.join(entry.getValue().getTables(), ","));
				continue;
			}
			
			Integer columnIndex = getColumnIndex(injection, columns);
			
			if(statement instanceof Insert) {
				ItemsList itemsList = ((Insert)statement).getItemsList();
				// Add or update
				if (columnIndex == -1) {
					if (injection.getAddIfMissing()) {
						columns.add(new Column(injection.getColumnName()));
						((ExpressionList) itemsList).getExpressions().add(getValue(injection));
					} else {
						logger.error(">>> This column '{}' is ignored because of the 'add-if-missing' attribute is {}", injection.getColumnName(), injection.getAddIfMissing());
					}
				} else if(columnIndex == -2){
					logger.error(">>> Multiple identical column names were found: {}", injection.getColumnName());
				} else if(columnIndex == -3){
					logger.error(">>> The 'column-index' and 'column-name' properties cannot be null at the same time");
				} else {
					if (itemsList instanceof MultiExpressionList) {
						final Integer idx = columnIndex;
						((MultiExpressionList) itemsList).getExpressionLists().forEach(el -> el.getExpressions().set(idx, getValue(injection)));
					} else {
						((ExpressionList) itemsList).getExpressions().set(columnIndex, getValue(injection));
					}
				}
				
			}else if(statement instanceof Update) {
				List<Expression> expressions = ((Update)statement).getExpressions();
				
				// Add or update
				if (columnIndex == -1) {
					if (injection.getAddIfMissing()) {
						columns.add(new Column(injection.getColumnName()));
						expressions.add(getValue(injection));
					} else {
						logger.error(">>> This column '{}' is ignored because of the 'add-if-missing' attribute is {}", injection.getColumnName(), injection.getAddIfMissing());
					}
				} else if(columnIndex == -2){
					logger.error(">>> Multiple identical column names were found: {}", injection.getColumnName());
				} else if(columnIndex == -3){
					logger.error(">>> The 'column-index' and 'column-name' properties cannot be null at the same time");
				} else {
					expressions.set(columnIndex, getValue(injection));
				}
			}
		}
	}
	
	private Table getTable(Statement statement) {
		if(statement instanceof Insert) {
			return ((Insert)statement).getTable();
		}else if(statement instanceof Update) {
			return ((Update)statement).getTable();
		}
		return null;
	}
	
	private List<Column> getColumnList(Statement statement) {
		if(statement instanceof Insert) {
			return ((Insert)statement).getColumns();
		}else if(statement instanceof Update) {
			return ((Update)statement).getColumns();
		}
		return null;
	}
	
	private OperationstType getOperationstType(Statement statement) {
		if(statement instanceof Insert) {
			return OperationstType.INSERT;
		}else if(statement instanceof Update) {
			return OperationstType.UPDATE;
		}
		return null;
	}

	private Integer getColumnIndex(ColumnInjection injection, List<Column> columns) {
		// column-index
		if (Objects.nonNull(injection.getColumnIndex()) && injection.getColumnIndex() > -1) {
			return injection.getColumnIndex();
		} else if (Objects.nonNull(injection.getColumnName())) {
			Stream<Column> stream = columns.stream() .filter(c -> injection.getColumnName().equalsIgnoreCase(removeBacktick(c.getColumnName())));

			List<Column> columnList = stream.collect(Collectors.toList());
			
			if (columnList == null || columnList.size() == 0) {
				return -1;
			}
			
			if (columnList.size() > 1) {
				return -2;
			}

			if (columnList.size() == 1) {
				Column currentColumn = columnList.get(0);

				return columns.indexOf(currentColumn);
			}
		} else {
			return -3;
		}

		return -4;
	}
	
	private Expression getValue(ColumnInjection injection) {
		return getValue(injection.getColumnType(), injection.getInjectValue(), injection.getInjectClass());
	}

	/**
	 * Get value by type, string value first ,default NullValue
	 * 
	 * @param type
	 * @param value
	 * @param clazz
	 * @return
	 */
	private Expression getValue(ColumnValueType type, String value, Class<? extends AbstractValueInjector> clazz) {

		Expression val = null;

		if (StringUtils.isNotBlank(value)) {
			if (ColumnValueType.STRING.equals(type)) {
				val = new StringValue(value);
			} else if (ColumnValueType.LONG.equals(type)) {
				val = new LongValue(value);
			} else if (ColumnValueType.DOUBLE.equals(type)) {
				val = new DoubleValue(value);
			} else if (ColumnValueType.DATE.equals(type)) {
				val = new DateValue(Date.valueOf(value));
			} else if (ColumnValueType.TIME.equals(type)) {
				val = new TimeValue().withValue(Time.valueOf(value));
			} else if (ColumnValueType.TIMESTAMP.equals(type)) {
				val = new TimestampValue().withValue(Timestamp.valueOf(value));
			} else if (ColumnValueType.HEX.equals(type)) {
				val = new HexValue(value);
			} else if (ColumnValueType.NULL.equals(type)) {
				val = new NullValue();
			}
		} else {
			if (clazz == null) {
				val = new NullValue();
			} else {
				try {
					AbstractValueInjector newInstance = clazz.newInstance();
					val = newInstance.getValue();
				} catch (InstantiationException e) {
					logger.error(">>> Init AbstractInjectValue instance error: ", e);
				} catch (IllegalAccessException e) {
					logger.error(">>> Init AbstractInjectValue instance error: ", e);
				}
			}
		}

		return val == null ? new NullValue() : val;
	}
	
	private String removeBacktick(String text) {
		return text.replace(BACKTICK, "");
	}
}
