package com.fixiu.flyway.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Block;
import net.sf.jsqlparser.statement.Commit;
import net.sf.jsqlparser.statement.CreateFunctionalStatement;
import net.sf.jsqlparser.statement.DeclareStatement;
import net.sf.jsqlparser.statement.DescribeStatement;
import net.sf.jsqlparser.statement.ExplainStatement;
import net.sf.jsqlparser.statement.SetStatement;
import net.sf.jsqlparser.statement.ShowColumnsStatement;
import net.sf.jsqlparser.statement.ShowStatement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.UseStatement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.sequence.AlterSequence;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.schema.CreateSchema;
import net.sf.jsqlparser.statement.create.sequence.CreateSequence;
import net.sf.jsqlparser.statement.create.synonym.CreateSynonym;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.grant.Grant;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.show.ShowTablesStatement;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;
import net.sf.jsqlparser.statement.values.ValuesStatement;

/**
 * SQL Parser
 */
public abstract class JsqlParserSupport {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	public String parserSingle(String sql, Object obj) {
		if (logger.isDebugEnabled()) {
			logger.debug(">>> Original SQL: " + sql);
		}
		try {
			Statement statement = CCJSqlParserUtil.parse(sql);
			return processParser(statement, 0, obj);
		} catch (JSQLParserException e) {
			throw new RuntimeException(">>> Failed to process, Error SQL: " + sql);
		}
	}

	public String parserMulti(String sql, Object obj) {
		if (logger.isDebugEnabled()) {
			logger.debug(">>> Original SQL: " + sql);
		}
		try {
			StringBuilder sb = new StringBuilder();
			Statements statements = CCJSqlParserUtil.parseStatements(sql);
			int i = 0;
			for (Statement statement : statements.getStatements()) {
				if (i > 0) {
					sb.append(";");
				}
				sb.append(processParser(statement, i, obj));
				i++;
			}
			return sb.toString();
		} catch (JSQLParserException e) {
			throw new RuntimeException("Failed to process, Error SQL: " + sql);
		}
	}

	/**
	 * Perform SQL parsing
	 *
	 * @param statement JsqlParser Statement
	 * @return sql
	 */
	protected String processParser(Statement statement, int index, Object obj) {
		if (statement instanceof Insert) {
			this.processInsert((Insert) statement, index, obj);
		} else if (statement instanceof Select) {
			this.processSelect((Select) statement, index, obj);
		} else if (statement instanceof Update) {
			this.processUpdate((Update) statement, index, obj);
		} else if (statement instanceof Delete) {
			this.processDelete((Delete) statement, index, obj);
		} else if (statement instanceof Commit) {
			this.processCommit((Commit) statement, index, obj);
		} else if (statement instanceof Replace) {
			this.processReplace((Replace) statement, index, obj);
		} else if (statement instanceof Drop) {
			this.processDrop((Drop) statement, index, obj);
		} else if (statement instanceof Truncate) {
			this.processTruncate((Truncate) statement, index, obj);
		} else if (statement instanceof CreateIndex) {
			this.processCreateIndex((CreateIndex) statement, index, obj);
		} else if (statement instanceof CreateSchema) {
			this.processCreateSchema((CreateSchema) statement, index, obj);
		} else if (statement instanceof CreateTable) {
			this.processCreateTable((CreateTable) statement, index, obj);
		} else if (statement instanceof CreateView) {
			this.processCreateView((CreateView) statement, index, obj);
		} else if (statement instanceof AlterView) {
			this.processAlterView((AlterView) statement, index, obj);
		} else if (statement instanceof Alter) {
			this.processAlter((Alter) statement, index, obj);
		} else if (statement instanceof Statements) {
			this.processStatements((Statements) statement, index, obj);
		} else if (statement instanceof Execute) {
			this.processExecute((Execute) statement, index, obj);
		} else if (statement instanceof SetStatement) {
			this.processSetStatement((SetStatement) statement, index, obj);
		} else if (statement instanceof ShowColumnsStatement) {
			this.processShowColumnsStatement((ShowColumnsStatement) statement, index, obj);
		} else if (statement instanceof ShowTablesStatement) {
			this.processShowTablesStatement((ShowTablesStatement) statement, index, obj);
		} else if (statement instanceof Merge) {
			this.processMerge((Merge) statement, index, obj);
		} else if (statement instanceof Upsert) {
			this.processUpsert((Upsert) statement, index, obj);
		} else if (statement instanceof UseStatement) {
			this.processUseStatement((UseStatement) statement, index, obj);
		} else if (statement instanceof Block) {
			this.processBlock((Block) statement, index, obj);
		} else if (statement instanceof ValuesStatement) {
			this.processValuesStatement((ValuesStatement) statement, index, obj);
		} else if (statement instanceof DescribeStatement) {
			this.processDescribeStatement((DescribeStatement) statement, index, obj);
		} else if (statement instanceof ExplainStatement) {
			this.processExplainStatement((ExplainStatement) statement, index, obj);
		} else if (statement instanceof ShowStatement) {
			this.processShowStatement((ShowStatement) statement, index, obj);
		} else if (statement instanceof DeclareStatement) {
			this.processDeclareStatement((DeclareStatement) statement, index, obj);
		} else if (statement instanceof Grant) {
			this.processGrant((Grant) statement, index, obj);
		} else if (statement instanceof CreateSequence) {
			this.processCreateSequence((CreateSequence) statement, index, obj);
		} else if (statement instanceof AlterSequence) {
			this.processAlterSequence((AlterSequence) statement, index, obj);
		} else if (statement instanceof CreateFunctionalStatement) {
			this.processCreateFunctionalStatement((CreateFunctionalStatement) statement, index, obj);
		} else if (statement instanceof CreateSynonym) {
			this.processCreateSynonym((CreateSynonym) statement, index, obj);
		} else {
			this.processUnsupportedOperation(statement, index, obj);
		}
		final String sql = statement.toString();
		if (logger.isDebugEnabled()) {
			logger.debug(">>> Parsed sql: " + sql);
		}
		return sql;
	}

	protected void processUnsupportedOperation(Statement statement, int index, Object obj) {
		throw new IllegalArgumentException(">>> Statement not supported: " + statement.toString());
	}

	protected void processCreateSynonym(CreateSynonym statement, int index, Object obj) {
	}

	protected void processCreateFunctionalStatement(CreateFunctionalStatement statement, int index, Object obj) {
	}

	protected void processAlterSequence(AlterSequence statement, int index, Object obj) {
	}

	protected void processCreateSequence(CreateSequence statement, int index, Object obj) {
	}

	protected void processGrant(Grant statement, int index, Object obj) {
	}

	protected void processDeclareStatement(DeclareStatement statement, int index, Object obj) {
	}

	protected void processShowStatement(ShowStatement statement, int index, Object obj) {
	}

	protected void processExplainStatement(ExplainStatement statement, int index, Object obj) {
	}

	protected void processDescribeStatement(DescribeStatement statement, int index, Object obj) {
	}

	protected void processValuesStatement(ValuesStatement statement, int index, Object obj) {
	}

	protected void processBlock(Block statement, int index, Object obj) {
	}

	protected void processUseStatement(UseStatement statement, int index, Object obj) {
	}

	protected void processUpsert(Upsert statement, int index, Object obj) {
	}

	protected void processMerge(Merge statement, int index, Object obj) {
	}

	protected void processShowTablesStatement(ShowTablesStatement statement, int index, Object obj) {
	}

	protected void processShowColumnsStatement(ShowColumnsStatement statement, int index, Object obj) {
	}

	protected void processSetStatement(SetStatement statement, int index, Object obj) {
	}

	protected void processExecute(Execute statement, int index, Object obj) {
	}

	protected void processStatements(Statements statement, int index, Object obj) {
	}

	protected void processAlter(Alter statement, int index, Object obj) {
	}

	protected void processAlterView(AlterView statement, int index, Object obj) {
	}

	protected void processCreateView(CreateView statement, int index, Object obj) {
	}

	protected void processCreateTable(CreateTable statement, int index, Object obj) {
	}

	protected void processCreateSchema(CreateSchema statement, int index, Object obj) {
	}

	protected void processCreateIndex(CreateIndex statement, int index, Object obj) {
	}

	protected void processTruncate(Truncate statement, int index, Object obj) {
	}

	protected void processDrop(Drop statement, int index, Object obj) {
	}

	protected void processReplace(Replace statement, int index, Object obj) {
	}

	protected void processCommit(Commit statement, int index, Object obj) {
	}

	protected void processInsert(Insert insert, int index, Object obj) {
	}

	protected void processDelete(Delete delete, int index, Object obj) {
	}

	protected void processUpdate(Update update, int index, Object obj) {
	}

	protected void processSelect(Select select, int index, Object obj) {
	}
}
