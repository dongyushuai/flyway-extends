/*
 * Copyright 2010-2020 Redgate Software Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fixiu.flyway.executor;

import java.sql.SQLException;

import org.flywaydb.core.api.executor.Context;
import org.flywaydb.core.api.executor.MigrationExecutor;
import org.flywaydb.core.internal.database.DatabaseExecutionStrategy;
import org.flywaydb.core.internal.database.DatabaseFactory;
import org.flywaydb.core.internal.resource.LoadableResource;
import org.flywaydb.core.internal.resource.ResourceName;
import org.flywaydb.core.internal.sqlscript.SqlScript;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutorFactory;
import org.flywaydb.core.internal.util.SqlCallable;

import com.fixiu.flyway.handler.SqlMigrationHandler;

/**
 * Database migration based on a sql file.
 */
public class CustomSqlMigrationExecutor implements MigrationExecutor {
	private final SqlScriptExecutorFactory sqlScriptExecutorFactory;

	/**
	 * The SQL script that will be executed.
	 */
	private final SqlScript sqlScript;
	
	SqlMigrationHandler handler = new SqlMigrationHandler();
	
	private ResourceName result;
	private LoadableResource resource;

	/**
	 * Creates a new sql script migration based on this sql script.
	 *
	 * @param sqlScript The SQL script that will be executed.
	 * @param resource 
	 * @param result 
	 */
	public CustomSqlMigrationExecutor(SqlScriptExecutorFactory sqlScriptExecutorFactory, SqlScript sqlScript, ResourceName result, LoadableResource resource

	) {
		this.sqlScriptExecutorFactory = sqlScriptExecutorFactory;
		this.sqlScript = sqlScript;
		this.result = result;
		this.resource = resource;

	}

	@Override
	public void execute(final Context context) throws SQLException {
		DatabaseExecutionStrategy strategy = DatabaseFactory.createExecutionStrategy(context.getConnection());
		strategy.execute(new SqlCallable<Boolean>() {
			@Override
			public Boolean call() throws SQLException {
				executeOnce(context);
				return true;
			}
		});
	}

	private void executeOnce(Context context) {
		handler.parse(context, sqlScript);
		sqlScriptExecutorFactory.createSqlScriptExecutor(context.getConnection()).execute(sqlScript);
	}

	@Override
	public boolean canExecuteInTransaction() {
		return sqlScript.executeInTransaction();
	}

	public SqlScript getSqlScript() {
		return sqlScript;
	}

	public ResourceName getResult() {
		return result;
	}

	public LoadableResource getResource() {
		return resource;
	}
}