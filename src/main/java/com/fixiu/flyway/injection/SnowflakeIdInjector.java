package com.fixiu.flyway.injection;

import com.fixiu.flyway.utils.SnowflakeManager;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;

/**
 * Snowflake ID will be set
 */
public class SnowflakeIdInjector extends AbstractValueInjector{
	SnowflakeManager snow = new SnowflakeManager(31L, 31L);
	@Override
	public Expression getValue() {
		return new LongValue(snow.nextValue());
	}

}
