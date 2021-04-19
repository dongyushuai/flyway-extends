package com.fixiu.flyway.handler;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Map.Entry;

import org.flywaydb.core.api.executor.Context;
import org.flywaydb.core.internal.sqlscript.ParsedSqlStatement;
import org.flywaydb.core.internal.sqlscript.SqlScript;
import org.flywaydb.core.internal.sqlscript.SqlStatement;
import org.flywaydb.core.internal.sqlscript.SqlStatementIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fixiu.flyway.context.ApplicationContextHelper;
import com.fixiu.flyway.sql.JsqlParserSupport;

import net.sf.jsqlparser.JSQLParserException;

/**
 * Handle sqls in {@link SqlStatementIterator}
 *
 */
public class SqlMigrationHandler {
	
	Logger log = LoggerFactory.getLogger(SqlMigrationHandler.class);
	
	Map<String, JsqlParserSupport> parser = ApplicationContextHelper.getContext().getBeansOfType(JsqlParserSupport.class);

	public void parse(Context context, SqlScript sqlScript) {
		SqlStatementIterator sqlStatements = sqlScript.getSqlStatements();
		try {
			handleStatement(sqlStatements);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (JSQLParserException e) {
			throw new RuntimeException(e);
		}
	}

	private void handleStatement(SqlStatementIterator sqlStatements)
			throws NoSuchFieldException, IllegalAccessException, JSQLParserException {
		if(parser.entrySet() == null || parser.entrySet().isEmpty()) {
			log.debug(">>> SQL Parser bean map is empty.");
		}
		while(sqlStatements.hasNext()) {
			SqlStatement sqlStatement = sqlStatements.next();
			ParsedSqlStatement orgi = (ParsedSqlStatement) sqlStatement;
			
			log.info(">>> Original sql is {}", orgi.getSql());
			
			for(Entry<String, JsqlParserSupport> map : parser.entrySet()) {
				String parserMulti = map.getValue().parserMulti(orgi.getSql(), null);
				Field sqlField = orgi.getClass().getDeclaredField("sql");
				
				sqlField.setAccessible(true);
				
				Field modifiersField = Field.class.getDeclaredField("modifiers");
			    modifiersField.setAccessible(true);
			    modifiersField.setInt(sqlField, sqlField.getModifiers() & ~Modifier.FINAL);
			    
			    sqlField.set(orgi, parserMulti);
			    
			    log.info(">>> Handle by {}, The new sql is {}", map.getKey(), orgi.getSql());
			}
			log.info(">>> Final sql is {}", orgi.getSql());
			
		}
	}
}
